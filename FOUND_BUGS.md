# Found Bugs

## BUG-001: "Create Player operation incorrectly implemented as GET with query parameters"
### Severity: **Major**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
The **Create Player** operation is implemented using HTTP **GET** with query parameters instead of **POST** with a JSON request body.
This violates REST principles and introduces risks such as:
- Improper API semantics
- Unintended duplicate resource creation
- Caching issues (browser, proxy, CDN)
- Exposure of sensitive data via query strings
- Lack of proper type safety

### Steps to Reproduce:
1. Send a `GET` request to `/player/create/{editor}` with required parameters in the query string.
2. Observe that a new player is created (`HTTP 200` returned with new ID).

### Actual Result:
A `GET` request creates a new player resource and returns a `200` status code.

The response body contains `id` and `login`, but other fields are returned as `null` (`screenName`, `gender`, `age`, `role`, `password`):
```json
{
  "id": 1473739907,
  "login": "login_23748",
  "password": null,
  "screenName": null,
  "gender": null,
  "age": null,
  "role": null
}
```

### Expected Result:
The create operation should be implemented as:
- `POST`
- Accepting `application/json` request body
- Not exposing sensitive fields (e.g., password) in query parameters
- Following RESTful conventions for resource creation
- Return `201` status code for successful creation


## BUG-002: "Password validation rules are not enforced during player creation"
### Severity: **Critical**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, the `password` must:
- Contain **latin letters and numbers**
- Have length between **7 and 15 characters**

The system accepts invalid passwords and successfully creates users (`HTTP 200`), violating multiple validation rules:
- Too short password (e.g., `J28`, length < 7)
- No digits (e.g., `CsvumKir`)
- Too long password (e.g., `R8UvykktwwD6rteMAnHueU2`, length > 15)
- Digits only (e.g., `1946328494`)

This indicates that password validation is either missing or not properly enforced.

### Steps to Reproduce:
1. Send `GET /player/create/{editor}` with invalid password values:
   - `password=J28`
   - `password=CsvumKir`
   - `password=R8UvykktwwD6rteMAnHueU2`
   - `password=1946328494`
2. Observe response status and body.

### Actual Result:
- HTTP 200 returned.
- A new player is created (new `id` returned).
- Invalid password values are accepted without validation errors.

### Expected Result:
The request should be rejected with a validation error (e.g., **400 Bad Request**) when:
- Password length is less than 7 characters
- Password length exceeds 15 characters
- Password does not contain both latin letters and digits


## BUG-003: "Login uniqueness constraint is not enforced during player creation"
### Severity: **Critical**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, the `login` field must be unique for each user.

The system allows creating a player with an already existing `login` value and returns **HTTP 200**.
Instead of rejecting the request, the API returns the same `id`, indicating that uniqueness validation is missing or incorrectly implemented.
This violates core business rules and may lead to data integrity issues.

### Steps to Reproduce:
1. Create a user with login.
2. Send another create request using the same `login` but different other parameters.
3. Observe the response.

### Actual Result:
- HTTP 200 returned.
- Same `id` returned for duplicate login.
- No validation error raised.

### Expected Result:
The second request should be rejected with a validation error (e.g., **400 Bad Request** or **409 Conflict**) indicating that the login already exists.


## BUG-004: "screenName uniqueness constraint is not enforced during player creation"
### Severity: **Major**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, the `screenName` field must be unique for each user.
The system allows creating a player with an already existing `screenName` and returns **HTTP 200** instead of rejecting the request.
This violates a core business rule and may lead to identity conflicts and data integrity issues.

### Steps to Reproduce:
1. Create a user with `screenName`.
2. Send another create request using the same `screenName` but a different `login`.
3. Observe the response.

### Actual Result:
- HTTP 200 returned.
- Player creation succeeds despite duplicate `screenName`.

### Expected Result:
The second request should be rejected with a validation error (e.g., **400 Bad Request** or **409 Conflict**) indicating that the `screenName` already exists.


## BUG-005: "Gender validation is not enforced during player creation"
### Severity: **Major**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, user's `gender` can only be: `male` or `female`.
The system accepts an invalid gender value (eg., `non-binary`) and successfully creates a user (`HTTP 200`), which violates the validation rules and degrades data integrity.

