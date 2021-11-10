package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.IntegerValue

class matrixGlideTestFly : FlyMode("matrixGlideTest") {
    private val ticksValue = IntegerValue("${valuePrefix}Ticks", 6, 0, 20)

    private var glideDelay = 0

    override fun onEnable() {
        glideDelay = 0
    }

    override fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround) {
            mc.timer.timerSpeed = 0.3F
            glideDelay++
        }

        if (glideDelay >= ticksValue.get() && !mc.thePlayer.onGround) {
            glideDelay = 0
            mc.thePlayer.motionY *= -0.0001
        }
    }
}