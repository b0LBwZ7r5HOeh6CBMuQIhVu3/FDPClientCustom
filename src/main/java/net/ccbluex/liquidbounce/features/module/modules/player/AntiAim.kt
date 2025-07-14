/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer

@ModuleInfo(name = "AntiAim", category = ModuleCategory.PLAYER)
class AntiAim : Module() {
    private val yawMode = ListValue("YawMove", arrayOf("Jitter", "Spin", "Back", "BackJitter", "Random"), "Spin")
    private val pitchMode = ListValue("PitchMode", arrayOf("Down", "Up", "Jitter", "AnotherJitter","Headless"), "Down")
    private val rotateValue = ListValue("SetRotate", arrayOf("Edit", "Send", "Client"), "Edit")
    private val delayValue = IntegerValue("Delay", 0, 0,1000)

    private var yaw = 0f
    private var pitch = 0f
    private val delayTimer = MSTimer()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(delayTimer.hasTimePassed(delayValue.get().toLong())) delayTimer.reset() else return
        when (yawMode.get().lowercase()) {
            "spin" -> {
                yaw += 20.0f
                if (yaw > 180.0f) {
                    yaw = -180.0f
                } else if (yaw < -180.0f) {
                    yaw = 180.0f
                }
            }
            "jitter" -> {
                yaw = mc.thePlayer.rotationYaw + if (mc.thePlayer.ticksExisted % 2 == 0) 90F else -90F
            }
            "random" -> yaw = if (mc.thePlayer.ticksExisted % 2 == 0) RandomUtils.nextDouble(-34.0, -114.0).toFloat() else RandomUtils.nextDouble(14.0, 154.0).toFloat()
            "back" -> {
                yaw = mc.thePlayer.rotationYaw + 180f
            }
            "backjitter" -> {
                yaw = mc.thePlayer.rotationYaw + 180f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
        }

        when (pitchMode.get().lowercase()) {
            "up" -> {
                pitch = -90.0f
            }
            "down" -> {
                pitch = 90.0f
            }
            "anotherjitter" -> {
                pitch = 60f + RandomUtils.nextDouble(-3.0, 3.0).toFloat()
            }
            "jitter" -> {
                pitch += 30.0f
                if (pitch > 90.0f) {
                    pitch = -90.0f
                } else if (pitch < -90.0f) {
                    pitch = 90.0f
                }
            }
            "Headless" ->{
                pitch = 180f
            }
        }

        when (rotateValue.get().lowercase()) {
            "edit" -> RotationUtils.setTargetRotation(Rotation(yaw, pitch))
            "send" -> mc.netHandler.addToSendQueue(C03PacketPlayer.C05PacketPlayerLook(yaw, pitch,mc.thePlayer.onGround))
            "client" -> {
               mc.thePlayer.rotationYaw = yaw
               mc.thePlayer.rotationPitch = pitch
            }

        }
    }
}