### Steps to Reproduce:
1. Send request:
   `GET /player/create/supervisor?password=n1ew73Akmx&role=user&gender=non-binary&screenName=screen_1c32267c&login=login_c34b8e1c&age=22`
2. Observe response.

### Actual Result:
- HTTP 200 returned.
- A new player is created (new `id` returned) with invalid gender input.
- No validation error is returned.

### Expected Result:
The request should be rejected with a validation error (e.g., **400 Bad Request**) because gender must be only `male` or `female`.


## BUG-006: "Role-based access control is not enforced for delete operation (user can delete other users, incl. admin)"
### Severity: **Blocker**
### Component: PlayerController – `DELETE /player/delete/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, a player with role `user`:
- can perform operations only on its own user
- **cannot delete** (even itself)

The system allows a `user` role to delete another player with role `admin` or `user` and returns **HTTP 204**.
This is a critical authorization bypass and violates the role model.

### Steps to Reproduce:
1. Use a user account login as editor (role `user`).
2. Send request:
   `DELETE /player/delete/{editor}`
   Body:
   `{ "playerId": {player_id_to_delete} }` (target user role = `admin`)
3. Observe response.

### Actual Result:
HTTP 204 No Content returned (delete succeeded).

### Expected Result:
Request must be rejected with **403 Forbidden** because:
- `user` role is not allowed to delete
- user must not be able to delete other users/admins


## BUG-007: "`GET /player/get/all` returns only 10 records with no pagination support"
### Severity: **Major**
### Component: PlayerController – `GET /player/get/all`
### Environment: http://3.68.165.45
### Description
The endpoint `GET /player/get/all` returns a maximum of **10 players**, but:
- No pagination parameters are defined in Swagger
- No `limit/offset`, `page/size`, or cursor mechanism exists
- The behavior is undocumented

As a result, the API silently truncates data and does not allow clients to retrieve the full dataset.

### Steps to Reproduce:
1. Create more than 10 users.
2. Call `GET /player/get/all`.
3. Observe that only 10 players are returned.

### Actual Result:
- Response contains only 10 players.
- No way to request additional records.

### Expected Result:
The API should either:
- Return all records (if intended), OR
- Implement documented pagination (limit/offset or page/size), OR
- Clearly document default limit and provide a way to override it.

### Notes:
Hardcoded limits without pagination:
- Break client data consistency
- Make automation unreliable
- Prevent complete data retrieval
- Violate predictable API contract principles

## BUG-008: "`GET /player/get/all` response items miss `role` field defined in Swagger"
### Severity: **Major**
### Component: PlayerController – `GET /player/get/all`
### Environment: http://3.68.165.45
### Description
Swagger `PlayerItem` model for `GET /player/get/all` includes `role`, but the actual response omits `role` for all returned players.
At the same time, `getPlayerByPlayerId` (`POST /player/get`) returns the full player object including `role` (and other fields), proving the backend has this data.
This is a contract inconsistency and prevents consumers from validating role-related behavior using the list endpoint.

### Steps to Reproduce:
1. Call `GET /player/get/all` and inspect `players[]` objects.
2. Call `POST /player/get` for one of the returned `id` values and compare the payloads.

### Actual Result:
- `GET /player/get/all`: `players[]` contains `id`, `screenName`, `gender`, `age` but **no `role`**.
- `POST /player/get`: returns full player object including `role` (and other fields).

### Expected Result:
`GET /player/get/all` should include `role` for each `PlayerItem` as defined in Swagger, and response models should be consistent across list vs get-by-id where applicable.


## BUG-009: "Type inconsistency for `age`: request parameter is string, response model is integer"
### Severity: **Medium**
### Component: PlayerController / Swagger Contract – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
In the Swagger contract, the `age` field is defined as a **string** in the create request query parameters, but as an **integer (int32)** in the response DTO.
This inconsistency breaks contract clarity and may cause issues with type-safe API usage, can hide server-side parsing/validation problems.

