package net.ccbluex.liquidbounce.features.module.modules.movement.flys.verus

import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.PostTickEvent
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.C00PacketKeepAlive
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3


class OldHYTVanilla : FlyMode("OldHYTVanilla") {
    private val speedValue = FloatValue("${valuePrefix}speed", 0.5f, 0.3f, 3f)
    private val disablerValue = BoolValue("${valuePrefix}doDisabler", false)
    private val bypassKickValue = BoolValue("${valuePrefix}BypassKick", false)
    private val flyTimer = MSTimer()
    var oldX = 0.0
    var oldY = 0.0
    var oldZ = 0.0
    var disable: Long = 0
    var oldYaw = 0f
    var lastFlag = 0
    var oldPitch = 0f
    var player: EntityOtherPlayerMP? = null
    var enabledTicks = 0
    var floatingTickCount = 0
    override fun onEnable() {
        flyTimer.reset()
        lastFlag = 0
        oldX = mc.thePlayer.posX
        oldY = mc.thePlayer.posY
        oldZ = mc.thePlayer.posZ
        oldYaw = mc.thePlayer.rotationYaw
        oldPitch = mc.thePlayer.rotationPitch
        EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile).also { player = it }.copyLocationAndAnglesFrom(mc.thePlayer)
        player!!.rotationYawHead = mc.thePlayer.rotationYawHead
        player!!.renderYawOffset = mc.thePlayer.renderYawOffset
        mc.thePlayer.noClip = true
        enabledTicks = 0
    }

    override fun onDisable() {

        mc.thePlayer.setPosition(oldX, oldY, oldZ)
        mc.thePlayer.rotationYaw = oldYaw
        mc.thePlayer.rotationPitch = oldPitch

        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        player = null
        mc.timer.timerSpeed = 1.0.toFloat()
        mc.thePlayer.capabilities.allowFlying = false
        mc.thePlayer.capabilities.setFlySpeed(0.05f)
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.speedInAir = 0.02f
    }

    override fun onPacket(event: PacketEvent) {
        val p: Packet<*> = event.getPacket() as Packet<*>
        if (event.getPacket() is S12PacketEntityVelocity) {
            event.setCanceled(true)
            lastFlag = enabledTicks
        }
        if (event.getPacket() is S08PacketPlayerPosLook) {
            oldX = (event.getPacket() as S08PacketPlayerPosLook).x
            oldY = (event.getPacket() as S08PacketPlayerPosLook).y
            oldZ = (event.getPacket() as S08PacketPlayerPosLook).z
            oldYaw = (event.getPacket() as S08PacketPlayerPosLook).yaw
            oldPitch = (event.getPacket() as S08PacketPlayerPosLook).pitch
            player!!.posX = oldX
            player!!.posY = oldY
            player!!.posZ = oldZ
            player!!.rotationYaw = oldYaw
            player!!.rotationPitch = oldPitch
            event.setCanceled(true)
            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(oldX, oldY, oldZ, oldYaw, oldPitch, true))
        }
        if (p is C00PacketKeepAlive || (p is C03PacketPlayer && p !is C04PacketPlayerPosition && p !is C05PacketPlayerLook && p !is C06PacketPlayerPosLook)) {
            event.setCanceled(true)
        }
        if (p is C03PacketPlayer) {
            floatingTickCount = if (mc.thePlayer.onGround) 0 else floatingTickCount + 1
        }
    }

    override fun onTick(event: TickEvent) {
        enabledTicks++;
    }

    override fun onPostTick(event: PostTickEvent) {
//        val timer: DoubleSetting = ModulesManager.getModuleByClass(Fly::class.java).timer
//        val enabledTicks: Int = ModulesManager.getModuleByClass(Fly::class.java).enabledTicks
        if (disablerValue.get()) {
            if (enabledTicks == 3) {
//            Client.notify(PopupMessage("Final Dad", "Enabled Final Dad! You can fly freely until you disable fly", ColorUtil.NotificationColors.GREEN, 40))
            }
            if (enabledTicks <= 2) {
                mc.timer.timerSpeed = 0.7F
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionY = 0.05
                mc.thePlayer.motionZ = 0.0
                mc.thePlayer.speedOnGround = 0.0f
                mc.thePlayer.speedInAir = 0.0f
                return
            }
            mc.timer.timerSpeed = 0.7F
            if (enabledTicks % 2 != 0) {
                return
            }
            if (mc.thePlayer.posY - player!!.posY > 10.0) {
                if (enabledTicks % 20 == 0) {
                    mc.thePlayer.setLocationAndAngles(player!!.posX, player!!.posY, player!!.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
//                Client.notify(PopupMessage("Final Dad Exploit", "You got lag backed! Please land on the ground if it keeps happening.", ColorUtil.NotificationColors.RED, 40))
                }
                return
            }
            if (enabledTicks % 10 == 0 && (enabledTicks - lastFlag >= 6 || mc.thePlayer.posY - player!!.posY > 4.0) && mc.thePlayer.getDistanceToEntity(player) >= 12.0f) {
                mc.thePlayer.setLocationAndAngles(player!!.posX, player!!.posY, player!!.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
//            Client.notify(PopupMessage("Final Dad Exploit", "You got lag backed! Please land on the ground if it keeps happening.", ColorUtil.NotificationColors.RED, 40))
            }
            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, true))
            PacketUtils.sendPacketNoEvent(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 50.0, mc.thePlayer.posZ, true))
        }
        mc.thePlayer.fallDistance = 0.0f
//        val speed: Double = ModulesManager.getModuleByClass(Fly::class.java).stableFlySpeed.getValue()
        mc.thePlayer.motionX = 0.0
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.motionZ = 0.0
        if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        MovementUtils.strafe(speedValue.get())
        val axisalignedbb2: AxisAlignedBB = mc.thePlayer.getEntityBoundingBox().expand(0.0625, 0.0625, 0.0625).addCoord(0.0, -0.55, 0.0)
        if (!mc.theWorld.checkBlockCollision(axisalignedbb2)) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 0.05, mc.thePlayer.posZ)
        }
        if (floatingTickCount < 60.0) {
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.thePlayer.motionY = speedValue.get() / 1.399999976158142
                MovementUtils.strafe(speedValue.get() / 1.399999976158142F)
            } else if (mc.thePlayer.isSneaking()) {
                mc.thePlayer.motionY = speedValue.get() / 1.399999976158142
                MovementUtils.strafe(speedValue.get() / 1.399999976158142F)
            }
        } else if (mc.thePlayer.isSneaking()) {
            mc.thePlayer.motionY = speedValue.get() / 1.399999976158142
            MovementUtils.strafe(speedValue.get() / 1.399999976158142F)
            MovementUtils.handleVanillaKickBypass()
            MovementUtils.handleVanillaKickBypass()
            floatingTickCount = 1
        }
    }

}