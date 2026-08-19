package com.zeroclient.client.buildai.pathing;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class SimplePathfinder {

    private static final int MAX_NODES = 3000;
    private static final int MAX_FALL = 3;

    public static List<BlockPos> findPath(BlockPos start, BlockPos goal, int maxDistance) {
        Level level = Minecraft.getInstance().level;
        if (level == null) return List.of();

        if (start.distSqr(goal) > (double) maxDistance * maxDistance) return List.of();

        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Map<BlockPos, Double> gScore = new HashMap<>();
        Set<BlockPos> closed = new HashSet<>();

        PriorityQueue<BlockPos> open = new PriorityQueue<>(
                Comparator.comparingDouble(p -> gScore.getOrDefault(p, Double.MAX_VALUE) + heuristic(p, goal)));

        gScore.put(start, 0.0);
        open.add(start);

        int processedNodes = 0;

        while (!open.isEmpty() && processedNodes < MAX_NODES) {
            BlockPos current = open.poll();
            processedNodes++;

            if (current.equals(goal) || isAdjacentToGoal(current, goal)) {
                return reconstructPath(cameFrom, current);
            }

            if (closed.contains(current)) continue;
            closed.add(current);

            for (BlockPos neighbor : getNeighbors(level, current)) {
                if (closed.contains(neighbor)) continue;
                if (neighbor.distSqr(start) > (double) maxDistance * maxDistance) continue;

                double tentativeG = gScore.getOrDefault(current, Double.MAX_VALUE) + current.distManhattan(neighbor);

                if (tentativeG < gScore.getOrDefault(neighbor, Double.MAX_VALUE)) {
                    cameFrom.put(neighbor, current);
                    gScore.put(neighbor, tentativeG);
                    open.add(neighbor);
                }
            }
        }

        return List.of();
    }

    private static boolean isAdjacentToGoal(BlockPos pos, BlockPos goal) {
        return pos.distSqr(goal) <= 2.0;
    }

    private static double heuristic(BlockPos a, BlockPos b) {
        return a.distManhattan(b);
    }

    private static List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> cameFrom, BlockPos current) {
        List<BlockPos> path = new ArrayList<>();
        path.add(current);
        while (cameFrom.containsKey(current)) {
            current = cameFrom.get(current);
            path.add(0, current);
        }
        return path;
    }

    private static List<BlockPos> getNeighbors(Level level, BlockPos current) {
        List<BlockPos> neighbors = new ArrayList<>();
        int[][] horizontalOffsets = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] offset : horizontalOffsets) {
            BlockPos sameLevel = current.offset(offset[0], 0, offset[1]);
            if (isStandable(level, sameLevel)) {
                neighbors.add(sameLevel);
                continue;
            }

            BlockPos stepUp = current.offset(offset[0], 1, offset[1]);
            if (isStandable(level, stepUp) && !isSolid(level, current.above(2))) {
                neighbors.add(stepUp);
                continue;
            }

            for (int fall = 1; fall <= MAX_FALL; fall++) {
                BlockPos stepDown = current.offset(offset[0], -fall, offset[1]);
                if (isStandable(level, stepDown)) {
                    neighbors.add(stepDown);
                    break;
                }
                if (isSolid(level, stepDown)) break;
            }
        }

        return neighbors;
    }

    private static boolean isStandable(Level level, BlockPos pos) {
        return isSolid(level, pos.below())
                && !isSolid(level, pos)
                && !isSolid(level, pos.above());
    }

    private static boolean isSolid(Level level, BlockPos pos) {
    return !level.getBlockState(pos).isAir();
}
}