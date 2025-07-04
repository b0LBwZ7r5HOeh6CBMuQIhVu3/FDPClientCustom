package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.event.BlockBBEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.StrafeEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S3FPacketCustomPayload
import net.minecraft.util.AxisAlignedBB
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt

@ModuleInfo(name = "BloxdPhysics", description = "Bloxd movement logic", category = ModuleCategory.WORLD)
class BloxdPhysics : Module() {

    private val spiderSpeedValue = FloatValue("SpiderSpeed", 5F, 4F, 10F)

    private val jumpGravityMul = FloatValue("JumpGravityMultiplier", 2.5F, 1F, 10F)
    private val jumpImpulseVec = FloatValue("JumpImpulseY", 8F, 4F, 10F)

    private val speedGravityMul = FloatValue("SpeedGravityMultiplier", 3.5F, 1F, 10F)
    private val speedImpulseVec = FloatValue("SpeedImpulseY", 0F, 0F, 10F)

    private val fallingGravityMul = FloatValue("FallingGravityMultiplier", 2F, 1F, 10F)
//    private val fallingImpulseVec = FloatValue("FallingImpulseY", 0F, 0F, 10F)
    private val fallingDist = FloatValue("FallingDist", 1.6F, 1F, 3F)

    private val mass = FloatValue("Mass", 1F, 0.1F, 5F)

    private val DELTA = IntegerValue("Ticks", 30, 20, 30)
    fun getDELTA(): Float {
        return 1f / DELTA.get()
    }

//    private val attackingBoost = BoolValue("attackingBoost", false)
    private val allowBHop = IntegerValue("AllowBHop", 3, 0, 3)

    private val damageBoost = BoolValue("DamageBoost", true)
    private val dbTime = IntegerValue("DamageBoostTime", 1100, 100, 2000)
    private val dbSpeed = FloatValue("DamageBoostSpeed", 1F, 0.8F, 1.5F)

    private var groundTicks = 0
    private var jumpFunny = 0
    private val damageTimer: MSTimer = MSTimer()

    private var normalJumping = false
    private var jumped = false

    // 临时变量用于下一次跳跃参数
    private var nextJumpImpulse: Float? = null
    private var nextGravityMultiplier: Float? = null
    private var applyNextNow: Boolean = false

    // 当前重力倍数
    private var currentGravityMultiplier: Float? = null

    /**
     * 重置所有移动速度
     */
    fun resetMotion() {
        PhysicsBody.velocityVector.set(0f, 0f, 0f)
        PhysicsBody.impulseVector.set(0f, 0f, 0f)
        PhysicsBody.forceVector.set(0f, 0f, 0f)
    }

    /**
     * 设置仅用于下一次跳跃的跳跃冲量、重力和重力倍数
     */
    fun setNextImpulseAndGravity(jumpImpulse: Float, gravityMultiplier: Float) {
        nextJumpImpulse = jumpImpulse
        nextGravityMultiplier = gravityMultiplier
    }

    /**
     * 立即强制设定跳跃冲量、重力和重力倍数
     */
    fun setNowMotionAndGravity(jumpImpulse: Float, gravityMultiplier: Float) {
        setNextImpulseAndGravity(jumpImpulse, gravityMultiplier)
        applyNextNow = true
    }

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

    companion object PhysicsBody {
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

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mc.thePlayer.onGround && PhysicsBody.velocityVector.y < 0) {
            PhysicsBody.velocityVector.set(0f, 0f, 0f)
        }

        val jumpIV = if (nextJumpImpulse != null) {
            nextJumpImpulse ?: jumpImpulseVec.get()
        }else if (mc.gameSettings.keyBindJump.pressed) {
            normalJumping = true
            jumpImpulseVec.get()
        } else {
            speedImpulseVec.get()
        }
        nextJumpImpulse = null

        if (mc.thePlayer.onGround && mc.thePlayer.motionY > 0 && !mc.thePlayer.isCollidedHorizontally && !jumped) {
            jumpFunny = min(jumpFunny + 1, allowBHop.get())
            PhysicsBody.impulseVector.add(Vec3(0f, jumpIV * (mc.thePlayer.motionY / 0.42).toFloat(), 0f))
            jumped = true
            currentGravityMultiplier = if (nextJumpImpulse != null){
                nextGravityMultiplier
            } else if (mc.gameSettings.keyBindJump.pressed) {
                jumpGravityMul.get()
            } else {
                speedGravityMul.get()
            }
        }

        groundTicks = if (mc.thePlayer.onGround) {
            jumped = false
            normalJumping = false
            currentGravityMultiplier = null
            groundTicks + 1
        } else 0
        if (groundTicks > 5) {
            jumpFunny = 0
        }

        if (mc.thePlayer.isCollidedHorizontally && !mc.thePlayer.isCollidedVertically) {
            PhysicsBody.velocityVector.set(0f, spiderSpeedValue.get(), 0f)
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
            if(mc.theWorld.getChunkFromBlockCoords(mc.thePlayer.position).isLoaded) {
                val currentGravityMul = if(mc.thePlayer.fallDistance >= fallingDist.get()){
                    fallingGravityMul.get()
                } else {
                    currentGravityMultiplier ?: jumpGravityMul.get()
                }

                if (applyNextNow){
                    currentGravityMultiplier = nextGravityMultiplier
                    nextGravityMultiplier = null
                    applyNextNow = false
                }

                PhysicsBody.getMotionForTick(
                    currentGravityMul,
                    mass.get(),
                    getDELTA()
                ).y * getDELTA().toDouble()
            } else 0.0
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

    override fun onDisable(){
        PhysicsBody.impulseVector.set(0f, 0f, 0f)
        PhysicsBody.forceVector.set(0f, 0f, 0f)
        PhysicsBody.velocityVector.set(0f, 0f, 0f)
    }

    @EventTarget
    fun onBlockBB(e: BlockBBEvent) {
        val thePlayer = mc.thePlayer ?: return

        // Bloxd 没有实现真正的碰撞检测，因为他们不是真正的方块游戏
        when (e.block) {
            // 这些方块是在原版方块游戏是不满全格，但是在 Bloxd 中是满格的
            Blocks.bed, Blocks.chest, Blocks.trapped_chest, Blocks.enchanting_table, Blocks.cauldron, Blocks.snow -> e.boundingBox = AxisAlignedBB(e.x.toDouble(), e.y.toDouble(), e.z.toDouble(), e.x + 1.0, e.y + 1.0, e.z + 1.0)

            // 特例
            Blocks.brewing_stand -> e.boundingBox = AxisAlignedBB(0.0,0.0,0.0,0.0,0.0,0.0)

            // 碰撞不绝对就是绝对不碰撞
            Blocks.snow_layer, Blocks.carpet -> e.boundingBox = AxisAlignedBB(0.0,0.0,0.0,0.0,0.0,0.0)

            else -> {
                val bb = e.boundingBox ?: return
                // 对所有值进行四舍五入
                e.boundingBox = AxisAlignedBB(bb.minX.roundToInt().toDouble(), bb.minY.roundToInt().toDouble(), bb.minZ.roundToInt().toDouble(),
                    bb.maxX.roundToInt().toDouble(), bb.maxY.roundToInt().toDouble(), bb.maxZ.roundToInt().toDouble())
            }
        }
    }

}