---
name: code-review
description: Review AppWatcher pull requests for correctness, security, and architectural fit. Use for code-review tasks to apply repository conventions and the maintainer's preferences for immutable, concise, reusable code with explicit dependencies and arguments.
---

# AppWatcher code review

Read and apply `.github/copilot-instructions.md` first. It is authoritative for architecture, build commands, and repository conventions. This skill adds the review method and maintainer preferences; do not duplicate or override the repository instructions.

## Review method

1. Review the merge-base diff, then inspect the relevant callers, callees, tests, schemas, and configuration needed to understand each changed behavior.
2. When the pull request links an issue, prior pull request, or CI failure, use the GitHub MCP server to read that context before judging the intended behavior.
3. Report only issues introduced or made materially worse by the diff. Do not report unrelated pre-existing code.
4. Prove a concrete failure mode before commenting. State the affected input or app flow, the impact, and the smallest sound fix direction.
5. Prioritize correctness, data loss, security/private-data exposure, lifecycle/concurrency bugs, and architectural violations. Skip praise, summaries, speculative concerns, and formatting-only comments.
6. Keep each comment concise and place it on the changed line that causes the issue. Combine comments with the same root cause.

## Maintainer preferences

### Immutability

- Prefer `val`, immutable data classes updated with `copy`, sealed interfaces, and `data object` singleton states/events.
- Keep mutation private and narrowly scoped. Do not expose `MutableStateFlow`, `MutableSharedFlow`, mutable collections, or mutable implementation state; expose `StateFlow`, `Flow`, read-only collections, or immutable snapshots.
- Use `ImmutableList`, `ImmutableSet`, `ImmutableMap`, and persistent collections for collection-bearing Compose state and reusable UI APIs. Convert once at the boundary rather than during recomposition.
- Local mutable builders are acceptable when they do not escape and make an algorithm clearer or cheaper. Do not demand persistent collections for Room/network DTO construction or internal paging assembly.
- Treat `@Immutable` as a contract: flag it when a property is mutable or exposes a type whose stability cannot be guaranteed.

### Concision, KTX, and reuse

- Before accepting a new helper or verbose platform interop, search the feature, `app/.../utils`, `:lib:ktx`, `:lib:compose`, and `:lib:framework`. Prefer an existing Kotlin/AndroidX KTX API or repository extension over repeated Java-style boilerplate.
- Reuse existing screen components, state/event/action types, paging factories, database queries, and conversion helpers instead of creating parallel implementations.
- Put a genuinely shared helper in the lowest correct module, but never move app-specific behavior into `:lib:*`.
- Concise means fewer concepts and less duplication, not clever chains, hidden work, broad abstractions, or compressed error handling. Do not request an extraction that has no clear reuse or domain meaning.

### Explicit dependencies and arguments

- For every new or changed app-owned function or constructor, require all data, behavior, configuration, and dependency arguments explicitly. The only allowed default is the Compose convention `modifier: Modifier = Modifier`.
- Flag defaults such as `null`, `false`, `0`, empty collections, ambient state, or `getKoin().get()` when they silently select behavior or hide a dependency. Make each call site choose explicitly.
- Prefer constructor injection for plain classes and the existing `singleOf`/`factoryOf` Koin wiring. Existing Android-owned and view-model `KoinComponent` boundaries are acceptable; pass dependencies explicitly from those owners into reusable screens and helpers.
- Flag service lookup (`getKoin`, `inject`, raw `Koin`) inside reusable or low-level composables/functions. `CompositionLocal` is for true UI ambient state such as context, density, theme, or pane scope, not app services.

## Compose scenes, screens, and view models

- Follow the nearby `BaseFlowViewModel<State, Event, Action>` pattern. A scene owns the view model, dependencies, `viewActions`, navigation, and activity APIs; a reusable `*Screen` receives immutable state, explicit dependencies, and one typed `onEvent`.
- Collect one-shot actions in a `LaunchedEffect` keyed by the view model or `Unit`. Capture changing callbacks with `rememberUpdatedState`; never restart the collector because a lambda changed.
- Flag effects that can launch duplicate collectors, state writes during composition, unstable/lazy-list keys, blocking work on the main thread, or coroutine code that swallows `CancellationException`.
- Preserve scene-owned `AppTheme`, Navigation 3 ownership, list-detail background/min-width behavior, system-bar ownership, and display-cutout protection described in the repository instructions.

## Watchlist paging (high-risk area)

- Preserve deterministic raw-query ordering with the row-id tie-breaker, pager-cache invalidation from `AppListTable.Queries.changes(db.apps())`, and stable section keys.
- Do not hide duplicate paging data with volatile Compose keys. Fix ordering, invalidation, separator construction, or the duplicated source rows.
- Keep `WatchListTagFilter.None`, `Untagged`, and `Tag(id)` semantically distinct; use `Tag` only for UI metadata.

## Data, modules, and private inputs

- A Room schema change requires the matching `AppsDatabase` migration, schema JSON, and migration coverage. Keep app-list SQL in `AppListTable.Queries`; convert Play Store documents outside entity constructors.
- Keep Play Store/DFE calls behind `:playstore`, app behavior in `:app`, and reusable platform/general code in the appropriate `:lib:*` module. Preserve the intentional `playstore/src/androidMain/java` source path and inherited Compose BOM.
- Treat committed or printed local/Firebase/Play/signing inputs, tokens, private Crashlytics URLs/logs, `local.properties`, or `app/google-services.json` as blocking security findings.
- For a `lib` gitlink update, verify the submodule commit exists on its remote and that the parent points to that exact commit.

## Tests and review threshold

- Require focused tests for changed behavior when a practical test seam exists, especially paging order/invalidation, filters, migrations, parsing, and view-model state transitions. Inspect CI reports because unit-test failures are configured `continue-on-error`.
- Do not comment on wrapping, blank lines, import order, final newlines, or the repository's intentional ktlint choices.
- If a concern cannot be demonstrated from the diff and repository context, do not post it as a finding.