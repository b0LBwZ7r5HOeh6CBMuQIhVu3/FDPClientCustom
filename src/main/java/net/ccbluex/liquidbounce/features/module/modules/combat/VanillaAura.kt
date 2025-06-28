/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import kotlin.Int.Companion.MIN_VALUE
import kotlin.collections.ArrayList

@ModuleInfo(name = "VanillaAura", category = ModuleCategory.COMBAT)
class VanillaAura : Module() {
    private val APSValue = IntegerValue("APS", 10, 1, 20)
    private val rangeValue = FloatValue("Range", 3.7f, 1f, 8f)
    private val ignoreHurtResistantValue = IntegerValue("ignoreHurtResistant", 8, -1, 20)
        .displayable { !onlyCritHitValue.get() }
    private val onlyCritHitValue = BoolValue("OnlyCritHit", true)
    private val autoBlockValue = BoolValue("AutoBlock", true)
    private val fakeAutoBlockValue = BoolValue("FakeAutoBlock", true)
    private val autoUnblockValue = BoolValue("AutoUnblock", true).displayable { autoBlockValue.get() }
    private val RotationValue = BoolValue("Rotation", true)
    private val KeepRotationValue = IntegerValue("RotationKeepLen", 1, -1, 10).displayable { RotationValue.get() }
    private val RotationBackValue = BoolValue("RotationBack", true).displayable { RotationValue.get() }
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val targetList = ArrayList<Entity>()
    private var blocking = false

    fun shouldDisplayBlocking(): Boolean {
        return blocking || (fakeAutoBlockValue.get() && targetList.isNotEmpty())
    }

    private val msTimer = MSTimer()

    private var lastY = -99999.00
    private var willCritical = false

    @EventTarget
    fun onMotion(event: MotionEvent) {
//        if (event.isPre()) {
//
//        } else {
//            if (autoBlockValue.get() && !blocking) {
//                block()
//            }
//        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent){
        val packet = event.packet
        if (onlyCritHitValue.get() && packet is C03PacketPlayer /*outdated kotlin moment, as will cause a warning*/ && (packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook)) {
            willCritical = packet.y < lastY
            lastY = packet.y
        }
    }


    private fun block() {
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blocking = true
    }

    private fun unblock() {
        blocking = false
        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
    }

    private fun setRot(rot: Rotation){
        if (KeepRotationValue.get() >= 0) {
            RotationUtils.setTargetRotation(rot, KeepRotationValue.get())
        } else {
            mc.netHandler.addToSendQueue(C05PacketPlayerLook(rot.yaw, rot.pitch, mc.thePlayer.onGround))
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!msTimer.hasTimePassed(1000L / APSValue.get())) return
        if(onlyCritHitValue.get() && /*mc.thePlayer.motionY >= 0*/ willCritical) return
        msTimer.reset()
        targetList.clear()
        mc.theWorld.loadedEntityList.forEach {
            if (EntityUtils.isSelected(it, true) && it != mc.thePlayer && it.getDistanceToEntity(mc.thePlayer) <= rangeValue.get()) {
                block()
                targetList.add(it)
            }
        }
        targetList.forEach {
            if (it.getDistanceToEntity(mc.thePlayer) <= rangeValue.get()) {
                val event = AttackEvent(it)
                LiquidBounce.eventManager.callEvent(event)
                if (event.isCancelled) {
                    return
                }
                if(ignoreHurtResistantValue.get() >= 0 && it.hurtResistantTime <= ignoreHurtResistantValue.get()) return@forEach
                if (autoBlockValue.get() && autoUnblockValue.get()) {
                    unblock()
                }
                val rotation = RotationUtils.getJigsawRotations(it)
                if(RotationValue.get()) {
                    setRot(rotation)
                }
                if (swingValue.equals("packet")) {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                } else if (swingValue.equals("normal")) {
                    mc.thePlayer.swingItem()
                }

                mc.netHandler.addToSendQueue(C02PacketUseEntity(it, C02PacketUseEntity.Action.ATTACK))
            }
            if (autoBlockValue.get()) {
                block()
            }
        }
        if(targetList.isEmpty()) {
            unblock()
            if (RotationBackValue.get()) setRot(Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch))
        }
    }

    override fun onDisable() {
        unblock()
        targetList.clear()
    }
}