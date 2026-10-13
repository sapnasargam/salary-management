import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'employees' },
  {
    path: 'employees',
    title: 'Employees · Salary Management',
    loadComponent: () => import('./employees/employee-list.component').then((m) => m.EmployeeListComponent),
  },
  {
    path: 'insights',
    title: 'Insights · Salary Management',
    loadComponent: () => import('./insights/insights-overview.component').then((m) => m.InsightsOverviewComponent),
  },
  {
    path: 'insights/:countryCode',
    title: 'Country insights · Salary Management',
    loadComponent: () => import('./insights/country-insights.component').then((m) => m.CountryInsightsComponent),
  },
  { path: '**', redirectTo: 'employees' },
];
