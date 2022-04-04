/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.tofweb.starlite.*;

import java.util.ArrayList;
import java.util.List;

public final class PathUtils extends MinecraftInstance {
    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ){
        return findBlinkPath(tpX, tpY, tpZ,5);
    }

    public static List<Vec3> findBlinkPath(final double tpX, final double tpY, final double tpZ,final double dist){
        return findBlinkPath(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, tpX, tpY, tpZ, dist);
    }

    public static List<Vec3> findBlinkPath(double curX, double curY, double curZ, final double tpX, final double tpY, final double tpZ, final double dashDistance) {
        final CellSpace space = new CellSpace();
        space.setGoalCell((int)tpX, (int)tpY, (int)tpZ); // it must be in this order
        space.setStartCell((int)curX, (int)curY, (int)curZ);

        final BlockManager blockManager = new MinecraftBlockManager(space, mc.theWorld);
        final Pathfinder pathfinder = new Pathfinder(blockManager);

        return simplifyPath(pathfinder.findPath());
    }

    public static ArrayList<Vec3> simplifyPath(final Path path) {
        final ArrayList<Vec3> finalPath = new ArrayList<>();

        Cell cell;
        for (int i = 1; i < path.size() - 1; i++) {
            cell = path.get(i);
            finalPath.add(new Vec3(cell.getX() + 0.5, cell.getY(), cell.getZ() + 0.5));
        }

        return finalPath;
    }

    public static class MinecraftBlockManager extends BlockManager {

        private final World world;

        public MinecraftBlockManager(CellSpace space, World world) {
            super(space);
            this.world = world;
        }

        @Override
        public boolean isBlocked(Cell cell) {
            if(cell.equals(space.getStartCell()) || cell.equals(space.getGoalCell())) return false;
            // check if the block is solid
            return isSolid(cell.getX(), cell.getY(), cell.getZ()) || isSolid(cell.getX(), cell.getY() + 1, cell.getZ());
        }

        private boolean isSolid(int x, int y, int z) {
            final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
            final Material material = block.getMaterial();
            return material.isSolid();
        }
    }
}
