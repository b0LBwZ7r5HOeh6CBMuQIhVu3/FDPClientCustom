package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import kotlin.math.sqrt
import net.minecraft.network.play.server.S12PacketEntityVelocity

class MatrixBHop2 : SpeedMode("MatrixBHop2") {
    private val speedMultiValue = FloatValue("MatrixBHop2-Speed", 1f, 0.7f, 1.2f)
    private val noTimerValue = BoolValue("NoTimer", false)
    private var rX = 0.0
    private var rY = 0.0
    private var rZ = 0.0
    private var jumped = false
    private var dist = 0.0

    override fun onUpdate() {
        fun setTimer(value: Float) {
            if(!noTimerValue.get()) {
                mc.timer.timerSpeed = value
            }
        }

        if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.onGround) {
            if (MovementUtils.getSpeed() < 0.217f) {
                MovementUtils.strafe(0.217f)
                mc.thePlayer.jumpMovementFactor = 0.0269f
            }
        }
        if (mc.thePlayer.motionY < 0 && !mc.thePlayer.isCollidedHorizontally) {
            setTimer(1.16f)
            if (mc.thePlayer.fallDistance > 1.4)
                setTimer(1.0f)
        } else {
           setTimer(0.95f)
        }
        if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
            setTimer(1.3f)
            if (MovementUtils.getSpeed() < 0.0776f)
                MovementUtils.strafe(0.0776f)
            mc.thePlayer.jump();
            if (mc.thePlayer.movementInput.moveStrafe == 0) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 1.0071f)
            }
        }
        if (!MovementUtils.isMoving()) {
            setTimer(1.0f)
        }

    }

    override fun onPacket(event: PacketEvent) {
            fun getDistance_(x1: Double, x2: Double, z1: Double, z2: Double) {
                return sqrt((x1 - x2) * (x1 - x2) + (z1 - z2) * (z1 - z2))
            }
        val packet = event.packet

        if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || mc.theWorld.getEntityByID(packet.getEntityID()) != mc.thePlayer) {
                return
            }
            event.cancelEvent()
            rx = (packet.getMotionX() / 8000.0).toDouble()
            rz = (packet.getMotionZ() / 8000.0).toDouble()
            if (getDistance_(rx, 0.0, rz, 0.0) > MovementUtils.getSpeed())
                MovementUtils.strafe(getDistance_(rx, 0.0, rz, 0.0).toFloat())
            mc.thePlayer.motionY = (packet.getMotionY() / 8000.0).toDouble()
            MovementUtils.strafe(MovementUtils.getSpeed() * 1.15f)
        }
    }
}
