# Database migration readiness

## Current repository findings

- The application has JPA entities for users, farms, fields, crop types, crop plantings, crop activity logs, expenses, and harvest records.
- The project has a MySQL JDBC driver and H2 for tests, but no Flyway or Liquibase dependency.
- No checked-in production `application.properties` / YAML configuration or existing migration scripts were found on `main`.
- The test profile configures H2 with `spring.jpa.hibernate.ddl-auto=create-drop`; this creates a test schema from entities and does not validate an upgrade of an existing MySQL database.
- `Field` retains a deprecated `availableArea` compatibility column. Its entity comment explicitly says it should be removed by a migration after existing installations have migrated. Removing it in the first migration without verifying existing deployments would be unsafe.
- The expense and harvest entities map to `expenses` and `harvest_records`, with foreign keys to `farms` and `crop_plantings`. They also have audit/void columns and indexes that need to be represented in SQL.

## Why a schema migration is not added yet

There is no checked-in canonical schema or migration history to establish which tables and columns already exist in deployed databases. A single migration that creates all tables may fail on existing installations; a migration that creates only expense and harvest tables may fail on a fresh database or if the existing table/column names differ. Enabling Flyway without an explicit baseline strategy could also make Flyway reject a non-empty database or mark it as already migrated without applying required changes.

## Safe next steps

1. Obtain a schema-only dump from the target MySQL database (for example, `mysqldump --no-data`) and confirm whether any environment has existing data. Do not share credentials or production data.
2. Compare that schema with the JPA entity mappings, including table/column names, indexes, unique constraints, foreign keys, and the legacy `fields.availableArea` column.
3. Choose an adoption plan:
   - **Fresh database only:** add a complete initial Flyway migration for all mapped tables, then use Flyway for subsequent changes.
   - **Existing database:** create a verified baseline matching the deployed schema, baseline existing installations at that version, and add a later migration for expense/harvest tables and indexes.
4. Keep Hibernate schema mutation disabled in production once migrations own schema changes; use schema validation instead.
5. Test both a fresh MySQL-compatible schema and an upgrade from a representative existing schema before deploying.

## Safety rule

Do not drop or rename legacy columns, or baseline a production database, until the actual deployed schema and data-retention requirements have been checked.
