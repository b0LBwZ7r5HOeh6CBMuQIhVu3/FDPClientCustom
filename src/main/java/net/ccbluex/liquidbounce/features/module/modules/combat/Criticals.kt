/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.AttackEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import java.util.*
import net.ccbluex.liquidbounce.features.module.modules.movement.Fly
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S0BPacketAnimation
import net.minecraft.stats.StatList

@ModuleInfo(name = "Criticals", category = ModuleCategory.COMBAT)
class Criticals : Module() {
val modeValue = ListValue("Mode", arrayOf("Vanilla", "Packet", "NCPPacket", "NCPPacket2", "Hypixel", "VulcanSemi", "OldHypixel", "MatrixSemi", "OldHypixel2", "Hypixel2", "Hypixel3", "huayutingTest", "AACPacket", "MiniPhase", "NanoPacket", "non-calculable", "invalid", "MiPacket", "AAC4.3.11OldHYT", "AAC5.0.14HYT", "AAC5.0.14HYT2", "Noteless", "NoGround", "Visual", "TPHop", "FakeCollide", "VerusSmart", "Mineplex", "More", "TestMinemora", "Motion", "Hover", "Matrix", "MiniPhase", "phasePacket", "packet1", "packet2", "AAC4Packet", "OldCubecraft"), "packet")
    val motionValue = ListValue("MotionMode", arrayOf("RedeSkyLowHop", "Hop", "Jump", "LowJump", "MinemoraTest", "Minis"), "Jump")
    val hoverValue = ListValue("HoverMode", arrayOf("AAC4", "AAC4Other", "OldRedesky", "Normal1", "Normal2", "Minis", "Minis2", "TPCollide", "2b2t", "Edit", "hover", "phase"), "AAC4")
    private val vanillaCritCheckValue = ListValue("VanillaCriticalCheck", arrayOf("Off", "Normal", "Strict"), "Normal")
    // private val packetHopMotionModeValue = ListValue("packetHopMotionMode", arrayOf("tp", "motion"), "tp")
    val hoverNoFall = BoolValue("HoverNoFall", true)
    val hoverCombat = BoolValue("HoverOnlyCombat", true)
    val delayValue = IntegerValue("Delay", 0, 0, 500)
    private val onGroundPacketValue = BoolValue("onGroundPacket", true)
    private val lessPacketValue = BoolValue("PacketLessPackets", true)
    private val timerValue = FloatValue("Timer", 0.82f, 0.1f, 1f)

    private val matrixTPHopValue = BoolValue("MatrixTPHop", false).displayable { modeValue.equals("Matrix") }
    private val hytMorePacketValue = BoolValue("HYTMorePacket", false).displayable { modeValue.equals("AAC5.0.14HYT") }
    private val motionSlowValue = BoolValue("motionSlow", false).displayable { modeValue.equals("Motion") }

    private val s08FlagValue = BoolValue("FlagPause", true)
    private val s08DelayValue = IntegerValue("FlagPauseTime", 100, 100, 5000).displayable { s08FlagValue.get() }

    private val hurtTimeValue = IntegerValue("HurtTime", 10, 0, 10)
    private val critRate = IntegerValue("CritRate", 100, 1, 100)
    private val lookValue = BoolValue("UseC06Packet", false)
    private val debugValue = BoolValue("DebugMessage", false)
    
    var antiDesync = false

    val msTimer = MSTimer()
    private val minemoraTimer = MSTimer()
    private var usedTimer = false
    val flagTimer = MSTimer()

    val syncTimer = MSTimer()

    private var target = 0
    private var needEdit = false
    var jState = 0
    var aacLastState = false
    var attacks = 0