### Steps to Reproduce:
1. Open Swagger definition for `GET /player/create/{editor}`.
2. Observe that request parameter `age` has type `string`.
3. Observe that response DTO (`PlayerCreateResponseDto`) defines `age` as `integer (int32)`.

### Actual Result:
`age` type differs between request and response:
- Request: `string`
- Response: `integer (int32)`

### Expected Result:
`age` should be consistently defined as an integer across request and response:
- Request parameter type: `integer`, format `int32`
- Response type: `integer`, format `int32`


## BUG-010: "Authorization relies on untrusted `{editor}` path parameter (security design flaw)"
### Severity: **Blocker**
### Component: PlayerController – `{editor}` path parameter usage
### Environment: http://3.68.165.45
### Description
API endpoints accept `{editor}` as a path parameter to represent the user performing the operation (create/delete/update).
This is not a trustworthy authorization model: a caller can claim any identity by sending an arbitrary `{editor}` value unless the backend validates it against an authenticated context.
This design enables authorization bypass risks and is inconsistent with secure role-based access control.

### Steps to Reproduce:
1. Call an endpoint with `{editor}` set to an arbitrary login value (including a non-existent user).
2. Observe that the API processes the request based on the provided `{editor}` rather than authenticated identity (e.g., creates/deletes when it should be forbidden).

### Actual Result:
Authorization appears to depend on the client-supplied `{editor}` value, which can be spoofed.

### Expected Result:
Editor identity must be derived from the authentication context (token/session) and validated server-side.
The API should not allow clients to provide acting user identity via request parameters.


## BUG-011: "Create Player allows creating a user without password (required field validation missing)"
### Severity: **Critical**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45
### Description
According to requirements, `password` is a **required** field and must satisfy format rules (latin letters + numbers, length 7–15).
The API allows creating a player without providing `password` and returns **HTTP 200** with a new `id`.
This violates business/security requirements and indicates missing required-field validation.

### Steps to Reproduce:
1. Send request without `password`:
   `GET /player/create/supervisor?age=21&gender=male&login=user_24124&role=user&screenName=user_oeiwrpowie`
2. Observe response.

### Actual Result:
- HTTP 200 returned.
- Player is created (new `id` returned).

### Expected Result:
Request should be rejected with a validation error (e.g., **400 Bad Request**) because `password` is mandatory and must meet format requirements.


## BUG-012: "User can update admin and another user"
### Severity: **Blocker**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45
### Description
According to the role model requirements, a user with role `user` can perform operations only on its own user (and cannot delete).  
The system allows a player with role `user` to update another player with role `admin` and `user` and returns **HTTP 200**.

### Steps to Reproduce:
1. Use an editor login with role `user` (e.g., `login_870ebec4`).
2. Send request:
   `PATCH /player/update/login_870ebec4/1977692556`
   Body:
   `{ "age": 30 }`
3. Observe response.

### Actual Result:
HTTP 200 returned and target `admin` user is updated:
- Response shows `id=1977692556`, `role=admin`, `age=30`.

### Expected Result:
Request must be rejected with **403 Forbidden**, because `user` role must not be able to update other users (especially `admin`).


## BUG-013: "Age upper boundary validation is incorrect on CREATE: age=60 is accepted though must be rejected"
### Severity: **Medium**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45

### Description
Requirements state that user must be **older than 16 and younger than 60** (valid range: **17–59**).
The API incorrectly accepts the boundary value `age=60` and returns **HTTP 200** with a new `id`.
The higher values (e.g., `age=61`) are rejected as expected.

### Steps to Reproduce:
1. Send request:
   `GET /player/create/supervisor?password=N7suc64xtl&role=user&gender=female&screenName=screen_036c2363&login=login_be43d2f7&age=60`
2. Observe response status.

### Actual Result:
- HTTP 200 returned.
- Player is created for `age=60`.

### Expected Result:
Request should be rejected with a validation error (e.g., **400 Bad Request**) because valid ages are **17–59** (age must be < 60).

### Notes:
Likely incorrect validation condition (e.g., `age > 60` instead of `age >= 60`, or `age <= 60` used for valid range).


