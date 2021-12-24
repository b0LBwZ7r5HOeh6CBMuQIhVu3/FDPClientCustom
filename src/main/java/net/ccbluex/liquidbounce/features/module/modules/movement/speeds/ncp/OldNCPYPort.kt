/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import kotlin.math.cos
import kotlin.math.sin

class OldNCPBHop : SpeedMode("OldNCPBHop") {
    override fun onEnable() {
        mc.timer.timerSpeed = 1.0866f
        super.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
                mc.thePlayer.motionY = 0.399
                val f = mc.thePlayer.rotationYaw * 0.017453292F;
                mc.thePlayer.motionX -= sin(f) * 0.089;
                mc.thePlayer.motionZ += cos(f) * 0.089;
            }else{
                if (mc.thePlayer.fallDistance <= 1) mc.thePlayer.motionY = -1
            }
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }
}