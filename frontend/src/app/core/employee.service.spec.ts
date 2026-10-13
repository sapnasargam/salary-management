import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { EmployeeService } from './employee.service';

describe('EmployeeService', () => {
  let service: EmployeeService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(EmployeeService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only non-blank filters, trimmed, with paging and sorting', () => {
    service
      .search({ search: '  asha ', country: 'IN', department: '', jobTitle: '   ', page: 2, size: 25, sort: 'annualSalary', direction: 'desc' })
      .subscribe();

    const req = http.expectOne((r) => r.url === '/api/employees');
    expect(req.request.params.keys().sort()).toEqual(['country', 'direction', 'page', 'search', 'size', 'sort']);
    expect(req.request.params.get('search')).toBe('asha');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [], page: 2, size: 25, totalElements: 0, totalPages: 0 });
  });

  it('uses REST verbs and ids for create, update and delete', () => {
    const body = {
      fullName: 'Asha Rao', email: 'asha@acme.com', jobTitle: 'Engineer', department: 'Engineering',
      countryCode: 'IN', annualSalary: 1_000_000, hireDate: '2021-03-15',
    };

    service.create(body).subscribe();
    service.update(7, body).subscribe();
    service.delete(7).subscribe();

    expect(http.expectOne({ method: 'POST', url: '/api/employees' }).request.body).toEqual(body);
    expect(http.expectOne({ method: 'PUT', url: '/api/employees/7' }).request.body).toEqual(body);
    http.expectOne({ method: 'DELETE', url: '/api/employees/7' });
  });
});