## BUG-014: "Age validation is not enforced on UPDATE: allows setting age=16 and age=60"
### Severity: **Major**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45
### Description
Requirements state that user must be **older than 16 and younger than 60** (valid range: **17–59**).
The update endpoint allows setting invalid boundary values:
- `age=16` (under minimum valid age)
- `age=60` (over maximum valid age)

Both updates succeed with **HTTP 200** and the response confirms the invalid age value was applied. This violates business validation rules and allows storing out-of-policy user data.

### Steps to Reproduce:
1. Send request:
   `PATCH /player/update/supervisor/{player_id}`
   Body: `{ "age": 16 }`
2. Send request:
   `PATCH /player/update/supervisor/{player_id}`
   Body: `{ "age": 60 }`
3. Observe the status code and response body.

### Actual Result:
- HTTP 200 returned for both requests.
- Response body shows updated invalid ages (`age: 16`, `age: 60`).

### Expected Result:
Requests should be rejected with validation error (e.g., **400 Bad Request**) because a valid age range is **17–59**.


## BUG-015: "Gender validation is not enforced on UPDATE: allows setting gender to invalid value"
### Severity: **Major**
### Priority: **High** (to be confirmed by PO/PM)
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45

### Description
Requirements state that user's `gender` can only be `male` or `female`.
The update endpoint allows setting `gender` to an invalid value (e.g., `other`) and returns **HTTP 200**.
The response confirms the invalid value was persisted.
This violates business validation rules and allows storing invalid domain data.

### Steps to Reproduce:
1. Send request:
   `PATCH /player/update/supervisor/{player_id}`
   Body:
   `{ "gender": "other" }`
2. Observe response.

### Actual Result:
- HTTP 200 returned.
- Response shows `gender: "other"`.

### Expected Result:
Request should be rejected with validation error (e.g., **400 Bad Request**) because gender must be only `male` or `female`.

### Notes:
Create endpoint already accepts invalid gender values as well; update endpoint shows the same validation gap and persists invalid data.

## BUG-016: "screenName uniqueness constraint is not enforced on UPDATE (duplicate screenName accepted)"
### Severity: **Critical**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45

### Description
Requirements state that `screenName` must be unique for each user.
The update endpoint allows changing a user's `screenName` to a value that already belongs to another user and returns **HTTP 200**, persisting the duplicate.
This violates a core business rule and can cause identity/UI conflicts and data integrity issues.

### Steps to Reproduce:
1. Create user A with `screenName=screen_a1049b53`.
2. Create user B with `screenName=screen_bab79b17`.
3. Update user A:
   `PATCH /player/update/supervisor/<userAId>`
   Body: `{ "screenName": "screen_bab79b17" }`
4. Observe response.

### Actual Result:
- HTTP 200 returned.
- Response shows user A now has `screenName: "screen_bab79b17"` (duplicate).

### Expected Result:
Request should be rejected with validation error (e.g., **400 Bad Request** or **409 Conflict**) because `screenName` must remain unique.

### Notes:
Create endpoint also allows duplicate `screenName`; update confirms the uniqueness constraint is not enforced consistently across operations.


## BUG-017: "Password validation is not enforced on UPDATE: invalid passwords are accepted"
### Severity: **Critical**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45

### Description
Requirements state that `password` must contain **latin letters and numbers** and have **length 7–15**.
The update endpoint accepts invalid password values (e.g., digits-only, too short/too long, missing digits/letters) and returns **HTTP 200**.
This indicates missing password validation on update and allows insecure credentials to be stored.

### Steps to Reproduce:
1. Create a valid user.
2. Send request:
   `PATCH /player/update/supervisor/{player_id}`
   Body: `{ "password": "6007028959888" }` (digits only / no letters)
3. Repeat with other invalid password combinations (too short, too long, no digits).

### Actual Result:
- HTTP 200 returned for invalid passwords.
- Update operation succeeds (no validation error returned).

### Expected Result:
Request should be rejected with validation error (e.g., **400 Bad Request**) for any password that does not meet the required format (letters+digits, length 7–15).

### Notes:
Create endpoint also shows weak/missing password validation; update endpoint confirms the same issue and allows persisting invalid credentials.


