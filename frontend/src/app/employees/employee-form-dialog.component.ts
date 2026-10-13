import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { Observable } from 'rxjs';

import { errorMessage, fieldErrors } from '../core/api-error';
import { EmployeeService } from '../core/employee.service';
import { Employee, EmployeeRequest, ReferenceData } from '../core/models';

/** yyyy-MM-dd in the user's time zone (toISOString would use UTC and can be off by a day). */
function localIsoDate(date: Date): string {
  const pad = (n: number) => `${n}`.padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

export interface EmployeeFormDialogData {
  employee?: Employee;
  referenceData: ReferenceData;
}

/** Create / edit an employee. Closes with the saved {@link Employee}, or nothing when cancelled. */
@Component({
  selector: 'app-employee-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatProgressBarModule,
  ],
  templateUrl: './employee-form-dialog.component.html',
  styles: `
    form { display: grid; grid-template-columns: 1fr 1fr; gap: 4px 16px; padding-top: 8px; }
    .full { grid-column: 1 / -1; }
    .form-error { color: var(--mat-sys-error); margin: 0 0 8px; }
    @media (max-width: 600px) { form { grid-template-columns: 1fr; } }
  `,
})
export class EmployeeFormDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<EmployeeFormDialogComponent, Employee>);
  private readonly employees = inject(EmployeeService);
  readonly data = inject<EmployeeFormDialogData>(MAT_DIALOG_DATA);

  readonly isEdit = !!this.data.employee;
  readonly today = localIsoDate(new Date());
  readonly saving = signal(false);
  readonly formError = signal<string | null>(null);

  readonly form = inject(FormBuilder).nonNullable.group({
    fullName: [this.data.employee?.fullName ?? '', [Validators.required, Validators.maxLength(120)]],
    email: [this.data.employee?.email ?? '', [Validators.required, Validators.email, Validators.maxLength(254)]],
    jobTitle: [this.data.employee?.jobTitle ?? '', [Validators.required, Validators.maxLength(100)]],
    department: [this.data.employee?.department ?? '', [Validators.required, Validators.maxLength(100)]],
    countryCode: [this.data.employee?.countryCode ?? '', Validators.required],
    annualSalary: [this.data.employee?.annualSalary ?? (null as number | null),
      [Validators.required, Validators.min(1), Validators.max(1_000_000_000)]],
    hireDate: [this.data.employee?.hireDate ?? this.today, Validators.required],
  });

  private readonly countryCode = toSignal(this.form.controls.countryCode.valueChanges, {
    initialValue: this.form.controls.countryCode.value,
  });

  /** Currency is implied by the country; shown so HR always knows which currency they are typing in. */
  readonly currency = computed(
    () => this.data.referenceData.countries.find((c) => c.code === this.countryCode())?.currency ?? '',
  );

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.formError.set(null);

    const request = this.form.getRawValue() as EmployeeRequest;
    const save$: Observable<Employee> = this.isEdit
      ? this.employees.update(this.data.employee!.id, request)
      : this.employees.create(request);

    save$.subscribe({
      next: (saved) => this.dialogRef.close(saved),
      error: (error) => {
        this.saving.set(false);
        this.applyServerErrors(error);
      },
    });
  }

  /** Shows server-side validation messages next to the matching inputs, or as a form-level error. */
  private applyServerErrors(error: unknown): void {
    const errors = fieldErrors(error);
    let shownOnField = false;
    for (const [field, message] of Object.entries(errors)) {
      const control = this.form.get(field);
      if (control) {
        control.setErrors({ server: message });
        control.markAsTouched();
        shownOnField = true;
      }
    }
    if (!shownOnField) {
      this.formError.set(errorMessage(error));
    }
  }

  errorFor(field: keyof typeof this.form.controls): string | null {
    const errors = this.form.controls[field].errors;
    if (!errors) return null;
    if (errors['server']) return errors['server'];
    if (errors['required']) return 'Required';
    if (errors['email']) return 'Enter a valid e-mail address';
    if (errors['maxlength']) return `At most ${errors['maxlength'].requiredLength} characters`;
    if (errors['min']) return 'Must be greater than 0';
    if (errors['max']) return 'That looks too large — check for extra zeros';
    return 'Invalid value';
  }
}
