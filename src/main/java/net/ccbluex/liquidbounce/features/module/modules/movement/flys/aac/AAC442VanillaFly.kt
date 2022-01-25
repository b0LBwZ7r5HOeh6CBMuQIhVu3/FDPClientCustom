//  updated at : 2022/1/24.
//

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
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.client.S12PacketEntityVelocity
import net.minecraft.util.AxisAlignedBB
import kotlin.math.sqrt

class AAC442VanillaFly : FlyMode("AAC4.4.2-Vanilla") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.033f, 0f, 1f)
    private val fastStopValue = BoolValue("${valuePrefix}FastStop", true)
    private val flagCheckValue = BoolValue("${valuePrefix}flagCheck", true)

    private val packets = mutableListOf<C03PacketPlayer>()
    private val timer = MSTimer()
    private var enabledTicks = 0
    private var lastFlag = 0
    private var oldX = 0.0
    private var oldY = 0.0
    private var oldZ = 0.0
    private var oldPitch = 0f
    private var oldYaw = 0f
    private var player: EntityOtherPlayerMP? = null

    override fun onEnable() {
        enabledTicks = 0
        lastFlag = 0
        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        oldYaw = mc.thePlayer.rotationYaw
        oldPitch = mc.thePlayer.rotationPitch
        player = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        player!!.copyLocationAndAnglesFrom(mc.thePlayer)
        player.rotationYawHead = mc.thePlayer.rotationYawHead
        player.renderYawOffset = mc.thePlayer.renderYawOffset
        mc.thePlayer.noClip = true
    }

    override fun onDisable() {
        enabledTicks = 0
        mc.thePlayer.setPosition(oldX, oldY, oldZ)
        mc.thePlayer.rotationYaw = oldYaw
        mc.thePlayer.rotationPitch = oldPitch
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = -0.4
        mc.thePlayer.motionZ = 0.0
        player = null
    }

    override fun onUpdate(event: UpdateEvent) {
        enabledTicks++
        if (enabledTicks == 3) {
            LiquidBounce.hud.addNotification(Notification("Final Dad", "Enabled Final Dad! You can fly freely until you disable fly", NotifyType.SUCCESS, 2000, 500))
        }
        if (enabledTicks <= 2) {
            mc.timer.timerSpeed = 0.3f
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.05
            mc.thePlayer.motionZ = 0.0
            LiquidBounce.hud.addNotification(Notification("Final Dad", "Enabling Final dad... Please wait..", NotifyType.INFO, 200, 500))
            return
        }
        mc.timer.timerSpeed = 1.0f
        mc.thePlayer.capabilities.isFlying = true
        mc.thePlayer.capabilities.flySpeed = speedValue.get()
        mc.thePlayer.setSprinting(true)
        mc.thePlayer.onGround = false
        mc.thePlayer.capabilities.allowFlying = true
        mc.thePlayer.noClip = true
        if (fastStopValue.get() && !MovementUtils.isMoving()) {
            mc.thePlayer.motionZ *= 0.8
            mc.thePlayer.motionX *= 0.8
        }
        if (enabledTicks % 2 != 0) {
            return
        }
        if (mc.thePlayer.posY - player.posY > 10.0) {
            if (enabledTicks % 20 == 0) {
                mc.thePlayer.setPositionAndRotation(player.posX, player.posY, player.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
                LiquidBounce.hud.addNotification(Notification("Fly", "You got lag backed! Please land on the ground if it keeps happening.", NotifyType.WARNING, 2000, 500))
            }
            return
        }
        if (enabledTicks % 10 == 0 && (enabledTicks - lastFlag >= 6 || mc.thePlayer.posY - player.posY > 4.0) && mc.thePlayer.getDistanceToEntity(player) >= 12.0f) {
            mc.thePlayer.setPositionAndRotation(player.posX, player.posY, player.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
            LiquidBounce.hud.addNotification(Notification("Fly", "You got lag backed! Please land on the ground if it keeps happening.", NotifyType.WARNING, 2000, 500))
        }
    }

    sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY,mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
    sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY+50,mc.thePlayer.posZ, true))
}

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is C03PacketPlayer) {
            event.cancelEvent()
        } else if (packet is S08PacketPlayerPosLook) {
            oldX = packet.getX()
            oldY = packet.getY()
            oldZ = packet.getZ()
            oldYaw = packet.getYaw()
            oldPitch = packet.getPitch()
            player.posX = oldX
            player.posY = oldY
            player.posZ = oldZ
            player.rotationYaw = oldYaw
            player.rotationPitch = oldPitch
            event.cancelEvent()
            sendPacketNoEvent(C06PacketPlayerPosLook(packet.x, packet.y, packet.z, packet.getYaw(), packet.getPitch(), true))
        } else if (packet is S12PacketEntityVelocity) {
            if (!flagCheckValue.get() || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) == mc.thePlayer) {
                lastFlag = enabledTicks
                event.cancelEvent()
            }
        }
    }
}