## BUG-018: "Swagger allows updating `role`, but requirements prohibit role updates"
### Severity: **Medium/Low**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}` (Swagger contract)
### Environment: http://3.68.165.45
### Description
According to the requirements, `/player/update/{editor}/{id}` must allow updating only the following fields:
- `age`
- `gender`
- `login`
- `password`
- `screenName`

However, Swagger `PlayerUpdateRequestDto` includes the `role` field, implying that role updates are supported.
This creates a mismatch between the documented contract and the business requirements. It may mislead client implementations and automated tests into assuming that role updates are allowed.

### Steps to Reproduce:
1. Open Swagger definition for `PATCH /player/update/{editor}/{id}`.
2. Inspect the request body schema (`PlayerUpdateRequestDto`).
3. Observe that the `role` field is included in the DTO.

### Actual Result:
Swagger contract includes `role` in the update request DTO.

### Expected Result:
Swagger contract must match the requirements and exclude `role` from the update request DTO (or explicitly document that `role` is ignored or rejected).

### Notes:
Runtime behavior appears to ignore role changes (the role remains unchanged), which aligns with the requirements.  
The defect is primarily a contract/documentation mismatch between Swagger and the specified behavior.

## BUG-019: "`/player/get` accepts invalid `playerId` values and returns HTTP 200"
### Severity: **Medium**
### Component: PlayerController – `POST /player/get`
### Environment: http://3.68.165.45
### Description
The `/player/get` endpoint accepts invalid `playerId` values (such as `null`, `0`, or negative numbers) and returns **HTTP 200** with an empty response body.
This indicates missing input validation and makes client-side error handling unreliable, as invalid requests are treated as successful.

### Observed Cases:
- `playerId = null` → HTTP 200
- `playerId = -1` → HTTP 200
- `playerId = 0` → HTTP 200

### Steps to Reproduce:
1. Send request:
   `POST /player/get`
   Body:
   `{ "playerId": null }`
2. Repeat with:
    - `{ "playerId": -1 }`
    - `{ "playerId": 0 }`
3. Observe responses.

### Actual Result:
HTTP 200 returned with empty response body (`Content-Length: 0`).

### Expected Result:
For invalid input (`null`, `<=0`), the API should return:
- **400 Bad Request** with validation error details.


## BUG-020: "`DELETE /player/delete/{editor}` returns 403 for invalid/non-existent `playerId` instead of 400/404"
### Severity: **Medium**
### Component: PlayerController – `DELETE /player/delete/{editor}`
### Environment: http://3.68.165.45
### Description
When deleting a player as an authorized editor (`supervisor`), the API returns **HTTP 403 Forbidden** for invalid or non-existent `playerId` values:
- `playerId = null`
- `playerId = -1`
- `playerId = 0`
- `playerId = 999999999` (non-existent)

This is incorrect because:
- **403** indicates an authorization failure, but `supervisor` is allowed to delete users per role model (except supervisor-role users).
- These cases represent **input validation** errors (null/<=0) or **not found** (non-existent ID).
  Returning 403 makes error handling ambiguous and violates HTTP semantics.

### Steps to Reproduce:
1. Send request:
   `DELETE /player/delete/supervisor`
2. Use each body below and observe status code:
    - `{ "playerId": null }`
    - `{ "playerId": -1 }`
    - `{ "playerId": 0 }`
    - `{ "playerId": 999999999 }`

### Actual Result:
API returns **HTTP 403 Forbidden** with empty body (`Content-Length: 0`) for all cases.

### Expected Result:
- For invalid input (`null`, `<=0`): **400 Bad Request** (preferably with validation error details).
- For non-existent valid ID: **404 Not Found** (preferably with error details).


## BUG-021: "`POST /player/get` exposes user password in response (critical security vulnerability)"
### Severity: **Blocker**
### Component: PlayerController – `POST /player/get`
### Environment: http://3.68.165.45
### Description
The `POST /player/get` endpoint returns the user's **password** in the response payload.  
Passwords must never be exposed via API responses (neither plain text nor hashed), as it enables credential leakage, account takeover, and violates basic security standards.

### Steps to Reproduce:
1. Send request:
   `POST /player/get`
2. Body:
   `{ "playerId": 1930029868 }`
