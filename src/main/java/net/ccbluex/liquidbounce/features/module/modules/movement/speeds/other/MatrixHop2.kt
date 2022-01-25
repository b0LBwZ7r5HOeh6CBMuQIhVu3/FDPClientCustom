package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils

class MatrixHop2 : SpeedMode("MatrixHop2") {

    override fun onUpdate() {
        if (MovementUtils.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump()
            }else{
                mc.thePlayer.speedInAir = 0.02099F
                mc.thePlayer.jumpMovementFactor = 0.027F
            }
        }
    }
}