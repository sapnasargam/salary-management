import { CurrencyPipe, DatePipe, DecimalPipe } from '@angular/common';
import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ActivatedRoute } from '@angular/router';
import { Subject, catchError, debounceTime, filter, of, switchMap, tap } from 'rxjs';

import { errorMessage } from '../core/api-error';
import { EmployeeService } from '../core/employee.service';
import { Employee, EmployeeQuery, Page, ReferenceData } from '../core/models';
import { ReferenceDataService } from '../core/reference-data.service';
import { ConfirmDialogComponent, ConfirmDialogData } from '../shared/confirm-dialog.component';
import { EmployeeFormDialogComponent, EmployeeFormDialogData } from './employee-form-dialog.component';

const EMPTY_PAGE: Page<Employee> = { content: [], page: 0, size: 25, totalElements: 0, totalPages: 0 };

/**
 * The employee directory: server-side search, filtering, sorting and paging over all employees,
 * plus add / edit / delete. Filters can be pre-set via query params (links from the Insights page).
 */
@Component({
  selector: 'app-employee-list',
  imports: [
    ReactiveFormsModule,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    MatProgressBarModule,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.scss',
})
export class EmployeeListComponent implements OnInit {
  private readonly employees = inject(EmployeeService);
  private readonly referenceDataService = inject(ReferenceDataService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly reload$ = new Subject<void>();

  readonly displayedColumns = ['fullName', 'jobTitle', 'department', 'country', 'annualSalary', 'hireDate', 'actions'];
  readonly pageSizeOptions = [10, 25, 50, 100];

  readonly filters = inject(FormBuilder).nonNullable.group({
    search: '',
    country: '',
    department: '',
    jobTitle: '',
  });

  readonly result = signal<Page<Employee>>(EMPTY_PAGE);
  readonly loading = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly referenceData = signal<ReferenceData | null>(null);

  pageIndex = 0;
  pageSize = 25;
  sort: Sort = { active: 'fullName', direction: 'asc' };

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    this.filters.patchValue({
      country: params.get('country') ?? '',
      department: params.get('department') ?? '',
      jobTitle: params.get('jobTitle') ?? '',
    });

    this.loadReferenceData();

    this.filters.valueChanges
      .pipe(debounceTime(300), takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.pageIndex = 0;
        this.reload$.next();
      });

    // switchMap cancels an in-flight request when filters change again, so stale results never win.
    this.reload$
      .pipe(
        tap(() => {
          this.loading.set(true);
          this.loadError.set(null);
        }),
        switchMap(() =>
          this.employees.search(this.buildQuery()).pipe(
            catchError((error) => {
              this.loadError.set(errorMessage(error));
              return of(null);
            }),
          ),
        ),
        tap(() => this.loading.set(false)),
        filter((page): page is Page<Employee> => page !== null),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((page) => this.result.set(page));

    this.reload$.next();
  }

  get hasActiveFilters(): boolean {
    return Object.values(this.filters.getRawValue()).some((value) => value.trim() !== '');
  }

  onPage(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.reload$.next();
  }

  onSort(sort: Sort): void {
    this.sort = sort.direction ? sort : { active: 'fullName', direction: 'asc' };
    this.pageIndex = 0;
    this.reload$.next();
  }

  clearFilters(): void {
    this.filters.reset();
  }

  openCreate(): void {
    this.openForm(undefined, 'Employee added');
  }

  openEdit(employee: Employee): void {
    this.openForm(employee, 'Changes saved');
  }

  confirmDelete(employee: Employee): void {
    this.dialog
      .open<ConfirmDialogComponent, ConfirmDialogData, boolean>(ConfirmDialogComponent, {
        data: {
          title: 'Delete employee?',
          message: `${employee.fullName} (${employee.email}) will be permanently removed.`,
          confirmLabel: 'Delete',
        },
      })
      .afterClosed()
      .pipe(
        filter(Boolean),
        switchMap(() => this.employees.delete(employee.id)),
      )
      .subscribe({
        next: () => {
          this.notify(`${employee.fullName} deleted`);
          this.reload$.next();
        },
        error: (error) => this.notify(errorMessage(error)),
      });
  }

  private openForm(employee: Employee | undefined, successMessage: string): void {
    const referenceData = this.referenceData();
    if (!referenceData) return;

    this.dialog
      .open<EmployeeFormDialogComponent, EmployeeFormDialogData, Employee>(EmployeeFormDialogComponent, {
        data: { employee, referenceData },
        width: '640px',
        maxWidth: '95vw',
      })
      .afterClosed()
      .pipe(filter((saved): saved is Employee => !!saved))
      .subscribe(() => {
        this.notify(successMessage);
        // A new job title or department may have been introduced.
        this.referenceDataService.refresh();
        this.loadReferenceData();
        this.reload$.next();
      });
  }

  private loadReferenceData(): void {
    this.referenceDataService.get().subscribe({
      next: (data) => this.referenceData.set(data),
      error: (error) => this.notify(errorMessage(error)),
    });
  }

  private buildQuery(): EmployeeQuery {
    return {
      ...this.filters.getRawValue(),
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.sort.active,
      direction: this.sort.direction || 'asc',
    };
  }

  private notify(message: string): void {
    this.snackBar.open(message, 'OK', { duration: 4000 });
  }
}
