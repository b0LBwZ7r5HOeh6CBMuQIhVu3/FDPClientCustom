/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.utils.timer.TimeUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.client.gui.GuiChat
import java.util.*

@ModuleInfo(name = "Spammer", category = ModuleCategory.MISC)
class Spammer : Module() {
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 1000, 0, 5000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val minDelayValueObject = minDelayValue.get()
            if (minDelayValueObject > newValue) set(minDelayValueObject)
            delay = TimeUtils.randomDelay(minDelayValue.get(), this.get())
        }
    }

    private val minDelayValue: IntegerValue = object : IntegerValue("MinDelay", 500, 0, 5000) {
        override fun onChanged(oldValue: Int, newValue: Int) {
            val maxDelayValueObject = maxDelayValue.get()
            if (maxDelayValueObject < newValue) set(maxDelayValueObject)
            delay = TimeUtils.randomDelay(this.get(), maxDelayValue.get())
        }
    }

    private val modeValue = ListValue("Mode", arrayOf("Single", "Insult", "OrderInsult"), "Single")
    private val endingChars = IntegerValue("EndingRandomChars", 5, 0, 30)
    private val messageValue = TextValue("Message", "Buy %r Minecraft %r Legit %r and %r stop %r using %r cracked %r servers %r%r")
        .displayable { !modeValue.contains("insult") }
    private val insultMessageValue = TextValue("InsultMessage", "[%s] %w [%s]")
        .displayable { modeValue.contains("insult") }

    private val msTimer = MSTimer()
    private var delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
    private var lastIndex = -1

    override fun onEnable() {
        lastIndex = -1
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.currentScreen != null && mc.currentScreen is GuiChat) {
            return
        }

        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(
                when (modeValue.get().lowercase()) {
                    "insult" -> {
                        replaceAbuse(KillInsults.getRandomOne())
                    }

                    "orderinsult" -> {
                        lastIndex++
                        if (lastIndex >= (KillInsults.insultWords.size - 1)) {
                            lastIndex = 0
                        }
                        replaceAbuse(KillInsults.insultWords[lastIndex])
                    }

                    else -> replace(messageValue.get())
                }
            )
            msTimer.reset()
            delay = TimeUtils.randomDelay(minDelayValue.get(), maxDelayValue.get())
        }
    }

    private fun replaceAbuse(str: String): String {
        return replace(insultMessageValue.get().replace("%w", str))
    }

    private fun replace(str: String): String {
        val r = Random()
        var string_ = str
        while (string_.contains("%f")) string_ = string_.substring(0, string_.indexOf("%f")) + r.nextFloat() + string_.substring(string_.indexOf("%f") + "%f".length)

        while (string_.contains("%i")) string_ = string_.substring(0, string_.indexOf("%i")) + r.nextInt(10000) + string_.substring(string_.indexOf("%i") + "%i".length)

        while (string_.contains("%s")) string_ = string_.substring(0, string_.indexOf("%s")) + RandomUtils.randomString(r.nextInt(8) + 1) + string_.substring(string_.indexOf("%s") + "%s".length)

        while (string_.contains("%ss")) string_ = string_.substring(0, string_.indexOf("%ss")) + RandomUtils.randomString(r.nextInt(4) + 1) + string_.substring(string_.indexOf("%ss") + "%ss".length)

        while (string_.contains("%ls")) string_ = string_.substring(0, string_.indexOf("%ls")) + RandomUtils.randomString(r.nextInt(15) + 1) + string_.substring(string_.indexOf("%ls") + "%ls".length)
        return string_
    }
}
