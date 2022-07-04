/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.potion.Potion


@ModuleInfo(name = "Regen", category = ModuleCategory.PLAYER)
class Regen : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Vanilla", "OldSpartan", "NewSpartan", "AAC4NoFire"), "Vanilla")
    private val healthValue = IntegerValue("Health", 18, 0, 20)
    private val foodValue = IntegerValue("Food", 18, 0, 20)
    private val speedValue = IntegerValue("Speed", 100, 1, 100)
    private val noAirValue = BoolValue("NoAir", false)
    private val oldMatrixValue = BoolValue("OldMatrix", false)
    private val c04PacketValue = BoolValue("PositionPacket", false)
    private val potionEffectValue = BoolValue("PotionEffect", false)

    private var resetTimer = false
    fun sendPacket(packet: C03PacketPlayer) {
        if (oldMatrixValue.get()) {
            mc.netHandler.addToSendQueue(C08PacketPlayerBlockPlacement(mc.thePlayer.getPosition().down(256), 256, null, 0.0f, 0.0f, 0.0f))
        }
        if (c04PacketValue.get()) {
            mc.netHandler.addToSendQueue(C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.onGround))
        } else {
            mc.netHandler.addToSendQueue(packet)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
        }
        resetTimer = false

        if ((!noAirValue.get() || mc.thePlayer.onGround) && !mc.thePlayer.capabilities.isCreativeMode &&
            mc.thePlayer.foodStats.foodLevel > foodValue.get() && mc.thePlayer.isEntityAlive && mc.thePlayer.health < healthValue.get()
        ) {
            if (potionEffectValue.get() && !mc.thePlayer.isPotionActive(Potion.regeneration)) {
                return
            }

            when (modeValue.get().lowercase()) {
                "vanilla" -> {
                    repeat(speedValue.get()) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "aac4nofire" -> {
                    if (mc.thePlayer.isBurning && mc.thePlayer.ticksExisted % 10 == 0) {
                        repeat(35) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    }
                }

                "newspartan" -> {
                    if (mc.thePlayer.ticksExisted % 5 == 0) {
                        resetTimer = true
                        mc.timer.timerSpeed = 0.98F
                        repeat(10) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer(true))
                        }
                    } else {
                        if (MovementUtils.isMoving()) mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }
                }

                "oldspartan" -> {
                    if (MovementUtils.isMoving() || !mc.thePlayer.onGround) {
                        return
                    }

                    repeat(9) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer(mc.thePlayer.onGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
