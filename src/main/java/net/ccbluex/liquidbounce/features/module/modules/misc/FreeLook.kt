package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "FreeLook", category = ModuleCategory.MISC, array = false)
class FreeLook : Module() {

    override fun onEnable() {
        mc.gameSettings.debugCamEnable = true
    }

    override fun onDisable() {
        mc.gameSettings.debugCamEnable = false
    }
}
