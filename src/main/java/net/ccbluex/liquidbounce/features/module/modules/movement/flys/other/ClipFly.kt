//  updated at : 2022/1/24.
//

/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.event.PacketEvent
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.BlockPos
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

class ClipFly : FlyMode("Clip") {
    private val xValue = FloatValue("${valuePrefix}-X", 5f, -5f, 5f)
    private val yValue = FloatValue("${valuePrefix}-Y", -2f, -5f, 5f)
    private val zValue = FloatValue("${valuePrefix}-Z", 5f, -5f, 5f)
    private val delayValue = IntegerValue("${valuePrefix}-Delay", 1000, 0, 3000)
    private val timerValue = FloatValue("${valuePrefix}-Timer", 0.7f, 0.2f, 1f)
    private val packetOnGroundValue = BoolValue("${valuePrefix}packetOnGround", true)
    private val markValue = BoolValue("${valuePrefix}mark", true)
    private val stopMoveValue = BoolValue("${valuePrefix}stopMove", true)
    private val stopMoveIIValue = BoolValue("${valuePrefix}stopMoveII", true)
    private val stopMoveIIIValue = BoolValue("${valuePrefix}stopMoveIII", true)

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

    override fun onRender3d(event: Render3DEvent) {
        val x = mc.thePlayer.posX + (-sin(yaw) * xValue.get())
        val y = mc.thePlayer.posY + yValue.get()
        val z = mc.thePlayer.posZ + (cos(yaw) * zValue.get())
        if (markValue.get()) {
            RenderUtils.drawBlockBox(BlockPos(x, y, z),
                if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()
                        .offset(x + 0.5, y, z + 0.5))
                        .isEmpty()) Color(255, 0, 0, 90) else Color(0, 255, 0, 90), false, true, 1f)
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.onGround = false
        mc.timer.timerSpeed = timerValue.get()
        if (stopMoveValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        if (stopMoveIIValue.get()) {
            mc.thePlayer.isInWeb = true
            }
            if (stopMoveIIIValue.get()) {
                mc.thePlayer.speedOnGround = 0.0f
                mc.thePlayer.speedInAir = 0.0f
            }


        if (timer.hasTimePassed(delayValue.get().toLong())) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.thePlayer.setPosition(mc.thePlayer.posX + (-sin(yaw) * xValue.get()), mc.thePlayer.posY + yValue.get(), mc.thePlayer.posZ + (cos(yaw) * zValue.get()))
            timer.reset()
        }
        mc.thePlayer.jumpMovementFactor = 0.00f
    }
}
