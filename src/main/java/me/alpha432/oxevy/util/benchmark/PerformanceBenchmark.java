package me.alpha432.oxevy.util.benchmark;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Performance benchmark utilities for Oxevy client.
 * Used to measure and track optimization improvements.
 *
 * @author Oxevy Team
 * @version 1.0
 */
public class PerformanceBenchmark {
    private static final Logger LOGGER = LogManager.getLogger("OxevyBenchmark");

    private static long entityIterationTime = 0;
    private static long renderTime = 0;
    private static long eventBusTime = 0;
    private static int frameCount = 0;

    /**
     * Start timing an operation.
     * @param name Name of the operation being benchmarked
     * @return Start timestamp
     */
    public static long start(String name) {
        return System.nanoTime();
    }

    /**
     * End timing and log the result.
     * @param name Name of the operation
     * @param start Start timestamp from start()
     */
    public static void end(String name, long start) {
        long elapsed = System.nanoTime() - start;
        long ms = elapsed / 1_000_000;
        if (ms > 1) { // Only log operations taking more than 1ms
            LOGGER.debug("{}: {}ms", name, ms);
        }
    }

    /**
     * Track entity iteration performance.
     */
    public static void trackEntityIteration(long nanos) {
        entityIterationTime = (entityIterationTime * 9 + nanos) / 10; // Exponential moving average
    }

    /**
     * Track render performance.
     */
    public static void trackRender(long nanos) {
        renderTime = (renderTime * 9 + nanos) / 10;
    }

    /**
     * Track EventBus performance.
     */
    public static void trackEventBus(long nanos) {
        eventBusTime = (eventBusTime * 9 + nanos) / 10;
    }

    /**
     * Increment frame counter.
     */
    public static void frame() {
        frameCount++;
    }

    /**
     * Get current performance statistics.
     */
    public static String getStats() {
        Minecraft mc = Minecraft.getInstance();
        return String.format(
            "Entity Cache: %d entities | Avg Iteration: %.2fms | Avg Render: %.2fms | Avg EventBus: %.2fms | Frames: %d",
            mc.level != null ? mc.level.entitiesForRendering().spliterator().estimateSize() : 0,
            entityIterationTime / 1_000_000.0,
            renderTime / 1_000_000.0,
            eventBusTime / 1_000_000.0,
            frameCount
        );
    }

    /**
     * Run a microbenchmark for entity iteration.
     * @return Operations per second
     */
    public static double benchmarkEntityIteration() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return 0;

        long start = System.nanoTime();
        int iterations = 1000;

        for (int i = 0; i < iterations; i++) {
            for (Entity entity : mc.level.entitiesForRendering()) {
                // Simulate simple check
                if (entity.isAlive()) {
                    double dist = mc.player.distanceToSqr(entity);
                }
            }
        }

        long elapsed = System.nanoTime() - start;
        return (iterations * 1_000_000_000.0) / elapsed;
    }

    /**
     * Reset all statistics.
     */
    public static void reset() {
        entityIterationTime = 0;
        renderTime = 0;
        eventBusTime = 0;
        frameCount = 0;
    }
}
