package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.BoolValue
import kotlin.math.cos
import kotlin.math.sin

class ClipFly : FlyMode("Clip") {
    private val xValue = FloatValue("${valuePrefix}-X", 5f, -5f, 5f)
    private val yValue = FloatValue("${valuePrefix}-Y", -2f, -5f, 5f)
    private val zValue = FloatValue("${valuePrefix}-Z", 5f, -5f, 5f)
    private val delayValue = IntegerValue("${valuePrefix}-Delay", 1000, 0, 3000)
    private val timerValue = FloatValue("${valuePrefix}-Timer", 0.7f, 0.2f, 1f)
    private val packetOnGroundValue = BoolValue("${valuePrefix}packetOnGround", true)

    private val timer = MSTimer()

    override fun onEnable() {
        timer.reset()
    }
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            packet.onGround = packetOnGroundValue.get()
        }
    }
    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.onGround = false
        mc.timer.timerSpeed = timerValue.get()
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        if (timer.hasTimePassed(delayValue.get().toLong())) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.setPosition(mc.thePlayer.posX + (-sin(yaw) * xValue.get()), mc.thePlayer.posY + yValue.get(), mc.thePlayer.posZ + (cos(yaw) * zValue.get()))
            timer.reset()
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
    }
}
