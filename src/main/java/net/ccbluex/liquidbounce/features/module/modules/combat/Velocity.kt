//  updated at : 2022/1/24.
//

/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.movement.Speed
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0FPacketConfirmTransaction
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


@ModuleInfo(name = "Velocity", category = ModuleCategory.COMBAT)
class Velocity : Module() {

    /**
     * OPTIONS
     */
    private val horizontalValue = FloatValue("Horizontal", 0F, 0F, 1F)
    private val verticalValue = FloatValue("Vertical", 0F, 0F, 1F)
    private val modeValue = ListValue(
        "Mode", arrayOf(
            "Simple", "Simple2", "AEMine", "Vanilla", "Tick", "OldAC", "AACPush", "AACZero", "AAC4Reduce", "AAC5Reduce", "AACPull", "AACUltraPull",
            "Redesky1", "Redesky2", "RedeSky3", "HYT1", "HYT2", "HYT3", "HYT4",
            "Vulcan", "hycraft",
            "AAC5.2.0", "AAC5.2.0Combat",
            "MatrixReduce", "MatrixSimple", "MatrixReverse",
            "Reverse", "SmoothReverse",
            "Jump",
            "Phase", "PacketPhase", "Glitch", "Spoof", "SlowDown", "NoMove", "Freeze",
            "Legit"
        ), "Simple"
    )
    private val velocityTickValue = IntegerValue("VelocityTick", 1, 0, 10).displayable { modeValue.equals("Tick") || modeValue.equals("OldSpartan") }
    private val velocityDelayValue = IntegerValue("VelocityDelay", 80, 0, 300).displayable { modeValue.equals("Simple2") }

    // Reverse
    private val reverseStrengthValue = FloatValue("ReverseStrength", 1F, 0.1F, 1F).displayable { modeValue.equals("strafe") }
    private val reverse2StrengthValue = FloatValue("SmoothReverseStrength", 0.05F, 0.02F, 0.1F).displayable { modeValue.equals("SmoothReverse") }

    // AAC Push
    private val aacPushXZReducerValue = FloatValue("AACPushXZReducer", 2F, 1F, 3F).displayable { modeValue.equals("AACPush") }
    private val aacPushYReducerValue = BoolValue("AACPushYReducer", true).displayable { modeValue.equals("AACPush") }

    private val oldACSlowValue = BoolValue("OldACSlow", true).displayable { modeValue.equals("OldAC") }

    // phase
    private val phaseHeightValue = FloatValue("PhaseHeight", 0.5F, 0F, 1F)
        .displayable { modeValue.contains("Phase") }
    private val phaseOnlyGround = BoolValue("PhaseOnlyGround", true)
        .displayable { modeValue.contains("Phase") }

    // legit
    private val legitStrafeValue = BoolValue("LegitStrafe", false).displayable { modeValue.equals("Legit") }
    private val legitFaceValue = BoolValue("LegitFace", true).displayable { modeValue.equals("Legit") }

    private val rspAlwaysValue = BoolValue("RedeskyAlwaysReduce", true)
        .displayable { modeValue.contains("RedeSky") }
    private val rspDengerValue = BoolValue("RedeskyOnlyDanger", false)
        .displayable { modeValue.contains("RedeSky") }

    // Hycraft
    private val hycraftY0 = BoolValue("Hycraft-ZeroY", true).displayable { modeValue.equals("hycraft") }
    private val hycraftSilentFlag = BoolValue("Hycraft-SilentFlag", true).displayable { modeValue.equals("hycraft") }

    private val onlyGroundValue = BoolValue("OnlyGround", false)
    private val onlyCombatValue = BoolValue("OnlyCombat", false)
    private val onlyHitVelocityValue = BoolValue("OnlyHitVelocity", false)
    private val noFireValue = BoolValue("noFire", false)
    private val alertValue = BoolValue("alert", false)
    private val timerValue = FloatValue("timer", 0.8F, 0.1F, 1F)
    private val timerOnlyPullUpValue = BoolValue("timerOnlyPullUpValue", true)

