/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.potion.Potion

class NCPForkHopTest : SpeedMode("NCPForkHopTest") {
    override fun onEnable() {
//        mc.timer.timerSpeed = 1.0866f
        super.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onPreMotion() {
        mc.gameSettings.keyBindJump.pressed = false
        if (MovementUtils.isMoving()) {
            if (MovementUtils.isOnGround(0.01) && mc.thePlayer.isCollidedVertically) {
                MovementUtils.setMotion(Math.max(0.275, MovementUtils.getBaseMoveSpeed() * 0.9))
                mc.thePlayer.jump()
                MovementUtils.setMotion(Math.max(0.38, MovementUtils.getBaseMoveSpeed() * 0.9))
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    var speed = if (MovementUtils.getSpeed() < 0.38f) MovementUtils.getSpeed() else 0.38f
                    if (mc.thePlayer.onGround && mc.thePlayer.isPotionActive(Potion.moveSpeed)) speed *= 1f + 0.1f * (1 + mc.thePlayer.getActivePotionEffect(
                        Potion.moveSpeed
                    ).amplifier)
                    MovementUtils.strafe(speed)
                    return
                } else if (mc.thePlayer.motionY < 0.2) mc.thePlayer.motionY -= 0.02
                MovementUtils.strafe(0.38f)
            }
        }else if (!MovementUtils.isMoving()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        super.onPreMotion()
    } 
}