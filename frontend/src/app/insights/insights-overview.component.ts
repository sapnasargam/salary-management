import { DecimalPipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { errorMessage } from '../core/api-error';
import { InsightsService } from '../core/insights.service';
import { OrgOverview } from '../core/models';
import { SalaryStatsTableComponent, StatsRow } from './salary-stats-table.component';

/** Org-wide view: headcount and salary statistics per country (each in its own currency). */
@Component({
  selector: 'app-insights-overview',
  imports: [DecimalPipe, MatIconModule, MatProgressBarModule, SalaryStatsTableComponent],
  template: `
    <h1>Salary insights</h1>
    <p class="subtitle">
      How ACME pays people, per country. Amounts are in each country's local currency, so figures are compared
      within a country, never across currencies.
    </p>

    @if (loading()) {
      <mat-progress-bar mode="indeterminate" />
    }
    @if (error(); as message) {
      <p class="error" role="alert">{{ message }}</p>
    }

    @if (overview(); as data) {
      <section class="tiles">
        <div class="tile">
          <span class="label">Employees</span>
          <span class="value">{{ data.totalHeadcount | number }}</span>
        </div>
        <div class="tile">
          <span class="label">Countries</span>
          <span class="value">{{ data.countriesWithEmployees }}</span>
        </div>
        @if (largestCountry(); as largest) {
          <div class="tile">
            <span class="label">Largest workforce</span>
            <span class="value">{{ largest.countryName }}</span>
            <span class="hint">{{ largest.salary.headcount | number }} people</span>
          </div>
        }
      </section>

      <section class="card">
        <h2><mat-icon>public</mat-icon> By country</h2>
        <app-salary-stats-table [rows]="rows()" labelHeader="Country"
                                linkTooltip="See job titles and departments" />
      </section>
    }
  `,
  styleUrl: './insights.scss',
})
export class InsightsOverviewComponent implements OnInit {
  private readonly insights = inject(InsightsService);

  readonly overview = signal<OrgOverview | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  readonly largestCountry = computed(() => this.overview()?.countries[0]);

  readonly rows = computed<StatsRow[]>(() =>
    (this.overview()?.countries ?? []).map((country) => ({
      label: `${country.countryName} (${country.currency})`,
      currency: country.currency,
      salary: country.salary,
      link: { commands: ['/insights', country.countryCode] },
    })),
  );

  ngOnInit(): void {
    this.insights.overview().subscribe({
      next: (overview) => {
        this.overview.set(overview);
        this.loading.set(false);
      },
      error: (error) => {
        this.error.set(errorMessage(error));
        this.loading.set(false);
      },
    });
  }
}
