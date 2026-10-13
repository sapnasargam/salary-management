import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { InsightsService } from '../core/insights.service';
import { CountryDetail } from '../core/models';
import { ReferenceDataService } from '../core/reference-data.service';
import { CountryInsightsComponent } from './country-insights.component';

const stats = (headcount: number, avg: number) => ({ headcount, min: avg - 10, max: avg + 10, average: avg, median: avg });

const INDIA: CountryDetail = {
  countryCode: 'IN', countryName: 'India', currency: 'INR', salary: stats(3, 2_000_000),
  byJobTitle: [{ name: 'Software Engineer', salary: stats(2, 2_100_000) }, { name: 'Recruiter', salary: stats(1, 900_000) }],
  byDepartment: [{ name: 'Engineering', salary: stats(2, 2_100_000) }],
};

describe('CountryInsightsComponent', () => {
  let fixture: ComponentFixture<CountryInsightsComponent>;
  let insights: jasmine.SpyObj<InsightsService>;

  beforeEach(() => {
    insights = jasmine.createSpyObj<InsightsService>('InsightsService', ['countryDetail']);
    insights.countryDetail.and.returnValue(of(INDIA));
    const referenceData = jasmine.createSpyObj<ReferenceDataService>('ReferenceDataService', ['get']);
    referenceData.get.and.returnValue(of({ countries: [], departments: [], jobTitles: [] }));

    TestBed.configureTestingModule({
      imports: [CountryInsightsComponent],
      providers: [
        provideNoopAnimations(),
        provideRouter([]),
        { provide: InsightsService, useValue: insights },
        { provide: ReferenceDataService, useValue: referenceData },
      ],
    });
    fixture = TestBed.createComponent(CountryInsightsComponent);
    fixture.componentRef.setInput('countryCode', 'IN');
    fixture.detectChanges();
  });

  it('loads the country from the route parameter and shows headline figures in local currency', () => {
    expect(insights.countryDetail).toHaveBeenCalledWith('IN');
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('India');
    expect(text).toContain('₹2,000,000');
  });

  it('links each job title to the employee list filtered by country and title', () => {
    const rows = fixture.componentInstance.byJobTitle();

    expect(rows.map((r) => r.label)).toEqual(['Software Engineer', 'Recruiter']);
    expect(rows[0].link).toEqual({ commands: ['/employees'], queryParams: { country: 'IN', jobTitle: 'Software Engineer' } });
    expect(fixture.componentInstance.byDepartment()[0].link?.queryParams).toEqual({ country: 'IN', department: 'Engineering' });
  });
});
