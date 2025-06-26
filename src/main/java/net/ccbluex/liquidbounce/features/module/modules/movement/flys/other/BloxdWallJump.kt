package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.features.module.modules.world.BloxdPhysics
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MinecraftInstance.mc
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.lowercase

class BloxdWallJump : FlyMode("BloxdWallJump") {
    private val mode = ListValue("${valuePrefix}Mode", arrayOf("Stable", "Stable2", "1", "Custom"), "Stable")
    private val speedBoost = FloatValue("${valuePrefix}BoostSpeed", 0.5f, 0f, 3f)
    private val timer = FloatValue("${valuePrefix}Timer", 1.0f, 0f, 2f)
    private val boostTicks = IntegerValue("${valuePrefix}BoostTicks", 27, 10, 40)
    private val jumpForce = FloatValue("${valuePrefix}JumpForce", 8f, 4f, 15f)
    private val showHitMessage = BoolValue("${valuePrefix}ShowHitMessage", true)
    private val randomize = BoolValue("${valuePrefix}Randomize", true)
    private val randomAmount = IntegerValue("${valuePrefix}RandomAmount", 1, 0, 30).displayable { randomize.get() }

    private var wallJumpState = false
    private var tick = 0

    override fun onEnable() {
        wallJumpState = false
        tick = 0
        if (showHitMessage.get()) {
            ClientUtils.displayChatMessage("§7[§b§lBloxdWallJump§7] §fA horizontal collision with a wall that height is two blocks or higher is required to bypass.")
        }
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!wallJumpState) {
            val player = mc.thePlayer
            val world = mc.theWorld

            val yaw = Math.toRadians(player.rotationYaw.toDouble())
            val blockPos1 = player.position.add(-sin(yaw) * 0.5, 0.0, cos(yaw) * 0.5)
            val blockPos2 = blockPos1.up()

            if (!world.getBlockState(blockPos1).block.isTranslucent &&
                !world.getBlockState(blockPos2).block.isTranslucent) {
                wallJumpState = true
            }
        }

        if (wallJumpState) {
            val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
            mc.timer.timerSpeed = timer.get()

            when (mode.get().lowercase()) {
                "stable", "stable2" -> {
                    BloxdPhysics.PhysicsBody.impulseVector.add(
                        BloxdPhysics.Vec3(
                            -sin(yaw).toFloat() * 0.416f,
                            if (mode.equals("stable")) jumpForce.get() else 0f,
                            cos(yaw).toFloat() * 0.416f
                        )
                    )

                    if (tick++ >= if (mode.equals("stable")) 27 else 30) {
                        resetState()
                    }
                }
                "custom" -> {
                    val randomFactor = if (randomize.get()) (Math.random() * randomAmount.get() * 0.01).toFloat() else 0f
                    val baseForce = 0.3f + (speedBoost.get() / 10f) + randomFactor

                    BloxdPhysics.PhysicsBody.impulseVector.add(
                        BloxdPhysics.Vec3(
                            -sin(yaw).toFloat() * baseForce,
                            jumpForce.get(),
                            cos(yaw).toFloat() * baseForce
                        )
                    )

                    if (tick++ >= boostTicks.get()) {
                        resetState()
                    }
                }

                "1" -> {
                    if (mc.thePlayer.isCollidedHorizontally) {
                        BloxdPhysics.PhysicsBody.impulseVector.add(
                            BloxdPhysics.Vec3(
                                0f,
                                jumpForce.get(),
                                0f
                            )
                        )
                        mc.thePlayer.onGround = true
                    }
                }
            }
        }
    }

    private fun resetState() {
        mc.timer.timerSpeed = 1.0f
        wallJumpState = false
        tick = 0
    }

    override fun onDisable() {
        mc.timer.timerSpeed = 1f
        BloxdPhysics.PhysicsBody.impulseVector.set(0f, 0f, 0f)
    }
}
