/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.XRay
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.util.*
import kotlin.concurrent.thread

@ModuleInfo(name = "I_LIKE_FOODS", category = ModuleCategory.WORLD)
object I_LIKE_FOODS : Module() {
    private val niceBlocks: HashMap<BlockPos, String> = HashMap()
    private val stopFun = BoolValue("stop funny", false)
    private val radV = IntegerValue("rad", 10, 0, 8964)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.ticksExisted % 10 != 0 || stopFun.get()) return
        val rad = radV.get()
        thread(start = true) {
            ClientUtils.logDebug("0")
            try {
                for (x in 100 downTo -rad + 1) {
                    for (y in 100 downTo -rad + 1) {
                        for (z in 100 downTo -rad + 1) {
                            val blockPos = BlockPos(
                                mc.thePlayer.posX.toInt() + x, mc.thePlayer.posY.toInt() + y,
                                mc.thePlayer.posZ.toInt() + z
                            )
                            mc.netHandler.addToSendQueue(
                                C07PacketPlayerDigging(
                                    C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
                                    blockPos, EnumFacing.UP
                                )
                            )
                        }
                    }
                }
            } catch (ignored: Exception) {
                // hi
            }
            ClientUtils.logDebug("1")
        }

    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (!stopFun.get()) return
        val packet = event.packet
        val xrayBlocks = LiquidBounce.moduleManager.getModule(XRay::class.java)!!.xrayBlocks // I don't think xray will be removed in th future lol.
        if (packet is S23PacketBlockChange) {
            val block = packet.getBlockState().block
            if (block in xrayBlocks) {
                niceBlocks[packet.blockPosition] = block.unlocalizedName
                return
            }
        }
        if (packet is S22PacketMultiBlockChange) {
            val blocks = packet.changedBlocks
            for (block in blocks) {
                val blockSt = block.blockState
                if (blockSt.block in xrayBlocks) {
                    niceBlocks[block.pos] = blockSt.block.unlocalizedName
                }
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (block in niceBlocks) {
            val pos = block.key
            RenderUtils.renderNameTag(block.value, pos.x + 0.5, pos.y + 0.0/*epic integer to float*/, pos.z + 0.5)

        }
    }
}