    private val overrideDirectionValue = ListValue("OverrideDirection", arrayOf("None", "Hard", "Offset"), "None")
    private val overrideDirectionYawValue = FloatValue("OverrideDirectionYaw", 0F, -180F, 180F)
        .displayable { !overrideDirectionValue.equals("None") }

    /**
     * VALUES
     */
    private var velocityTimer = MSTimer()
    private var velocityCalcTimer = MSTimer()
    private var velocityInput = false
    private var velocityTick = 0

    // SmoothReverse
    private var reverseHurt = false

    // AACPush
    private var jump = false

    // Legit
    private var pos: BlockPos? = null

    private var redeCount = 24
    private var usedTimer = false

    private var templateX = 0
    private var templateY = 0
    private var templateZ = 0

    private var templateZA = 0.00
    private var templateYA = 0.00
    private var templateXA = 0.00

    private var isMatrixOnGround = false

    override val tag: String
        get() = modeValue.get()

    override fun onDisable() {
        mc.thePlayer?.speedInAir = 0.02F
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
        // if(onlyHitVelocityValue.get() && mc.thePlayer.motionY<0.05) return；
        if (noFireValue.get() && mc.thePlayer.isBurning) return
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (velocityInput) {
            velocityTick++
        } else velocityTick = 0

        if (redeCount < 24) redeCount++
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
        // if(onlyHitVelocityValue.get() && mc.thePlayer.motionY<0.05) return；
        if (noFireValue.get() && mc.thePlayer.isBurning) return
        if (usedTimer) {
            mc.timer.timerSpeed = 1F
            usedTimer = false
        }

        if (timerValue.get() < 1F && mc.thePlayer.hurtTime > 0 && (mc.thePlayer.motionY > 0 || !timerOnlyPullUpValue.get())) {
            mc.timer.timerSpeed = timerValue.get()
            usedTimer = true
        }
        when (modeValue.get().lowercase()) {
            "tick" -> {
                if (velocityTick > velocityTickValue.get()) {
                    if (mc.thePlayer.motionY > 0) mc.thePlayer.motionY = 0.0
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.jumpMovementFactor = -0.00001f
                    velocityInput = false
                }
                if (mc.thePlayer.onGround && velocityTick > 1) {
                    velocityInput = false
                }
            }

            "aemine" -> {
                if (mc.thePlayer.hurtTime <= 0) {
                    return
                }
                if (mc.thePlayer.hurtTime >= 6) {
                    MovementUtils.strafe()
                    mc.thePlayer.motionY *= 0.727
                } else if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(0F)
                    mc.thePlayer.motionY -= 0.095
                }
            }

            "simple2" -> if (mc.thePlayer.hurtTime > 0 && velocityTimer.hasTimePassed(velocityDelayValue.get().toLong())) {
                mc.thePlayer.motionX *= horizontalValue.get()
                mc.thePlayer.motionZ *= horizontalValue.get()
                mc.thePlayer.motionY *= verticalValue.get()
                velocityTimer.reset()
            }

            "jump" -> if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.motionY = 0.42
            }

            "oldac" -> if (mc.thePlayer.hurtTime > 0) {
                if (oldACSlowValue.get()) {
                    mc.thePlayer.motionX *= horizontalValue.get()
                    mc.thePlayer.motionZ *= horizontalValue.get()
                }
                mc.thePlayer.onGround = true
            }

