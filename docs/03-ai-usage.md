# How AI was used

**Tool:** Claude Code (agentic coding assistant, Claude Opus model) running in VS Code, with access to the
terminal (Maven, npm, git, headless Chrome).

## Workflow
1. **Requirements first.** The assessment document was given to the agent. The agent wrote the one-page
   requirements (`01-requirements.md`) and design notes (`02-design.md`) *before* any code, and they
   were committed first.
2. **Thin vertical slices with a test at each step.** Each slice was built in order: scaffold → employee
   CRUD → insights → seeding → UI → packaging. The full test suite ran before every commit, and each
   commit is one coherent step.
3. **Verification against the real system, not just tests.** After seeding 10k rows the agent started the
   app, timed every endpoint (numbers in `02-design.md`), and took headless-Chrome screenshots to check
   the UI.
4. **Human direction.** The candidate steered the important choices: project location, git identity,
   matching the UI to Incubyte's visual style, and keeping to exactly what the assessment asks for.

## Key prompts
> Read this assessment document properly and complete it. Afterwards, explain all the logic and
> strategy used to build it.

> Make the UI format like the Incubyte website.

The agent fetched incubyte.co, pulled the real palette and fonts from its CSS (`#014d43`, `#d3fe73`,
`#ebfff6`, Fraunces, Inclusive Sans, pill buttons) and applied them as Material design tokens.

> Read my assignment document properly — follow exactly the pattern written in the assignment, nothing
> extra; the frontend theme should match this company.

Extras that were not asked for (docker-compose, actuator health endpoint) were removed.

## Where AI output was corrected (by test failures or review)
| Issue caught | How | Fix |
|---|---|---|
| `Sort.Direction` request param rejected `asc` (case-sensitive enum binding) → 400 | Integration test | Parse direction explicitly with a clear 400 message |
| Generic `catch (Exception)` handler would have turned Spring's 404s into 500s, and `IllegalArgumentException` → 400 would hide bugs | Self-review | Removed both handlers; only explicit, known exceptions are mapped |
| `toISOString()` for "today" is UTC → wrong date before 05:30 IST | Review | Local-date helper |
| Sass "mixed declarations" deprecation | Build warnings | Moved token overrides into their own block |
| PowerShell `$reset` variable clashed with `-Reset` switch (variable names are case-insensitive) | Running the seed script | Renamed the variable |
| An unused `emailSuffix` field in the generator | Review | Removed |

## Guardrails used
- Business rules live in plain classes (`SalaryStatistics`, `EmployeeService`, `EmployeeDataGenerator`),
  so their tests are fast and deterministic. No test depends on time, network or random order.
- Integration tests use a throw-away SQLite file per Spring context, so they are isolated.
- The seed data is deterministic (fixed random seed), so demos and tests are reproducible.
