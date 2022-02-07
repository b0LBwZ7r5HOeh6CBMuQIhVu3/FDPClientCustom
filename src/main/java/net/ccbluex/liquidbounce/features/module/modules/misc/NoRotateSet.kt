/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

@ModuleInfo(name = "NoRotateSet", category = ModuleCategory.MISC)
class NoRotateSet : Module() {

    private val noLoadingValue = BoolValue("NoLoading", true)
    private val confirmValue = BoolValue("Confirm", true)
    private val confirmBackValue = BoolValue("ConfirmBack", true)
    private val usePrevRotationValue = BoolValue("usePrevRotation", true)
    private val vulcanValue = BoolValue("vulcan", true)
    private val illegalRotationValue = BoolValue("ConfirmIllegalRotation", false)
    private val noZeroValue = BoolValue("NoZero", false)

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is S08PacketPlayerPosLook) {
            if ((noZeroValue.get() && packet.getYaw() == 0F && packet.getPitch() == 0F) ||
                (noLoadingValue.get() && !mc.netHandler.doneLoadingTerrain)) {
                return
            }
            val yaw = if (usePrevRotationValue.get()) mc.thePlayer.prevRotationYaw else mc.thePlayer.rotationYaw
            val pitch = if (usePrevRotationValue.get()) mc.thePlayer.prevRotationPitch else mc.thePlayer.rotationPitch
            val valcanYaw = if(vulcanValue.get()) RandomUtils.nextFloat(-2F,2F) else 0
            if (illegalRotationValue.get() || packet.getPitch() <= 90 && packet.getPitch() >= -90 &&
                RotationUtils.serverRotation != null && packet.getYaw() != RotationUtils.serverRotation.yaw &&
                packet.getPitch() != RotationUtils.serverRotation.pitch) {

                if (confirmValue.get()) {
                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(packet.getYaw() + valcanYaw.toFloat(), packet.getPitch(), mc.thePlayer.onGround))
                }
                if (confirmBackValue.get()) {
                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(yaw, pitch, mc.thePlayer.onGround))
                }
            }

            packet.yaw = yaw
            packet.pitch = pitch
        }
    }
}