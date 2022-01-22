/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.movement.flys.aac

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import java.awt.Color

class AAC1910Fly : FlyMode("AAC1.9.10") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 0.3f, 0f, 1f)
    private val motionYValue = FloatValue("${valuePrefix}motionY", 0.42f, 0f, 1f)
    // private val c03packetsValue = FloatValue("${valuePrefix}c03packets", 0.3f, 0f, 1f)

    private var aacJump = 0.0

    override fun onUpdate(event: UpdateEvent) {
        if (mc.gameSettings.keyBindJump.isKeyDown) aacJump += 0.2

        if (mc.gameSettings.keyBindSneak.isKeyDown) aacJump -= 0.2

        if (fly.launchY + aacJump > mc.thePlayer.posY) {
            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
            mc.thePlayer.motionY = motionYValue.get().toDouble()
            MovementUtils.strafe(speedValue.get())
        }

        MovementUtils.strafe()
    }
/*    override fun onRender3d(event: Render3DEvent) {
        RenderUtils.drawPlatform(fly.launchY + aacJump, Color(0, 0, 255, 90), 1)
    }*/
}