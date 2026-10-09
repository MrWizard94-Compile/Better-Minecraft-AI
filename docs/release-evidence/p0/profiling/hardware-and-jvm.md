# Phase P0 hardware, JVM, and profiling record

## Host

- CPU: Intel Core i7-9700, 8 cores / 8 logical processors.
- RAM: 51,409,330,176 bytes installed (approximately 48 GiB).
- GPU: NVIDIA GeForce GTX 1660 Ti.
- Runtime OS: Gradle/ModLauncher reports Windows 11 10.0 amd64; registry reports legacy product string Windows 10 Home, display 26H2, build 26300.9457.
- PowerShell: 7.6.6.
- Java: Eclipse Temurin 21.0.10+7 LTS, 64-bit server VM.
- Gradle JVM setting: `-Xmx2G -Dfile.encoding=UTF-8`.

## Captured P0 metrics

- Five matched samples per arm in one GameTest server process.
- Each sample retains arrival result/duration, final distance, maximum path nodes, stall ticks, overlapping observer handler nanoseconds, and rolling server tick median/p95/p99.
- Vanilla control: 5/5 arrivals, median 113 ticks, p95 114, path nodes 14, stalls 0, mean rolling tick-p95 sample 2.86976 ms, observer handler 0 ns.
- OBSERVE: 5/5 arrivals, median 113 ticks, p95 114, path nodes 14, stalls 0, mean rolling tick-p95 sample 4.78512 ms, mean overlapping observer window 1,545,080 ns.

The tick samples are short rolling windows captured while five isolated tests may execute concurrently; they are not independent replicates. Observer windows overlap and include all observed mobs in the process. The difference is therefore not attributed to BMAI as a performance regression or improvement.

## Not performed

No JFR, Spark, allocation/GC profile, heap-retention inspection, 1/8/32/64 entity load matrix, overload test, or 60-minute soak was run. These are A1/P2/P4 gates and remain `NOT RUN`. P0's only performance conclusion is that no gross hang/crash occurred and behavior outcomes matched in the small deterministic scene.
