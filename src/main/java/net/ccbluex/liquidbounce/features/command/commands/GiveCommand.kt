package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.item.ItemUtils
import net.ccbluex.liquidbounce.utils.misc.StringUtils
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction

class GiveCommand : Command("give", emptyArray()) {
    override fun execute(args: Array<String>) {
        val thePlayer = mc.thePlayer ?: return
        val usedAlias = args[0].lowercase()

        if (args.size <= 1) {
            chatSyntax("$usedAlias <item> [amount] [data] [datatag] [packet/client]")
            return
        }

        if (mc.playerController.isNotCreative && args.size < 5) {
            alert("§c§lError: §3You need to be in creative mode.")
            return
        }

        val itemStack = ItemUtils.createItem(StringUtils.toCompleteString(args.copyOfRange(0,4), 1))

        if (itemStack == null) {
            chatSyntaxError()
            return
        }

        val emptySlot = thePlayer.inventory.firstEmptyStack

        if (emptySlot != -1) {
            when(args[5].lowercase()){
                "packet" -> mc.netHandler.addToSendQueue(C10PacketCreativeInventoryAction(emptySlot, itemStack))
                "client" -> thePlayer.inventory.setInventorySlotContents(emptySlot, itemStack)
                else -> {
                    alert("§c§lError: §3Unknown option '${args[5]}'.")
                    return
                }
            }
            chat("§7Given [§8${itemStack.displayName}§7] * §8${itemStack.stackSize}§7 to §8${mc.session.username}§7.")
        } else {
            chat("Your inventory is full.")
        }

    }
}