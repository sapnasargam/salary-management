import { CurrencyPipe, DecimalPipe } from '@angular/common';
import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { Router, RouterLink } from '@angular/router';

import { errorMessage } from '../core/api-error';
import { InsightsService } from '../core/insights.service';
import { CountryDetail, CountryOption, GroupInsight } from '../core/models';
import { ReferenceDataService } from '../core/reference-data.service';
import { SalaryStatsTableComponent, StatsRow } from './salary-stats-table.component';

/**
 * One country in depth: overall statistics plus breakdowns by job title and department.
 * Answers e.g. "what is the average salary of a Software Engineer in India?".
 */
@Component({
  selector: 'app-country-insights',
  imports: [
    CurrencyPipe,
    DecimalPipe,
    RouterLink,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTabsModule,
    SalaryStatsTableComponent,
  ],
  templateUrl: './country-insights.component.html',
  styleUrl: './insights.scss',
})
export class CountryInsightsComponent {
  private readonly insights = inject(InsightsService);
  private readonly router = inject(Router);

  /** Bound from the `:countryCode` route parameter. */
  readonly countryCode = input.required<string>();

  readonly detail = signal<CountryDetail | null>(null);
  readonly loading = signal(false);
  readonly error = signal<string | null>(null);
  readonly countries = signal<CountryOption[]>([]);

  readonly byJobTitle = computed(() => this.toRows(this.detail()?.byJobTitle, 'jobTitle'));
  readonly byDepartment = computed(() => this.toRows(this.detail()?.byDepartment, 'department'));

  constructor() {
    inject(ReferenceDataService).get().subscribe((data) => this.countries.set(data.countries));
    effect(() => this.load(this.countryCode()));
  }

  switchCountry(code: string): void {
    this.router.navigate(['/insights', code]);
  }

  private load(code: string): void {
    this.loading.set(true);
    this.error.set(null);
    this.insights.countryDetail(code).subscribe({
      next: (detail) => {
        this.detail.set(detail);
        this.loading.set(false);
      },
      error: (error) => {
        this.detail.set(null);
        this.error.set(errorMessage(error));
        this.loading.set(false);
      },
    });
  }

  private toRows(groups: GroupInsight[] | undefined, filter: 'jobTitle' | 'department'): StatsRow[] {
    const detail = this.detail();
    if (!detail || !groups) return [];
    return groups.map((group) => ({
      label: group.name,
      currency: detail.currency,
      salary: group.salary,
      link: { commands: ['/employees'], queryParams: { country: detail.countryCode, [filter]: group.name } },
    }));
  }
}
