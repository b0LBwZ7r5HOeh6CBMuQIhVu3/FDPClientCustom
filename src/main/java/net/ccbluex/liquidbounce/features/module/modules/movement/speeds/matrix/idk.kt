/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.matrix

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings

@Suppress("UnclearPrecedenceOfBinaryExpression")
class idk : SpeedMode("idk") {
    private var groundTick = 0


    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            mc.timer.timerSpeed = if (mc.thePlayer.motionY > 0) {
                1F
            } else {
                1F
            }
            when {
                mc.thePlayer.onGround -> {
                    if (groundTick >= 1) {
                        mc.gameSettings.keyBindJump.pressed = true
                        mc.timer.timerSpeed = 1.15F
                    }
                    groundTick++
                }
                else -> {
                    groundTick = 0
                    mc.gameSettings.keyBindJump.pressed = true
                    if (MovementUtils.getSpeed() < 0.19f) {
                        MovementUtils.strafe(0.19f)
                    }
                    MovementUtils.move(0.01f * 0.1f)
                    mc.thePlayer.motionY += 0f * 0.03
                }
            }
        }
    }


    override fun onEnable() {

    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        mc.thePlayer!!.speedInAir = 0.02f
    }
}