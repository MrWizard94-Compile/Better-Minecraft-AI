# AGENTS Constitution skill evolution payload

## Status

**Staged locally, validated, not propagated.** No canonical commit, push, pull request, installed-mirror update, or binding-pack mutation was authorized.

The complete ready-to-apply patch is [`constitution-skill-evolution.patch`](constitution-skill-evolution.patch).

| Field | Value |
|---|---|
| Canonical repository | `https://github.com/MrWizard94-Compile/Agents-Constitution` |
| Frozen base revision | `969ae2caa33c1e4c9de325282fe6029e35a1caea` |
| Base skill version | 5.2.5 |
| Proposed skill version | 5.2.9 |
| Patch bytes | 16,488 |
| Patch SHA-256 | `E8CDA20E0DF1C040107BFD54D7FDBF533FD63560358B377BFC17598C59F88F6A` |

## Reusable improvement

An installed generated mirror accepted `-StartPath`, while a caller could reasonably use `-WorkspacePath` from another mirror/schema expectation and fail before discovery starts. The proposed change:

- adds `WorkspacePath` as a PowerShell alias for canonical `StartPath`;
- instructs the skill to inspect the installed resolver's declared parameters instead of guessing across mirror revisions;
- preserves manual discovery/no-guessed-parameter fallback;
- requires a named read-only host intake to be attempted when host policy requires it;
- requires exact degraded-state disclosure and forbids pretending an unavailable intake ran;
- permits a bounded fallback only to host-named local entry documents that establish project boundaries, authority, and safe routing;
- requires dependency locking and verification on every declared operating-system and architecture target before candidate status;
- treats one host's metadata as insufficient proof for target-selected natives, classifiers, POMs, and module metadata;
- requires authoritative checksum corroboration when available and forbids globally weakening locks or verification for an OS-specific resolution difference;
- permits only narrow, documented exceptions that retain exact-version constraints and artifact-integrity verification;
- distinguishes green candidate evidence from explicit human acceptance when a project contract has an approval gate;
- forbids inferring milestone approval from commit/push authorization, review activity, silence, or another phase's approval;
- requires the exact human decision, date, immutable accepted revision, limitations, and remaining authorization boundaries to be retained while higher-phase gates remain `NOT RUN`;
- adds canonical validator assertions so the resolver alias, host-intake safeguards, cross-platform dependency closure, and human approval boundary cannot silently regress; and
- records the meaningful failure reduction in the evolution ledger.

Changed canonical paths: `SKILL.md`, `scripts/resolve-pack.ps1`, `tools/validate-source.py`, `VERSION`, `SOURCE.json`, `CHANGELOG.md`, and `evolution/LEDGER.md`.

## Validation

A clean detached worktree at the frozen base accepted the patch with `git apply --check`. After applying the payload in that isolated worktree:

- `python -X utf8 tools/validate-source.py` → PASS for canonical source v5.2.9;
- PowerShell AST parse of `scripts/resolve-pack.ps1` → PASS;
- `-StartPath <BMAI workspace>` → `C:\WPAI\AGENTS Constitution`;
- `-WorkspacePath <BMAI workspace>` → the same binding pack;
- `-RequestedPackRoot 'C:\WPAI\AGENTS Constitution'` → the same binding pack; and
- `git diff --check` → PASS.

The cross-platform rule is grounded in this invocation's clean-runner evidence: a Windows-generated lock/verification state omitted Linux-selected Netty and LWJGL artifacts. The repair required native Ubuntu resolution, checksum corroboration, a narrowly scoped lock exception with strict version and checksum controls retained, and a required two-OS CI matrix.

The approval-gate rule is grounded in the P0 acceptance lifecycle: executable evidence and authorized pushes produced a green candidate, but only the later exact director statement `I accept P0` completed the phase. The acceptance record preserves that statement, accepted SHA, date, limitations, and the fact that every A1 gate remains `NOT RUN`.

The new intake fallback is also grounded in an observed negative case from this
invocation: `wpai_vault` was absent from the tool catalog and rejected as an unknown
MCP server, while the explicitly named `C:\WPAI\AGENTS.md` and
`C:\WPAI\WPAI-CONTEXT.md` files provided the required read-only routing and
authority boundary. The report does not claim the unavailable intake ran.

The evolution guard requires an authorized commit range and therefore was not misrepresented as run. It will detect substantive paths (`SKILL.md`, resolver, validator) once the payload is placed on an authorized canonical branch.
