package net.ccbluex.liquidbounce.features.module.modules.misc

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "FreeLook", category = ModuleCategory.MISC, array = false)
class FreeLook : Module() {

    override fun onEnable() {
        mc.gameSettings.debugCamEnable = true
        mc.gameSettings.thirdPersonView = 2
    }

    override fun onDisable() {
        mc.gameSettings.debugCamEnable = false
        mc.gameSettings.thirdPersonView = 0
    }
}
