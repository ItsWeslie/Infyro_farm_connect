# API Contract (Verified Endpoints)

This document records the endpoints and DTO constraints verified in the current source. It is an initial contract, not yet a complete inventory of every controller. Authentication is required where controller methods use the authenticated `Principal`; use the application's configured authentication mechanism.

## Conventions

- JSON request and response bodies are used where a body is present.
- Dates use ISO-8601 calendar format: `YYYY-MM-DD`.
- Monetary values and quantities are represented as decimal numbers.
- Resource ownership is enforced by service methods. Requests must not be treated as authorized merely because an ID is known.
- Expense and harvest cancellation is a soft-void operation. Responses retain `voided`, `statusLabel`, `voidedAt`, and `voidReason` fields.

## Farm summaries

Base path: `/api/farms/{farmId}`

| Method | Path | Purpose | Success |
|---|---|---|---|
| GET | `/summary` | Farm totals and grouped summaries | `200 OK` |
| GET | `/expenses/summary` | Expense totals and category breakdown | `200 OK` |
| GET | `/harvests/summary` | Harvest counts and quantities by unit | `200 OK` |
| GET | `/plantings/summary` | Planting counts by status | `200 OK` |

The expense and harvest summary endpoints accept optional `from` and `to` query parameters in `YYYY-MM-DD` format. The date range is inclusive. A range where `from` is after `to` is rejected with `400 Bad Request`.

### Response fields

- `FarmSummaryResponse`: `farmId`, `farmName`, `totalFields`, `totalPlantings`, `plantingsByStatus`, `totalExpenses`, `expensesByCategory`, `harvestQuantityByUnit`, `harvestRecordCount`.
- `ExpenseSummaryResponse`: `farmId`, `from`, `to`, `recordCount`, `totalAmount`, `byCategory`.
- `HarvestSummaryResponse`: `farmId`, `from`, `to`, `recordCount`, `quantityByUnit`.
- `PlantingSummaryResponse`: `farmId`, `totalPlantings`, `countsByStatus`.

Summary totals exclude voided expense and harvest records. Grouped maps use the corresponding enum values as JSON object keys.

## Expenses

Base path: `/api/farms/{farmId}/expenses`

| Method | Path | Purpose | Success |
|---|---|---|---|
| POST | `/` | Create an expense for the farm | `201 Created` |
| GET | `/` | List farm expenses, including cancelled history | `200 OK` |
| GET | `/{id}` | Get one expense belonging to the farm | `200 OK` |
| DELETE | `/{id}` | Soft-cancel an expense | `204 No Content` |

### Create request

```json
{
  "cropPlantingId": 123,
  "amount": 125.50,
  "expenseDate": "2026-10-20",
  "category": "<ExpenseCategory enum value>",
  "description": "Seeds and supplies"
}
```

- `cropPlantingId` is optional; if supplied, the planting must belong to the same farm and owner.
- `amount` is required, at least `0.01`, with up to 12 integer digits and 2 decimal places.
- `expenseDate` is required and cannot be in the future.
- `category` is required and must be a valid `ExpenseCategory` enum value.
- `description` is required, non-blank, and at most 1000 characters.

Expense response fields: `id`, `farmId`, `cropPlantingId`, `amount`, `expenseDate`, `category`, `description`, `createdAt`, `updatedAt`, `voided`, `statusLabel`, `voidedAt`, `voidReason`.

## Harvest records

Base path: `/api/crop-plantings/{plantingId}/harvest-records`

| Method | Path | Purpose | Success |
|---|---|---|---|
| POST | `/` | Create a harvest record | `201 Created` |
| GET | `/` | List planting harvest records, including cancelled history | `200 OK` |
| GET | `/{id}` | Get one harvest record for the planting | `200 OK` |
| DELETE | `/{id}` | Soft-cancel a harvest record | `204 No Content` |

### Create request

```json
{
  "harvestDate": "2026-10-20",
  "quantity": 12.500,
  "unit": "<HarvestUnit enum value>",
  "notes": "First harvest"
}
```

- `harvestDate` is required, cannot be in the future, and must not precede the planting date.
- `quantity` is required, at least `0.001`, with up to 11 integer digits and 3 decimal places.
- `unit` is required and must be a valid `HarvestUnit` enum value.
- `notes` is optional and at most 2000 characters.
- The planting must belong to the authenticated owner and be in an allowed harvest status.

Harvest response fields: `id`, `cropPlantingId`, `harvestDate`, `quantity`, `unit`, `notes`, `createdAt`, `updatedAt`, `voided`, `statusLabel`, `voidedAt`, `voidReason`.

## Current error behavior and work still required

The following behaviors are verified in the source/business rules:

- Request DTO validation is applied to expense and harvest creation using `@Valid`.
- Invalid or reversed summary date ranges are intended to return `400 Bad Request`.
- Missing resources and parent-resource mismatches in expense/harvest get/delete operations return `404 Not Found`.
- Repeated cancellation is rejected as a conflict (`409`).
- Creating a harvest with a future date or a date before planting is rejected as a bad request (`400`).

The error response body is not yet standardized by this document. Before promising one universal JSON error schema, audit the existing exception handlers and Spring Security behavior, then add contract tests for validation, unauthenticated requests, ownership failures, missing resources, and conflicts.

## Scope limitations

This initial document covers Farm Summary, Expense, and Harvest Record endpoints verified from their controllers and DTOs. Other controllers and authentication routes must be inventoried before this can be called the complete API reference. Exact enum values should be confirmed against the enum definitions before clients rely on them.
