/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.player.AutoTool
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.extensions.getBlock
import net.ccbluex.liquidbounce.utils.extensions.getEyeVec3
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.block.Block
import net.minecraft.block.BlockAir
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import kotlin.math.sqrt
import java.awt.Color
import net.minecraft.util.MathHelper

@ModuleInfo(name = "Fucker", category = ModuleCategory.WORLD)
object Fucker : Module() {

    /**
     * SETTINGS
     */

    private val blockValue = BlockValue("Block", 26)
    private val swingValue = ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")
    private val throughWallsValue = ListValue("ThroughWalls", arrayOf("None", "Raycast", "Around"), "None")
    private val rangeValue = FloatValue("Range", 5F, 1F, 7F)
    private val actionValue = ListValue("Action", arrayOf("Destroy", "Use"), "Destroy")
    private val instantValue = BoolValue("Instant", false)
    private val switchValue = IntegerValue("SwitchDelay", 250, 0, 1000)
    private val rotationsValue = BoolValue("Rotations", true)
    private val otherRatationsValue = BoolValue("otherRatations", false)
    private val surroundingsValue = BoolValue("Surroundings", true)
    private val noHitValue = BoolValue("NoHit", false)
    private val bypassValue = BoolValue("Bypass", false)
    private val autoToolValue = BoolValue("AutoTool", false)
    private val matrixValue = BoolValue("Matrix", false)
    private val teamsValue = BoolValue("Teams", false)
    private val rotationStrafeValue = BoolValue("rotationStrafe", true)
    private val delayValue = BoolValue("Delay", false)
    private val delayTimeValue = IntegerValue("DelayTime", 10000, 0, 30000)

    /**
     * VALUES
     */

    private var pos: BlockPos? = null
    private var oldPos: BlockPos? = null
    private var teamPos: BlockPos? = null
    private var blockHitDelay = 0
    private var breaking = false
    private val switchTimer = MSTimer()
    private val delayTimer = MSTimer()
    private var isRealBlock = false
    var currentDamage = 0F

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(breaking){
            breaking = false
        }
        val targetId = blockValue.get()
        if(teamPos == null && teamsValue.get()){
            teamPos = find(targetId)
            return
        }
        if(BlockUtils.getCenterDistance(teamPos?:BlockPos(0,0,0)) < rangeValue.get()+3 && teamsValue.get()){
            return
        }
        if(delayValue.get() && !delayTimer.hasTimePassed(delayTimeValue.get().toLong()) ){
            return
        }
        if (noHitValue.get()) {
            val killAura = LiquidBounce.moduleManager[KillAura::class.java]!!

            if (killAura.state && killAura.target != null) {
                return
            }
        }

        

        if (pos == null || Block.getIdFromBlock(BlockUtils.getBlock(pos)) != targetId ||
            BlockUtils.getCenterDistance(pos!!) > rangeValue.get()) {
            pos = find(targetId)
        }

        // Reset current breaking when there is no target block
        if (pos == null) {
            currentDamage = 0F
            return
        }

        var currentPos = pos ?: return
        var rotations = RotationUtils.faceBlock(currentPos) ?: return

        // Surroundings
        var surroundings = false

        if (surroundingsValue.get()) {
            val eyes = mc.thePlayer.getPositionEyes(1F)
            val blockPos = mc.theWorld.rayTraceBlocks(eyes, rotations.vec, false,
                    false, true).blockPos

            if (blockPos != null && blockPos.getBlock() !is BlockAir) {
                if (currentPos.x != blockPos.x || currentPos.y != blockPos.y || currentPos.z != blockPos.z) {
                    surroundings = true
                }

                pos = blockPos
                currentPos = pos ?: return
                rotations = RotationUtils.faceBlock(currentPos) ?: return
            }
        }

        // Reset switch timer when position changed
        if (oldPos != null && oldPos != currentPos) {
            currentDamage = 0F
            switchTimer.reset()
        }

        oldPos = currentPos

        if (!switchTimer.hasTimePassed(switchValue.get().toLong())) {
            return
        }

        // Block hit delay
        if (blockHitDelay > 0) {
            blockHitDelay--
            return
        }
        breaking = true
        // Face block
        if (rotationsValue.get()) {
            RotationUtils.setTargetRotation(rotations.rotation)
        }

