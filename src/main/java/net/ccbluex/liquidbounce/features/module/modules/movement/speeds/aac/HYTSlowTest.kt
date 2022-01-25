/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.aac

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class HYTSlowTest : SpeedMode("HYTSlowTest") {
    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.gameSettings.keyBindJump.pressed = true
            MovementUtils.strafe(MovementUtils.getSpeed() * 1.0114514f)
        }
    }
}