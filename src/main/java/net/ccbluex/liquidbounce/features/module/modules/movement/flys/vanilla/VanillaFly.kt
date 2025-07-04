package net.ccbluex.liquidbounce.features.module.modules.movement.flys.vanilla

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import kotlin.math.cos
import kotlin.math.sin

class VanillaFly : FlyMode("Vanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)
    private val vspeedValue = FloatValue("${valuePrefix}Vertical", 2f, 0f, 5f)
    private val glideSpeedValue = FloatValue("${valuePrefix}glideSpeed", -0.1f, -0.8f, 0f)
    private val kickBypassValue = BoolValue("${valuePrefix}KickBypass", false)
    private val keepAliveValue = BoolValue("${valuePrefix}KeepAlive", false) // old KeepAlive fly combined
    private val noClipValue = BoolValue("${valuePrefix}NoClip", false)
    private val spoofValue = BoolValue("${valuePrefix}SpoofGround", false)
    private val editMoveValue = BoolValue("${valuePrefix}EditMove", false)
    private val bloxdValue = BoolValue("${valuePrefix}Bloxd", false)

    private var packets = 0

    override fun onEnable() {
        packets = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        if (keepAliveValue.get()) {
            mc.netHandler.addToSendQueue(C00PacketKeepAlive())
        }
        if (noClipValue.get()) {
            mc.thePlayer.noClip = true
        }

        mc.thePlayer.capabilities.isFlying = false

        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = glideSpeedValue.get().toDouble()
        mc.thePlayer.motionZ = 0.0
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            mc.thePlayer.motionY += vspeedValue.get()
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            mc.thePlayer.motionY -= vspeedValue.get()
        }

        MovementUtils.strafe(speedValue.get())
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (packet is C03PacketPlayer) {
            if(spoofValue.get()) {
                packet.onGround = true
            }
            packets++
            if (packets == 40 && kickBypassValue.get()) {
                MovementUtils.handleVanillaKickBypass()
                packets = 0
            }
        }
    }

    override fun onMove(event: MoveEvent) {
        if(!editMoveValue.get()) return
/*

        val vMul = if(bloxdValue.get()) {vspeedValue.get().toDouble() / 0.42} else 1.toDouble()
        val glideMul = if(bloxdValue.get()) {glideSpeedValue.get().toDouble() / 0.42} else 1.toDouble()
*/

        event.x = 0.0
        event.y = glideSpeedValue.get().toDouble()/* * glideMul*/
        event.z = 0.0
        if (mc.gameSettings.keyBindJump.isKeyDown) {
            event.y += vspeedValue.get()/* * vMul*/
        }
        if (mc.gameSettings.keyBindSneak.isKeyDown) {
            event.y -= vspeedValue.get()/* * vMul*/
        }

        if(!MovementUtils.isMoving()) return


//        val speedMul = if(bloxdValue.get()) {speedValue.get().toDouble() / 0.5} else 1.toDouble()

        val yaw = MovementUtils.direction
        event.x = -sin(yaw) * speedValue.get()/* * speedMul*/
        event.z = cos(yaw) * speedValue.get()/* * speedMul*/

    }

}
