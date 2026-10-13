# Salary Management — Requirements (one-pager)

## Goal
Replace the spreadsheets ACME's HR team uses to manage salaries for **~10,000 employees across multiple
countries** with a web application where an **HR Manager** can (1) maintain employee salary records and
(2) quickly answer "how does the org pay people?" questions.

## Persona & key questions
**HR Manager** — not technical, lives in Excel today. The questions they need to answer:
1. *What is the min / max / average salary in country X?*
2. *What is the average salary for job title Y in country X?*
3. *How is pay distributed across countries and departments (headcount, median, spread)?*
4. *Find employee Z and correct / update their salary.*

## Scope & features (in)
| # | Feature | Why |
|---|---------|-----|
| F1 | **Employee directory** — paginated table, search by name/email/code, filter by country / department / job title, sortable columns | Excel-like browsing, but fast for 10k rows |
| F2 | **Add / edit / delete employee** with server-side validation | Core data maintenance; validation prevents the bad data typical in spreadsheets |
| F3 | **Currency derived from country** (employee in India ⇒ INR) | Removes a whole class of data-entry mistakes |
| F4 | **Org overview insights** — headcount and min / max / avg / median salary *per country* | Answers Q1 and Q3 at a glance |
| F5 | **Country drill-down** — the same stats broken down by job title and by department within a country | Answers Q2 and Q3 |
| F6 | **Seed script** generating 10,000 realistic, deterministic employees | Required; also gives a realistic dataset for demo and performance checks |

## Deliberately left out (and why)
| Not building | Reasoning |
|--------------|-----------|
| **Currency conversion / org-wide totals in one currency** | Needs a trustworthy, dated FX source. A mixed-currency average would be *wrong* and misleading, so insights are always grouped by country in local currency. |
| **Authentication / roles** | One persona, one trusted user in this exercise. In production this sits behind company SSO (Spring Security + OIDC); the API is stateless so that is additive. |
| **Salary history / audit trail** | Valuable (raises, compliance) but doubles the data model. First follow-up after launch. |
| **Excel import / export** | Clearly the next most valuable feature for this persona (migration path), but the 10k seed covers the demo data need. |
| **Payroll, tax, bonuses, benefits** | Different domain (payroll processing) — out of scope for *salary management*. |
| **Charts-heavy dashboards** | Tables with clear numbers answer the questions; charts are polish. |

## Non-functional requirements
- List and insight endpoints respond in **< 300 ms** for 10k employees on a laptop.
- Salary stored as an **integer amount of whole currency units per year** — no floating-point money.
- Server is the source of truth for validation; the UI mirrors it for good UX.
- Fast, deterministic tests: pure unit tests for business rules, slice tests for the web layer,
  a few integration tests against a real (in-memory) database.
- Runs with one command locally; ships as one Docker image (API + UI).

## Success criteria
An HR Manager can open the app, find any employee in a few seconds, change their salary, and answer
Q1–Q3 without exporting anything to Excel.
