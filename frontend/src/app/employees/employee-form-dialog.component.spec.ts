import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, throwError } from 'rxjs';

import { EmployeeService } from '../core/employee.service';
import { Employee, ReferenceData } from '../core/models';
import { EmployeeFormDialogComponent, EmployeeFormDialogData } from './employee-form-dialog.component';

const REFERENCE_DATA: ReferenceData = {
  countries: [
    { code: 'IN', name: 'India', currency: 'INR' },
    { code: 'US', name: 'United States', currency: 'USD' },
  ],
  departments: ['Engineering'],
  jobTitles: ['Software Engineer'],
};

const ASHA: Employee = {
  id: 7, fullName: 'Asha Rao', email: 'asha@acme.com', jobTitle: 'Software Engineer', department: 'Engineering',
  countryCode: 'IN', countryName: 'India', currency: 'INR', annualSalary: 1_800_000, hireDate: '2021-03-15',
};

describe('EmployeeFormDialogComponent', () => {
  let fixture: ComponentFixture<EmployeeFormDialogComponent>;
  let component: EmployeeFormDialogComponent;
  let employees: jasmine.SpyObj<EmployeeService>;
  let dialogRef: jasmine.SpyObj<MatDialogRef<EmployeeFormDialogComponent>>;

  function setUp(data: EmployeeFormDialogData): void {
    employees = jasmine.createSpyObj<EmployeeService>('EmployeeService', ['create', 'update']);
    dialogRef = jasmine.createSpyObj<MatDialogRef<EmployeeFormDialogComponent>>('MatDialogRef', ['close']);
    TestBed.configureTestingModule({
      imports: [EmployeeFormDialogComponent],
      providers: [
        provideNoopAnimations(),
        { provide: EmployeeService, useValue: employees },
        { provide: MatDialogRef, useValue: dialogRef },
        { provide: MAT_DIALOG_DATA, useValue: data },
      ],
    });
    fixture = TestBed.createComponent(EmployeeFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('derives the currency from the selected country', () => {
    setUp({ referenceData: REFERENCE_DATA });

    expect(component.currency()).toBe('');
    component.form.controls.countryCode.setValue('US');
    expect(component.currency()).toBe('USD');
  });

  it('does not call the API while the form is invalid', () => {
    setUp({ referenceData: REFERENCE_DATA });

    component.save();

    expect(employees.create).not.toHaveBeenCalled();
    expect(component.form.controls.fullName.touched).toBeTrue();
    expect(component.errorFor('fullName')).toBe('Required');
  });

  it('creates a new employee and closes with the saved record', () => {
    setUp({ referenceData: REFERENCE_DATA });
    employees.create.and.returnValue(of(ASHA));
    component.form.setValue({
      fullName: 'Asha Rao', email: 'asha@acme.com', jobTitle: 'Software Engineer', department: 'Engineering',
      countryCode: 'IN', annualSalary: 1_800_000, hireDate: '2021-03-15',
    });

    component.save();

    expect(employees.create).toHaveBeenCalledWith(jasmine.objectContaining({ countryCode: 'IN', annualSalary: 1_800_000 }));
    expect(dialogRef.close).toHaveBeenCalledWith(ASHA);
  });

  it('edits an existing employee, pre-filling the form', () => {
    setUp({ employee: ASHA, referenceData: REFERENCE_DATA });
    employees.update.and.returnValue(of({ ...ASHA, annualSalary: 2_000_000 }));

    expect(component.isEdit).toBeTrue();
    expect(component.currency()).toBe('INR');
    component.form.controls.annualSalary.setValue(2_000_000);
    component.save();

    expect(employees.update).toHaveBeenCalledWith(7, jasmine.objectContaining({ annualSalary: 2_000_000 }));
  });

  it('shows server-side field errors next to the matching input and keeps the dialog open', () => {
    setUp({ employee: ASHA, referenceData: REFERENCE_DATA });
    employees.update.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 409, error: { detail: 'Conflict', errors: { email: 'This e-mail is already used by another employee' } },
    })));

    component.save();

    expect(component.errorFor('email')).toBe('This e-mail is already used by another employee');
    expect(component.saving()).toBeFalse();
    expect(dialogRef.close).not.toHaveBeenCalled();
  });

  it('shows a form-level message when the error is not about a specific field', () => {
    setUp({ employee: ASHA, referenceData: REFERENCE_DATA });
    employees.update.and.returnValue(throwError(() => new HttpErrorResponse({ status: 0 })));

    component.save();

    expect(component.formError()).toContain('Cannot reach the server');
  });
});
