/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.command.CommandManager
import net.ccbluex.liquidbounce.features.module.modules.misc.KillInsults
import net.ccbluex.liquidbounce.ui.cape.GuiCapeManager
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGu
import net.ccbluex.liquidbounce.ui.font.Fonts

class ReloadScriptsCommand : Command("reload", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        alert("Reloading...")
                LiquidBounce.commandManager = CommandManager()
                LiquidBounce.commandManager.registerCommands()
                LiquidBounce.isStarting = true
                LiquidBounce.scriptManager.disableScripts()
                LiquidBounce.scriptManager.unloadScripts()
                for(module in LiquidBounce.moduleManager.modules)
                    LiquidBounce.moduleManager.generateCommand(module)
                LiquidBounce.scriptManager.loadScripts()
                LiquidBounce.scriptManager.enableScripts()
            LiquidBounce.configManager.load(LiquidBounce.configManager.nowConfig, false)
                LiquidBounce.isStarting = false

                LiquidBounce.clickGui = ClickGui()
                // LiquidBounce.fileManager.loadConfig(LiquidBounce.fileManager.clickGuiConfig)
        alert("Reloaded.")
        LiquidBounce.isStarting = false
        LiquidBounce.isLoadingConfig = false
        System.gc()
    }
}
