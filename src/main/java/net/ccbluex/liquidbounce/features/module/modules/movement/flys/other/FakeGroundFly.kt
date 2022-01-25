/*
 *
 *  * MotherF▉▉▉▉▉▉▉▉▉Client Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.movement.flys.other

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.JumpEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StepEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.AxisAlignedBB
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue

class FakeGroundFly : FlyMode("FakeGround") {
    private val noJumpValue = BoolValue("${valuePrefix}noJump", false)
    private val useLaunchPosYValue = BoolValue("${valuePrefix}useLaunchPosY", true)
    private val packetSpoofGroundValue = ListValue("${valuePrefix}packetSpoofGround", arrayOf("spoofGround", "noGround", "noSpoof"), "noSpoof")

    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!packetSpoofGroundValue.equals("noSpoof") && packet is C03PacketPlayer) {
            packet.onGround = packetSpoofGroundValue.equals("spoofGround")
        }
    }

    override fun onBlockBB(event: BlockBBEvent) {
        val blockY = if (useLaunchPosYValue.get()) mc.thePlayer.posY else fly.launchY
        if (event.block is BlockAir && event.y <= blockY) {
            event.boundingBox = AxisAlignedBB.fromBounds(event.x.toDouble(), event.y.toDouble(), event.z.toDouble(), event.x + 1.0, blockY, event.z + 1.0)
        }
    }

    override fun onJump(event: JumpEvent) {
        if (noJumpValue.get()) event.cancelEvent()
    }

    override fun onStep(event: StepEvent) {
        if (noJumpValue.get()) event.stepHeight = 0f
    }
}