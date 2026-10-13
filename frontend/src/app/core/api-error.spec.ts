import { HttpErrorResponse } from '@angular/common/http';

import { errorMessage, fieldErrors } from './api-error';

describe('api-error', () => {
  it('extracts field errors from a problem response', () => {
    const error = new HttpErrorResponse({ status: 409, error: { detail: 'Conflict', errors: { email: 'taken' } } });

    expect(fieldErrors(error)).toEqual({ email: 'taken' });
  });

  it('returns no field errors for other failures', () => {
    expect(fieldErrors(new HttpErrorResponse({ status: 500 }))).toEqual({});
    expect(fieldErrors(new Error('boom'))).toEqual({});
  });

  it('prefers the problem detail, explains network failures, and falls back to a generic message', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 404, error: { detail: 'Employee 9 not found' } })))
      .toBe('Employee 9 not found');
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Cannot reach the server');
    expect(errorMessage(new Error('boom'))).toContain('Something went wrong');
  });
});