3. Inspect the response body.

### Actual Result:
Response includes a `password` field with a non-null value:
```json
{
  "id": 1930029868,
  "login": "login_0740a45e",
  "password": "L8TETXnoWYDFBpY",
  "screenName": "screen_be788fa0",
  "gender": "male",
  "age": 46,
  "role": "user"
}
```
### Expected Result:
Response must not include the user's password.


## BUG-022: "`PATCH /player/update/{editor}/{id}` does not validate `id` and returns incorrect status codes for invalid/non-existent IDs"
### Severity: **Major**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45

### Description
The update endpoint does not properly validate the path parameter `id` and returns inconsistent/incorrect status codes:
- `id = -1` → **200** with **empty body** (should be **400**)
- `id = 0` → **200** with **empty body** (should be **400**)
- `id = 999999999` (non-existent) → **200** with **empty body** (should be **404**)
- `id = null` (sent as `/null`) → **500 Internal Server Error** (should be **400**)

This indicates missing input validation, unreliable error handling, and potential backend exception leakage.

### Steps to Reproduce:
1. Send request:
   `PATCH /player/update/supervisor/-1`
   Body: `{ "age": 30 }`
2. Send request:
   `PATCH /player/update/supervisor/0`
   Body: `{ "age": 30 }`
3. Send request:
   `PATCH /player/update/supervisor/999999999`
   Body: `{ "age": 30 }`
4. Send request:
   `PATCH /player/update/supervisor/null`
   Body: `{ "age": 30 }`

### Actual Result:
- `-1`, `0`, `999999999` → **HTTP 200**, `Content-Length: 0`
- `null` → **HTTP 500** with error payload:
    - `status: 500`
    - `path: /player/update/supervisor/null`

### Expected Result:
- For invalid `id` values (`null`, `<= 0`): **400 Bad Request** with validation error details.
- For non-existent but valid `id`: **404 Not Found** with error details.
- The API should never return **500** for invalid client input.


# Improvements / Architectural Observations

## IMPROVEMENT-001: "`GET /player/get/all` list items do not include `login`"
### Impact: **Low–Medium**
### Component: PlayerController – `GET /player/get/all`
### Environment: http://3.68.165.45
### Description
The list endpoint returns `id`, `screenName`, `gender`, `age`, and `role`, but does not include `login` (which is unique per requirements).
This may force clients to perform additional lookups (`/player/get`) to resolve login for items, increasing latency and load.

### Recommendation
Consider including `login` in `PlayerItem` or provide filtering/search capabilities to avoid N+1 requests.

## IMPROVEMENT-006: Provide structured error response bodies for 4xx client errors
### Impact: **Medium**
### Component: PlayerController – `/player/create/{editor}`
### Environment: http://3.68.165.45

### Description
When client errors occur (e.g., validation failure or insufficient permissions), the API correctly returns HTTP **4xx** status codes (e.g., 400, 403), but the response body is empty.

Although the status codes are technically correct, the absence of a structured error payload reduces API usability, complicates debugging, and limits precise automated test assertions.

Providing standardized error responses would improve API clarity, debuggability, and client integration quality.

### Current Behavior
Examples:

1. Disallowed role:
   `/player/create/supervisor?...&role=supervisor`  
   → HTTP 400 with empty body

2. Invalid role value:
   `/player/create/supervisor?...&role=invalid_role`  
   → HTTP 400 with empty body

3. Unauthorized editor:
   `/player/create/user?...`  
   → HTTP 403 with empty body

### Recommendation
Return structured, consistent error responses for all 4xx cases.

Example – validation error:

```json
{
  "error": "Validation failed",
  "field": "role",
  "message": "Role must be 'admin' or 'user'"
}
```

Example – authorization error:

```json
{
  "error": "Forbidden",
  "message": "Only admin or supervisor can create users"
}
```

### Benefits
- Improves client-side error handling
- Enables precise automated test assertions
- Reduces integration ambiguity
- Aligns with RESTful API best practices
- Improves API documentation clarity (Swagger/OpenAPI)

Standardizing error responses across all endpoints is recommended.


