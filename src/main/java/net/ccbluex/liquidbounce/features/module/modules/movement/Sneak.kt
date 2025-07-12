package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C0BPacketEntityAction

@ModuleInfo(name = "Sneak", category = ModuleCategory.MOVEMENT, array = false)
class Sneak : Module() {

    private val modeValue = ListValue("Mode", arrayOf("Legit", "Vanilla", "Switch", "MineSecure"), "MineSecure")
    private val switchDelayValue = IntegerValue("SwitchDelay", 500, 0, 2000).displayable { modeValue.get().equals("switch", true) }
    private val stopMoveValue = BoolValue("StopMove", false)

    private var sneaking = false
    private val switchTimer = MSTimer()

    override fun onEnable() {
        switchTimer.reset()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (stopMoveValue.get() && MovementUtils.isMoving()) {
            if (sneaking) {
                onDisable()
            }
            return
        }

        when (modeValue.get().lowercase()) {
            "legit" -> mc.gameSettings.keyBindSneak.pressed = true
            "vanilla" -> {
                if (sneaking) {
                    return
                }

                mc.netHandler.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.START_SNEAKING
                    )
                )
            }

            "switch" -> {
                when (event.eventState) {
                    EventState.PRE -> {
                        if (!switchTimer.hasTimePassed(switchDelayValue.get().toLong())) return

                        mc.netHandler.addToSendQueue(
                            C0BPacketEntityAction(
                                mc.thePlayer,
                                C0BPacketEntityAction.Action.START_SNEAKING
                            )
                        )
                        mc.netHandler.addToSendQueue(
                            C0BPacketEntityAction(
                                mc.thePlayer,
                                C0BPacketEntityAction.Action.STOP_SNEAKING
                            )
                        )
                    }

                    EventState.POST -> {
                        mc.netHandler.addToSendQueue(
                            C0BPacketEntityAction(
                                mc.thePlayer,
                                C0BPacketEntityAction.Action.STOP_SNEAKING
                            )
                        )
                        mc.netHandler.addToSendQueue(
                            C0BPacketEntityAction(
                                mc.thePlayer,
                                C0BPacketEntityAction.Action.START_SNEAKING
                            )
                        )
                        switchTimer.reset()
                    }

                    else -> {}
                }
            }

            "minesecure" -> {
                if (event.eventState == EventState.PRE) {
                    return
                }

                mc.netHandler.addToSendQueue(
                    C0BPacketEntityAction(
                        mc.thePlayer,
                        C0BPacketEntityAction.Action.START_SNEAKING
                    )
                )
            }
        }
    }

    @EventTarget
    fun onWorld(worldEvent: WorldEvent) {
        sneaking = false
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return

        when (modeValue.get().lowercase()) {
            "legit" -> {
                if (!mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.gameSettings.keyBindSneak.pressed = false
                }
            }

            "vanilla", "switch", "minesecure" -> mc.netHandler.addToSendQueue(
                C0BPacketEntityAction(
                    player,
                    C0BPacketEntityAction.Action.STOP_SNEAKING
                )
            )
        }
        sneaking = false
    }
}