    override fun onEnable() {
        if (modeValue.equals("NoGround") || modeValue.equals("Hover")) {
            if(mc.thePlayer.onGround && vanillaCritCheckValue.get() != "Off"){
                mc.thePlayer.jump()
            }else{
                if(!mc.thePlayer.onGround && vanillaCritCheckValue.get().equals("Strict") && mc.thePlayer.motionY > 0){
                    mc.thePlayer.motionY = -0.1
                }
            }
        }
        jState = 0
        attacks = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        if (event.targetEntity is EntityLivingBase) {
            val entity = event.targetEntity
            target = entity.entityId
            
            if(!vanillaCritCheckValue.get().equals("Off")){
                if ( (!mc.thePlayer.onGround && (mc.thePlayer.motionY < 0 || vanillaCritCheckValue.get().equals("Normal")) ) || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb || mc.thePlayer.isInWater ||
                mc.thePlayer.isInLava || mc.thePlayer.ridingEntity != null || entity.hurtTime > hurtTimeValue.get() ||
                LiquidBounce.moduleManager[Fly::class.java]!!.state || !msTimer.hasTimePassed(delayValue.get().toLong()) || Random().nextInt(100) > critRate.get()) {
                return
                }
            }
            
            if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
                return

            needEdit = true
            fun sendCriticalPacket(xOffset: Double = 0.0, yOffset: Double = 0.0, zOffset: Double = 0.0, ground: Boolean) {
                val x = mc.thePlayer.posX + xOffset
                val y = mc.thePlayer.posY + yOffset
                val z = mc.thePlayer.posZ + zOffset
                if (lookValue.get()) {
                    mc.netHandler.addToSendQueue(C06PacketPlayerPosLook(x, y, z, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, ground))
                } else {
                    mc.netHandler.addToSendQueue(C04PacketPlayerPosition(x, y, z, ground))
                }
            }

            antiDesync = true
            syncTimer.reset()

            when (modeValue.get().lowercase()) {
                "vanilla" -> {
                    sendCriticalPacket(yOffset = 1.0, ground = false)
                }
                "oldcubecraft" -> {
                    sendCriticalPacket(yOffset = 0.0626, ground = false)
                    sendCriticalPacket(yOffset = 9999e-18, ground = false)
                }
                "packet" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = true)
                    sendCriticalPacket(ground = false)
                    sendCriticalPacket(yOffset = 1.1E-5, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "ncppacket" -> {
                    sendCriticalPacket(yOffset = 0.11, ground = false)
                    sendCriticalPacket(yOffset = 0.1100013579, ground = false)
                    sendCriticalPacket(yOffset = 0.0000013579, ground = false)
                }
                "ncppacket2" -> {
                    sendCriticalPacket(yOffset = 0.0626, ground = onGroundPacketValue.get())
                    if (!lessPacketValue.get()) sendCriticalPacket(ground = false)
                }
                "mipacket" -> {
                    sendCriticalPacket(yOffset = 0.0625, ground = false)
                    sendCriticalPacket(ground = false)
                }
                

                "aac5.0.14hyt" -> { //AAC5.0.14HYT moment but with bad cfg(cuz it will flag for timer)
                    if (timerValue.get() != 1F) {
                        minemoraTimer.reset()
                        usedTimer = true
                        mc.timer.timerSpeed = timerValue.get()
                    }
                    if(hytMorePacketValue.get()) {sendCriticalPacket(yOffset = 0.05250000000101, ground = false)}
                    sendCriticalPacket(yOffset = 0.00133545, ground = false)
                    if (hytMorePacketValue.get()) {
                        sendCriticalPacket(yOffset = 0.01400000001010, ground = false)
                    }
                    sendCriticalPacket(yOffset = -0.000000433, ground = false)
                }
                "aac5.0.14hyt2" -> { 
                    if (timerValue.get() != 1F) {
                        minemoraTimer.reset()
                        usedTimer = true
                        mc.timer.timerSpeed = timerValue.get()
                    }
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.02324649713461, ground = false)
                    sendCriticalPacket(yOffset = 0.0014749900000101, ground = false)
                    sendCriticalPacket(yOffset = 0.12, ground = false)
                }
                "hypixel" -> {
                    sendCriticalPacket(yOffset = 0.04132332, ground = false)
                    sendCriticalPacket(yOffset = 0.023243243674, ground = false)
                    sendCriticalPacket(yOffset = 0.01, ground = false)
                    sendCriticalPacket(yOffset = 0.0011, ground = false)
                }
                "oldhypixel" -> {
                    sendCriticalPacket(ground = false)
                    sendCriticalPacket(yOffset = RandomUtils.nextDouble(0.01, 0.06), ground = false)
                    sendCriticalPacket(ground = false)
                }
                "huautingtest" -> {
                    sendCriticalPacket(yOffset = RandomUtils.nextDouble(0.00130000000001, 0.0014), ground = false)
                    sendCriticalPacket(yOffset = -0.00000074518164, ground = false)
                }
                "oldhypixel2" -> {
                    sendCriticalPacket(yOffset = RandomUtils.nextDouble(0.01, 0.06), ground = false)
                    sendCriticalPacket(ground = mc.thePlayer.onGround)
                }
                "aac4.3.11oldhyt" -> {
                    sendCriticalPacket(yOffset = 0.042487, ground = false)
                    sendCriticalPacket(yOffset = 0.0104649713461000007, ground = false)
                    sendCriticalPacket(yOffset = 0.0014749900000101, ground = false)
                    sendCriticalPacket(yOffset = 0.0000007451816400000, ground = false)
                }
                
                "vulcansemi" -> {
                    attacks++
                    if(attacks > 6) {
                        sendCriticalPacket(yOffset = 0.2, ground = false)
                        sendCriticalPacket(yOffset = 0.1216, ground = false)
                        attacks = 0
                    }else{
                        antiDesync = false
                    }
                }
                
                "noteless" -> {
                    sendCriticalPacket(yOffset = 0.11921599284565, ground = false)
                    sendCriticalPacket(yOffset = 0.00163166800276, ground = false)
                    sendCriticalPacket(yOffset = 0.15919999545217, ground = false)
                    sendCriticalPacket(yOffset=0.11999999731779,ground = false)
                }
                "matrixsemi" -> {
                    attacks++
                    if(attacks > 3) {
                        sendCriticalPacket(yOffset = 0.110314, ground = false)
                        sendCriticalPacket(yOffset = 0.0200081, ground = false)
                        sendCriticalPacket(yOffset = 0.00000001300009, ground = false)
                        sendCriticalPacket(yOffset = 0.000000000022, ground = false)
                        sendCriticalPacket(ground = true)
                        attacks = 0
                    }else{
                        antiDesync = false
                    }
                }
                
                "verussmart" -> {
                    attacks ++
                    if (attacks > 4) {
                        attacks = 0
                        
                        sendCriticalPacket(yOffset = 0.001, ground = true)
                        sendCriticalPacket(ground = false)
                    }else{
                        antiDesync = false
                    }
                }
                
                "hypixel2" -> {
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                }
                "hypixel3" -> {
                    sendCriticalPacket(yOffset = 0.0114514, ground = false)
                }
                "mineplex" -> {
                    sendCriticalPacket(yOffset = 0.0000000000000045, ground = false)
                    sendCriticalPacket(ground = false)
                }

                "more" -> {
                    sendCriticalPacket(yOffset = 0.00000000001, ground = false)
                    sendCriticalPacket(ground = false)
                }

                // Minemora criticals without test
                "testminemora" -> {
                    sendCriticalPacket(yOffset = 0.0114514, ground = false)
                    sendCriticalPacket(yOffset = 0.0010999999940395355, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.0012016413, ground = false)
                }

                "aacpacket" -> {
                    sendCriticalPacket(yOffset = 0.05250000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.01400000001304, ground = false)
                    sendCriticalPacket(yOffset = 0.00150000001304, ground = false)
                }

                "fakecollide" -> {
                    val motionX: Double
                    val motionZ: Double
                    if (MovementUtils.isMoving()) {
                        motionX = mc.thePlayer.motionX
                        motionZ = mc.thePlayer.motionZ
                    } else {
                        motionX = 0.00
                        motionZ = 0.00
                    }
                    mc.thePlayer.triggerAchievement(StatList.jumpStat)
                    sendCriticalPacket(xOffset = motionX / 3, yOffset = 0.20000004768372, zOffset = motionZ / 3, ground = false)
                    sendCriticalPacket(xOffset = motionX / 1.5, yOffset = 0.12160004615784, zOffset = motionZ / 1.5, ground = false)
                }
                "aac4packet" -> {
                    val motionX: Double
                    val motionZ: Double
                    if (MovementUtils.isMoving()) {
                        motionX = mc.thePlayer.motionX
                        motionZ = mc.thePlayer.motionZ
                    } else {
                        motionX = 0.00
                        motionZ = 0.00
                    }
                    sendCriticalPacket(xOffset = -(motionX / 3), yOffset = 3e-14, zOffset = -(motionZ / 3), ground = onGroundPacketValue.get())
                    sendCriticalPacket(xOffset = -(motionX / 1.5), yOffset = 8e-15, zOffset = -(motionZ / 1.5), ground = false)
                    mc.thePlayer.motionX *= 0.0
                    mc.thePlayer.motionZ *= 0.0
                }
                "packet1" -> {
                    sendCriticalPacket(yOffset = 8e-15, ground = onGroundPacketValue.get())
                    if (!lessPacketValue.get()) sendCriticalPacket(ground = false)
                }
                "packet2" -> {
                    sendCriticalPacket(yOffset = 0.012, ground = onGroundPacketValue.get())
                    sendCriticalPacket(yOffset = 0.012008726, ground = false)
                    if (!lessPacketValue.get()) sendCriticalPacket(ground = false)
                }
                
                "miniphase" -> {
                    sendCriticalPacket(yOffset = -0.0125, ground = false)
                    sendCriticalPacket(yOffset =  0.01275, ground = false)
                    sendCriticalPacket(yOffset = -0.00025, ground = false)
                }

                "nanopacket" -> {
                    sendCriticalPacket(yOffset =  0.00973333333333, ground = false)
                    sendCriticalPacket(yOffset =  0.001, ground = false)
                    sendCriticalPacket(yOffset = -0.01200000000007, ground = false)
                    sendCriticalPacket(yOffset = -0.0005, ground = false)

                }

                "non-calculable" -> {
                    sendCriticalPacket(yOffset =  1E-5, ground = false)
                    sendCriticalPacket(yOffset =  1E-7, ground = false)
                    sendCriticalPacket(yOffset = -1E-6, ground = false)
                    sendCriticalPacket(yOffset = -1E-4, ground = false)

                }

                "invalid" -> {
                    sendCriticalPacket(yOffset =  1E+27, ground = false)
                    sendCriticalPacket(yOffset = -1E+68, ground = false)
                    sendCriticalPacket(yOffset =  1E+41, ground = false)
                }

                "tphop" -> {
                    sendCriticalPacket(yOffset = 0.02, ground = false)
                    sendCriticalPacket(yOffset = 0.01, ground = false)
                    mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + 0.01, mc.thePlayer.posZ)
                }
/*                "packethop" -> {
                    if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox()
                            .offset(0, packetHopPacketHeightValue.get().toDouble(), 0).expand(0, 0, 0)).isEmpty()) {

                        sendCriticalPacket(yOffset = packetHopPacketHeightValue.get()
                            .toDouble(), ground = packetOnGroundValue.get())

                        if (packetHopMotionModeValue.equals("tp")) {
                            mc.thePlayer.setPositionAndUpdate(mc.thePlayer.posX, mc.thePlayer.posY + packetHopMotionValue.get()
                                .toDouble(), mc.thePlayer.posZ)
                        }
                    }
                    if (packetHopMotionModeValue.equals("motion")) {
                        mc.thePlayer.motionY = packetHopMotionValue.get().toDouble()
                    }
                }*/
                "matrix" -> {
                    if (matrixTPHopValue.get()) mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.4012362343123123126537612537125321671253715623682676, mc.thePlayer.posZ)
                    sendCriticalPacket(yOffset = 0.0672234246, ground = true)
                    sendCriticalPacket(yOffset = 0.00, ground = false)
                }
                "miniphase" -> {
                    sendCriticalPacket(yOffset = -0.0125, ground = false)
                    sendCriticalPacket(yOffset = 0.01275, ground = false)
                    sendCriticalPacket(yOffset = -0.00025, ground = true)
                }
                "phasepacket" -> {
                    sendCriticalPacket(yOffset = -8e-15, ground = false)
                }
                "visual" -> mc.thePlayer.onCriticalHit(entity)

