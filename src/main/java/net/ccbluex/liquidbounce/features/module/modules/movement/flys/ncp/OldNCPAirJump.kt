package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class OldNCPAirJump : FlyMode("OldNCPAirump") {

    override fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.onGround = true
        mc.thePlayer.isAirBorne = false
    }
}