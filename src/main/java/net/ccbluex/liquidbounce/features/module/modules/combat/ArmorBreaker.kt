/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.features.module.modules.exploit

import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemStack

@ModuleInfo(name = "ArmorBreaker", category = ModuleCategory.COMBAT)
class ArmorBreaker : Module() {

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (EntityUtils.isSelected(event.targetEntity?,true)) {
            val current? = mc.thePlayer.getHeldItem()
            for (i in 0..45) {
                val toSwitch = mc.thePlayer.inventoryContainer.getSlot(i).getStack()
                if ((current != null) && (toSwitch != null) && ((toSwitch.getItem() instanceof ItemSword))) {
                    mc.playerController.windowClick(0, i, mc.thePlayer.inventory.currentItem, 2, mc.thePlayer);
                }
            }
        }
    }

}
