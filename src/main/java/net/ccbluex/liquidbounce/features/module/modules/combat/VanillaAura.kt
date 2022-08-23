/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.exploit

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumFacing
import scala.math.Ordering
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@ModuleInfo(name = "VanillaAura", category = ModuleCategory.EXPLOIT)
class VanillaAura : Module() {
    private val APSValue = IntegerValue("APS", 10, 1, 20)
    private val rangeValue = FloatValue("Range", 3.7f, 1f, 8f)
    private val autoBlockValue = BoolValue("AutoBlock", true)
    private val instantRotationValue = BoolValue("instantRotation", true)
    private val targetList = ArrayList<Entity>()
    private var blocked = false
    private val msTimer = MSTimer()

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.isPre()) {

        } else {
            if (autoBlockValue.get() && !blocked) {
                block()
            }
        }

    }

    private fun block() {
        mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem()))
        blocked = true
    }

    private fun unblock() {
        blocked = false
        mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        targetList.clear()
        mc.theWorld.loadedEntityList.forEach {
            if (EntityUtils.isSelected(it, true) && it != mc.thePlayer && it.getDistanceToEntity(mc.thePlayer) <= rangeValue.get()) {
                targetList.add(it)
            }
        }
        if (!msTimer.hasTimePassed(1000L / APSValue.get())) return
        msTimer.reset()
        targetList.forEach {
            if (it.getDistanceToEntity(mc.thePlayer) <= rangeValue.get()) {
                if (autoBlockValue.get()) {
                    unblock()
                }
                val rotation = RotationUtils.getJigsawRotations(it)
                if (instantRotationValue.get()) {
                    mc.netHandler.addToSendQueue(C05PacketPlayerLook(rotation.yaw, rotation.pitch, mc.thePlayer.onGround))
                } else {
                    RotationUtils.setTargetRotation(rotation, 1)
                }
                mc.netHandler.addToSendQueue(C0APacketAnimation())
                mc.netHandler.addToSendQueue(C02PacketUseEntity(it, C02PacketUseEntity.Action.ATTACK))
            }
            if (autoBlockValue.get()) {
                block()
            }

        }
    }
}