/** Mirrors the backend API contract (see docs/02-design.md). Money is whole units of `currency`. */

export interface Employee {
  id: number;
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  countryName: string;
  currency: string;
  annualSalary: number;
  hireDate: string; // ISO yyyy-MM-dd
}

export interface EmployeeRequest {
  fullName: string;
  email: string;
  jobTitle: string;
  department: string;
  countryCode: string;
  annualSalary: number;
  hireDate: string;
}

export type SortDirection = 'asc' | 'desc';

export interface EmployeeQuery {
  search?: string;
  country?: string;
  department?: string;
  jobTitle?: string;
  page: number;
  size: number;
  sort: string;
  direction: SortDirection;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CountryOption {
  code: string;
  name: string;
  currency: string;
}

export interface ReferenceData {
  countries: CountryOption[];
  departments: string[];
  jobTitles: string[];
}

export interface SalaryStatistics {
  headcount: number;
  min: number;
  max: number;
  average: number;
  median: number;
}

export interface CountryInsight {
  countryCode: string;
  countryName: string;
  currency: string;
  salary: SalaryStatistics;
}

export interface OrgOverview {
  totalHeadcount: number;
  countriesWithEmployees: number;
  countries: CountryInsight[];
}

export interface GroupInsight {
  name: string;
  salary: SalaryStatistics;
}

export interface CountryDetail {
  countryCode: string;
  countryName: string;
  currency: string;
  salary: SalaryStatistics;
  byJobTitle: GroupInsight[];
  byDepartment: GroupInsight[];
}
