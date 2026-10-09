# Phase P0 release decision

## Current decision

**P0 BLOCKED**

The implementation/evidence work for P0-02 through P0-05 is green, and every executable local subcheck of P0-01 is green. P0 still cannot be called a candidate because the mandatory contract requires the complete wrapper and source state to be committed. This repository has no commits, and WPAI governance reserves commit authority to the director.

No tag, push, release, package publication, distribution, or external message has occurred.

## Smallest unblock action

The director authorizes a local P0 foundation commit. Codex then:

1. performs the final secret/status/whitespace audit;
2. stages the exact candidate files;
3. creates the authorized commit without pushing;
4. records the immutable SHA in this evidence package; and
5. reruns the clean local gate against that committed state.

A push and GitHub CI execution are separate authorizations. They are useful before later delivery but are not silently bundled into the requested local commit.

## Candidate acceptance statement after unblock

If the post-commit rerun remains green, the accurate P0 statement will be:

> The project builds, instruments without modifying AI, has repeatable tests and baseline, and has an evidence-backed zombie enhancement insertion strategy.

The status at that point becomes `P0 CANDIDATE — AWAITING DIRECTOR APPROVAL`. Only explicit director acceptance can complete the P0 approval gate and authorize moving to P1.

## P1 hold

No P1 implementation is authorized or started. After P0 acceptance, the next smallest task is a Minecraft-independent, fully tested profile/mode transition model and explicit ordinary-zombie C0 allow-list contract. It should still leave production navigation in OBSERVE until the lifecycle adapter's unload/death/dimension/config/conflict tests are complete.