        when {
            // Destory block
            actionValue.equals("destroy") || surroundings || !isRealBlock -> {
                mc.thePlayer.isSprinting = false
                // Auto Tool
                val autoTool = LiquidBounce.moduleManager[AutoTool::class.java]!!
                if (autoTool.state && autoToolValue.get()) {
                    autoTool.switchSlot(currentPos)
                }
                if(matrixValue.get()) mc.gameSettings.keyBindUseItem.pressed = true
                // Break block
                if (instantValue.get()) {
                    // CivBreak style block breaking
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    if (swingValue.equals("Normal")) {
                        mc.thePlayer.swingItem()
                    } else if (swingValue.equals("Packet")) {
                        mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    currentDamage = 0F
                    return
                }

                // Minecraft block breaking
                val block = currentPos.getBlock() ?: return

                if (currentDamage == 0F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))

                    if (mc.thePlayer.capabilities.isCreativeMode ||
                            block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, pos) >= 1.0F) {
                        if (swingValue.equals("Normal")) {
                            mc.thePlayer.swingItem()
                        } else if (swingValue.equals("Packet")) {
                            mc.netHandler.addToSendQueue(C0APacketAnimation())
                        }
                        mc.playerController.onPlayerDestroyBlock(pos, EnumFacing.DOWN)

                        currentDamage = 0F
                        pos = null
                        return
                    }
                }

                if (swingValue.equals("Normal")) {
                    mc.thePlayer.swingItem()
                } else if (swingValue.equals("Packet")) {
                    mc.netHandler.addToSendQueue(C0APacketAnimation())
                }
                currentDamage += block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, currentPos)
                mc.theWorld.sendBlockBreakProgress(mc.thePlayer.entityId, currentPos, (currentDamage * 10F).toInt() - 1)

                if (currentDamage >= 1F) {
                    mc.netHandler.addToSendQueue(C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                            currentPos, EnumFacing.DOWN))
                    mc.playerController.onPlayerDestroyBlock(currentPos, EnumFacing.DOWN)
                    blockHitDelay = 4
                    currentDamage = 0F
                    pos = null
                    mc.thePlayer.isSprinting = true
                }
                if(matrixValue.get()) mc.gameSettings.keyBindUseItem.pressed = false
            }

            // Use block
            actionValue.equals("use") -> {
                if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.heldItem, pos, EnumFacing.DOWN,
                        Vec3(currentPos.x.toDouble(), currentPos.y.toDouble(), currentPos.z.toDouble()))) {
                    if (swingValue.equals("Normal")) {
                        mc.thePlayer.swingItem()
                    } else if (swingValue.equals("Packet")) {
                        mc.netHandler.addToSendQueue(C0APacketAnimation())
                    }
                    blockHitDelay = 4
                    currentDamage = 0F
                    pos = null
                }
            }
        }
    }
    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if(!rotationStrafeValue.get() || !breaking)
            return
        val (yaw) = RotationUtils.targetRotation ?: return
        var strafe = event.strafe
        var forward = event.forward
        val friction = event.friction

        var f = strafe * strafe + forward * forward

        if (f >= 1.0E-4F) {
        f = MathHelper.sqrt_float(f)

        if (f < 1.0F) {
            f = 1.0F
        }
        f = friction / f
        strafe *= f
        forward *= f

        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())
        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
        }
        event.cancelEvent()
    }
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        RenderUtils.drawBlockBox(pos ?: return, Color.RED, false, true, 1F)
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        teamPos = null
        delayTimer.reset()
    }
    override fun onEnable() {
        teamPos = null
    }
    /**
     * Find new target block by [targetID]
     */
    private fun find(targetID: Int): BlockPos? {
        val block = BlockUtils.searchBlocks(rangeValue.get().toInt() + 1)
            .filter {
                Block.getIdFromBlock(it.value) == targetID && BlockUtils.getCenterDistance(it.key) <= rangeValue.get() &&
                        (isHitable(it.key) || surroundingsValue.get())
            }
            .minByOrNull { BlockUtils.getCenterDistance(it.key) }?.key ?: return null

        if (bypassValue.get()) {
            val upBlock = block.up()
            if (BlockUtils.getBlock(upBlock) != Blocks.air) {
                isRealBlock = false
                return upBlock
            }
        }

        isRealBlock = true
        return if (otherRatationsValue.get()) block.add(0.5, 0.5, 0.5) else block
    }

    /**
     * Check if block is hitable (or allowed to hit through walls)
     */
    private fun isHitable(blockPos: BlockPos): Boolean {
        return when (throughWallsValue.get().lowercase()) {
            "raycast" -> {
                val eyesPos = mc.thePlayer.getEyeVec3()
                val movingObjectPosition = mc.theWorld.rayTraceBlocks(eyesPos,
                        Vec3(blockPos.x + 0.5, blockPos.y + 0.5, blockPos.z + 0.5), false,
                        true, false)

                movingObjectPosition != null && movingObjectPosition.blockPos == blockPos
            }
            "around" -> !BlockUtils.isFullBlock(blockPos.down()) || !BlockUtils.isFullBlock(blockPos.up()) || !BlockUtils.isFullBlock(blockPos.north()) ||
                    !BlockUtils.isFullBlock(blockPos.east()) || !BlockUtils.isFullBlock(blockPos.south()) || !BlockUtils.isFullBlock(blockPos.west())
            else -> true
        }
    }

    override val tag: String
        get() = BlockUtils.getBlockName(blockValue.get())
}
