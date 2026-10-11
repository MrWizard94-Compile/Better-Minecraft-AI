# Phase P0 gate status

| Gate | Status | Evidence link | Blocker / deviation |
|---|---|---|---|
| P0-01 clean build and pinned wrapper | PASS | [manifest](manifest.md), [build and CI](build-and-ci.md), [build environment](../../BUILD_ENVIRONMENT.md) | Complete wrapper/source committed and pushed; clean local build and native Windows/Ubuntu CI pass with locks, verification metadata, warning gates, and reproducible JARs. |
| P0-02 OBSERVE truly non-invasive | PASS | [test results](test-results.md), [control log](runtime/gametest-vanilla-control.log), [OBSERVE log](runtime/gametest-observe.log), [source audit](../../compat/HOOK_AUDIT.md) | 7/7 in both arms; control counters are all zero; enabled test asserts unchanged navigation/goal/target selector identities and samples post-tick only. Limitation: scaled load cost is not P0 evidence. Owner: P1/P2. |
| P0-03 source hook/ownership audit | PASS | [hook audit](../../compat/HOOK_AUDIT.md), [framework ADR](../../adr/ADR-001-ai-framework-selection.md), [test results](test-results.md) | Exact 1.21.1/21.1.256 source and SHA cited; real zombie proves one MOVE owner and exact-object rollback. Production adapter intentionally not shipped in P0. Owner: P1. |
| P0-04 baseline evidence | PASS | [baseline protocol](../../benchmarks/BASELINE.md), [summary](benchmark-summary.md), [JSON](benchmark-summary.json), [raw data](benchmark-data/) | Seed 0, 5 matched replicates/arm, 10/10 arrivals, zero exceptions; duration/nodes/stalls match by pair. Descriptive short-run baseline only; no improvement/performance claim. Owner: P1/A1 benchmark expansion. |
| P0-05 GameTest + dedicated/integrated boot | PASS | [test results](test-results.md), [dedicated log](runtime/dedicated-server.log), [integrated log](runtime/integrated-client.log) | 7/7 GameTests per arm; loopback dedicated server ready/stopped/saved; real integrated player + ready server sustained 100 active ticks and shut down normally. Automated smoke is not human visual acceptance. Owner: later release/manual gate. |
| A1-01 functional navigation | NOT RUN | [P0 scope](../../P0_CHECKLIST.md) | P0 proves only a test lease; no enhanced production navigation exists. Owner: P1/P2. |
| A1-02 measured improvement | NOT RUN | [baseline](../../benchmarks/BASELINE.md) | No ENHANCED arm or frozen A1 challenge suite; no improvement claim. Owner: P2. |
| A1-03 performance and soak | NOT RUN | [profiling record](profiling/hardware-and-jvm.md) | No load matrix, profiler, heap study, overload test, or 60-minute soak. Owner: P2/P4/A1 release gate. |
| A1-04 compatibility and fallback | NOT RUN | [compatibility matrix](compatibility-matrix.md), [hook audit](../../compat/HOOK_AUDIT.md) | P0 exact-object fallback passes, but the A1 mod/interoperability/removal/save lifecycle matrix is not run. Owner: P1/A1. |
| A1-05 reliability/security | NOT RUN | [risks](risks-and-limitations.md) | P0 has no command, packet, async engine, or enhanced result validation; required A1 stress/security scenarios do not exist. Owner: P1–P4. |
| A1-06 tests, CI, packaging | NOT RUN | [build and CI](build-and-ci.md) | P0 GitHub CI is green, but no A1 suite, release tag/checkout reproduction, packaged installation, or distribution test exists. Owner: director/Codex at a future A1 delivery. |
| A1-07 director approval | NOT RUN | [release decision](release-decision.md) | No A1 candidate exists and no director approval has been requested or granted. Owner: future director decision. |

## Gate interpretation

P0-01 through P0-05 have executed evidence and pass. The implementation head is committed, pushed, and green on native Windows and Ubuntu runners.

Current status: **P0 CANDIDATE — AWAITING DIRECTOR APPROVAL**. The corresponding Constitution review is recorded in the [Section 0 self-audit](section-0-self-audit.md).

No A1 status is inferred from the P0 ownership proof. All A1 rows remain `NOT RUN` exactly as required for phase separation.
