package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.block.material.Material
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraft.util.AxisAlignedBB
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.sqrt

@ModuleInfo(name = "BloxdPhysics", description = "Bloxd movement logic", category = ModuleCategory.WORLD)
class BloxdPhysics : Module() {
    private val jumpIV = FloatValue("JumpImpulseVector", 4F, 4F, 10F)
    private val spiderSpeedValue = FloatValue("SpiderSpeed", 5F, 4F, 10F)
    private val gravityMul = FloatValue("GravityMultiplier", 2F, 1F, 10F)
    private val mass = FloatValue("Mass", 1F, 0.1F, 5F)

    private val DELTA = IntegerValue("Ticks", 30, 20, 30)
    fun getDELTA(): Float {
        return 1f / DELTA.get()
    }

    private val attackingBoost = BoolValue("attackingBoost", false)
    private val compatibility = BoolValue("compatibility", false)
    private val AllowBHop = IntegerValue("AllowBHop", 3, 0, 3)

    private val damageBoost = BoolValue("DamageBoost", true)
    private val dbTime = IntegerValue("DamageBoostTime", 1100, 100, 2000)
    private val dbSpeed = FloatValue("DamageBoostSpeed", 1F, 0.8F, 1.5F)

    private var groundTicks = 0
    private var jumpFunny = 0
    private val damageTimer: MSTimer = MSTimer()

    data class Vec3(
        var x: Float = 0f,
        var y: Float = 0f,
        var z: Float = 0f
    ) {
        fun add(vec: Vec3): Vec3 {
            x += vec.x
            y += vec.y
            z += vec.z
            return this
        }

        fun set(x: Float, y: Float, z: Float): Vec3 {
            this.x = x
            this.y = y
            this.z = z
            return this
        }

        fun multiply(num: Float): Vec3 {
            x *= num
            y *= num
            z *= num
            return this
        }
    }

    object PhysicsBody {
        val impulseVector = Vec3()
        val forceVector = Vec3()
        val velocityVector = Vec3()
        val gravityVector = Vec3(0f, -10f, 0f)

        fun getMotionForTick(gravityMul: Float, mass: Float, DELTA: Float): Vec3 {
            // forces
            val massDiv = 1f / mass
            forceVector.multiply(massDiv)
            // gravity
            forceVector.add(gravityVector)
            forceVector.multiply(gravityMul)

            // impulses
            impulseVector.multiply(massDiv)
            forceVector.multiply(DELTA)
            impulseVector.add(forceVector)
            // velocity
            velocityVector.add(impulseVector)

            forceVector.set(0f, 0f, 0f)
            impulseVector.set(0f, 0f, 0f)

            return velocityVector
        }
    }

    private fun getMoveDir(forward: Float, strafe: Float, yaw: Float, bps: Float): Vec3 {
        val f = sin(yaw * Math.PI.toFloat() / 180f) * bps
        val g = cos(yaw * Math.PI.toFloat() / 180f) * bps
        var finalForward = forward
        var finalStrafe = strafe
        val sqrt = sqrt(forward * forward + strafe * strafe)

        if (sqrt > 1) {
            finalForward = forward / sqrt
            finalStrafe = strafe / sqrt
        }

        return Vec3(
            finalStrafe * g - finalForward * f,
            0f,
            finalForward * g + finalStrafe * f
        )
    }

    fun calculateImpulseForHeight(heightInBlocks: Float): Float {
        val gravity = -10.0f
        val gravityMultiplier = 4.0f
        val delta = 0.05f

        // (gravity * gravityMultiplier) * delta = (-10 * 4) * 0.05 = -2.0
        val gravityVelocityChangePerTick = (gravity * gravityMultiplier) * delta

        if (heightInBlocks <= 0) {
            return 0.0f
        }

        val a = 0.025
        val b = 0.025
        val c = -heightInBlocks

        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) {
            return 0.0f
        }

        val v0 = (-b + sqrt(discriminant)) / (2 * a)

        val impulse = v0 - gravityVelocityChangePerTick

        return impulse.toFloat()
    }


    @EventTarget
    fun onStrafe(event: StrafeEvent) {

        if (mc.thePlayer.onGround && PhysicsBody.velocityVector.y < 0) {
            PhysicsBody.velocityVector.set(0f, 0f, 0f)
        }

        if (mc.thePlayer.onGround && mc.thePlayer.motionY >= 0.419f) {
            jumpFunny = min(jumpFunny + 1, AllowBHop.get())
            PhysicsBody.impulseVector.add(Vec3(0f, jumpIV.get(), 0f))
        }

        groundTicks = if (mc.thePlayer.onGround) groundTicks + 1 else 0
        if (groundTicks > 5) {
            jumpFunny = 0
        }

        if (mc.thePlayer.isCollidedHorizontally) {
            PhysicsBody.velocityVector.set(0f, 5f, 0f)
        }

        val moveDir = getMoveDir(
            event.forward,
            event.strafe,
            mc.thePlayer.rotationYaw,
            if (damageTimer.timePassed() < dbTime.get() && damageBoost.get()) dbSpeed.get() else (if (mc.thePlayer.isUsingItem) 0.06f else 0.26f + 0.025f * jumpFunny)
        )
        event.cancelEvent()
        mc.thePlayer.motionX = moveDir.x.toDouble()
        mc.thePlayer.motionY =
            if(mc.theWorld.getChunkFromBlockCoords(mc.thePlayer.position).isLoaded)
                PhysicsBody.getMotionForTick(
                    if(attackingBoost.get() && LiquidBounce.combatManager.target != null)
                     gravityMul.get() * 2
                 else gravityMul.get(), mass.get(), getDELTA()).y * getDELTA().toDouble()
            else 0.0
        mc.thePlayer.motionZ = moveDir.z.toDouble()
    }


    @EventTarget
    fun onPacket(event: PacketEvent) {

        val packet = event.packet

        when (packet) {
            is S3FPacketCustomPayload -> {
                if (packet.channelName == "bloxd:resyncphysics") {
                    val data = packet.bufferData
                    jumpFunny = 0
                    PhysicsBody.impulseVector.set(0f, 0f, 0f)
                    PhysicsBody.forceVector.set(0f, 0f, 0f)
                    PhysicsBody.velocityVector.set(data.readFloat(), data.readFloat(), data.readFloat())
                }
            }
            is S12PacketEntityVelocity -> {
                if (mc.thePlayer != null && packet.entityID == mc.thePlayer.entityId) {
                    damageTimer.reset()
                }
            }
        }
    }


    @EventTarget
    fun onBlockBB(e: BlockBBEvent) {
        val thePlayer = mc.thePlayer ?: return

        // Bloxd server didn't ipmled the non-full block bounding boxes correctly because it's not a real block game
        when (e.block) {
            Blocks.bed, Blocks.chest, Blocks.trapped_chest, Blocks.enchanting_table, Blocks.cauldron, Blocks.snow -> e.boundingBox = AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 1.0, e.z + 1.0)

            Blocks.brewing_stand -> e.boundingBox = AxisAlignedBB(e.x.toDouble() - 0.5, e.y.toDouble(), e.z.toDouble() - 1, e.x + 1.5, e.y + 1.0, e.z + 1.5)

            Blocks.snow_layer, Blocks.carpet -> e.boundingBox = AxisAlignedBB(0.0,0.0,0.0,0.0,0.0,0.0)

            else -> return
        }
    }
}