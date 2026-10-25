# Database migrations

The application uses Flyway to manage the development MySQL schema. Versioned SQL scripts live under `src/main/resources/db/migration`.

## Local setup

1. Start MySQL and create an empty database named `infyro_farm_connect`.
2. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` if your local connection differs from the defaults.
3. Start the application. Flyway applies pending migrations before Hibernate validates the entity mappings.

The initial migration creates the schema represented by the current JPA entities, including expense and harvest records. It preserves the `fields.available_area` compatibility column because the current `Field` entity still maps it.

## Existing development databases

This initial migration is intended for a new, empty development database. If you already have a local database created by Hibernate, back it up first. Do not run Flyway against a non-empty schema that has no Flyway history without deliberately choosing and verifying a baseline procedure. For a disposable development database, the simplest clean start is to drop and recreate the empty schema, then restart the application.

Tests use H2 with Hibernate `create-drop` and disable Flyway; they validate application behavior, not the MySQL migration itself. Before relying on this migration, run it against a local MySQL instance and verify startup completes with `ddl-auto=validate`.
