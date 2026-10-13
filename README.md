# ACME Salary Management

A web app for ACME's HR Manager to manage salaries of 10,000 employees across 10 countries and answer
"how does the org pay people?". It replaces the HR team's spreadsheets.

**Stack:** Java 17 · Spring Boot 3 · SQLite + Flyway · Angular 19 · Angular Material

| Doc | What's inside |
|-----|---------------|
| [docs/01-requirements.md](docs/01-requirements.md) | One-page requirements: goal, scope, what's left out and why |
| [docs/02-design.md](docs/02-design.md) | Architecture diagram, key decisions & trade-offs, data model, API, performance |
| [docs/03-ai-usage.md](docs/03-ai-usage.md) | How AI tools were used, prompts, and what was corrected |

## Features
- **Employees:** paginated directory of all 10k employees, with search by name or e-mail and filters for
  country, department and job title. Columns are sortable. Employees can be added, edited and deleted,
  with validation.
- **Salary insights:** per-country headcount and min / median / average / max salary. Each country breaks
  down by job title and department. Every row links to the matching employees.
- **Currency follows country:** salaries are stored in local currency and never mixed across countries.

## Run locally
Prerequisites: JDK 17, Maven 3.9, Node 20+.

```bash
# 1. Backend on :8080 – creates salary.db and seeds 10,000 employees on first start
cd backend
mvn spring-boot:run

# 2. Frontend on :4200 (proxies /api to :8080)
cd frontend
npm install
npm start
```
Open http://localhost:4200

### Seed script
Seeding runs automatically when the database is empty. To run it on its own:
```bash
./scripts/seed.sh            # or  .\scripts\seed.ps1          — seed if empty
./scripts/seed.sh --reset    # or  .\scripts\seed.ps1 -Reset   — wipe and regenerate 10,000 employees
```
The same random seed always produces the same employees.

## Tests
```bash
cd backend  && mvn test            # 50 tests: unit, web-slice and SQLite integration (~20 s)
cd frontend && npm run test:ci     # 16 tests, headless Chrome (< 1 s execution)
```

## Deploy
The `Dockerfile` builds one image that serves both the UI and the API on port 8080.
```bash
docker build -t salary-management .
docker run -p 8080:8080 -v salary-data:/data salary-management
```
On **Render**: New → Blueprint → select this repo (`render.yaml`).

## Project layout
```
backend/   Spring Boot API — packages by feature: employee, insights, country, seed, referencedata, common
frontend/  Angular app — core (API services/models), employees, insights, shared
docs/      requirements, design, AI usage
scripts/   seed scripts
```
