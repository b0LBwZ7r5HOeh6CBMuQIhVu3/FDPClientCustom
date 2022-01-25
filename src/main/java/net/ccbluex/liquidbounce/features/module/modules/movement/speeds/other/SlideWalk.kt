/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class SlideWalk : SpeedMode("SlideWalk") {
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.thePlayer.onGround = false
        } else {
        	if (!mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0,-0.01,0.0)).isEmpty()) mc.thePlayer.onGround = true
        }
    }
}
