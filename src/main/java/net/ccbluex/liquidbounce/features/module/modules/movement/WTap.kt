
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.client.HUD
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.minecraft.client.settings.GameSettings
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

@ModuleInfo(name = "WTap", category = ModuleCategory.MOVEMENT)
class WTap : Module() {
    private val radiusValue = FloatValue("Radius", 2.0f, 0.1f, 4.0f)
    private val autoJumpValue = BoolValue("AutoJump", false)
    private var direction = true
    private var yaw = 0f

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val target = LiquidBounce.combatManager.target
        if (mc.thePlayer.getDistanceToEntity(target) <= radiusValue.get()){
            if(autoJumpValue.get()){mc.gameSettings.keyBindJump.pressed = true}
            if(noForwardValue.get()){mc.gameSettings.keyBindForward.pressed = false}
            mc.thePlayer.isSprinting = false
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val target = LiquidBounce.combatManager.target
        if (renderValue.get() && canStrafe(target)) {
            GL11.glDisable(3553)
            GL11.glEnable(2848)
            GL11.glEnable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3042)
            GL11.glBlendFunc(770, 771)
            GL11.glHint(3154, 4354)
            GL11.glHint(3155, 4354)
            GL11.glHint(3153, 4354)
            GL11.glDisable(2929)
            GL11.glDepthMask(false)
            GL11.glLineWidth(3f)

            GL11.glBegin(3)
            val x = target!!.lastTickPosX + (target.posX - target.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
            val y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
            val z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ
            val radius = radiusValue.get()
            for (i in 0..360 step 5) {
                RenderUtils.glColor(Color.getHSBColor(if (i <180) { HUD.rainbowStart.get() + (HUD.rainbowStop.get() - HUD.rainbowStart.get()) * (i / 180f) } else { HUD.rainbowStart.get() + (HUD.rainbowStop.get() - HUD.rainbowStart.get()) * (-(i-360) / 180f) }, 0.7f, 1.0f))
                GL11.glVertex3d(x - sin(i * Math.PI / 180F) * radius, y, z + cos(i * Math.PI / 180F) * radius)
            }
            GL11.glEnd()

            GL11.glDepthMask(true)
            GL11.glEnable(2929)
            GL11.glDisable(2848)
            GL11.glDisable(2881)
            GL11.glEnable(2832)
            GL11.glEnable(3553)
        }
    }

    private fun canStrafe(target: EntityLivingBase?): Boolean {
        return target != null &&
                (!holdSpaceValue.get() || mc.thePlayer.movementInput.jump) &&
                (!onlySpeedValue.get() || LiquidBounce.moduleManager[Speed::class.java]!!.state)
    }
}
