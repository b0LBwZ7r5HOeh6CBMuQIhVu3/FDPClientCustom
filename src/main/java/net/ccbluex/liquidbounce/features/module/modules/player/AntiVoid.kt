/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MoveEvent
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.block.BlockUtils
import net.ccbluex.liquidbounce.utils.misc.FallingPlayer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.block.BlockAir
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos

@ModuleInfo(name = "AntiVoid", category = ModuleCategory.PLAYER)
class AntiVoid : Module() {
    private val modeValue = ListValue("Mode", arrayOf("Blink", "TPBack", "MotionFlag", "PacketFlag", "GroundSpoof", "OldHypixel", "Jartex", "OldCubecraft", "MinemoraTest", "OldRedesky", "MatrixFlag", "Predict"), "Blink")
    private val maxFallDistValue = FloatValue("MaxFallDistance", 10F, 5F, 20F)
    private val resetMotion = BoolValue("ResetMotion", false).displayable { modeValue.equals("Blink") }
    private val startFallDistValue = FloatValue("BlinkStartFallDistance", 2F, 0F, 5F).displayable { modeValue.equals("Blink") }
    private val autoScaffold = BoolValue("BlinkAutoScaffold", true).displayable { modeValue.equals("Blink") }
    private val motionflagValue = FloatValue("MotionFlag-MotionY", 1.0F, 0.0F, 5.0F).displayable { modeValue.equals("MotionFlag") }
    private val voidOnly = BoolValue("OnlyVoid", true)

    private val packetCache = ArrayList<C03PacketPlayer>()
    private var blink = false
    private var canBlink = false
    private var canSpoof = false
    private var tried = false
    private var flagged = false

    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var lastRecY = 0.0

    override fun onEnable() {
        blink = false
        canBlink = false
        canSpoof = false
        lastRecY = mc.thePlayer.posY
        tried = false
        flagged = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround) {
            tried = false
            flagged = false
        }

