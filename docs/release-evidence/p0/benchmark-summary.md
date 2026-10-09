# P0 benchmark summary

This is a descriptive Phase P0 baseline, not a claim that Better Minecraft AI improves navigation. Both arms use the same vanilla PathNavigation route. The vanilla-control arm forces the BMAI observer off; the OBSERVE arm enables read-only sampling.

| Result | Vanilla control | OBSERVE |
|---|---:|---:|
| Samples | 5 | 5 |
| Successful arrivals | 5 / 5 | 5 / 5 |
| Exceptions | 0 | 0 |
| Duration median (ticks) | 113 | 113 |
| Duration p95, nearest rank (ticks) | 114 | 114 |
| Path nodes median | 14 | 14 |
| Stall ticks median | 0 | 0 |
| Final distance median (blocks) | 1.285962 | 1.285783 |
| Tick p95 sample mean (ms) | 2.86976 | 4.78512 |
| Observer handler time mean (ns, overlapping window) | 0 | 1545080 |

## Paired interpretation

- Matched behavior: **True**.
- Every replicate matched on scenario, seed, entity count, warmup and replicate ID.
- Success, duration, path-node count and stall count matched in every pair.
- Maximum absolute paired final-distance delta: 0.000591 blocks (P0 descriptive-analysis tolerance: 0.001 blocks; this was not preregistered as an A1 acceptance threshold).
- The control arm recorded zero observer handler time. The OBSERVE value is a process-global overlapping window and is retained as diagnostic instrumentation cost, not isolated per-entity CPU.
- Server tick percentiles are short-run rolling-server samples gathered while five isolated GameTests execute in one process. They are useful for detecting gross anomalies only; they are not a statistically powered performance comparison.

Raw data: [vanilla-control CSV](benchmark-data/p0-vanilla_control.csv) and [OBSERVE CSV](benchmark-data/p0-observe.csv). Machine-readable analysis: [benchmark-summary.json](benchmark-summary.json).
