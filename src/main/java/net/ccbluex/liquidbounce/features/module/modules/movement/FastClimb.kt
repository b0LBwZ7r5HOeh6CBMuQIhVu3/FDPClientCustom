/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.block.BlockUtils.collideBlockIntersects
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockVine
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "FastClimb", category = ModuleCategory.MOVEMENT)
object FastClimb : Module() {

    val mode = ListValue(
        "Mode",
        arrayOf("Vanilla", "Delay", "Clip", "AAC3.0.0", "AAC3.0.5", "SAAC3.1.2", "AAC3.1.2"), "Vanilla"
    )
    private val speed = FloatValue("Vanilla-Speed", 1F, 0.01F,5F)

    private val climbSpeed = FloatValue("Delay-ClimbSpeed", 1F, 0.01F,5F)
    private val tickDelay = IntegerValue("Delay-TickDelay", 10, 1, 20)


    private val climbDelay = tickDelay
    private var climbCount = 0

    private fun playerClimb() {
        mc.thePlayer.motionY = 0.0
        mc.thePlayer.isInWeb = true
        mc.thePlayer.onGround = true

        mc.thePlayer.isInWeb = false
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val mode = mode

        val thePlayer = mc.thePlayer ?: return

        when {
            mode.get() == "Vanilla" && thePlayer.isCollidedHorizontally && thePlayer.isOnLadder -> {
                event.y = speed.get().toDouble()
                thePlayer.motionY = 0.0
            }

            mode.get() == "Delay" && thePlayer.isCollidedHorizontally && thePlayer.isOnLadder -> {

                if (climbCount >= climbDelay.get()) {

                    event.y = climbSpeed.get().toDouble()
                    playerClimb()

                    val currentPos =
                        C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, true)

                    mc.netHandler.addToSendQueue(currentPos)

                    climbCount = 0

                } else {
                    thePlayer.posY = thePlayer.prevPosY

                    playerClimb()
                    climbCount += 1

                }
            }


            mode.get() == "AAC3.0.0" && thePlayer.isCollidedHorizontally -> {
                var x = 0.0
                var z = 0.0

                when (thePlayer.horizontalFacing) {
                    EnumFacing.NORTH -> z = -0.99
                    EnumFacing.EAST -> x = 0.99
                    EnumFacing.SOUTH -> z = 0.99
                    EnumFacing.WEST -> x = -0.99
                    else -> {}
                }

                val block = BlockPos(thePlayer.posX + x, thePlayer.posY, thePlayer.posZ + z).getBlock()

                if (block is BlockLadder || block is BlockVine) {
                    event.y = 0.5
                    thePlayer.motionY = 0.0
                }
            }

            mode.get() == "AAC3.0.5" && mc.gameSettings.keyBindForward.isKeyDown &&
                    collideBlockIntersects(thePlayer.entityBoundingBox) {
                        it is BlockLadder || it is BlockVine
                    } -> {
                event.x = 0.0
                event.y = 0.5
                event.z = 0.0

                thePlayer.motionX = 0.0
                thePlayer.motionY = 0.0
                thePlayer.motionZ = 0.0
            }

            mode.get() == "SAAC3.1.2" && thePlayer.isCollidedHorizontally &&
                    thePlayer.isOnLadder -> {
                event.y = 0.1649
                thePlayer.motionY = 0.0
            }

            mode.get() == "AAC3.1.2" && thePlayer.isCollidedHorizontally &&
                    thePlayer.isOnLadder -> {
                event.y = 0.1699
                thePlayer.motionY = 0.0
            }

            mode.get() == "Clip" && thePlayer.isOnLadder && mc.gameSettings.keyBindForward.isKeyDown -> {
                for (i in thePlayer.posY.toInt()..thePlayer.posY.toInt() + 8) {
                    val block = BlockPos(thePlayer.posX, i.toDouble(), thePlayer.posZ).getBlock()

                    if (block !is BlockLadder) {
                        var x = 0.0
                        var z = 0.0

                        when (thePlayer.horizontalFacing) {
                            EnumFacing.NORTH -> z = -1.0
                            EnumFacing.EAST -> x = 1.0
                            EnumFacing.SOUTH -> z = 1.0
                            EnumFacing.WEST -> x = -1.0
                            else -> {}
                        }

                        thePlayer.setPosition(thePlayer.posX + x, i.toDouble(), thePlayer.posZ + z)
                        break
                    } else {
                        thePlayer.setPosition(thePlayer.posX, i.toDouble(), thePlayer.posZ)
                    }
                }
            }
        }
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        if (mc.thePlayer != null && (event.block is BlockLadder || event.block is BlockVine) &&
            mode.get() == "AAC3.0.5" && mc.thePlayer.isOnLadder
        )
            event.boundingBox = null
    }

    override val tag: String
        get() = mode.get()
}