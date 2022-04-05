package net.ccbluex.liquidbounce.utils.block;

import me.liuli.path.Cell;
import me.liuli.path.IWorldProvider;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockWall;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class MinecraftWorldProvider implements IWorldProvider {

    private final World world;

    public MinecraftWorldProvider(World world) {
        this.world = world;
    }

    @Override
    public boolean isBlocked(Cell cell) {
        return isSolid(cell.x, cell.y, cell.z) || isSolid(cell.x, cell.y + 1, cell.z) || unableToStand(cell.x, cell.y - 1, cell.z);
    }

    private boolean isSolid(int x, int y, int z) {
        final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        final Material material = block.getMaterial();
        return material.isSolid();
    }

    private boolean unableToStand(int x, int y, int z) {
        final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        return block instanceof BlockFence || block instanceof BlockWall;
    }
}