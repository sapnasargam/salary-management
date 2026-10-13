CREATE TABLE country (
    code     TEXT PRIMARY KEY,           -- ISO 3166-1 alpha-2
    name     TEXT NOT NULL UNIQUE,
    currency TEXT NOT NULL               -- ISO 4217
);

CREATE TABLE employee (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    full_name     TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE,
    job_title     TEXT    NOT NULL,
    department    TEXT    NOT NULL,
    country_code  TEXT    NOT NULL REFERENCES country (code),
    annual_salary INTEGER NOT NULL CHECK (annual_salary > 0),  -- whole units of the country's currency
    hire_date     TEXT    NOT NULL                             -- ISO date yyyy-MM-dd
);

CREATE INDEX idx_employee_country ON employee (country_code);
CREATE INDEX idx_employee_country_job_title ON employee (country_code, job_title);
CREATE INDEX idx_employee_department ON employee (department);
CREATE INDEX idx_employee_full_name ON employee (full_name);
