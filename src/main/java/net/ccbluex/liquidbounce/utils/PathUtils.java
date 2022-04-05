/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils;

import me.liuli.path.Cell;
import me.liuli.path.Pathfinder;
import net.ccbluex.liquidbounce.utils.block.MinecraftWorldProvider;
import net.minecraft.util.Vec3;

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
        final Pathfinder pathfinder = new Pathfinder(new Cell((int)curX, (int)curY, (int)curZ), new Cell((int)tpX, (int)tpY, (int)tpZ),
                Pathfinder.COMMON_NEIGHBORS, new MinecraftWorldProvider(mc.theWorld));

        return simplifyPath(pathfinder.findPath(1000), dashDistance);
    }

    public static ArrayList<Vec3> simplifyPath(final ArrayList<Cell> path, final double dashDistance) {
        final ArrayList<Vec3> finalPath = new ArrayList<>();

        Cell cell;
        for (int i = 1; i < path.size() - 1; i++) {
            cell = path.get(i);
            finalPath.add(new Vec3(cell.x + 0.5, cell.y, cell.z + 0.5));
        }

        return finalPath;
    }
}
