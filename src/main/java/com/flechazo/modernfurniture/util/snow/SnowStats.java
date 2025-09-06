package com.flechazo.modernfurniture.util.snow;

import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record SnowStats(int totalSnowBlocks, int snowCycles, Map<BlockPos, Integer> snowLayers,
                        double averageProcessTime, double currentDensity, int activeSections, long cacheHitRate,
                        long memoryUsage, double currentCoverage, boolean reachedCycleLimit,
                        boolean reachedCoverageLimit) {
    public SnowStats(int totalSnowBlocks, int snowCycles, Map<BlockPos, Integer> snowLayers,
                     double averageProcessTime, double currentDensity, int activeSections,
                     long cacheHitRate, long memoryUsage, double currentCoverage,
                     boolean reachedCycleLimit, boolean reachedCoverageLimit) {
        this.totalSnowBlocks = totalSnowBlocks;
        this.snowCycles = snowCycles;
        this.snowLayers = new HashMap<>(snowLayers);
        this.averageProcessTime = averageProcessTime;
        this.currentDensity = currentDensity;
        this.activeSections = activeSections;
        this.cacheHitRate = cacheHitRate;
        this.memoryUsage = memoryUsage;
        this.currentCoverage = currentCoverage;
        this.reachedCycleLimit = reachedCycleLimit;
        this.reachedCoverageLimit = reachedCoverageLimit;
    }

    @Override
    @NotNull
    public String toString() {
        return String.format(
                "SnowStats{blocks=%d, cycles=%d, coverage=%.2f%%, avgTime=%.2fms, sections=%d, " +
                        "cacheHit=%d, memory=%dMB, cycleLimit=%s, coverageLimit=%s}",
                totalSnowBlocks, snowCycles, currentCoverage * 100, averageProcessTime,
                activeSections, cacheHitRate, memoryUsage / (1024 * 1024),
                reachedCycleLimit, reachedCoverageLimit
        );
    }
}