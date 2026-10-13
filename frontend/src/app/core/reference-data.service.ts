import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, shareReplay } from 'rxjs';

import { ReferenceData } from './models';

/** Dropdown values. Cached for the session; call {@link refresh} after edits that may add new values. */
@Injectable({ providedIn: 'root' })
export class ReferenceDataService {
  private readonly http = inject(HttpClient);
  private cached$?: Observable<ReferenceData>;

  get(): Observable<ReferenceData> {
    this.cached$ ??= this.http.get<ReferenceData>('/api/reference-data').pipe(shareReplay(1));
    return this.cached$;
  }

  refresh(): void {
    this.cached$ = undefined;
  }
}
