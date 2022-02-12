/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.minecraft.entity.player.EntityPlayer
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.entity.effect.EntityLightningBolt
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "HitLightning", category = ModuleCategory.RENDER)
class HitLightning : Module() {
    private val onlyKilledValue = BoolValue("onlyKilledValue", true)
    val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)
    var target = killAura?.target

    @EventTarget
    fun onPacket(e: PacketEvent) {
        var packet = e.packet
        if (!onlyKilledValue.get() && packet is C02PacketUseEntity && packet.action == C02PacketUseEntity.Action.ATTACK) {
                mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(EntityLightningBolt(mc.theWorld, packet.getEntityFromWorld(mc.theWorld).posX, packet.getEntityFromWorld(mc.theWorld).posY, packet.getEntityFromWorld(mc.theWorld).posZ)))
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.0f))
            target?.let { mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.LAVA.particleID, target!!.posX, target!!.posY, target!!.posZ, 0.0, 0.0, 0.0) }
            }
        }
    @EventTarget
    fun onKilled(event: EntityKilledEvent) {
        val target = event.targetEntity

        if (target !is EntityPlayer || !onlyKilledValue.get()) {
            return
        }

                mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)))
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("random.explode"), 1.0f))
                mc.soundHandler.playSound(PositionedSoundRecord.create(ResourceLocation("ambient.weather.thunder"), 1.0f))
            target?.let { mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.LAVA.particleID, target!!.posX, target!!.posY, target!!.posZ, 0.0, 0.0, 0.0) }
    }
}
