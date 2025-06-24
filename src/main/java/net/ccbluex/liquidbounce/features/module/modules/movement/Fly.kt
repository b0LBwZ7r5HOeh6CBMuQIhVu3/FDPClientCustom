/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.features.module.modules.exploit.Damage
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.ClassUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.*
import net.minecraft.network.play.server.S19PacketEntityStatus
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.sqrt

@ModuleInfo(name = "Fly", category = ModuleCategory.MOVEMENT, autoDisable = EnumAutoDisableType.FLAG, keyBind = Keyboard.KEY_F)
class Fly : Module() {
    private val modes = ClassUtils.resolvePackage("${this.javaClass.`package`.name}.flys", FlyMode::class.java)
        .map { it.newInstance() as FlyMode }
        .sortedBy { it.modeName }

    private val mode: FlyMode
        get() = modes.find { modeValue.equals(it.modeName) } ?: throw NullPointerException() // this should not happen

    private val modeValue: ListValue = object : ListValue("Mode", modes.map { it.modeName }.toTypedArray(), "Vanilla") {
        override fun onChange(oldValue: String, newValue: String) {
            if (state) onDisable()
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state) onEnable()
        }
    }

    private val motionResetValue = BoolValue("MotionReset", false)

    private val needDamageValue = BoolValue("NeedDamage", false)
    private val damageTimeValue = IntegerValue("DamageTime", 1300, 500, 5000).displayable { needDamageValue.get() }
    private val toggleDamageModule = BoolValue("ToggleDamageModule", true).displayable { needDamageValue.get() }

    // Visuals
    private val markValue = ListValue("Mark", arrayOf("Up", "Down", "Off"), "Up")
    private val fakeDamageValue = BoolValue("FakeDamage", false)
    private val viewBobbingValue = BoolValue("ViewBobbing", false)
    private val viewBobbingYawValue = FloatValue("ViewBobbingYaw", 0.1f, 0f, 0.5f)

    var launchX = 0.0
    var launchY = 0.0
    var launchZ = 0.0
    var launchYaw = 0f
    var launchPitch = 0f

    var antiDesync = false

    val damagedTimer = MSTimer()

    override fun onEnable() {
        antiDesync = false
        if (mc.thePlayer.onGround && fakeDamageValue.get()) {
            val event = PacketEvent(S19PacketEntityStatus(mc.thePlayer, 2.toByte()), PacketEvent.Type.RECEIVE)
            LiquidBounce.eventManager.callEvent(event)
            if (!event.isCancelled) {
                mc.thePlayer.handleStatusUpdate(2.toByte())
            }
        }

        launchX = mc.thePlayer.posX
        launchY = mc.thePlayer.posY
        launchZ = mc.thePlayer.posZ
        launchYaw = mc.thePlayer.rotationYaw
        launchPitch = mc.thePlayer.rotationPitch

        if (needDamageValue.get() && toggleDamageModule.get()) {
            LiquidBounce.moduleManager[Damage::class.java]!!.onEnable()
        }

        mode.onEnable()
    }

    override fun onDisable() {
        antiDesync = false
        mc.thePlayer.capabilities.isFlying = false
        mc.thePlayer.capabilities.flySpeed = 0.05f
        mc.thePlayer.noClip = false

        mc.timer.timerSpeed = 1F
        mc.thePlayer.speedInAir = 0.02F
        mc.thePlayer.speedOnGround = 0.1F

        if (motionResetValue.get()) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionZ = 0.0
        }

        mode.onDisable()
    }

    @EventTarget
    fun onRender3d(event: Render3DEvent) {
        if (markValue.equals("Off")) {
            return
        }

        RenderUtils.drawPlatform(
            if (markValue.equals("Up")) launchY + 2.0 else launchY,
            if (mc.thePlayer.entityBoundingBox.maxY < launchY + 2.0) Color(0, 255, 0, 90) else Color(255, 0, 0, 90),
            1.0)
//        mode.onRender3d(event)
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(needDamageValue.get() && mc.thePlayer.hurtTime > 0) {
            damagedTimer.reset()
        }
        mode.onUpdate(event)
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(viewBobbingValue.get()) {
            mc.thePlayer.cameraYaw = viewBobbingYawValue.get()
            mc.thePlayer.prevCameraYaw = viewBobbingYawValue.get()
        }
        mode.onMotion(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        mode.onPacket(event)
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if(needDamageValue.get() && damagedTimer.hasTimePassed(damageTimeValue.get().toLong())) {
            event.zero()
            return
        }
        mode.onMove(event)
    }

    @EventTarget
    fun onBlockBB(event: BlockBBEvent) {
        mode.onBlockBB(event)
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        mode.onJump(event)
    }

    @EventTarget
    fun onStep(event: StepEvent) {
        mode.onStep(event)
    }
    @EventTarget
    fun onTick(event: TickEvent) {
        mode.onTick(event)
    }
    @EventTarget
    fun onPostTick(event: PostTickEvent) {
        mode.onPostTick(event)
    }
    override val tag: String
        get() = modeValue.get()

    /**
     * 读取mode中的value并和本体中的value合并
     * 所有的value必须在这个之前初始化
     */
    override val values = super.values.toMutableList().also { modes.map { mode -> mode.values.forEach { value -> it.add(value.displayable { modeValue.equals(mode.modeName) }) } } }
}
