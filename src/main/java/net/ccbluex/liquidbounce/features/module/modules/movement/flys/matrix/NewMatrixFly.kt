package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import java.lang.Math.*

class NewMatrixAirBounce : FlyMode("NewMatrixFlyFly") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1.5f, 0.2f, 1.7f)
    private val verticalSpeedValue = FloatValue("${valuePrefix}VerticalSpeed", 0.8f, 0.2f, 1.7f)
    private val timerValue = FloatValue("${valuePrefix}Timer", 0.8f, 0.3f, 1.3f)
    private val airBounceHeightValue = FloatValue("${valuePrefix}AirBounceHeight", 0.8f, 0.3f, 1.3f)

    private var boostMotion = 0
    override fun onEnable() {
        boostMotion = 0
    }

    fun FlagPacket(yaw: Double) {
        /*        mc.netHandler.addToSendQueue(
                    C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY,
                        mc.thePlayer.posZ,
                        true
                    )
                )*/
        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition()) //funni packet
        mc.netHandler.addToSendQueue(
            C03PacketPlayer.C04PacketPlayerPosition(
                mc.thePlayer.posX + -sin(yaw) * 15,
                mc.thePlayer.posY + 50,
                mc.thePlayer.posZ + cos(yaw) * 15,
                false
            )
        )
    }

    override fun onUpdate(event: UpdateEvent) {
        if (boostMotion == 0) {
            FlagPacket(MovementUtils.direction)
            boostMotion = 1 //1
            mc.timer.timerSpeed = 0.1f
        } else if (boostMotion == 1) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        } else if (boostMotion == 2) { // getting flag, 2
            MovementUtils.strafe(speedValue.get())
            mc.thePlayer.motionY = verticalSpeedValue.get().toDouble()
            boostMotion = 3
        } else if (boostMotion < 4) { // waiting, 3
            boostMotion++
        } else/* if (boostMotion >= 5)*/ {
            mc.timer.timerSpeed = timerValue.get()
            if (mc.thePlayer.posY < fly.launchY - airBounceHeightValue.get()) {
                boostMotion = 0
            }
        }
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.currentScreen == null && packet is S08PacketPlayerPosLook) {
            mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
            mc.netHandler.addToSendQueue(
                C03PacketPlayer.C06PacketPlayerPosLook(
                    packet.x,
                    packet.y,
                    packet.z,

                    packet.yaw,
                    packet.pitch,
                    false
                )
            )
            if (boostMotion == 1) {
                boostMotion = 2
            }
            event.cancelEvent()
        }
        if (mc.currentScreen == null && packet is C03PacketPlayer && boostMotion == 1) {
            event.cancelEvent()
        }
    }
}
