import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { AfterViewInit, Component, ViewChild, effect, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';

import { SalaryStatistics } from '../core/models';

export interface StatsRow {
  label: string;
  currency: string;
  salary: SalaryStatistics;
  /** Router link + query params for drilling down from this row. */
  link?: { commands: string[]; queryParams?: Record<string, string> };
}

/** Sortable table of salary statistics (headcount, min, median, average, max). Sorting is client-side. */
@Component({
  selector: 'app-salary-stats-table',
  imports: [MatTableModule, MatSortModule, MatIconModule, MatTooltipModule, RouterLink, CurrencyPipe, DecimalPipe],
  template: `
    <div class="table-scroll">
      <table mat-table [dataSource]="dataSource" matSort>
        <ng-container matColumnDef="label">
          <th mat-header-cell *matHeaderCellDef mat-sort-header>{{ labelHeader() }}</th>
          <td mat-cell *matCellDef="let row">
            @if (row.link) {
              <a [routerLink]="row.link.commands" [queryParams]="row.link.queryParams" [matTooltip]="linkTooltip()">
                {{ row.label }}
              </a>
            } @else {
              {{ row.label }}
            }
          </td>
        </ng-container>

        <ng-container matColumnDef="headcount">
          <th mat-header-cell *matHeaderCellDef mat-sort-header arrowPosition="before" class="num">Headcount</th>
          <td mat-cell *matCellDef="let row" class="num">{{ row.salary.headcount | number }}</td>
        </ng-container>

        @for (column of moneyColumns; track column.key) {
          <ng-container [matColumnDef]="column.key">
            <th mat-header-cell *matHeaderCellDef mat-sort-header arrowPosition="before" class="num">
              {{ column.label }}
            </th>
            <td mat-cell *matCellDef="let row" class="num" [class.emphasis]="column.key === 'median'">
              @if (row.salary.headcount > 0) {
                {{ row.salary[column.key] | currency: row.currency : 'symbol-narrow' : '1.0-0' }}
              } @else {
                <span class="muted">—</span>
              }
            </td>
          </ng-container>
        }

        <tr mat-header-row *matHeaderRowDef="columns"></tr>
        <tr mat-row *matRowDef="let row; columns: columns" [class.empty]="row.salary.headcount === 0"></tr>
        <tr class="mat-row" *matNoDataRow>
          <td class="muted no-data" [attr.colspan]="columns.length">No data</td>
        </tr>
      </table>
    </div>
  `,
  styles: `
    .table-scroll { overflow-x: auto; }
    table { width: 100%; }
    a { color: var(--mat-sys-primary); text-decoration: none; font-weight: 500; }
    a:hover { text-decoration: underline; }
    .emphasis { font-weight: 500; }
    .muted { color: var(--mat-sys-on-surface-variant); }
    .empty { opacity: 0.6; }
    .no-data { padding: 24px; text-align: center; }
  `,
})
export class SalaryStatsTableComponent implements AfterViewInit {
  readonly rows = input.required<StatsRow[]>();
  readonly labelHeader = input('Name');
  readonly linkTooltip = input('');

  readonly moneyColumns = [
    { key: 'min', label: 'Min' },
    { key: 'median', label: 'Median' },
    { key: 'average', label: 'Average' },
    { key: 'max', label: 'Max' },
  ] as const;
  readonly columns = ['label', 'headcount', ...this.moneyColumns.map((c) => c.key)];

  readonly dataSource = new MatTableDataSource<StatsRow>([]);

  @ViewChild(MatSort) sort!: MatSort;

  constructor() {
    effect(() => (this.dataSource.data = this.rows()));
    this.dataSource.sortingDataAccessor = (row, column) =>
      column === 'label' ? row.label.toLowerCase() : row.salary[column as keyof SalaryStatistics];
  }

  ngAfterViewInit(): void {
    this.dataSource.sort = this.sort;
  }
}
