import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { CountryDetail, OrgOverview } from './models';

@Injectable({ providedIn: 'root' })
export class InsightsService {
  private readonly http = inject(HttpClient);

  overview(): Observable<OrgOverview> {
    return this.http.get<OrgOverview>('/api/insights/countries');
  }

  countryDetail(countryCode: string): Observable<CountryDetail> {
    return this.http.get<CountryDetail>(`/api/insights/countries/${encodeURIComponent(countryCode)}`);
  }
}
