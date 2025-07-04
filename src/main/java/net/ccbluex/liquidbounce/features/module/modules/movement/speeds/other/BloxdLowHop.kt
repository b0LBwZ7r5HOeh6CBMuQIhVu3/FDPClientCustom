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
//                4 -> BloxdPhysics.PhysicsBody.velocityVector.y += 0.03f
//                5 -> BloxdPhysics.PhysicsBody.velocityVector.y -= 0.1905189780583944f
//                6 -> BloxdPhysics.PhysicsBody.velocityVector.y *= 1.01f
//                7 -> BloxdPhysics.PhysicsBody.velocityVector.y /= 1.5f
        }
        if(mc.thePlayer.onGround){

            if (firstJump) {
                firstJump = false
            } else return

            event.x *= 0.98
            event.z *= 0.98
        }else if(airTicks > 7){
//            event.x *= 1.02
//            event.z *= 1.02
        }

    }


    override fun onUpdate() {
        if (mc.thePlayer.motionY > 0.1 && mc.thePlayer.fallDistance < 1) {
            mc.timer.timerSpeed = 4.914514F
        } else if (mc.thePlayer.motionY < 0 && mc.thePlayer.fallDistance < 1) {
            mc.timer.timerSpeed = 1.1F
        } else if (mc.thePlayer.fallDistance >= 1){
            mc.timer.timerSpeed = 1.0F
        }
//        if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1) {
//            mc.thePlayer.jumpMovementFactor = 0.026f
//        }else{
//            mc.thePlayer.jumpMovementFactor = 0.0247f
//        }
        if(mc.thePlayer.motionY > 0.1){
            LiquidBounce.moduleManager.getModule(BloxdPhysics::class.java)!!.setNowMotionAndGravity(8f,4f)
        }
    }
}