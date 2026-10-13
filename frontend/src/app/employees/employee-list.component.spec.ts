import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { EmployeeService } from '../core/employee.service';
import { Employee, Page } from '../core/models';
import { ReferenceDataService } from '../core/reference-data.service';
import { EmployeeListComponent } from './employee-list.component';

const PAGE: Page<Employee> = {
  content: [{
    id: 1, fullName: 'Asha Rao', email: 'asha@acme.com', jobTitle: 'Software Engineer', department: 'Engineering',
    countryCode: 'IN', countryName: 'India', currency: 'INR', annualSalary: 1_800_000, hireDate: '2021-03-15',
  }],
  page: 0, size: 25, totalElements: 1, totalPages: 1,
};

describe('EmployeeListComponent', () => {
  let fixture: ComponentFixture<EmployeeListComponent>;
  let employees: jasmine.SpyObj<EmployeeService>;

  beforeEach(() => {
    employees = jasmine.createSpyObj<EmployeeService>('EmployeeService', ['search', 'delete']);
    employees.search.and.returnValue(of(PAGE));
    const referenceData = jasmine.createSpyObj<ReferenceDataService>('ReferenceDataService', ['get', 'refresh']);
    referenceData.get.and.returnValue(of({ countries: [{ code: 'IN', name: 'India', currency: 'INR' }], departments: [], jobTitles: [] }));

    TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: EmployeeService, useValue: employees },
        { provide: ReferenceDataService, useValue: referenceData },
        // Arriving from the Insights page with a drill-down link.
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap({ country: 'IN', jobTitle: 'Software Engineer' }) } } },
      ],
    });
    fixture = TestBed.createComponent(EmployeeListComponent);
  });

  it('applies filters from the URL to the first request and renders the results', () => {
    fixture.detectChanges();

    expect(employees.search).toHaveBeenCalledOnceWith(jasmine.objectContaining({
      country: 'IN', jobTitle: 'Software Engineer', page: 0, size: 25, sort: 'fullName', direction: 'asc',
    }));
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Asha Rao');
    expect(text).toContain('₹1,800,000');
    expect(text).toContain('1 matching employees');
  });

  it('debounces typing in the search box and restarts from the first page', fakeAsync(() => {
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.pageIndex = 3;

    component.filters.controls.search.setValue('a');
    component.filters.controls.search.setValue('as');
    component.filters.controls.search.setValue('asha');
    tick(300);

    expect(employees.search).toHaveBeenCalledTimes(2);
    expect(employees.search.calls.mostRecent().args[0]).toEqual(jasmine.objectContaining({ search: 'asha', page: 0 }));
  }));

  it('resets to the first page when the sort changes', () => {
    fixture.detectChanges();
    const component = fixture.componentInstance;
    component.pageIndex = 2;

    component.onSort({ active: 'annualSalary', direction: 'desc' });

    expect(employees.search.calls.mostRecent().args[0])
      .toEqual(jasmine.objectContaining({ sort: 'annualSalary', direction: 'desc', page: 0 }));
  });
});
