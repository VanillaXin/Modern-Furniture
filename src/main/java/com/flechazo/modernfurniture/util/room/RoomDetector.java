package com.flechazo.modernfurniture.util.room;

import com.flechazo.modernfurniture.ModernFurniture;
import com.flechazo.modernfurniture.config.module.RoomDetectionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 *
 * <p>基于 BFS 实现的通用封闭空间检测系统</p>
 * @see RoomDetectionConfig 获取搜索参数配置
 */
public class RoomDetector {

    /**
     * 执行封闭空间检测
     *
     * @param level    目标世界对象
     * @param startPos 检测起始坐标
     * @return 包含所有可通过方块的集合，若失败返回空集合
     */
    public static Set<BlockPos> findRoom(Level level, BlockPos startPos) {
        long startTime = System.currentTimeMillis();

        Set<BlockPos> visited = new HashSet<>(8192);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(1024);
        Set<BlockPos> roomBlocks = new HashSet<>(8192);

        if (!isPassable(level, startPos)) {
            return Collections.emptySet();
        }

        final int maxRadius = RoomDetectionConfig.maxSearchDistance;
        final int maxVolume = RoomDetectionConfig.maxRoomSize;
        final long maxTime = RoomDetectionConfig.maxSearchTimeMs;

        final int minX = startPos.getX() - maxRadius;
        final int maxX = startPos.getX() + maxRadius;
        final int minY = Math.max(level.getMinBuildHeight(), startPos.getY() - maxRadius);
        final int maxY = Math.min(level.getMaxBuildHeight(), startPos.getY() + maxRadius);
        final int minZ = startPos.getZ() - maxRadius;
        final int maxZ = startPos.getZ() + maxRadius;

        queue.offer(startPos);
        visited.add(startPos);
        roomBlocks.add(startPos);

        int processed = 0;

        while (!queue.isEmpty()) {
            if (++processed % 1000 == 0) {
                if (System.currentTimeMillis() - startTime > maxTime) {
                    ModernFurniture.LOGGER.debug("[房间检测] 超时: {}方块, {}ms", processed, System.currentTimeMillis() - startTime);
                    return Collections.emptySet();
                }
                if (roomBlocks.size() >= maxVolume) {
                    ModernFurniture.LOGGER.debug("[房间检测] 体积过大: {}方块, {}ms", roomBlocks.size(), System.currentTimeMillis() - startTime);
                    return Collections.emptySet();
                }
            }

            var current = queue.poll();
            int x = current.getX();
            int y = current.getY();
            int z = current.getZ();

            checkAndAdd(level, new BlockPos(x, y + 1, z), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
            checkAndAdd(level, new BlockPos(x, y - 1, z), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
            checkAndAdd(level, new BlockPos(x + 1, y, z), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
            checkAndAdd(level, new BlockPos(x - 1, y, z), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
            checkAndAdd(level, new BlockPos(x, y, z + 1), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
            checkAndAdd(level, new BlockPos(x, y, z - 1), visited, queue, roomBlocks, minX, maxX, minY, maxY, minZ, maxZ);
        }

        long elapsed = System.currentTimeMillis() - startTime;
        ModernFurniture.LOGGER.debug("[房间检测] 完成: {}方块, {}ms", roomBlocks.size(), elapsed);

        return roomBlocks;
    }

    /**
     * 检查并添加相邻方块到队列
     */
    private static void checkAndAdd(Level level, BlockPos pos, Set<BlockPos> visited,
                                    ArrayDeque<BlockPos> queue, Set<BlockPos> roomBlocks,
                                    int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        if (x < minX || x > maxX || y < minY || y > maxY || z < minZ || z > maxZ) {
            return;
        }

        if (visited.add(pos)) {
            if (!level.hasChunkAt(pos)) {
                return;
            }

            if (isPassable(level, pos)) {
                queue.offer(pos);
                roomBlocks.add(pos);
            }
        }
    }

    /**
     * 检查方块是否可通过
     *
     * @param level 世界对象
     * @param pos   方块位置
     * @return 如果方块可通过返回true
     */
    public static boolean isPassable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);

        if (state.isAir()) return true;
        if (!state.getFluidState().isEmpty()) return true;

        return state.hasProperty(BlockStateProperties.OPEN) && state.getValue(BlockStateProperties.OPEN);
    }

    /**
     * 异步执行房间检测
     *
     * @param level    目标世界对象
     * @param startPos 检测起始坐标
     * @return 包含搜索结果的CompletableFuture
     */
    public static CompletableFuture<Set<BlockPos>> findRoomAsync(Level level, BlockPos startPos) {
        return CompletableFuture.supplyAsync(() -> findRoom(level, startPos));
    }
}