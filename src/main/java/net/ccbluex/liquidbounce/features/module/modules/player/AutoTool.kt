/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.ClickBlockEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.minecraft.util.BlockPos
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C09PacketHeldItemChange
import kotlin.math.max

@ModuleInfo(name = "AutoTool", category = ModuleCategory.PLAYER)
class AutoTool : Module() {
    private val bypass = BoolValue("bypass", true)
    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        switchSlot(event.clickedBlock ?: return)
    }

    fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1F
        var bestSlot = -1

        val block = mc.theWorld.getBlockState(blockPos).block

        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            val speed = item.getStrVsBlock(block)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

        if (bestSlot != -1 && mc.thePlayer.inventory.currentItem != bestSlot) {
            if (max(mc.thePlayer.inventory.currentItem, bestSlot) != 1 && bypass.get()) {
                if (bestSlot > mc.thePlayer.inventory.currentItem) {
                    for (i in mc.thePlayer.inventory.currentItem..bestSlot - 1) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(i))
                    }
                } else {
                    for (i in mc.thePlayer.inventory.currentItem downTo bestSlot - 1) {
                        mc.netHandler.addToSendQueue(C09PacketHeldItemChange(i))
                    }
                }
            }
            mc.thePlayer.inventory.currentItem = bestSlot
        }
    }
}