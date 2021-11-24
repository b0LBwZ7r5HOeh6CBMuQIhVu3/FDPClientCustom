
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
    private val noForwardValue = BoolValue("noForward", false)
    private var direction = true
    private var yaw = 0f

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val target = LiquidBounce.combatManager.target
        if (target != null && mc.thePlayer.getDistanceToEntity(target) <= radiusValue.get()){
            if(autoJumpValue.get()){mc.gameSettings.keyBindJump.pressed = true}
            if(noForwardValue.get()){mc.gameSettings.keyBindForward.pressed = false}
            mc.thePlayer.isSprinting = false
        }
        mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)
        mc.gameSettings.keyBindSprint.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSprint)
        mc.gameSettings.keyBindForward.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindForward)
    }


}
