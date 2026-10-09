package com.mrwizard94.betterminecraftai;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * P0 in-game proofs for non-interference, reversible ownership, and the matched baseline protocol.
 */
@GameTestHolder(BetterMinecraftAi.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BetterMinecraftAiGameTests {
    private static final String BENCHMARK_ARM_PROPERTY = "better_minecraft_ai.benchmark.arm";
    private static final String BENCHMARK_OUTPUT_PROPERTY = "better_minecraft_ai.benchmark.output";
    private static final int BENCHMARK_TIMEOUT_TICKS = 140;
    private static final Set<Path> INITIALIZED_BENCHMARK_FILES = new HashSet<>();

    private BetterMinecraftAiGameTests() {
    }

    /**
     * Proves the P0 observer does not replace controllers, selectors, goals, navigation, or combat attributes.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void observeModeDoesNotMutateZombieAi(GameTestHelper helper) {
        prepareLane(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 1, 2);
        PathNavigation navigation = zombie.getNavigation();
        Object moveControl = zombie.getMoveControl();
        Set<WrappedGoal> goals = Set.copyOf(zombie.goalSelector.getAvailableGoals());
        Set<WrappedGoal> targetGoals = Set.copyOf(zombie.targetSelector.getAvailableGoals());
        double movementSpeed = zombie.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        double attackDamage = zombie.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        ObservationService observer = BetterMinecraftAi.observationService();

        helper.runAfterDelay(12, () -> {
            helper.assertTrue(navigation == zombie.getNavigation(), "Observer replaced vanilla PathNavigation");
            helper.assertTrue(moveControl == zombie.getMoveControl(), "Observer replaced vanilla MoveControl");
            helper.assertTrue(goals.equals(zombie.goalSelector.getAvailableGoals()), "Observer changed vanilla goals");
            helper.assertTrue(
                    targetGoals.equals(zombie.targetSelector.getAvailableGoals()),
                    "Observer changed vanilla target goals");
            helper.assertTrue(
                    movementSpeed == zombie.getAttributeValue(
                            net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED),
                    "Observer changed zombie movement speed");
            helper.assertTrue(
                    attackDamage == zombie.getAttributeValue(
                            net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE),
                    "Observer changed zombie attack damage");

            if (observer.isEnabled()) {
                helper.assertTrue(observer.isObserving(zombie.getUUID()), "Observer never attached after safe tick");
                helper.assertTrue(observer.entitySampleCount(zombie.getUUID()) > 0L, "Observer recorded no samples");
                helper.assertTrue(observer.controllersMatch(zombie.getUUID(), zombie), "Controller identities changed");
            } else {
                helper.assertFalse(observer.isObserving(zombie.getUUID()), "Disabled observer attached to zombie");
            }
            helper.succeed();
        });
    }

    /**
     * Demonstrates a C0 zombie MOVE lease through vanilla GoalSelector arbitration and exact-object removal.
     * This is a test-only ownership proof; shipped P0 runtime never adds the goal.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 60)
    public static void zombieMoveOwnershipIsExclusiveAndReversible(GameTestHelper helper) {
        prepareLane(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 1, 2);
        PathNavigation navigation = zombie.getNavigation();
        Set<WrappedGoal> originalGoals = Set.copyOf(zombie.goalSelector.getAvailableGoals());
        ProbeMoveGoal probe = new ProbeMoveGoal(zombie, helper.absolutePos(new BlockPos(16, 1, 2)));

        helper.runAfterDelay(2, () -> zombie.goalSelector.addGoal(0, probe));

        helper.runAfterDelay(12, () -> {
            WrappedGoal wrappedProbe = zombie.goalSelector.getAvailableGoals().stream()
                    .filter(wrapped -> wrapped.getGoal() == probe)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Probe goal disappeared before verification"));
            long runningMoveOwners = zombie.goalSelector.getAvailableGoals().stream()
                    .filter(WrappedGoal::isRunning)
                    .filter(wrapped -> wrapped.getFlags().contains(Goal.Flag.MOVE))
                    .count();

            helper.assertTrue(wrappedProbe.isRunning(), "Probe never acquired the vanilla MOVE lease");
            helper.assertTrue(runningMoveOwners == 1L, "More than one MOVE goal owned navigation");
            helper.assertTrue(probe.startCount() == 1, "Probe started an unexpected number of times");
            helper.assertTrue(probe.moveAccepted(), "Vanilla PathNavigation rejected the probe path");
            helper.assertTrue(navigation == zombie.getNavigation(), "Ownership proof replaced PathNavigation");

            probe.disable();
            zombie.goalSelector.removeGoal(probe);
            helper.assertTrue(probe.stopCount() == 1, "Removing the running probe did not stop it exactly once");
            helper.assertTrue(navigation.isDone(), "Probe removal left a stale vanilla path");

            helper.runAfterDelay(2, () -> {
                helper.assertTrue(
                        originalGoals.equals(zombie.goalSelector.getAvailableGoals()),
                        "Probe removal did not restore the original goal set");
                helper.assertTrue(navigation == zombie.getNavigation(), "Fallback changed PathNavigation identity");
                helper.succeed();
            });
        });
    }

    /**
     * Runs fixed vanilla-navigation trial one and retains machine-readable raw data for the selected arm.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 160, setupTicks = 20)
    public static void p0VanillaNavigationBaseline1(GameTestHelper helper) {
        runP0VanillaNavigationBaseline(helper, 1);
    }

    /**
     * Runs fixed vanilla-navigation trial two.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 160, setupTicks = 20)
    public static void p0VanillaNavigationBaseline2(GameTestHelper helper) {
        runP0VanillaNavigationBaseline(helper, 2);
    }

    /**
     * Runs fixed vanilla-navigation trial three.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 160, setupTicks = 20)
    public static void p0VanillaNavigationBaseline3(GameTestHelper helper) {
        runP0VanillaNavigationBaseline(helper, 3);
    }

    /**
     * Runs fixed vanilla-navigation trial four.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 160, setupTicks = 20)
    public static void p0VanillaNavigationBaseline4(GameTestHelper helper) {
        runP0VanillaNavigationBaseline(helper, 4);
    }

    /**
     * Runs fixed vanilla-navigation trial five.
     *
     * @param helper GameTest helper
     */
    @GameTest(template = "empty", timeoutTicks = 160, setupTicks = 20)
    public static void p0VanillaNavigationBaseline5(GameTestHelper helper) {
        runP0VanillaNavigationBaseline(helper, 5);
    }

    private static void runP0VanillaNavigationBaseline(GameTestHelper helper, int replicate) {
        prepareLane(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, 2, 1, 2);
        BlockPos target = helper.absolutePos(new BlockPos(16, 1, 2));
        ProbeMoveGoal probe = new ProbeMoveGoal(zombie, target);
        BenchmarkTrial trial = new BenchmarkTrial(
                helper,
                zombie,
                probe,
                target,
                replicate,
                helper.getTick() + 2);

        helper.runAfterDelay(2, () -> zombie.goalSelector.addGoal(0, probe));
        helper.onEachTick(trial::sample);
        helper.runAfterDelay(BENCHMARK_TIMEOUT_TICKS + 2, trial::failIfIncomplete);
    }

    private static void prepareLane(GameTestHelper helper) {
        helper.killAllEntities();
        helper.setNight();
        for (int x = 0; x < 20; x++) {
            for (int z = 0; z < 5; z++) {
                helper.setBlock(x, 0, z, Blocks.SMOOTH_STONE);
                helper.setBlock(x, 1, z, Blocks.AIR);
                helper.setBlock(x, 2, z, Blocks.AIR);
            }
        }
    }

    private static synchronized void appendBenchmark(BenchmarkResult result) throws IOException {
        String rawArm = System.getProperty(BENCHMARK_ARM_PROPERTY, "observe");
        if (!rawArm.matches("[a-z][a-z0-9_]{0,31}")) {
            throw new IllegalArgumentException("Invalid benchmark arm: " + rawArm);
        }

        Path root = Path.of(System.getProperty(BENCHMARK_OUTPUT_PROPERTY, "run/p0-benchmark-data"))
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(root);
        Path output = root.resolve("p0-" + rawArm + ".csv").normalize();
        if (!output.startsWith(root)) {
            throw new IllegalArgumentException("Benchmark output escaped its configured root");
        }

        if (INITIALIZED_BENCHMARK_FILES.add(output)) {
            Files.writeString(
                    output,
                    BenchmarkResult.header() + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        Files.writeString(
                output,
                result.toCsv(rawArm) + System.lineSeparator(),
                StandardCharsets.UTF_8,
                StandardOpenOption.APPEND);
    }

    private static final class ProbeMoveGoal extends Goal {
        private final Zombie zombie;
        private final BlockPos target;
        private boolean enabled = true;
        private boolean moveAccepted;
        private int startCount;
        private int stopCount;

        private ProbeMoveGoal(Zombie zombie, BlockPos target) {
            this.zombie = zombie;
            this.target = target;
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return enabled;
        }

        @Override
        public boolean canContinueToUse() {
            return enabled;
        }

        @Override
        public void start() {
            startCount++;
            moveAccepted = zombie.getNavigation().moveTo(
                    target.getX() + 0.5,
                    target.getY(),
                    target.getZ() + 0.5,
                    1.0);
        }

        @Override
        public void stop() {
            stopCount++;
            zombie.getNavigation().stop();
        }

        void disable() {
            enabled = false;
        }

        boolean moveAccepted() {
            return moveAccepted;
        }

        int startCount() {
            return startCount;
        }

        int stopCount() {
            return stopCount;
        }
    }

    private static final class BenchmarkTrial {
        private final GameTestHelper helper;
        private final Zombie zombie;
        private final ProbeMoveGoal probe;
        private final BlockPos target;
        private final int replicate;
        private final long startTick;
        private final long startObserverNanos;
        private double previousX;
        private double previousY;
        private double previousZ;
        private int stallTicks;
        private int maximumPathNodes;
        private boolean complete;

        private BenchmarkTrial(
                GameTestHelper helper,
                Zombie zombie,
                ProbeMoveGoal probe,
                BlockPos target,
                int replicate,
                long startTick) {
            this.helper = helper;
            this.zombie = zombie;
            this.probe = probe;
            this.target = target;
            this.replicate = replicate;
            this.startTick = startTick;
            this.startObserverNanos = BetterMinecraftAi.observationService().handlerNanos();
            this.previousX = zombie.getX();
            this.previousY = zombie.getY();
            this.previousZ = zombie.getZ();
        }

        void sample() {
            if (complete || helper.getTick() < startTick) {
                return;
            }

            net.minecraft.world.level.pathfinder.Path path = zombie.getNavigation().getPath();
            if (path != null) {
                maximumPathNodes = Math.max(maximumPathNodes, path.getNodeCount());
            }

            double deltaX = zombie.getX() - previousX;
            double deltaY = zombie.getY() - previousY;
            double deltaZ = zombie.getZ() - previousZ;
            if (zombie.getNavigation().isInProgress()
                    && deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ < 1.0E-6) {
                stallTicks++;
            }
            previousX = zombie.getX();
            previousY = zombie.getY();
            previousZ = zombie.getZ();

            Vec3 targetCenter = Vec3.atBottomCenterOf(target);
            boolean arrived = probe.startCount() > 0
                    && probe.moveAccepted()
                    && zombie.getNavigation().isDone()
                    && zombie.position().distanceToSqr(targetCenter) <= 2.25;
            if (arrived) {
                finish(true);
            }
        }

        void failIfIncomplete() {
            if (!complete) {
                finish(false);
                helper.fail("Vanilla navigation baseline timed out");
            }
        }

        private void finish(boolean success) {
            if (complete) {
                return;
            }
            complete = true;
            long durationTicks = helper.getTick() - startTick;
            Vec3 targetCenter = Vec3.atBottomCenterOf(target);
            double finalDistance = Math.sqrt(zombie.position().distanceToSqr(targetCenter));
            long observerNanos = BetterMinecraftAi.observationService().handlerNanos() - startObserverNanos;
            long[] tickTimes = Arrays.stream(helper.getLevel().getServer().getTickTimesNanos())
                    .filter(value -> value > 0L)
                    .sorted()
                    .toArray();
            BenchmarkResult result = new BenchmarkResult(
                    replicate,
                    helper.getLevel().getSeed(),
                    durationTicks,
                    success,
                    finalDistance,
                    maximumPathNodes,
                    stallTicks,
                    observerNanos,
                    percentileMilliseconds(tickTimes, 0.50),
                    percentileMilliseconds(tickTimes, 0.95),
                    percentileMilliseconds(tickTimes, 0.99));
            probe.disable();
            zombie.goalSelector.removeGoal(probe);
            try {
                appendBenchmark(result);
            } catch (IOException exception) {
                throw new UncheckedIOException("Unable to retain P0 benchmark evidence", exception);
            }
            if (success) {
                helper.succeed();
            }
        }

        private static double percentileMilliseconds(long[] sortedNanos, double percentile) {
            if (sortedNanos.length == 0) {
                return 0.0;
            }
            int index = Math.max(0, (int) Math.ceil(percentile * sortedNanos.length) - 1);
            return sortedNanos[index] / 1_000_000.0;
        }
    }

    private record BenchmarkResult(
            int replicate,
            long worldSeed,
            long durationTicks,
            boolean success,
            double finalDistance,
            int pathNodes,
            int stallTicks,
            long observerNanos,
            double tickMedianMilliseconds,
            double tickP95Milliseconds,
            double tickP99Milliseconds) {
        static String header() {
            return "arm,scenario,replicate,world_seed,entity_count,warmup_ticks,duration_ticks,success,"
                    + "final_distance,path_nodes,stall_ticks,observer_handler_nanos,tick_median_ms,tick_p95_ms,"
                    + "tick_p99_ms,exceptions";
        }

        String toCsv(String arm) {
            return String.format(
                    Locale.ROOT,
                    "%s,open_plain_1,%d,%d,1,20,%d,%s,%.6f,%d,%d,%d,%.6f,%.6f,%.6f,0",
                    arm,
                    replicate,
                    worldSeed,
                    durationTicks,
                    success,
                    finalDistance,
                    pathNodes,
                    stallTicks,
                    observerNanos,
                    tickMedianMilliseconds,
                    tickP95Milliseconds,
                    tickP99Milliseconds);
        }
    }
}
