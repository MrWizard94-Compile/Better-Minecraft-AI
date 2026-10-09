# Phase P0 risks and limitations

| Risk / limitation | Current control | Required owner / next evidence |
|---|---|---|
| No Git commit exists | Manifest/gate table explicitly say `P0 BLOCKED`; no fake SHA | Director authorizes commit; Codex commits and reruns final gate |
| GitHub CI unobserved | SHA-pinned workflow and all local constituent tasks validated | Authorized push/PR before treating CI as green |
| Vanilla-control artifact is loaded | Observer is forced off before mod entry construction and counters stay zero; vanilla navigation performs movement | Add a mod-absent external baseline runner before A1 scoring |
| P0 scene is simple and only seed 0 | Raw data retained and claims limited to plumbing/non-interference | Freeze multi-scene, ≥3-seed, ≥20-attempt A1 protocol before tuning |
| Performance samples are short, concurrent, and process-global | No causal performance claim; limitations encoded in summary | Sequential load matrix plus profiler and soak in later phase |
| Observer retains one small record per currently observed loaded mob | Leave cleanup, server-stop cleanup, no persistence, and only primitive/identity state | Load/retention profiling before scale claims |
| Stop summaries show pre-clear active/pending counts | Handler clears both collections immediately after logging | If needed, add an explicit post-clear diagnostic assertion in P1 |
| Config change requires restart for the supported rollback procedure | `false` attaches/samples nothing; P0 owns no route or saved state | Atomic live reload belongs to P1/A1 and needs tests |
| Ownership adapter exists only in GameTest | Prevents unapproved P1 behavior from shipping | Productionize only after C0 eligibility/conflict/lifecycle state model passes |
| GameTest holder is included in the development/runtime JAR | It is not an event subscriber; tests run only when the namespace is explicitly enabled | Confirm packaged production behavior before release; optionally separate fixtures in a later build design |
| Automated integrated smoke is not visual/manual acceptance | Requires real player, ready integrated server, 100 ticks, and normal save/shutdown | Human play/visual check in release matrix |
| Dedicated smoke has no external client login | Loopback readiness/shutdown verified; integrated login separately verified | Dedicated client-connect matrix in A1 |
| Upstream dev-runtime warnings remain | Exact logger ownership investigated, current NeoForge used, no suppression, no error/fatal | Recheck packaged runtime/upstream updates; upstream report requires authorization |
| No public project license selected | Metadata says All Rights Reserved; third-party notices retained | Director selects license before distribution |
| External navigation/behavior mods untested | No dependencies and unsupported entities unchanged in P0 | A1/A5 compatibility matrix with exact pins |
| No mod-removal/save migration run | P0 registers no blocks/items/entities/attachments/save data | Formal backup/removal/reopen test in A1 |

## Rollback

Set `observation_enabled = false` in `better_minecraft_ai-common.toml` and restart at a normal server lifecycle boundary. The disabled arm proves no observer state attaches. Because production P0 never owns a goal or navigation route and writes no save schema, vanilla remains the sole controller. Back up valued worlds before changing any mod set.

## Prohibited inference

P0 evidence does not mean Minecraft AI is improved, the project is universally compatible, the observer is free at scale, the test lease is ready for production, or A1 is approved. Those statements require later gates.
