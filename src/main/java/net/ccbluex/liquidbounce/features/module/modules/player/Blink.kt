/*
 *
 *  * MotherF▉▉▉▉▉▉▉▉▉Client Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.render.Breadcrumbs
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.math.min
import kotlin.math.roundToInt
import java.util.concurrent.LinkedBlockingQueue

@ModuleInfo(name = "Blink", category = ModuleCategory.PLAYER)
class Blink : Module() {
    private val packets = LinkedBlockingQueue<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    private var disableLogger = false
    private val positions = LinkedList<DoubleArray>()
    private val pulseValue = BoolValue("Pulse", false)
    private val pingValue = BoolValue("ping", false)
    private val actionValue = BoolValue("action", true)
    private val moveValue = BoolValue("move", true)
    private val movingCheckValue = BoolValue("movingCheck", true).displayable { moveValue.get() }
    private val pingCalcValue = BoolValue("PingCalc", true)
    private val pingCalcDupePacketValue = BoolValue("PingCalcDupePacket", true).displayable { pingCalcValue.get() }
    private val dupePacketsPerMSValue = IntegerValue("dupePacketsPerMS", 30, 5, 500).displayable { pingCalcValue.get() && pingCalcDupePacketValue.get() }
    private val maxDupePacketsValue = IntegerValue("MaxDupePackets", 5, 0, 15).displayable { pingCalcValue.get() && pingCalcDupePacketValue.get() }
    private val dupeC00Value = BoolValue("dupeC00", true).displayable { pingCalcValue.get() && pingCalcDupePacketValue.get() }
    private val dupeC0FValue = BoolValue("dupeC0F", true).displayable { pingCalcValue.get() && pingCalcDupePacketValue.get() }
    private val debugValue = BoolValue("debug", true)
    private val serverSidePositionValue = BoolValue("serverSidePosition", true)
    private val pulseDelayValue = IntegerValue("PulseDelay", 1000, 500, 5000).displayable { pulseValue.get() }
    private val pulseTimer = MSTimer()
    private var packetsDuped = 0
    private var c00PacketToDupe = C00PacketKeepAlive()
    private var c0fPacketToDupe = C0FPacketConfirmTransaction()

    override fun onEnable() {
        if (mc.thePlayer == null) return

        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        // fakePlayer!!.clonePlayer(mc.thePlayer, true)
        fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
        // fakePlayer!!.rotationYawHead = mc.thePlayer.rotationYawHead
        mc.theWorld.addEntityToWorld(-1337, fakePlayer)

        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    mc.thePlayer.posX,
                    mc.thePlayer.entityBoundingBox.minY + mc.thePlayer.getEyeHeight() / 2,
                    mc.thePlayer.posZ
                )
            )
            positions.add(doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ))
        }
        pulseTimer.reset()
    }

    override fun onDisable() {
        if (mc.thePlayer == null) return
        blink()
        if (fakePlayer != null) {
            mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
            fakePlayer = null
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (mc.thePlayer == null || disableLogger) return
        if (packet is C03PacketPlayer && (!movingCheckValue.get() || packet.isMoving() || packet.getRotating()) && moveValue.get()) { // Cancel all movement stuff
            event.cancelEvent()
        }
        if (
            ((packet is C08PacketPlayerBlockPlacement ||
                    packet is C0APacketAnimation ||
                    packet is C0BPacketEntityAction ||
                    packet is C02PacketUseEntity) && actionValue.get()) ||
            ((packet is C00PacketKeepAlive || packet is C0FPacketConfirmTransaction) && pingValue.get())
        ) {
            if (packet is C0FPacketConfirmTransaction)
                c0fPacketToDupe = packet

            if (packet is C00PacketKeepAlive)
                c00PacketToDupe = packet

            event.cancelEvent()
            packets.add(packet)
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(positions) {
            positions.add(
                doubleArrayOf(
                    mc.thePlayer.posX,
                    mc.thePlayer.entityBoundingBox.minY,
                    mc.thePlayer.posZ
                )
            )
        }
        if (pulseValue.get() && pulseTimer.hasTimePassed((pulseDelayValue.get() - (if (pingCalcValue.get()) EntityUtils.getPing(mc.thePlayer) else 0)).toLong())) {
            if (pingCalcValue.get() && EntityUtils.getPing(mc.thePlayer) > pulseDelayValue.get() && pingCalcDupePacketValue.get()) {
                val packetsAmounts = min(
                    ((pulseDelayValue.get() - EntityUtils.getPing(mc.thePlayer)) / dupePacketsPerMSValue.get()).toDouble()
                        .roundToInt(),
                    if (maxDupePacketsValue.get() > 0) maxDupePacketsValue.get() else 800
                )

                repeat(packetsAmounts) {
                    if (dupeC00Value.get()) {
                        packets.add(c00PacketToDupe)
                    }
                    if (dupeC0FValue.get()) {
                        packets.add(c0fPacketToDupe)
                    }
                }
                if (debugValue.get()) alert("Blink §7» duped " + packetsAmounts.toString() + " packet(s) to make sure your ping is around " + (if (dupeC0FValue.get() || dupeC00Value.get()) "the Blink value you set" else "your ping(lol)") + " §7("/*please excuse my typing error(s)*/ + EntityUtils.getPing(mc.thePlayer)
                    .toString() + (if (dupeC00Value.get()) ">" else "<") + (if (dupeC0FValue.get()) ">" else "<") + pulseDelayValue.get()
                    .toString() + ")")
            }
            blink()
            if(serverSidePositionValue.get()){
                if(mc.theWorld!!.getEntityByID(fakePlayer!!.entityId) == null){
                    mc.theWorld.addEntityToWorld(-1337, fakePlayer)
                }
                fakePlayer!!.setPositionAndRotation(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
//              fakePlayer!!.copyLocationAndAnglesFrom(mc.thePlayer)
                fakePlayer!!.setInvisible(mc.gameSettings.thirdPersonView == 0)
                fakePlayer!!.renderDistanceWeight = if(mc.gameSettings.thirdPersonView == 0) 0.0 else 1.0
            }else{
                mc.theWorld.removeEntityFromWorld(fakePlayer!!.entityId)
            }
            pulseTimer.reset()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val breadcrumbs = LiquidBounce.moduleManager[Breadcrumbs::class.java]!!
        if(!serverSidePositionValue.get()) return
        synchronized(positions) {
            GL11.glPushMatrix()
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            GL11.glLineWidth(2F)
            GL11.glBegin(GL11.GL_LINE_STRIP)
            RenderUtils.glColor(breadcrumbs.color)
            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ
            for (pos in positions) GL11.glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)
            GL11.glColor4d(1.0, 1.0, 1.0, 1.0)
            GL11.glEnd()
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glPopMatrix()
        }
    }

    override val tag: String
        get() = packets.size.toString()

    private fun blink() {
        try {
            disableLogger = true
            while (!packets.isEmpty()) {
                mc.netHandler.networkManager.sendPacket(packets.take())
            }
            disableLogger = false
        } catch (e: Exception) {
            e.printStackTrace()
            disableLogger = false
        }
        synchronized(positions) { positions.clear() }
    }
}