            "hyt1" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.motionX *= 0.0.toDouble()
                        mc.thePlayer.motionZ *= 0.0.toDouble()
                        // mc.timer.timerSpeed = 0.9F
                    } else {
                        mc.thePlayer.motionX *= 0.5.toDouble()
                        mc.thePlayer.motionZ *= 0.5.toDouble()
                        // mc.timer.timerSpeed = 0.9F
                    }
                }
            }

            "hyt2" -> {
                if (mc.thePlayer.hurtTime <= 0) {
                    return
                }
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= 0.0.toDouble()
                    mc.thePlayer.motionZ *= 0.0.toDouble()
                    // mc.timer.timerSpeed = 0.8F
                } else {
                    mc.thePlayer.motionX *= 0.80114514.toDouble()
                    mc.thePlayer.motionZ *= 0.80114514.toDouble()
                    mc.thePlayer.motionY *= 0.80114514.toDouble()
                }
            }

            "hyt3" -> if (mc.thePlayer.hurtTime > 0) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionX *= 0.6.toDouble()
                    mc.thePlayer.motionZ *= 0.6.toDouble()
                    // mc.timer.timerSpeed = 0.8F
                } else {
                    mc.thePlayer.motionX *= 0.8.toDouble()
                    mc.thePlayer.motionZ *= 0.8.toDouble()
                    // mc.timer.timerSpeed = 0.7F
                }
            }

            "hyt4" -> {
                if (!mc.thePlayer.onGround) {
                    if (velocityInput) {
                        mc.thePlayer.speedInAir = 0.02f
                        mc.thePlayer.motionX *= 0.6.toDouble()
                        mc.thePlayer.motionZ *= 0.6.toDouble()
                    }
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    mc.thePlayer.speedInAir = 0.02f
                }
                if (mc.thePlayer.hurtTime > 0) {
                    val QQ = Math.floor(Math.random() * 4)
                    if (QQ == 0.0) {
                        mc.thePlayer.motionX = 0.0087048710342.toDouble()
                        mc.thePlayer.motionZ = 0.0087048710342.toDouble()
                    }
                    if (QQ == 1.0) {
                        mc.thePlayer.motionX = 0.0088041410129.toDouble()
                        mc.thePlayer.motionZ = 0.0088041410129.toDouble()
                    }
                    if (QQ == 2.0) {
                        mc.thePlayer.motionX = 0.00951043207.toDouble()
                        mc.thePlayer.motionZ = 0.00951043207.toDouble()
                    }
                    if (QQ == 3.0) {
                        mc.thePlayer.motionX = 0.009545643206.toDouble()
                        mc.thePlayer.motionZ = 0.009545643206.toDouble()
                    }
                    if (mc.gameSettings.keyBindForward.isKeyDown || mc.gameSettings.keyBindBack.isKeyDown || mc.gameSettings.keyBindLeft.isKeyDown || mc.gameSettings.keyBindRight.isKeyDown) {
                        mc.thePlayer.motionX = 0.0.toDouble()
                        mc.thePlayer.motionZ = 0.0.toDouble()
                    }
                }
            }

            "slowdown" -> if (mc.thePlayer.hurtTime > 0) {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
                if (mc.thePlayer.motionY > 0) mc.thePlayer.motionY = 0.0
            }

            "strafe" -> {
                if (!velocityInput) {
                    return
                }

                if (!mc.thePlayer.onGround) {
                    MovementUtils.strafe(MovementUtils.getSpeed() * reverseStrengthValue.get())
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                }
            }

            "redesky3" -> {
                if (mc.thePlayer.hurtTime == 9) {
                    velocityTick = 0
                }

                if (velocityTick < velocityTickValue.get()) {
                    mc.timer.timerSpeed = timerValue.get()
                }

                if (velocityTick == velocityTickValue.get() || mc.thePlayer == null || mc.thePlayer.ticksExisted < 3) {
                    mc.timer.timerSpeed = 1f
                }

                if (mc.thePlayer!!.hurtTime == 9) {
                    templateXA = mc.thePlayer.motionX
                    templateYA = mc.thePlayer.motionY
                    templateZA = mc.thePlayer.motionZ
                }

                if (mc.thePlayer!!.hurtTime > 3) {
                    mc.thePlayer.motionX *= horizontalValue.get()
                    mc.thePlayer.motionZ *= horizontalValue.get()
                    mc.thePlayer.motionY *= verticalValue.get()
                }

                if (velocityInput) {
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.STOP_SNEAKING
                        )
                    )
                    velocityInput = false
                }
            }

            "smoothreverse" -> {
                if (!velocityInput) {
                    mc.thePlayer.speedInAir = 0.02F
                    return
                }

                if (mc.thePlayer.hurtTime > 0) {
                    reverseHurt = true
                }

                if (!mc.thePlayer.onGround) {
                    if (reverseHurt) {
                        mc.thePlayer.speedInAir = reverse2StrengthValue.get()
                    }
                } else if (velocityTimer.hasTimePassed(80L)) {
                    velocityInput = false
                    reverseHurt = false
                }
            }

            "aac4reduce" -> {
                if (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && velocityInput && velocityTimer.hasTimePassed(80L)) {
                    mc.thePlayer.motionX *= 0.62
                    mc.thePlayer.motionZ *= 0.62
                }
                if (velocityInput && (mc.thePlayer.hurtTime < 4 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5reduce" -> {
                if (mc.thePlayer.hurtTime > 1 && velocityInput) {
                    mc.thePlayer.motionX *= 0.81
                    mc.thePlayer.motionZ *= 0.81
                }
                if (velocityInput && (mc.thePlayer.hurtTime < 5 || mc.thePlayer.onGround) && velocityTimer.hasTimePassed(120L)) {
                    velocityInput = false
                }
            }

            "aac5.2.0combat" -> {
                if (mc.thePlayer.hurtTime > 0 && velocityInput) {
                    velocityInput = false
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    mc.thePlayer.motionY = 0.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }
                if (velocityTimer.hasTimePassed(80L) && velocityInput) {
                    velocityInput = false
                    mc.thePlayer.motionX = templateX / 8000.0
                    mc.thePlayer.motionZ = templateZ / 8000.0
                    mc.thePlayer.motionY = templateY / 8000.0
                    mc.thePlayer.jumpMovementFactor = -0.002f
                }
            }

            "aacpush" -> {
                if (jump) {
                    if (mc.thePlayer.onGround) {
                        jump = false
                    }
                } else {
                    // Strafe
                    if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.motionX != 0.0 && mc.thePlayer.motionZ != 0.0) {
                        mc.thePlayer.onGround = true
                    }

                    // Reduce Y
                    if (mc.thePlayer.hurtResistantTime > 0 && aacPushYReducerValue.get() &&
                        !LiquidBounce.moduleManager[Speed::class.java]!!.state
                    ) {
                        mc.thePlayer.motionY -= 0.014999993
                    }
                }

                // Reduce XZ
                if (mc.thePlayer.hurtResistantTime >= 19) {
                    val reduce = aacPushXZReducerValue.get()

                    mc.thePlayer.motionX /= reduce
                    mc.thePlayer.motionZ /= reduce
                }
            }

            "matrixreduce" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (mc.thePlayer.onGround) {
                        if (mc.thePlayer.hurtTime <= 6) {
                            mc.thePlayer.motionX *= 0.70
                            mc.thePlayer.motionZ *= 0.70
                        }
                        if (mc.thePlayer.hurtTime <= 5) {
                            mc.thePlayer.motionX *= 0.80
                            mc.thePlayer.motionZ *= 0.80
                        }
                    } else if (mc.thePlayer.hurtTime <= 10) {
                        mc.thePlayer.motionX *= 0.60
                        mc.thePlayer.motionZ *= 0.60
                    }
                }
            }

            "matrixnew" -> {
                if (mc.thePlayer.hurtResistantTime == 16) {
                    mc.thePlayer.motionX *= 0.25;
                    mc.thePlayer.motionZ *= 0.25;
                }
            }

            "matrixold" -> {
                if (mc.thePlayer.hurtTime == 9) {
                    templateXA = mc.thePlayer.motionX;
                    templateZA = mc.thePlayer.motionZ;
                } else if (mc.thePlayer.hurtResistantTime == 15) {
                    mc.thePlayer.motionX = -templateXA * 0.275;
                    mc.thePlayer.motionZ = -templateZA * 0.75;
                    mc.thePlayer.motionY = -0.0535;
                }
            }

            "aacpull" -> {
                if (mc.thePlayer.hurtTime == 9) {
                    templateXA = mc.thePlayer.motionX;
                    templateZA = mc.thePlayer.motionZ;
                } else if (mc.thePlayer.hurtTime == 4) {
                    mc.thePlayer.motionX = -templateXA * 0.6;
                    mc.thePlayer.motionZ = -templateZA * 0.6;
                }
            }

            "aacultrapull" -> {
                if (mc.thePlayer.hurtTime == 9) {
                    templateXA = mc.thePlayer.motionX;
                    templateZA = mc.thePlayer.motionZ;
                } else if (mc.thePlayer.hurtTime == 8) {
                    mc.thePlayer.motionX = -templateXA * 0.45;
                    mc.thePlayer.motionZ = -templateZA * 0.45;
                }
            }

            "matrixnewtest" -> {
                if (mc.thePlayer.hurtResistantTime == 15) {
                    mc.thePlayer.motionX *= 0.75;
                    mc.thePlayer.motionZ *= 0.75;
                    mc.thePlayer.motionY *= -0.75;
                    mc.thePlayer.setVelocity(0.toDouble(), mc.thePlayer.motionY, 0.toDouble())
                }
            }

            "matrixground" -> {
                isMatrixOnGround = mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown
                if (isMatrixOnGround) mc.thePlayer.onGround = false
            }

            "glitch" -> {
                mc.thePlayer.noClip = velocityInput

                if (mc.thePlayer.hurtTime == 7) {
                    mc.thePlayer.motionY = 0.4
                }

                velocityInput = false
            }

            "aaczero" -> {
                if (mc.thePlayer.hurtTime > 0) {
                    if (!velocityInput || mc.thePlayer.onGround || mc.thePlayer.fallDistance > 2F) {
                        return
                    }

                    mc.thePlayer.addVelocity(0.0, -1.0, 0.0)
                    mc.thePlayer.onGround = true
                } else {
                    velocityInput = false
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent?) {
        if (event == null) return

        if (mc.thePlayer.hurtTime > 0) {
            if (noFireValue.get() && mc.thePlayer.isBurning) return
            if (modeValue.get().lowercase() == "nomove") {
                event.zeroXZ()
            }
            if (modeValue.get().lowercase() == "freeze") {
                event.zero()
            }
        }

    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || (onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }
        if (noFireValue.get() && mc.thePlayer.isBurning) return
        val packet = event.packet
        if (packet is C03PacketPlayer && mc.thePlayer.hurtTime > 0) {
            when (modeValue.get().lowercase()) {
                "freeze" -> event.cancelEvent()
            }
        } else if (packet is C0FPacketConfirmTransaction && mc.thePlayer.hurtTime > 0) {
            if (modeValue.equals("Vulcan") && packet.uid > 0) {
                event.cancelEvent()
                if (alertValue.get()) alert("lag " + packet.uid.toString())
            }
        } else if (packet is S08PacketPlayerPosLook) {
            if (modeValue.get().equals("hycraft", true) && hycraftSilentFlag.get() && mc.thePlayer.onGround) {
                mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), true))
            }
        } else if (packet is S12PacketEntityVelocity) {
            if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                return
            }
            if (onlyHitVelocityValue.get() &&
                (abs(packet.getMotionX()) < 0.02 || abs(packet.getMotionZ()) < 0.02 || abs(packet.getMotionY()) < 10) //anti-cheat test
                || (abs(packet.getMotionX()) > 6999 || abs(packet.getMotionZ()) > 6999) //p2w server long-jump block
            )
                return
            if (alertValue.get()) {
                alert(
                    "Velocity §7» " + (packet.getMotionX().toString() + packet.getMotionY() + packet.getMotionZ())
                )
            }
            velocityTimer.reset()
            velocityTick = 0

            if (!overrideDirectionValue.equals("None")) {
                val yaw = Math.toRadians(
                    if (overrideDirectionValue.get() == "Hard") {
                        overrideDirectionYawValue.get()
                    } else {
                        mc.thePlayer.rotationYaw + overrideDirectionYawValue.get() + 90
                    }.toDouble()
                )
                val dist = sqrt((packet.motionX * packet.motionX + packet.motionZ * packet.motionZ).toDouble())
                val x = cos(yaw) * dist
                val z = sin(yaw) * dist
                packet.motionX = x.toInt()
                packet.motionZ = z.toInt()
            }

            when (modeValue.get().lowercase()) {
                "tick" -> {
                    velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "simple" -> {
                    //velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    /*if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }*/

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "vulcan" -> {
                    //velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }

                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                }

                "hycraft" -> {
                    if (mc.thePlayer.onGround) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ, true))
                        packet.motionX = (packet.getMotionX() * 0F).toInt()
                        packet.motionY = (if (hycraftY0.get()) packet.getMotionY() * 0F else packet.getMotionY()).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0F).toInt()
                    } else {
                        event.cancelEvent()
                    }
                }

                "redesky3" -> {
                    //velocityInput = true
                    val horizontal = horizontalValue.get()
                    val vertical = verticalValue.get()

                    /*if (horizontal == 0F && vertical == 0F) {
                        event.cancelEvent()
                    }*/
                    mc.netHandler.addToSendQueue(
                        C0BPacketEntityAction(
                            mc.thePlayer,
                            C0BPacketEntityAction.Action.START_SNEAKING
                        )
                    )
                    // Does blocking also reduce knockback? XD
                    packet.motionX = (packet.getMotionX() * horizontal).toInt()
                    packet.motionY = (packet.getMotionY() * vertical).toInt()
                    packet.motionZ = (packet.getMotionZ() * horizontal).toInt()
                    velocityInput = true
                }

                "vanilla" -> {
                    event.cancelEvent()
                }

                "matrixsimple" -> {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (mc.thePlayer.onGround) {
                        packet.motionX = (packet.getMotionX() * 0.9).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.9).toInt()
                    }
                }

                "matrixground" -> {
                    packet.motionX = (packet.getMotionX() * 0.36).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.36).toInt()
                    if (isMatrixOnGround) {
                        packet.motionY = (-628.7).toInt()
                        packet.motionX = (packet.getMotionX() * 0.6).toInt()
                        packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                    }
                }

                "matrixreverse" -> {
                    packet.motionX = (packet.getMotionX() * -0.3).toInt()
                    packet.motionZ = (packet.getMotionZ() * -0.3).toInt()
                }

                "aac4reduce" -> {
                    velocityInput = true
                    packet.motionX = (packet.getMotionX() * 0.6).toInt()
                    packet.motionZ = (packet.getMotionZ() * 0.6).toInt()
                }

                "aac5.2.0" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ, true))
                }

                "aac5reduce", "strafe", "smoothreverse", "aaczero" -> velocityInput = true

                "phase" -> {
                    if (!mc.thePlayer.onGround && phaseOnlyGround.get()) {
                        return
                    }

                    velocityInput = true
                    mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ)
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }

                "aac5.2.0combat" -> {
                    event.cancelEvent()
                    velocityInput = true
                    templateX = packet.motionX
                    templateZ = packet.motionZ
                    templateY = packet.motionY
                }

                "spoof" -> {
                    event.cancelEvent()
                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + packet.motionX / 8000.0, mc.thePlayer.posY + packet.motionY / 8000.0, mc.thePlayer.posZ + packet.motionZ / 8000.0, false))
                }

                "packetphase" -> {
                    if (!mc.thePlayer.onGround && phaseOnlyGround.get()) {
                        return
                    }

                    mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY - phaseHeightValue.get(), mc.thePlayer.posZ, false))
                    event.cancelEvent()
                    packet.motionX = 0
                    packet.motionY = 0
                    packet.motionZ = 0
                }

                "huayuting" -> {

                }

                "glitch" -> {
                    if (!mc.thePlayer.onGround) {
                        return
                    }

                    velocityInput = true
                    event.cancelEvent()
                }

                "legit" -> {
                    pos = BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
                }

                "redesky2" -> {
                    if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                        return
                    }

                    val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get() + 1) ?: return
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                    packet.motionX = 0
                    packet.motionZ = 0
                    for (i in 0..redeCount) {
                        // mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                        mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                    }
                    if (redeCount > 12) redeCount -= 5
                }

                "redesky1" -> {
                    if (packet.getMotionX() == 0 && packet.getMotionZ() == 0) { // ignore horizonal velocity
                        return
                    }

                    if (rspDengerValue.get()) {
                        val pos = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, packet.motionX / 8000.0, packet.motionY / 8000.0, packet.motionZ / 8000.0, 0f, 0f, 0f, 0f).findCollision(60)
                        if (pos != null && pos.y > (mc.thePlayer.posY - 7)) {
                            return
                        }
                    }

                    val target = LiquidBounce.combatManager.getNearByEntity(LiquidBounce.moduleManager[KillAura::class.java]!!.rangeValue.get()) ?: return
                    if (rspAlwaysValue.get()) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                        // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                        packet.motionX = 0
                        packet.motionZ = 0
                        // event.cancelEvent() better stuff
                    }

                    if (velocityCalcTimer.hasTimePassed(500)) {
                        if (!rspAlwaysValue.get()) {
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionZ = 0.0
                            // mc.thePlayer.motionY=(packet.motionY/8000f)*1.0
                            packet.motionX = 0
                            packet.motionZ = 0
                        }
                        val count = if (!velocityCalcTimer.hasTimePassed(800)) {
                            8
                        } else if (!velocityCalcTimer.hasTimePassed(1200)) {
                            12
                        } else {
                            25
                        }
                        for (i in 0..count) {
                            // mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                        }
                        velocityCalcTimer.reset()
                    } else {
                        packet.motionX = (packet.motionX * 0.6).toInt()
                        packet.motionZ = (packet.motionZ * 0.6).toInt()
                        for (i in 0..4) {
                            // mc.thePlayer.sendQueue.addToSendQueue(C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK))
                            mc.thePlayer.sendQueue.addToSendQueue(C0APacketAnimation())
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        when (modeValue.get().lowercase()) {
            "legit" -> {
                if (pos == null || mc.thePlayer.hurtTime <= 0) {
                    return
                }

                val rot = RotationUtils.getRotations(pos!!.x.toDouble(), pos!!.y.toDouble(), pos!!.z.toDouble())
                if (legitFaceValue.get()) {
                    RotationUtils.setTargetRotation(rot)
                }
                val yaw = rot.yaw
                if (legitStrafeValue.get()) {
                    val speed = MovementUtils.getSpeed()
                    val yaw1 = Math.toRadians(yaw.toDouble())
                    mc.thePlayer.motionX = -sin(yaw1) * speed
                    mc.thePlayer.motionZ = cos(yaw1) * speed
                } else {
                    var strafe = event.strafe
                    var forward = event.forward
                    val friction = event.friction

                    var f = strafe * strafe + forward * forward

                    if (f >= 1.0E-4F) {
                        f = MathHelper.sqrt_float(f)

                        if (f < 1.0F) {
                            f = 1.0F
                        }

                        f = friction / f
                        strafe *= f
                        forward *= f

                        val yawSin = MathHelper.sin((yaw * Math.PI / 180F).toFloat())
                        val yawCos = MathHelper.cos((yaw * Math.PI / 180F).toFloat())

                        mc.thePlayer.motionX += strafe * yawCos - forward * yawSin
                        mc.thePlayer.motionZ += forward * yawCos + strafe * yawSin
                    }
                }
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isInWeb || (onlyGroundValue.get() && !mc.thePlayer.onGround)) {
            return
        }

        if ((onlyGroundValue.get() && !mc.thePlayer.onGround) || (onlyCombatValue.get() && !LiquidBounce.combatManager.inCombat)) {
            return
        }

        when (modeValue.get().lowercase()) {
            "aacpush" -> {
                jump = true

                if (!mc.thePlayer.isCollidedVertically) {
                    event.cancelEvent()
                }
            }

            "aaczero" -> if (mc.thePlayer.hurtTime > 0) {
                event.cancelEvent()
            }
        }
    }
}
