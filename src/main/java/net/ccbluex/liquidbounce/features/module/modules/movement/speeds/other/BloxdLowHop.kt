package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.features.module.modules.world.BloxdPhysics
import net.ccbluex.liquidbounce.utils.MovementUtils
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class BloxdLowHop : SpeedMode("BloxdLowHop") {
    var firstJump = false
    var airTicks = 0
//    var physics = LiquidBounce.moduleManager.getModule(BloxdPhysics::class.java)!!
    override fun onPreMotion() {
        if (mc.thePlayer.isInWater) return
        if (MovementUtils.isMoving()) {
            if (!firstJump){
//                physics.setNextImpulseAndGravity(8f, 4f)
            }
            if (mc.thePlayer.onGround) {
                LiquidBounce.moduleManager.getModule(BloxdPhysics::class.java)!!.setNextImpulseAndGravity(8f,2f)
                mc.thePlayer.jump()
//                if (firstJump) {
////                    BloxdPhysics.PhysicsBody.velocityVector.x *= 0.98f
////                    BloxdPhysics.PhysicsBody.velocityVector.z *= 0.98f
//                    firstJump = false
////                    physics.setNextImpulseAndGravity(8f, 4f)
//                }
            }

        } else {
            firstJump = true
        }
    }

    override fun onTick() {
        if(!mc.thePlayer.onGround) {
            airTicks++


            if (airTicks >= 7) {
//                BloxdPhysics.PhysicsBody.velocityVector.x *= 1.02f
//                BloxdPhysics.PhysicsBody.velocityVector.z *= 1.02f
//                if (!mc.thePlayer.isCollidedHorizontally) MovementUtils.forward(if (firstJump) 0.0016 else 0.001799)
            }
        } else {
            airTicks = 0
        }
    }

    override fun onMove(event: MoveEvent) {
        if(!MovementUtils.isMoving()) return
        when (airTicks) {
            1 -> {
                val (x, z) = MovementUtils.getStrafeXZ(0.2177f); event.x = x.toDouble(); event.z = z.toDouble()
            }
            2 -> {
                val (x, z) = MovementUtils.getStrafeXZ(0.21f); event.x = x.toDouble(); event.z = z.toDouble()
            }
            3 -> {
                val (x, z) = MovementUtils.getStrafeXZ(0.208f); event.x = x.toDouble(); event.z = z.toDouble()
            }
//                4 -> event.x *= 1.0109999980926514
//                5 -> event.z *= 1.0109999980926514
//                6 -> event.x *= 1.0129999809265138
//                7 -> event.z *= 1.0129999809265138
        }
        if(mc.thePlayer.onGround){

            if (firstJump) {
                firstJump = false
            } else return

            event.x *= 0.98
            event.z *= 0.98
        }/*else if(airTicks > 7){
            event.x *= 1.0199999809265137
            event.z *= 1.0199999809265137
        }*/

    }


    override fun onUpdate() {
        if (mc.thePlayer.motionY > 0.1 && mc.thePlayer.fallDistance < 1) {
            mc.timer.timerSpeed = 4.914514F
        } else if (mc.thePlayer.fallDistance < 0.1) {
            mc.timer.timerSpeed = 4.114514F
        } else if (mc.thePlayer.fallDistance in 0.2..0.4){
            mc.timer.timerSpeed = 2F
        } else {
            mc.timer.timerSpeed = 1.05F
        }
//        if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
//            mc.thePlayer.jumpMovementFactor = 0.026f
//        }else{
//            mc.thePlayer.jumpMovementFactor = 0.0247f
//        }
        if(mc.thePlayer.motionY > 0.1 && !mc.gameSettings.keyBindJump.isKeyDown) {
            LiquidBounce.moduleManager.getModule(BloxdPhysics::class.java)!!.setNowMotionAndGravity(8f,4f)
        }
    }
}