# ARCHITECTURE.md

## Highlights

- **Strict layering:** Domain has no dependency on data or datasource; each layer has its own models and mappers at boundaries (no type leakage).
- **Single responsibility:** Sync orchestration lives in `SyncPendingUseCase`; repositories only persist or upload; partial-failure and “when to stop” logic is in one place and testable with fakes.
- **Scenario-driven mock API:** `MockSyncApi` is configurable (fail on Nth call, timeout, server error, network unavailable) so all assignment scenarios are reproducible without a real backend.
- **Explicit error model:** Domain exceptions (e.g. `ServerRejectedDomainException`, `NetworkUnavailableDomainException`) plus `isRetryable()` so the engine can distinguish “retry later” from “this request is bad” and stop early on repeated network failures.
- **Concurrent-sync safety:** One `Mutex` in the use case guarantees only one sync runs at a time; a second caller blocks until the first finishes—no duplicate uploads or corrupted state.
- **Progress and result:** `SyncResultDomainModel` gives exact syncedIds and failed list (with responseId + exception); `StateFlow<SyncStateDomainModel>` exposes Idle / Syncing(total, current) / Result for future UI (“Uploading 3 of 8…”).

---

## What architecture did you choose and why? What alternatives did you consider?

I chose **Clean Architecture** with three modules: **domain**, **data**, and **datasource**. The domain holds repository interfaces and use cases; the data layer implements repositories and maps between domain models and datasource types; the datasource provides Room for local persistence and a mock upload API. Only the data module depends on domain and datasource, so the domain stays free of frameworks and I/O. Sync logic (which responses to retry, when to stop on network issues) lives in the use case, making behaviour easy to test with fakes.

**Use case outputs (name: what it does):**

- **SubmitSurveyUseCase** — Persists a survey response locally; output: none (side effect only).
- **GetPendingResponsesUseCase** — Exposes the list of responses that are pending or failed and not yet synced; output: `Flow<List<SurveyResponseDomainModel>>`.
- **GetPendingCountUseCase** — Exposes the current count of pending responses for UI or triggers; output: `Flow<Int>`.
- **SyncPendingUseCase** — Runs the sync queue (upload pending responses one by one, mark success/failure, stop early on consecutive network failures); output: `SyncResultDomainModel` (syncedIds, failed list with responseId + DomainException, optional stoppedReason). Also exposes `state: StateFlow<SyncStateDomainModel>` (Idle / Syncing(total, current) / Result) for progress reporting.

**Scenario coverage (assignment → implementation):**

| Scenario | What the assignment asks | How it’s implemented |
|---------|--------------------------|----------------------|
| 1 – Offline storage | Persist responses, track synced vs pending, attachments, repeating sections, storage growth | Room entity with `syncStatus` (Pending/Synced/Failed); answers/repeating sections as JSON; attachments as paths; pending = status Pending or Failed; storage policy extendable in data/datasource. |
| 2 – Partial failure | Successes not re-uploaded; caller knows who succeeded/failed; next sync only retries failed/untried | On success: `updateSyncStatus(id, Synced)`; on failure: `Failed` + append to `SyncResultDomainModel.failed`; next run uses `getPendingResponsesOnce()` (excludes Synced). |
| 3 – Network degradation | Detect network down vs one-off error; stop early; tell caller what happened | Retryable failures (e.g. timeout) counted; when consecutive count ≥ config, set `StoppedReason.NetworkDown` and return; result includes syncedIds, failed, stoppedReason. |
| 4 – Concurrent sync | Only one sync at a time; no corruption or duplicate work | `Mutex` in `SyncPendingUseCase`; second caller blocks until first `invoke()` completes. |
| 5 – Network error handling | Map failures to one model; distinguish “try again” vs “bad request” | Datasource returns own failure type → data maps to `DataException` → domain `DomainException`; `DomainException.isRetryable()` drives early-stop and messaging. |

**Testing:** Domain: `SyncPendingUseCaseTest` (Given/When/Then, fakes only) covers empty queue, all succeed, partial failure, early stop on network, single sync at a time. Data: `SurveyRepositoryTest` (in-memory Room) covers save/retrieve, status tracking, pending count; `DataExceptionToDomainExceptionMapperTest` covers each exception type and `Throwable` mapping. The mock API is configured in tests to simulate each scenario.

---

## How would you extend this to handle media file uploads where photos must be compressed before uploading?

Attachments would remain **file paths** in the domain and database. Compression would live in the **data/datasource** layer: before calling the upload API, a “prepare upload” step would run a compressor (e.g. image resize/compress on a background dispatcher) for each path, write the result to a temp or cache file, and pass that path (or bytes) into the API payload. The upload contract could be extended (e.g. a field for “compressed” paths or a separate media endpoint). After a successful sync, policy could delete or downsample originals to limit storage growth on low-end devices. All of this stays behind the repository interface so the domain and sync engine stay unchanged.

### Image compression implementation 

