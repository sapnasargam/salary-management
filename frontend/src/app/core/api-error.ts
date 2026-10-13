import { HttpErrorResponse } from '@angular/common/http';

/** Field-level messages from an RFC 7807 problem response (`{ errors: { field: message } }`). */
export function fieldErrors(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.error && typeof error.error.errors === 'object') {
    return error.error.errors as Record<string, string>;
  }
  return {};
}

/** A message suitable for a snackbar: the problem `detail` if present, otherwise a generic fallback. */
export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Cannot reach the server. Please check your connection.';
    }
    if (typeof error.error?.detail === 'string') {
      return error.error.detail;
    }
  }
  return 'Something went wrong. Please try again.';
}
