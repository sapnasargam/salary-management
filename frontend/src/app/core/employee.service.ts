import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Employee, EmployeeQuery, EmployeeRequest, Page } from './models';

@Injectable({ providedIn: 'root' })
export class EmployeeService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/employees';

  search(query: EmployeeQuery): Observable<Page<Employee>> {
    let params = new HttpParams();
    for (const [key, value] of Object.entries(query)) {
      // Blank filters are omitted so the URL only describes what the user actually filtered on.
      if (value !== undefined && value !== null && `${value}`.trim() !== '') {
        params = params.set(key, `${value}`.trim());
      }
    }
    return this.http.get<Page<Employee>>(this.baseUrl, { params });
  }

  create(request: EmployeeRequest): Observable<Employee> {
    return this.http.post<Employee>(this.baseUrl, request);
  }

  update(id: number, request: EmployeeRequest): Observable<Employee> {
    return this.http.put<Employee>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
