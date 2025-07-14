/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import com.google.gson.JsonArray
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.file.FileManager
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.FileUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.player.EntityPlayer
import java.io.File

@ModuleInfo(name = "AutoMessage", category = ModuleCategory.MISC)
object AutoMessage : Module() {
    private val onKilledValue = BoolValue("OnKilled", true)
    private val killedMessageValue = TextValue("KilledMessage", "%name% how are you today?")
    private val onAttackingValue = BoolValue("OnAttacking", true)
    private val attackingMessageValue = TextValue("AttackingMessage", "/friend add %name%")
    private val onSeenValue = BoolValue("OnSeen", true)
    private val seenMessageValue = TextValue("SeenMessage", "/report %name% bad_username")
    private val onHurtValue = BoolValue("OnHurt", true)
    private val hurtMessageValue = TextValue("HurtMessage", "")
    private val clientNameValue = TextValue("clientName", "[Debug] ")
    private val repeat = IntegerValue("Repeat", 1, 1, 10)

    private val seen = mutableListOf<String>()

    @EventTarget
    fun onKilled(event: EntityKilledEvent) {
        val target = event.targetEntity

        if (!onKilledValue.get() || target !is EntityPlayer) {
            return
        }
        repeat(repeat.get(), {
            mc.thePlayer.sendChatMessage(killedMessageValue.get().replace("%name%", target.name))
        })
    }

    @EventTarget
    fun onAttacking(event: AttackEvent) {
        val target = event.targetEntity

        if (!onAttackingValue.get() || target !is EntityPlayer) {
            return
        }

        repeat(repeat.get(), {
            if(target.hurtTime >= 10 && onHurtValue.get()){
                mc.thePlayer.sendChatMessage(hurtMessageValue.get().replace("%name%", target.name))
            } else {
                mc.thePlayer.sendChatMessage(attackingMessageValue.get().replace("%name%", target.name))
            }
        })
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        seen.clear()
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!onSeenValue.get()) {
            return
        }
        mc.netHandler.playerInfoMap.forEach {
            val name = it.gameProfile.name
            if(name != mc.session.username && !seen.contains(name)) {
                seen.add(name)
                mc.thePlayer.sendChatMessage(seenMessageValue.get().replace("%name%", name))
            }
        }
    }
}