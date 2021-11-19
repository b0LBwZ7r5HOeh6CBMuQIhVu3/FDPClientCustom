package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacketNoEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB

class AAC442VanillaFly : FlyMode("AAC4.4.2-Vanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 2f, 0f, 5f)
    private val fastStopValue = BoolValue("${valuePrefix}FastStop", true)

    private val packets = mutableListOf<C03PacketPlayer>()
    private val timer = MSTimer()
    private var enabledTicks = 0
    private var flyClip = false
    private var flyStart = false

    override fun onEnable() {

    }

    override fun onDisable() {

    }

    override fun onUpdate(event: UpdateEvent) {
        enabledTicks++
        if (enabledTicks == 3) {
           LiquidBounce.hud.addNotification(Notification("Fly", "Enabled Final Dad! You can fly freely until you disable fly", NotifyType.SUCCESS, 2000, 500))
        }
        if (enabledTicks <= 2) {
            mc.timer.timerSpeed = 0.3f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.05
            mc.thePlayer.motionZ = 0.0
            return;
            }
        mc.timer.timerSpeed = 1.0f
        mc.thePlayer.capabilities.isFlying = true
        mc.thePlayer.capabilities.flySpeed = speedValue.get()
        if(fastStopValue.get() && !MovementUtils.isMoving()){
            mc.thePlayer.motionZ *= 0.6
            mc.thePlayer.motionX *= 0.6
        }
        if (enabledTicks % 2 != 0) {
            return;
        }
        sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY,mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
        sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY+50,mc.thePlayer.posZ, true))
    }

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }else if (packet is S08PacketPlayerPosLook) {
            val x = packet.x - mc.thePlayer.posX
            val y = packet.y - mc.thePlayer.posY
            val z = packet.z - mc.thePlayer.posZ
            val diff = sqrt(x * x + y * y + z * z)
            if (diff <= 8) {
                event.cancelEvent()
                sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.getYaw(), packet.getPitch(), true))
            } else {
                event.cancelEvent()
                mc.thePlayer.setPositionAndRotation(packet.x, packet.y, packet.z, packet.getYaw(), packet.getPitch())
                LiquidBounce.hud.addNotification(Notification("Fly", "You got lag backed! Please land on the ground if it keeps happening.", NotifyType.WARNING, 2000, 500))
            }
        }
    }
}