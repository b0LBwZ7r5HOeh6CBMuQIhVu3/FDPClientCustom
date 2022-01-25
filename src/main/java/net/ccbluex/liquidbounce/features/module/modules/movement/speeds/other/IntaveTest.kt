package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class IntaveTest : SpeedMode("IntaveTest") {
	override fun onMove(event: MoveEvent) {
		if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
			if (MovementUtils.isMoving()) {
				mc.gameSettings.keyBindJump.pressed = false
				if (mc.thePlayer.onGround) {
					mc.thePlayer.jump()
					mc.thePlayer.motionX *= 1.0255
					mc.thePlayer.motionZ *= 1.0255
				}
				MovementUtils.strafe()
			}
		}
	}
}
