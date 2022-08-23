/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.ncp

import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.minecraft.client.Minecraft
import net.minecraft.potion.Potion
import net.minecraft.util.MovementInput
import java.math.BigDecimal
import java.math.RoundingMode

class OldNCP2 : SpeedMode("OldNCP2") {
    private val moveSpeedVanilla = 0.0
    private var stage = 0
    private var moveSpeed = 0.0
    private val lastDist = 0.0

    override fun onEnable() {

    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f

    }

    override fun onMove(event: MoveEvent) {
        if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
            return
        }
        val movementInput: MovementInput = mc.thePlayer.movementInput
        var forward = movementInput.moveForward
        var strafe = movementInput.moveStrafe
        var yaw: Float = Minecraft.getMinecraft().thePlayer.rotationYaw
        if (this.stage === 1 && (mc.thePlayer.moveForward !== 0.0f || mc.thePlayer.moveStrafing !== 0.0f)) {
            this.stage = 2
            this.moveSpeed = 1.38 * baseMoveSpeed - 0.01
        } else if (this.stage === 2) {
            this.stage = 3
            event.y = 0.399399995803833
            this.moveSpeed *= 2.149
        } else if (this.stage === 3) {
            this.stage = 4
            val difference: Double = 0.66 * (this.lastDist - baseMoveSpeed)
            this.moveSpeed = this.lastDist - difference
            if ((forward != 0.0f || strafe != 0.0f) && !mc.gameSettings.keyBindJump.pressed) {
                event.y = -0.4
                // mc.thePlayer.motionY = -1;
                stage = 2
            }
        } else {
            if (mc.theWorld.getCollisionBoxes(
                    mc.thePlayer.collisionBoundingBox.offset(0.0, mc.thePlayer.motionY, 0.0)
                ).size > 0
                || mc.thePlayer.isCollidedVertically
            ) {
                this.stage = 1
            }
            this.moveSpeed = this.lastDist - this.lastDist / 159.0
        }

        this.moveSpeed = Math.max(this.moveSpeed, baseMoveSpeed)
        if (forward == 0.0f && strafe == 0.0f) {
            event.x = 0.0
            event.z = 0.0
        } else if (forward != 0.0f) {
            if (strafe >= 1.0f) {
                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
                strafe = 0.0f
            } else if (strafe <= -1.0f) {
                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
                strafe = 0.0f
            }
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        val mx = Math.cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val mz = Math.sin(Math.toRadians((yaw + 90.0f).toDouble()))
        event.x = forward * this.moveSpeed * mx + strafe * this.moveSpeed * mz
        event.z = forward * this.moveSpeed * mz - strafe * this.moveSpeed * mx
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