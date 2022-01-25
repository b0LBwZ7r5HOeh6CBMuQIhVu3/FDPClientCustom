/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command

class clearChatCommand : Command("clearChat", arrayOf("clearChat")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        mc.ingameGUI.getChatGUI().clearChatMessages()
    }
}
