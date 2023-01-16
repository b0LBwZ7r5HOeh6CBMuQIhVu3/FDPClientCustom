/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "FastUse", category = ModuleCategory.PLAYER)
class FastUse : Module() {
    private val modeValue = ListValue(
        "Mode",
        arrayOf("Instant", "Timer", "CustomDelay", "DelayedInstant", "MinemoraTest", "AAC", "NewAAC", "StableAAC"),
        "DelayedInstant"
    )
    private val packetValue = ListValue("Packet", arrayOf("C03flying", "C04position", "C05look", "C06position_look"), "C04position")
    private val timerValue = FloatValue("Timer",1.22F,0.1F,2.0F).displayable { modeValue.equals("Timer") || modeValue.equals("CustomDelay") }
    private val durationValue =IntegerValue("InstantDelay", 14, 0, 35).displayable { modeValue.equals("DelayedInstant") }
    private val delayValue = IntegerValue("CustomDelay", 0, 0, 300).displayable { modeValue.equals("CustomDelay") }
    private val noMoveValue = BoolValue("NoMove", false)
    private val testValue = BoolValue("test", false)
    // private val bowValue = BoolValue("bow", false)

    private val msTimer = MSTimer()
    private var usedTimer = false
    private var c03s = 0
    private var c05s = 0
    private var yaw = 0.0f
    private var pitch = 0.0f
    private fun sendPacket(packet: String = "C03flying") {
        when (packet.get().lowercase()) {
            "c03flying" -> mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
            "c04position" -> mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.onGround))
            "c05look" -> mc.netHandler.addToSendQueue(C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
            "c06position_look" -> Pmc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround))
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (!mc.thePlayer.isUsingItem) {
            return
        }

        val usingItem = mc.thePlayer.itemInUse.item
        if (usingItem is ItemFood || usingItem is ItemBucketMilk || usingItem is ItemPotion/* || (usingItem is ItemBow && bowValue.get())*/) {
/*            if(usingItem is ItemBow && bowValue.get()){
            yaw = if (RotationUtils.targetRotation != null)
                RotationUtils.targetRotation.yaw
            else
                mc.thePlayer.rotationYaw

            pitch = if (RotationUtils.targetRotation != null)
                RotationUtils.targetRotation.pitch
            else
                mc.thePlayer.rotationPitch

            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0.0f, 0.0f, 0.0f))
            mc.thePlayer.itemInUseCount = mc.thePlayer.inventory.getCurrentItem().maxItemUseDuration - 1
            }*/
            when (modeValue.get().lowercase()) {
                "delayedinstant" -> if (mc.thePlayer.itemInUseDuration > durationValue.get()) {
                    repeat(36 - mc.thePlayer.itemInUseDuration) {
                        sendPacket(packetValue.get())
                    }

                    if (usingItem !is ItemBow) mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }

                "instant" -> {
                    repeat(35) {
                        sendPacket(packetValue.get())
                    }

                    if (usingItem !is ItemBow) mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }
                "aac" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    if (mc.thePlayer.itemInUseDuration > 14) {
                        repeat(23) {
                            sendPacket(packetValue.get())
                        }
                        if (usingItem !is ItemBow) mc.playerController.onStoppedUsingItem(mc.thePlayer)
                    }
                }
                "newaac" -> {
                    mc.timer.timerSpeed = 0.49F
                    usedTimer = true
                    repeat(2) {
                        sendPacket(packetValue.get())
                    }

                    // mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }
                "stableaac" -> {
                    mc.timer.timerSpeed = 0.5F
                    usedTimer = true
                    sendPacket(packetValue.get())
                    // mc.playerController.onStoppedUsingItem(mc.thePlayer)
                }
                "timer" -> {
                    mc.timer.timerSpeed = timerValue.get()
                    usedTimer = true
                }

                "minemoratest" -> {
                    mc.timer.timerSpeed = 0.5F
                    usedTimer = true
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        repeat(2) {
                            sendPacket(packetValue.get())
                        }
                    }
                }

                "customdelay" -> {
                    mc.timer.timerSpeed = timerValue.get()
                    usedTimer = true
                    if (!msTimer.hasTimePassed(delayValue.get().toLong())) {
                        return
                    }

                    sendPacket(packetValue.get())
                    msTimer.reset()
                }
            }
            if (c03s >= 40) {
                if (usingItem !is ItemBow) mc.playerController.onStoppedUsingItem(mc.thePlayer)
                c03s = 0
            }
/*            if(c05s >= 40){
                if(usingItem is ItemBow && bowValue.get()) mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, if (MovementUtils.isMoving()) BlockPos(-1, -1, -1) else BlockPos.ORIGIN, EnumFacing.DOWN))
                c05s = 0
            }*/
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        if (event == null) return

        if (!mc.thePlayer.isUsingItem || !noMoveValue.get()) return
        val usingItem1 = mc.thePlayer.itemInUse.item
        if ((usingItem1 is ItemFood || usingItem1 is ItemBucketMilk || usingItem1 is ItemPotion))
            event.zeroXZ()
    }

    @EventTarget
    fun onPacket(event: PacketEvent?) {
        if (event == null) return
        val packet = event.packet

        if (packet is C03PacketPlayer && testValue.get() && packet !is C05PacketPlayerLook && packet !is C04PacketPlayerPosition && packet !is C06PacketPlayerPosLook) {
            if (mc.thePlayer.isUsingItem) c03s++ else c03s = 0
        }
/*        if (packet is C05PacketPlayerLook && bowValue.get()){
            if(mc.thePlayer.isUsingItem) c05s++ else c05s = 0
        }*/
    }

    override fun onDisable() {
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    override val tag: String
        get() = modeValue.get()
}
