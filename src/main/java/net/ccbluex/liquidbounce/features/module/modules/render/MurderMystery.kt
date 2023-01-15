/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.item.ItemSword
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import java.awt.Color
import net.minecraft.entity.player.EntityPlayer

@ModuleInfo(name = "MurderMystery", category = ModuleCategory.RENDER)
class MurderMystery : Module() {
    private var sus: EntityLivingBase? = null

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (sus != null && mc.thePlayer != null && mc.thePlayer.ticksExisted > 2) {
            RenderUtils.drawEntityBox(sus, Color(255, 0, 0), true, true, 2)
        }

        @EventTarget
        fun onUpdate(event: UpdateEvent) {
            if (sus != null && sus.isDead) {
                sus = null
                return
            }
            if (sus == null) {
                for (entity in mc.theWorld.loadedEntityList) {
                    if (entity != mc.thePlayer && entity is EntityPlayer && entity.getHeldItem()
                            .getItem() is itemSword) {
                        sus = entity
                        alert("§c§lMurderer » " + entity.getName())
                        return
                    }
                }
            }
        }

        @EventTarget
        fun onWorld(event: WorldEvent) {
            sus = null
        }
    }