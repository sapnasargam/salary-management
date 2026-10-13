# How I Used AI on This Assessment

**Tool:** Claude Code, an agentic coding assistant, run inside VS Code with access to the terminal
(Maven, npm, git, headless Chrome).

The brief asked for AI to be used *intentionally while maintaining correctness and quality*. My approach:
**I owned the problem, the decisions and the quality bar. The AI agent was a fast pair that implemented
in small steps.** Nothing was accepted just because the AI produced it. Every step had to pass tests
and be checked against the running application.

---

## 1. My workflow

| Step | What I did | Why |
|------|------------|-----|
| **Understand the problem** | Started from the brief and the HR Manager persona. I wrote down the questions HR actually needs answered, e.g. "average salary for a job title in a country". | Product thinking before code. The features come from user questions, not from a CRUD template. |
| **Requirements first** | One-page requirements (`01-requirements.md`) and design notes (`02-design.md`), committed **before any code**. Includes what I deliberately left out and why. | Gives the AI a clear spec and stops scope creep. |
| **Small vertical slices** | Built in order: scaffold → employee API → insights → seeding → UI → packaging. Each slice is its own commit. | Easy to review and easy to roll back. The git history shows how the solution evolved. |
| **Tests as the gate** | The full test suite ran before every commit. A slice was "done" only when it was green. | AI output is a draft until tests prove it. |
| **Verify on the real system** | Seeded 10,000 employees, ran the app, timed every endpoint, and checked the UI with screenshots. | Tests alone don't show performance or what the UI looks like. |
| **Keep to the brief** | Re-checked the solution against the assessment line by line and removed anything not asked for (docker-compose, an actuator endpoint). | Good engineering judgement includes knowing what *not* to build. |

## 2. Decisions I made, and why I stand by them

| Decision | Reasoning |
|----------|-----------|
| **Never mix currencies.** All salary statistics are grouped per country, in local currency. | Averaging INR with USD gives a meaningless number. Real FX conversion needs a trusted, dated rate source, so I left it out deliberately rather than show misleading numbers. |
| **Currency is derived from country**, not entered separately | Removes a whole class of data-entry errors that spreadsheets allow. |
| **Show the median next to the average** | A few high salaries pull the average up. The median answers "what does a typical person earn?". |
| **Salary stored as a whole number (`long`)** | No floating-point rounding on money. |
| **Statistics in plain Java over a lean 4-column query** | SQLite cannot compute a median. 10k rows aggregate in about 60 ms, and the logic is unit-testable without a database. The scaling path (SQL `GROUP BY` / Postgres `percentile_cont`) is documented. |
| **Server-side search, filtering, sorting and paging** | Ten thousand rows should never be shipped to the browser. |
| **Deterministic seed data** (fixed random seed) | Same data on every run, so demos and tests are reproducible. |
| **One deployable artifact** (UI bundled into the Spring Boot jar) | One thing to deploy, no cross-origin (CORS) setup. |
| **UI follows Incubyte's visual language** | Palette and fonts taken from incubyte.co's own CSS and applied once, as Angular Material design tokens. |

## 3. How I kept AI output honest: issues caught and fixed

These came up during development. Each was caught by a test, a build warning or review, never by luck.

| Issue in the generated code | Caught by | Fix |
|-----------------------------|-----------|-----|
| The `direction=asc` query parameter was rejected (case-sensitive enum binding), so every search returned 400 | Integration test | Parse the direction explicitly, with a clear 400 for invalid values |
| A catch-all exception handler would have turned Spring's own 404s into 500s, and mapping `IllegalArgumentException` → 400 would have hidden real bugs | Code review | Removed both. Only explicit, known exceptions are mapped |
| "Today" computed with `toISOString()` is UTC, so the hire-date limit is wrong for Indian users before 05:30 | Code review | Local-date helper |
| Sass "mixed declarations" deprecation warnings (195 of them) | Build output | Restructured the theme file |
| The seed script's `$reset` variable clashed with its `-Reset` switch (PowerShell names are case-insensitive) | Running the script | Renamed the variable |
| An unused field in the data generator | Code review | Removed |

## 4. Prompts and instructions I used

The working style was conversational and iterative. The instructions that shaped the result:

1. *"Read the assessment document carefully and build the solution it describes: requirements doc first,
   then backend, UI, seed data, tests and artifacts, with incremental commits."*
2. *"Match the UI to Incubyte's website."* The agent took the real palette and fonts from incubyte.co
   rather than guessing.
3. *"Re-check the solution against the assessment document line by line. Follow exactly what it asks
   for, nothing extra."* This led to removing docker-compose and the actuator endpoint.
4. *"Is my AI-usage write-up appropriate for the interview?"* I checked what Incubyte expects and
   chose to be fully transparent, as the brief explicitly asks.

## 5. What I would do next

- **Test-first (TDD) in a pairing setup.** In this exercise tests were written alongside each slice,
  not strictly before it. In a team I'd drive slices test-first.
- **Salary history and audit trail.** Raises over time, and who changed what.
- **Excel import/export.** The natural migration path for an HR team that lives in spreadsheets.
- **Authentication via company SSO** (Spring Security + OIDC). The API is stateless, so this can be
  added without changing it.
- **Postgres** if write concurrency or data volume grows beyond what SQLite handles comfortably.
