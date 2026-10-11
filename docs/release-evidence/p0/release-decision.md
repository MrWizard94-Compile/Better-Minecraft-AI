# Phase P0 release decision

## Current decision

**P0 DONE — DIRECTOR ACCEPTED 2026-10-11**

The director explicitly accepted the evidence-backed P0 candidate with the exact statement:

> I accept P0

The accepted candidate is commit `5848143e16a48db2da38cd859f663b845cc7e62b`. GitHub Actions [run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309) passed its Windows build, native Ubuntu build, paired P0 GameTests, artifact upload, and required-gates job with zero annotations.

This decision closes P0 only. It does not mark any A1 row `PASS`, authorize P1 implementation, approve an A1 release, select a public license, create a tag, publish a package, install the mod externally, or authorize public distribution.

## P0 acceptance record

| Field | Accepted value |
|---|---|
| Project | Better Minecraft AI |
| Milestone | P0 |
| Candidate version | `0.1.0-p0` |
| Accepted commit SHA | `5848143e16a48db2da38cd859f663b845cc7e62b` |
| Minecraft / NeoForge / Java | 1.21.1 / 21.1.256 / Java 21 |
| Hardware / server settings | Local Windows host documented in `docs/BUILD_ENVIRONMENT.md`; `-Xmx2G`; loopback dedicated smoke; clean GitHub Windows 2025 and Ubuntu 24.04 runners |
| Evidence folder / CI | `docs/release-evidence/p0/`; [run 38157185309](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38157185309) |
| Functional result | OBSERVE is non-invasive; a test-only real-zombie MOVE lease proves exclusive vanilla arbitration and exact rollback |
| Baseline comparison | Five vanilla-control plus five OBSERVE trials locally and in CI; all arrivals; matched duration/nodes/stalls; zero exceptions |
| Performance result | Descriptive P0 plumbing/non-interference baseline only; no improvement, scale, or A1 performance claim |
| Compatibility result | P0 matrix accepted as recorded; unsupported entities and external AI/navigation integrations remain unchanged or `NOT RUN` |
| Required gates | P0: PASS 5 / FAIL 0 / BLOCKED 0 / NOT RUN 0; A1: PASS 0 / NOT RUN 7 |
| Known accepted limitations | One simple seed-0 scene; short paired samples; no external client on dedicated smoke; no packaged installation/removal or third-party compatibility matrix |
| Director decision | APPROVED for P0 only |
| Director approval date | 2026-10-11 |
| Release publication separately authorized? | NO |

The accepted P0 statement is:

> The project builds, instruments without modifying AI, has repeatable tests and baseline, and has an evidence-backed zombie enhancement insertion strategy.

## P1 boundary

No P1 implementation was started in this acceptance transaction. If separately authorized, the next smallest task is a Minecraft-independent, fully tested profile/mode transition model and explicit ordinary-zombie C0 allow-list contract. Production navigation must remain OBSERVE until the future lifecycle adapter's unload, death, dimension, configuration, and ownership-conflict tests pass.