**Placement:** In the data layer, a “prepare payload” step runs before `SurveyResponseUploadRepository.uploadResponse`: for each `attachmentPath`, run compression off the main thread (e.g. `Dispatchers.Default` or a dedicated executor), then pass compressed paths (or upload compressed bytes via a separate media API). The domain still only sees paths; the repository implementation (or a dedicated “attachment preparer”) does the work.

**Strategy:** Resize to a max dimension (e.g. 1920px) and compress to JPEG with configurable quality (e.g. 80%); store result in a cache dir and either (a) attach that path to the upload payload, or (b) upload media first and attach returned URLs.

---

## Describe a scenario where your network detection logic could make a wrong decision. How would you mitigate it?

The engine stops after N **consecutive** “retryable” failures (e.g. timeouts) and sets a “network down”–style reason. On flaky 3G, the *next* request might have succeeded; we may stop too early and leave items unsynced until the next manual or scheduled sync. **Mitigations:** (1) Make N configurable (e.g. 2–3) and tune for real networks. (2) Add a short backoff and one or two retries for the *same* item before counting it toward “consecutive network failures,” so transient blips do not trigger an early stop. (3) Expose why we stopped (e.g. “consecutive timeouts”) so the caller or a background job can retry when connectivity might have improved, and so the agent sees a clear message.

---

## How would your design support remote troubleshooting — if a field agent's sync is failing and the support team cannot physically access the device, what data would you log, expose, or report to help diagnose the issue?

Log and report: (1) Last N sync result summaries (synced ids, failed ids with error type/code/message, and “stopped reason” if any). (2) Device/OS version, app version, free storage, and pending response count. (3) For each failed response id: last error, timestamp, and optionally payload size or attachment count (no PII). (4) A simple “sync health” flag or enum (e.g. all synced / partial / repeated failures / stopped due to network). Expose this via an in-app “report” or a small diagnostic endpoint that uploads an anonymized summary (or a secure token so support can pull logs from a backend). Optionally, a one-time “sync debug” mode that logs full request/response for one run (redacted) to trace server vs client issues. In design terms: add a `SyncReporter` or logger interface in the data layer; the engine (or repository impl) calls it with each result and context. Implementations can write to logcat, file, or send to a backend, keeping the engine testable.

### Remote monitoring dashboard or logging infrastructure

**Logging pipeline:** Device sends anonymized sync summaries (and optional device context) to a backend after each sync or on demand (“Send report”). Backend stores them by device/session id (no PII); retention e.g. 30–90 days. **Dashboard:** Support views aggregate by region/app version: success rate, pending counts, top failure types, “stopped reason” distribution. Drill-down to a device: last N sync results, last error per failed response, free storage trend. Alerts when many devices in a region report “network down” or repeated server errors

---

## The app is used in agricultural regions where geospatial data matters (field boundary mapping, GPS accuracy in rural areas with poor satellite coverage, canopy cover assessment). You are NOT implementing any maps features, but: what technical challenges would you anticipate when adding GPS-based field boundary capture to this app, and how would you validate the accuracy of captured coordinates?

*Maps / GPS implementation*

**Challenges:** (1) **Accuracy** — rural areas often have poor satellite visibility (multipath, canopy); raw GPS can be 5–20 m or worse. (2) **Battery** — continuous GPS for boundary tracing drains the device. (3) **Offline** — boundaries must be stored and synced like other survey data; large polygons increase payload size. (4) **Integrity** — detecting and rejecting invalid shapes (self-intersections, impossible areas) before upload. **Validation:** Record accuracy metadata (e.g. HDOP, number of satellites) with each point and flag low-confidence segments; apply sanity checks (max area, max point count, simple geometry validation); optional post-processing (smooth or simplify polygons); on the server, run the same checks and flag suspicious boundaries for review. **Implementation approach (design only):** Model a boundary as a list of coordinates (or GeoJSON) in a new domain/datasource model; store in Room (e.g. JSON column or separate table); sync with the same pipeline as survey responses (pending/synced/failed). Capture runs on a background thread with location updates; battery and accuracy trade-offs are tunable (e.g. interval, min accuracy).

---

## Device configuration and storage policy (low-end devices, 16–32 GB)

To align with field devices (limited storage, 50+ surveys/day with photos), the following policies can be implemented in the **data/datasource** layer without changing the domain:

- **Delete attachments after successful sync:** Once a response is marked `Synced`, delete the local attachment files (or move to a “synced” cache that is evicted first). The server holds the canonical copy; local storage is freed.
- **Periodic cleanup:** A scheduled job (e.g. WorkManager or app startup) deletes or archives **synced** responses older than N days (e.g. 7–30), and evicts their attachment paths. Pending/failed responses are never auto-deleted.
- **Eviction by storage pressure:** When free space drops below a threshold (e.g. 500 MB), apply more aggressive rules: delete attachment files for synced responses first; then optionally remove synced response records beyond a retention window. Never evict pending/failed.
- **Configurable limits:** Allow backend or build-time config for max attachment size, max attachments per response, and retention days, so behaviour can be tuned per region or device tier without code changes.

All of this stays behind the repository and optional “storage policy” interfaces so the sync engine and domain remain unchanged.

---

## One thing you would do differently with more time
