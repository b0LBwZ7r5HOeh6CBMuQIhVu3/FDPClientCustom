/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.minecraft.potion.Potion
import net.minecraft.util.MathHelper
import net.minecraft.util.MovementInput
import java.math.BigDecimal
import java.math.RoundingMode


class OldNCP3 : SpeedMode("OldNCP3") {
    private val moveSpeedVanilla = 0.0
    private var stage = 0
    private var moveSpeed = 0.0
    private val lastDist = 0.0

    override fun onEnable() {

    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f

    }

    override fun onUpdate() {
        val input: MovementInput = mc.thePlayer.movementInput
        if (mc.thePlayer.isInWater() || mc.thePlayer.isCollidedHorizontally || mc.thePlayer.movementInput.sneak || input.moveForward <= 0) {
            return
        }
        mc.gameSettings.keyBindJump.pressed = false
//            mc.timer.timerSpeed = 1.095F; //TODO fix
        //            mc.timer.timerSpeed = 1.095F; //TODO fix
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionY = 0.399
            val f: Float = mc.thePlayer.rotationYaw * 0.017453292f
            mc.thePlayer.motionX -= (MathHelper.sin(f) * 0.089)
            mc.thePlayer.motionZ += (MathHelper.cos(f) * 0.089)
        } else {
            if (mc.thePlayer.fallDistance <= 0.5) {
                mc.thePlayer.motionY = -1.0
            }
            return
        }
    }


    private val baseMoveSpeed: Double
        get() {
            var baseSpeed = 0.2873
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) baseSpeed *= 1.0 + 0.2 * (mc.thePlayer.getActivePotionEffect(
                Potion.moveSpeed
            ).amplifier + 1)
            return baseSpeed
        }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }
}