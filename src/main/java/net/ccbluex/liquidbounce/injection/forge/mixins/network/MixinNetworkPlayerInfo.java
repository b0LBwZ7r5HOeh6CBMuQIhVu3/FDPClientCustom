package net.ccbluex.liquidbounce.injection.forge.mixins.network;

import com.mojang.authlib.GameProfile;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.features.module.modules.misc.NameProtect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Random;
import java.util.Objects;

@Mixin(NetworkPlayerInfo.class)
public class MixinNetworkPlayerInfo {
    @Shadow
    @Final
    private GameProfile gameProfile;

    private static final ResourceLocation TEXTURE_LIULIAQUI = new ResourceLocation("fdpclient/skin/TEXTURE_LIULIAQUI.png");
    private static final ResourceLocation TEXTURE_PIGEON233 = new ResourceLocation("fdpclient/skin/TEXTURE_PIGEON233.png");
    @Inject(method = "getLocationSkin", cancellable = true, at = @At("HEAD"))
    private void injectSkinProtect(CallbackInfoReturnable<ResourceLocation> cir) {
        NameProtect nameProtect = LiquidBounce.moduleManager.getModule(NameProtect.class);


        if (nameProtect.getState() && nameProtect.getAllPlayersValue().get()) {
            if (nameProtect.getAllPlayersValue().get() || Objects.equals(gameProfile.getId(), Minecraft.getMinecraft().getSession().getProfile().getId())) {
                cir.setReturnValue(DefaultPlayerSkin.getSkinType(this.gameProfile.getId()) == "default" ? TEXTURE_LIULIAQUI : TEXTURE_PIGEON233);

                cir.cancel();
            }
        }
    }
}