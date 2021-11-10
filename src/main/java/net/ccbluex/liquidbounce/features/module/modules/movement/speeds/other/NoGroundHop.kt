/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class NoGroundHop : SpeedMode("NoGroundHop") {
	private var legitJump = false
    override fun onPreMotion() {
    	if(!legitJump) mc.thePlayer.onGround = false
        if (MovementUtils.isMoving()) {
            if (!mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0,-0.01,0.0)).isEmpty()) {
                    if (legitJump) {
                        mc.thePlayer.jump()
                        legitJump = false
                        return
                    }else {
                    	mc.thePlayer.jump()
                    }

            }
        } else {
        	if (!mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(0.0,-0.01,0.0)).isEmpty()) legitJump = true
        }
    }
}
