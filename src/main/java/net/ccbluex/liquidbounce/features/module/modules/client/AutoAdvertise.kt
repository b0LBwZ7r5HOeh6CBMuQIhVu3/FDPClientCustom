/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import java.util.*
import kotlin.concurrent.schedule

@ModuleInfo(name = "AutoAdvertise", category = ModuleCategory.CLIENT, array = false, defaultOn = false)
class AutoAdvertise : Module() {


    @EventTarget
    fun onWorld(event: WorldEvent) {
        MovementUtils.handleVanillaKickBypass()
        MovementUtils.handleVanillaKickBypass()
    }
}
