# Phase P0 release decision

## Current decision

**P0 CANDIDATE — AWAITING DIRECTOR APPROVAL**

P0-01 through P0-05 are green. The complete foundation, dependency-verification repairs, and native Linux verification are committed and pushed through implementation head `9935c4c320293337b3d24a8bcef5c8b5abc8ffe3`. GitHub Actions [run 38021120525](https://github.com/MrWizard94-Compile/Better-Minecraft-AI/actions/runs/38021120525) passed the Windows build, Ubuntu build, paired P0 GameTests, and required-gates jobs.

No tag, GitHub release, package publication, installation, distribution, or external announcement has occurred. The authorized commits and pushes are source/evidence delivery only.

## Remaining approval action

The director reviews this evidence package and explicitly accepts or rejects the P0 candidate. Acceptance may authorize P1 planning; it does not silently authorize a tag, public release, package installation, or distribution.

The accurate candidate statement is:

> The project builds, instruments without modifying AI, has repeatable tests and baseline, and has an evidence-backed zombie enhancement insertion strategy.

Only explicit director acceptance can complete the P0 approval gate and authorize moving to P1.

## P1 hold

No P1 implementation is authorized or started. After P0 acceptance, the next smallest task is a Minecraft-independent, fully tested profile/mode transition model and explicit ordinary-zombie C0 allow-list contract. It should still leave production navigation in OBSERVE until the lifecycle adapter's unload/death/dimension/config/conflict tests are complete.
