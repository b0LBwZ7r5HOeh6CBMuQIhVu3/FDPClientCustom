package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.exploit.AntiDesync;
import net.ccbluex.liquidbounce.features.module.modules.exploit.PacketFixer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Objects;

@Mixin(C08PacketPlayerBlockPlacement.class)
public class MixinC08PacketPlayerBlockPlacement {
    @Shadow
    private BlockPos position;
    @Shadow
    private int placedBlockDirection;
    @Shadow
    private ItemStack stack;
    @Shadow
    private float facingX;
    @Shadow
    private float facingY;
    @Shadow
    private float facingZ;

    private float idk(){
        switch(LiquidBounce.moduleManager.getModule(PacketFixer.class).getFixViaForgePlacement().get().toLowerCase()){
            case "funnyfloat" : return (float) (14f + Math.random());
            case "1.12.2" : return 1f;
        }
        return 16f;
    }
    /**
     * @author
     * @reason
     */
    @Overwrite
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.position);
        buf.writeByte(this.placedBlockDirection);
        buf.writeItemStackToBuffer(this.stack);
        buf.writeByte((int) (this.facingX * idk()));
        buf.writeByte((int) (this.facingY * idk()));
        buf.writeByte((int) (this.facingZ * idk()));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.position = buf.readBlockPos();
        this.placedBlockDirection = buf.readUnsignedByte();
        this.stack = buf.readItemStackFromBuffer();
        this.facingX = buf.readUnsignedByte() / idk();
        this.facingY = buf.readUnsignedByte() / idk();
        this.facingZ = buf.readUnsignedByte() / idk();
    }
}