                "motion" -> {
                    if(timerValue.get() != 1F){
                        minemoraTimer.reset()
                        usedTimer = true
                        mc.timer.timerSpeed = timerValue.get()
                    }
                    if(motionSlowValue.get()){
                        mc.thePlayer.motionX *= 0.2
                        mc.thePlayer.motionZ *= 0.2
                    }
                    when (motionValue.get().lowercase()) {
                        "jump" -> mc.thePlayer.motionY = 0.42
                        "lowjump" -> mc.thePlayer.motionY = 0.3425
                        "redeskylowhop" -> mc.thePlayer.motionY = 0.35
                        "hop" -> {
                            mc.thePlayer.motionY = 0.1
                            mc.thePlayer.fallDistance = 0.1f
                            mc.thePlayer.onGround = false
                        }
                        "minemoratest" -> {
                            mc.thePlayer.motionY = 0.114514886
                        }
                        "minis" -> {
                            mc.thePlayer.motionY = 0.0000194382390
                        }
                    }
                }
            }
            msTimer.reset()
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        
        if (packet is S08PacketPlayerPosLook) {
            flagTimer.reset()
            antiDesync = false
            if (s08FlagValue.get()) {
                jState = 0
            }
        }

        if (packet is C03PacketPlayer && (MovementUtils.isMoving() || syncTimer.hasTimePassed(1000L) || msTimer.hasTimePassed(((delayValue.get() / 5) + 75).toLong())))
            antiDesync = false
        
        if(s08FlagValue.get() && !flagTimer.hasTimePassed(s08DelayValue.get().toLong()))
            return

        if (packet is C03PacketPlayer) {
            when (modeValue.get().lowercase()) {
                "noground" -> packet.onGround = false
                "edit" -> {if(needEdit) {
                    packet.onGround = false
                    if (hoverNoFall.get() && mc.thePlayer.fallDistance > 3f && mc.thePlayer.motionY < 0.0) packet.onGround = (mc.thePlayer.ticksExisted % 2 == 0)
                    needEdit = false
                }}
                "motion" -> {
                    if(timerValue.get() != 1F) {
                        if ((mc.thePlayer.fallDistance > 0.12f) && (mc.thePlayer.fallDistance < 10f) && (mc.timer.timerSpeed < 1f) && usedTimer) {
                            mc.timer.timerSpeed = 1f
                            usedTimer = false
                        }
                    }
                }
                "aac5.0.14hyt","noteless" -> {
                    if(timerValue.get() != 1F) {
                        if (minemoraTimer.hasTimePassed(120L) && (mc.timer.timerSpeed < 1f) && usedTimer) {
                            mc.timer.timerSpeed = 1f
                            usedTimer = false
                        }
                    }
                }
                "hover" -> {
                    if (hoverCombat.get() && !LiquidBounce.combatManager.inCombat) return
                    packet.isMoving = true
                    when (hoverValue.get().lowercase()) {
                        "2b2t" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.02
                                    3 -> packet.y += 0.01
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis2" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.015625
                                } else if (jState> 100) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "tpcollide" -> {
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.20000004768372
                                    3 -> packet.y += 0.12160004615784
                                    4 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "minis" -> {
                            if (mc.thePlayer.onGround && !aacLastState) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                                jState++
                                if (jState % 2 == 0) {
                                    packet.y += 0.0625
                                } else if (jState> 50) {
                                    if (hoverNoFall.get()) packet.onGround = true
                                    jState = 1
                                }
                            } else jState = 0
                        }
                        "normal1" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.001335979112147
                                    3 -> packet.y += 0.0000000131132
                                    4 -> packet.y += 0.0000000194788
                                    5 -> packet.y += 0.00000000001304
                                    6 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "aac4other" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.00101
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.001
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "aac4" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 0.000000000000136
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 0.000000000000036
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "hover" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y += 1e-13
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y += 1e-13
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "phase" -> {
                            if (mc.thePlayer.onGround && !aacLastState && hoverNoFall.get()) {
                                packet.onGround = mc.thePlayer.onGround
                                aacLastState = mc.thePlayer.onGround
                                packet.y -= 1e-13
                                return
                            }
                            aacLastState = mc.thePlayer.onGround
                            packet.y -= 1e-13
                            if (mc.thePlayer.onGround || !hoverNoFall.get()) packet.onGround = false
                        }
                        "normal2" -> {
                            if (mc.thePlayer.onGround) {
                                if (!(hoverNoFall.get() && jState == 0)) packet.onGround = false
                                jState++
                                when (jState) {
                                    2 -> packet.y += 0.00000000000667547
                                    3 -> packet.y += 0.00000000000045413
                                    4 -> packet.y += 0.000000000000036
                                    5 -> {
                                        if (hoverNoFall.get()) packet.onGround = true
                                        jState = 1
                                    }
                                    else -> jState = 1
                                }
                            } else jState = 0
                        }
                        "oldredesky" -> {
                            if (hoverNoFall.get() && mc.thePlayer.fallDistance> 0) {
                                packet.onGround = true
                                return
                            }

                            if (mc.thePlayer.onGround) {
                                packet.onGround = false
                            }
                        }
                    }
                }
            }
        }
        if (packet is S0BPacketAnimation && debugValue.get()) {
            if (packet.animationType == 4 && packet.entityID == target) {
                alert("Criticals §7» " + packet.entityID.toString())
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}
