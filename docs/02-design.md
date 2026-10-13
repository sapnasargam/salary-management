# Design Notes

## Architecture

```
┌──────────────────────┐   HTTP/JSON    ┌──────────────────────────────────────────┐
│ Angular SPA          │ ─────────────▶ │ Spring Boot API                          │
│ (Angular Material)   │   /api/**      │                                          │
│  • Employees page    │                │  web      → controllers, DTOs, errors    │
│  • Insights page     │                │  service  → use cases, validation rules  │
└──────────────────────┘                │  domain   → Employee, SalaryStatistics   │
                                        │  repo     → Spring Data JPA              │
                                        │  seed     → deterministic data generator │
                                        └───────────────┬──────────────────────────┘
                                                        │ JDBC
                                                ┌───────▼───────┐
                                                │ SQLite (file) │  schema via Flyway
                                                └───────────────┘
```

In production the Angular build is copied into the Spring Boot jar, so **one artifact / one Docker
image** serves both UI and API (no CORS, one thing to deploy). In development they run separately and
the Angular dev server proxies `/api` to `:8080`.

## Key decisions

| Decision | Choice | Alternatives considered / reasoning |
|----------|--------|--------------------------------------|
| Backend | Java 17 + Spring Boot 3 | Matches the role. Boring, well-known stack. |
| Database | SQLite (file) | Zero-ops, enough for 10k rows, relational. Swappable to Postgres by changing the JDBC URL + dialect; schema is plain SQL in Flyway migrations. |
| Schema management | Flyway migrations | `ddl-auto=validate` — the schema is explicit and versioned, Hibernate never mutates it. |
| Money | `long annualSalary` in whole units of the employee's currency | No floating-point; cents are irrelevant for annual salary. |
| Currency | Derived from country (reference table `country`) | HR can't enter a mismatched currency. |
| Multi-currency stats | Always grouped by country | Averaging INR with USD is meaningless without FX. Documented in requirements. |
| Statistics | Pure Java `SalaryStatistics` over a lightweight projection `(country, department, jobTitle, salary)` | Median/percentiles aren't portable SQL (SQLite has no `percentile_cont`). Pulling 10k `long`s is ~ms. Pure function ⇒ trivially unit-tested. Scaling note below. |
| Search & filters | Server-side, paginated, JPA `Specification` | 10k rows shouldn't be shipped to the browser; filters compose cleanly. |
| Seeding | Deterministic generator (fixed random seed) + JDBC batch insert, idempotent | Same data every run ⇒ reproducible demos/tests. Batch insert of 10k rows takes < 1 s. |
| Errors | RFC 7807 `ProblemDetail` with field errors | Standard, UI can map field errors to form controls. |
| UI | Angular 19 standalone components + Angular Material | Role asks for Angular; Material gives an accessible table/paginator/forms out of the box. |

## Data model

```
country(code PK, name, currency)
employee(id PK, employee_code UNIQUE, full_name, email UNIQUE, job_title, department,
         country_code FK → country, annual_salary, hire_date, created_at, updated_at)
indexes: country_code, (country_code, job_title), department, full_name
```

## API

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/api/employees?search=&country=&department=&jobTitle=&page=&size=&sort=` | Paginated directory |
| GET | `/api/employees/{id}` | One employee |
| POST | `/api/employees` | Create (201 + Location) |
| PUT | `/api/employees/{id}` | Update |
| DELETE | `/api/employees/{id}` | Delete (204) |
| GET | `/api/reference-data` | Countries (with currency), departments, job titles for dropdowns |
| GET | `/api/insights/countries` | Per-country headcount, min / max / avg / median |
| GET | `/api/insights/countries/{code}` | Country summary + breakdown by job title and department |

## Performance considerations
- 10k rows: list queries are indexed and paginated (default page size 25).
- Insights load a 4-column projection (~10k rows) and aggregate in memory: measured in tens of ms.
- **If this grew to 1M+ employees**: move min/max/avg/count into SQL `GROUP BY` (Postgres also gives
  `percentile_cont` for median), or maintain a pre-aggregated table refreshed on write; add caching
  with invalidation on employee writes.
- SQLite allows one writer at a time — fine for a single HR team; Postgres for real multi-user load.