## OBSERVATION-001: "`DELETE /player/delete/{editor}` requires request body with `playerId`"
### Impact: **Medium**
### Component: PlayerController – `DELETE /player/delete/{editor}`
### Environment: http://3.68.165.45
### Description
Delete operation requires a JSON body containing `playerId`.
Some HTTP clients/proxies handle DELETE bodies inconsistently, which can reduce interoperability and complicate automation.

### Recommendation
If interoperability becomes an issue, consider moving `playerId` to the path or query, or document supported clients/proxy expectations.

## OBSERVATION-002: "`POST /player/get` is used for retrieval (RPC-style)"
### Impact: **Low**
### Component: PlayerController – `POST /player/get`
### Environment: http://3.68.165.45
### Description
The API retrieves a player by ID via POST with a JSON body containing `playerId`. This is consistent with an RPC-style API but differs from resource-oriented REST conventions.

### Recommendation
No action is required if the RPC style is intentional. If REST alignment is desired, consider GET-based resource endpoints in future versions.

## OBSERVATION-003: "`PATCH /player/update/{editor}/{id}` uses a full DTO schema, which blurs PATCH semantics (partial vs replace)"
### Impact: **Medium**
### Component: PlayerController – `PATCH /player/update/{editor}/{id}`
### Environment: http://3.68.165.45
### Description
In Swagger, `PATCH /player/update/{editor}/{id}` is defined with `PlayerUpdateRequestDto` that contains most player fields (and may be interpreted as “send the whole object”).
This is confusing for PATCH semantics because PATCH is typically used for **partial updates** (send only changed fields). If clients serialize the full DTO with missing fields as `null`, it can lead to:
- accidental data loss (if backend treats nulls as overwrites),
- inconsistent behavior between clients,
- unclear contract (does `null` mean “clear the field” or “ignore”?).

Runtime behavior observed so far suggests the backend **ignores null fields**, but this is not stated in the contract.

### Recommendation
Clarify and enforce partial update behavior by:
- Documenting how `null` is handled (ignored vs clears field),
- Providing examples in Swagger,
- Using a schema that reflects partial updates (optional fields only),
- Optionally supporting `application/json-patch+json` / `application/merge-patch+json` semantics.


## OBSERVATION-004: "`/player/get` returns HTTP 200 with empty body when player is not found"
### Impact: **Medium**
### Component: PlayerController – `POST /player/get`
### Environment: http://3.68.165.45

### Description
When `playerId` does not exist (e.g., `999999999`), the API returns **HTTP 200** with an empty response body.
In an RPC-style API this may be interpreted as “request processed successfully but entity not found”, however the absence of an explicit not-found indicator makes it hard for clients to distinguish:
- not found vs
- invalid input vs
- server-side issues producing empty responses.

### Recommendation
Return an explicit not-found signal, e.g.:
- HTTP **404 Not Found**, or
- HTTP 200 with a structured response such as `{ "player": null, "error": "NOT_FOUND" }`
  and document the behavior in Swagger.


## OBSERVATION-005: "`/player/create/{editor}` response model includes `password` field`"
### Impact: **Major**
### Component: PlayerController – `GET /player/create/{editor}`
### Environment: http://3.68.165.45

### Description
The **Create Player** endpoint response model includes a `password` field in the public API contract.

Although the current implementation returns this field as `null`, its presence in the response schema exposes internal authentication-related structure to API consumers.

Example response:

```json
{
  "id": 1869113932,
  "login": "login_30_0_e5fa4a88",
  "password": null,
  "screenName": null,
  "gender": null,
  "age": null,
  "role": null
}
```

Including a `password` field in API responses:
- Exposes sensitive domain model attributes unnecessarily
- Increases risk of accidental credential leakage in future changes
- Indicates insufficient separation between persistence entities and public DTOs
- Violates secure-by-design and least-privilege API principles

Even when the value is `null`, authentication-related fields should never be part of externally exposed response models.

### Recommendation
- Remove the `password` field from the public response DTO entirely.
- Ensure strict separation between internal entity models and API response models.
- Review other endpoints to confirm that no authentication or sensitive fields are exposed in API contracts.
- Update Swagger/OpenAPI documentation accordingly.

