package net.ccbluex.liquidbounce.features.module.modules.movement.flys.matrix

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.value.IntegerValue

class matrixSlowTestFly : FlyMode("matrixSlowTest") {
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
            mc.thePlayer.onGround = true

        }
                if (mc.thePlayer.ticksExisted % 4 == 0) {
                    mc.timer.timerSpeed = 0.01f;
                }
                if (mc.thePlayer.ticksExisted % 20 == 0 && !mc.thePlayer.onGround) {
                    mc.timer.timerSpeed = 1000f;
                } else {
                    mc.timer.timerSpeed = 0.02f;
                }
    }
}