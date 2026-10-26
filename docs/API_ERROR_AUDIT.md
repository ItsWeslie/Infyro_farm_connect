# API Error and Ownership Audit

Audit basis: controller, service, security, DTO, and exception-handler source on the current `main` branch, inspected on 2026-10-26. This is a source review, not a claim that every behavior has already been verified by HTTP integration tests.

## Findings

### Central exception handling

`GlobalExceptionHandler` is a `@RestControllerAdvice` and currently handles:

| Exception | Status | Body |
|---|---:|---|
| `UserNotFoundException` | 404 | `APIResponse { message }` |
| `InvalidTokenException` | 400 | `APIResponse { message }` |
| `UserAlreadyExistsException` | 409 | `APIResponse { message }` |
| `FarmNotFoundException` | 404 | `APIResponse { message }` |
| `FieldIsFullWithCropsException` | 400 | `APIResponse { message }` |
| `FieldNotFoundException` | 404 | `APIResponse { message }` |

The handler does not currently declare handlers for bean-validation exceptions, malformed JSON, constraint violations, or a generic unexpected exception. Some failures may therefore use Spring's default error body while others use the application's `APIResponse`. This is the clearest inconsistency to address.

### Authentication and authorization

`SecurityConfig` configures stateless sessions, inserts a JWT filter, permits `APIConstants.AUTH/**`, requires role `USER` for `APIConstants.USER/**`, and requires authentication for other requests. The configuration shown does not explicitly configure a shared JSON response body for authentication failures or access-denied failures. Actual filter behavior must be tested before documenting exact status/body combinations.

Controller methods commonly pass `Principal.getName()` into service methods, allowing service/repository queries to scope data to the current owner's email. This is a useful baseline and should be retained.

### Ownership and not-found policy

- Farm and field services use owner-scoped repository lookups and throw `FarmNotFoundException` or `FieldNotFoundException` when the record is not visible to that owner. These exceptions map to 404, concealing whether another owner owns the record.
- Farm summary service uses `findByIdAndOwner_Email`; a farm not visible to the caller maps to 404.
- Crop planting service uses owner-scoped queries and returns 404 for a planting/field not found.
- Crop activity service uses owned-planting and owned-activity helper methods; verify those helper implementations with focused tests.
- Expense and harvest controllers first load the record through owner-filtered service methods and then check that its farm/planting ID matches the parent path; a parent mismatch returns 404.
- Security configuration itself requires authentication for all non-auth routes. Ensure the JWT filter's behavior for missing/invalid tokens is covered by tests.

### Existing status-code patterns

| Case | Current observed behavior | Follow-up |
|---|---|---|
| Resource absent or not owned | Usually 404 through owner-scoped lookup | Preserve and contract-test |
| Duplicate user | 409 | Preserve and contract-test |
| Invalid token exception | 400 | Verify whether this exception is used for login/API authentication; do not assume it covers filter-level 401 responses |
| Bean validation failure | No explicit global handler found | Add field-level validation response and tests |
| Malformed JSON / invalid enum input | No explicit global handler found | Standardize as 400 and test |
| Business rule conflict | Some services throw `ResponseStatusException(409)` | Preserve status and normalize body |
| Unexpected server exception | No explicit generic handler found | Return safe generic 500; log details server-side only |
| Unauthenticated / forbidden request | Security entry point/denied handler not explicitly configured here | Test actual filter-chain responses, then define contract |

## Recommended error contract

Introduce a dedicated error response DTO rather than expanding the success-oriented `APIResponse`:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "fieldErrors": [
    {
      "field": "amount",
      "message": "Amount must be greater than zero"
    }
  ]
}
```

Use stable machine-readable codes. Keep `fieldErrors` empty or omit it for errors without field-level details. Do not expose exception class names, stack traces, SQL messages, tokens, or other internal details.

Proposed status policy:
- 400: malformed JSON, invalid enum/date formats, bean validation, invalid business-state transition where existing behavior is 400.
- 401: missing/invalid authentication.
- 403: authenticated but explicitly forbidden by authorization policy; for owner-scoped resources that are intentionally concealed, retain 404 consistently.
- 404: missing resource or resource not visible under owner-scoped queries.
- 409: conflicting state, duplicate resource, repeated cancellation, or deletion blocked by history.
- 500: unexpected server error with a generic client-safe message.

## Implementation sequence

1. Add `ErrorResponse` and field-error DTO (or equivalent), then extend `GlobalExceptionHandler` for validation, unreadable JSON, `ResponseStatusException`, and safe fallback 500 handling.
2. Add explicit JSON authentication-entry-point and access-denied handling only after confirming JWT filter behavior and the intended 401/403 policy.
3. Add focused MVC tests for each error class, including stable response schema and status.
4. Add HTTP integration tests for owner A attempting to access owner B's farm, field, planting, expense, harvest, and activity records.
5. Update `docs/API.md` only after the error contract is implemented and tests establish the actual behavior.

## Scope caveat

This is an initial audit based on the files inspected, not a complete enumeration of all controllers, exception types, or authentication flows. New error handlers should be introduced with tests and without changing business rules or owner-scoped 404 behavior inadvertently.