        when (modeValue.get().lowercase()) {
            "groundspoof" -> {
                if (!voidOnly.get() || checkVoid()) {
                    canSpoof = mc.thePlayer.fallDistance > maxFallDistValue.get()
                }
            }

            "motionflag" -> {
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.motionY += motionflagValue.get()
                        mc.thePlayer.fallDistance = 0.0F
                        tried = true
                    }
                }
            }

            "minemoratest" -> {
                if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
                    posX = mc.thePlayer.prevPosX
                    posY = mc.thePlayer.prevPosY
                    posZ = mc.thePlayer.prevPosZ
                }
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, 1.7976931348623157E+308, mc.thePlayer.posZ + 10, false))
                        mc.thePlayer.setPositionAndUpdate(posX, posY + 0.42, posZ)
                        mc.thePlayer.fallDistance = 0.0F
                        tried = true
                    }
                }
            }
            "oldredesky" -> {
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && mc.thePlayer.motionY < 0.2) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.5, mc.thePlayer.posZ, false))
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 50, mc.thePlayer.posZ, false))
                        mc.thePlayer.fallDistance = 0.0F
                    }
                }
            }
            "packetflag" -> {
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 1, mc.thePlayer.posY + 1, mc.thePlayer.posZ + 1, false))
                        tried = true
                    }
                }
            }
            "matrixflag" -> {
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        repeat(10) {
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX + 257, mc.thePlayer.posY - 4567, mc.thePlayer.posZ + 257, false))
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - 257, mc.thePlayer.posY - 0, mc.thePlayer.posZ - 257, true))
                        }
                        tried = true
                    }
                }
            }
            "tpback" -> {
                if (mc.thePlayer.onGround && BlockUtils.getBlock(BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)) !is BlockAir) {
                    posX = mc.thePlayer.prevPosX
                    posY = mc.thePlayer.prevPosY
                    posZ = mc.thePlayer.prevPosZ
                }
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance > maxFallDistValue.get() && !tried) {
                        mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                        mc.thePlayer.fallDistance = 0F
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        tried = true
                    }
                }
            }

            "jartex" -> {
                canSpoof = false
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get() && mc.thePlayer.posY <lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ *= 0.838
                        mc.thePlayer.motionX *= 0.838
                        canSpoof = true
                    }
                }
                lastRecY = mc.thePlayer.posY
            }

            "oldcubecraft" -> {
                canSpoof = false
                if (!voidOnly.get() || checkVoid()) {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get() && mc.thePlayer.posY <lastRecY + 0.01 && mc.thePlayer.motionY <= 0 && !mc.thePlayer.onGround && !flagged) {
                        mc.thePlayer.motionY = 0.0
                        mc.thePlayer.motionZ = 0.0
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.jumpMovementFactor = 0.00f
                        canSpoof = true
                        if (!tried) {
                            tried = true
                            mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, (32000.0).toDouble(), mc.thePlayer.posZ, false))
                        }
                    }
                }
                lastRecY = mc.thePlayer.posY
            }

            "blink" -> {
                if (!blink) {
                    val collide = FallingPlayer(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, 0.0, 0.0, 0.0, 0F, 0F, 0F, 0F).findCollision(60)
                    if (canBlink && (collide == null || (mc.thePlayer.posY - collide.y)> startFallDistValue.get())) {
                        posX = mc.thePlayer.posX
                        posY = mc.thePlayer.posY
                        posZ = mc.thePlayer.posZ
                        motionX = mc.thePlayer.motionX
                        motionY = mc.thePlayer.motionY
                        motionZ = mc.thePlayer.motionZ

                        packetCache.clear()
                        blink = true
                    }

                    if (mc.thePlayer.onGround) {
                        canBlink = true
                    }
                } else {
                    if (mc.thePlayer.fallDistance> maxFallDistValue.get()) {
                        mc.thePlayer.setPositionAndUpdate(posX, posY, posZ)
                        if (resetMotion.get()) {
                            mc.thePlayer.motionX = 0.0
                            mc.thePlayer.motionY = 0.0
                            mc.thePlayer.motionZ = 0.0
                            mc.thePlayer.jumpMovementFactor = 0.00f
                        } else {
                            mc.thePlayer.motionX = motionX
                            mc.thePlayer.motionY = motionY
                            mc.thePlayer.motionZ = motionZ
                            mc.thePlayer.jumpMovementFactor = 0.00f
                        }

                        if (autoScaffold.get()) {
                            LiquidBounce.moduleManager[Scaffold::class.java]!!.state = true
                        }

                        packetCache.clear()
                        blink = false
                        canBlink = false
                    } else if (mc.thePlayer.onGround) {
                        blink = false

                        for (packet in packetCache) {
                            mc.netHandler.addToSendQueue(packet)
                        }
                    }
                }
            }
        }
    }

    private fun checkVoid(): Boolean {
        var i = (-(mc.thePlayer.posY-1.4857625)).toInt()
        var dangerous = true
        while (i <= 0) {
            dangerous = mc.theWorld.getCollisionBoxes(mc.thePlayer.entityBoundingBox.offset(mc.thePlayer.motionX * 0.5, i.toDouble(), mc.thePlayer.motionZ * 0.5)).isEmpty()
            i++
            if (!dangerous) break
        }
        return dangerous
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        when (modeValue.get().lowercase()) {
            "blink" -> {
                if (blink && (packet is C03PacketPlayer)) {
                    packetCache.add(packet)
                    event.cancelEvent()
                }
            }

            "groundspoof" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    packet.onGround = true
                }
            }

            "jartex" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    packet.onGround = true
                }
                if (canSpoof && (packet is S08PacketPlayerPosLook)) {
                    flagged = true
                }
            }

            "oldcubecraft" -> {
                if (canSpoof && (packet is C03PacketPlayer)) {
                    if (packet.y < 1145.141919810) event.cancelEvent()
                }
                if (canSpoof && (packet is S08PacketPlayerPosLook)) {
                    flagged = true
                }
            }

            "oldhypixel" -> {
                if (packet is S08PacketPlayerPosLook && mc.thePlayer.fallDistance> 3.125) mc.thePlayer.fallDistance = 3.125f

                if (packet is C03PacketPlayer) {
                    if (voidOnly.get() && mc.thePlayer.fallDistance >= maxFallDistValue.get() && mc.thePlayer.motionY <= 0 && checkVoid()) {
                        packet.y += 11.0
                    }
                    if (!voidOnly.get() && mc.thePlayer.fallDistance >= maxFallDistValue.get()) packet.y += 11.0
                }
            }
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        when (modeValue.get().lowercase()){
            "predict" -> if (!mc.thePlayer.onGround && FallingPlayer(mc.thePlayer).findCollision(60) == null) {
                    event.zero()
            }
        }
    }

    override val tag: String
        get() = modeValue.get()
}