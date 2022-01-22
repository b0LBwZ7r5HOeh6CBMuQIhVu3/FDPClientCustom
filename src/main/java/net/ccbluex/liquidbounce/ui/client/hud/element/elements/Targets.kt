/*
 *
 *  * MotherF▉▉▉▉▉▉▉▉▉Client Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.hud.designer.GuiHudDesigner
import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.Animation
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.roundToInt

@ElementInfo(name = "Targets")
class Targets : Element(-46.0, -40.0, 1F, Side(Side.Horizontal.MIDDLE, Side.Vertical.MIDDLE)) {
    private val modeValue = ListValue(
        "Mode",
        arrayOf(
            "Orange",
            "Novoline",
            "NewNovoline",
            "Astolfo",
            "Liquid",
            "Flux",
            "NewFlux",
            "Rise",
            "Zamorozka",
            "Arris",
            "Tenacity",
            "Slowly",
            "Fancy"
        ),
        "Rise"
    )
    private val animSpeedValue = IntegerValue("AnimSpeed", 10, 5, 20)
    private val hpAnimTypeValue = EaseUtils.getEnumEasingList("HpAnimType")
    private val hpAnimOrderValue = EaseUtils.getEnumEasingOrderList("HpAnimOrder")
    private val switchModeValue = ListValue("SwitchMode", arrayOf("Slide", "Zoom", "None"), "Slide")
    private val switchAnimTypeValue = EaseUtils.getEnumEasingList("SwitchAnimType")
    private val switchAnimOrderValue = EaseUtils.getEnumEasingOrderList("SwitchAnimOrder")
    private val switchAnimSpeedValue = IntegerValue("SwitchAnimSpeed", 20, 5, 40)
    private val backGroundAlphaValue = IntegerValue("BackGroundAlpha", 170, 0, 255)
    private val fontValue = FontValue("Font", Fonts.font40)

    private var prevTarget: EntityLivingBase? = null
    private var displayPercent = 0f
    private var lastUpdate = System.currentTimeMillis()
    private val decimalFormat = DecimalFormat("0.0")

    private var hpEaseAnimation: Animation? = null
    private var easingHP = 0f
        get() {
            if (hpEaseAnimation != null) {
                field = hpEaseAnimation!!.value.toFloat()
                if (hpEaseAnimation!!.state == Animation.EnumAnimationState.STOPPED) {
                    hpEaseAnimation = null
                }
            }
            return field
        }
        set(value) {
            if (hpEaseAnimation == null || (hpEaseAnimation != null && hpEaseAnimation!!.to != value.toDouble())) {
                hpEaseAnimation = Animation(EaseUtils.EnumEasingType.valueOf(hpAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(hpAnimOrderValue.get()), field.toDouble(), value.toDouble(), animSpeedValue.get() * 100L).start()
            }
        }

    private fun getHealth(entity: EntityLivingBase?): Float {
        return if (entity == null || entity.isDead) { 0f } else { entity.health }
    }

    override fun drawElement(partialTicks: Float): Border? {
        var target = LiquidBounce.combatManager.target
        val time = System.currentTimeMillis()
        val pct = (time - lastUpdate) / (switchAnimSpeedValue.get() * 50f)
        lastUpdate = System.currentTimeMillis()

        if (mc.currentScreen is GuiHudDesigner) {
            target = mc.thePlayer
        }
        if (target != null) {
            prevTarget = target
        }
        prevTarget ?: return getTBorder()

        if (target != null) {
            if (displayPercent < 1) {
                displayPercent += pct
            }
            if (displayPercent > 1) {
                displayPercent = 1f
            }
        } else {
            if (displayPercent > 0) {
                displayPercent -= pct
            }
            if (displayPercent < 0 || !LiquidBounce.combatManager.inCombat) {
                displayPercent = 0f
                prevTarget = null
                return getTBorder()
            }
        }

        easingHP = getHealth(target)

        val easedPersent = EaseUtils.apply(EaseUtils.EnumEasingType.valueOf(switchAnimTypeValue.get()), EaseUtils.EnumEasingOrder.valueOf(switchAnimOrderValue.get()), displayPercent.toDouble()).toFloat()
        when (switchModeValue.get().lowercase()) {
            "zoom" -> {
                val border = getTBorder() ?: return null
                GL11.glScalef(easedPersent, easedPersent, easedPersent)
                GL11.glTranslatef(((border.x2 * 0.5f * (1 - easedPersent)) / easedPersent), ((border.y2 * 0.5f * (1 - easedPersent)) / easedPersent), 0f)
            }
            "slide" -> {
                val percent = EaseUtils.easeInQuint(1.0 - easedPersent)
                val xAxis = ScaledResolution(mc).scaledWidth - renderX
                GL11.glTranslated(xAxis * percent, 0.0, 0.0)
            }
        }

        when (modeValue.get().lowercase()) {
            "orange" -> drawOrange(prevTarget!!)
            "novoline" -> drawNovo(prevTarget!!)
            "newnovoline" -> drawnewNovo(prevTarget!!)
            "astolfo" -> drawAstolfo(prevTarget!!)
            "liquid" -> drawLiquid(prevTarget!!)
            "flux" -> drawFlux(prevTarget!!)
            "rise" -> drawRise(prevTarget!!)
            "zamorozka" -> drawZamorozka(prevTarget!!)
            "arris" -> drawArris(prevTarget!!)
            "tenacity" -> drawTenacity(prevTarget!!)
            "newflux" -> drawNewFlux(prevTarget!!)
            "slowly" -> drawSlowly(prevTarget!!)
            "fancy" -> drawFancy(prevTarget!!)
        }

        return getTBorder()
    }

    private fun drawOrange(target: EntityLivingBase) {
        val font = fontValue.get()
        val h = 40f

        val additionalWidth = font.getStringWidth(target.name).coerceAtLeast(75)
        RenderUtils.drawBorderedRect(0f, 0f, 45f + additionalWidth, h,3F, Color(59, 59, 59, backGroundAlphaValue.get()).rgb,Color(50,50,50).rgb)

        // Head
        RenderUtils.drawHead(target.skin, 5, 5, 30, 30)

        // info text
        font.drawString(target.name, 40F - 0.5F, 5F, Color(0,111,255).rgb, false)
        font.drawString(target.name, 40F, 5F, Color.WHITE.rgb, false)

        val yPos = 5 + font.FONT_HEIGHT + 3f

        // hp bar
        RenderUtils.drawRect(40f, yPos, 40F + additionalWidth, yPos + 4, Color.white.rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40F + additionalWidth, yPos + 13, Color.white.rgb)

        RenderUtils.drawRect(40f, yPos, 40 + (easingHP / target.maxHealth) * additionalWidth, yPos + 4, ColorUtils.healthColor(getHealth(target), target.maxHealth).rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40 + (target.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)

    }
    private fun drawSlowly(target: EntityLivingBase) {
        /*      val font = fontValue.get()
              // val font = Fonts.minecraftFont

              val length = 60.coerceAtLeast(font.getStringWidth(target.name)).coerceAtLeast(font.getStringWidth("${decimalFormat2.format(target.health)} ❤")).toFloat() + 10F
              RenderUtils.drawRect(0F, 0F, 32F + length, 36F, Color(0, 0, 0, backGroundAlphaValue.get()).rgb)
              if (mc.netHandler.getPlayerInfo(target.uniqueID) != null) drawHead(mc.netHandler.getPlayerInfo(target.uniqueID).locationSkin, 1, 1, 30, 30)
              font.drawStringWithShadow(target.name, 33F, 2F, -1)
              font.drawStringWithShadow("${decimalFormat2.format(target.health)} ❤", length + 32F - 1F - font.getStringWidth("${decimalFormat2.format(target.health)} ❤").toFloat(), 22F, ColorUtils.healthColor(getHealth(target), target.maxHealth).rgb)

              // easingHealth += ((target.health - easingHealth) / 2.0F.pow(10.0F - fadeSpeed.get())) * RenderUtils.deltaTime

              RenderUtils.drawRect(0F, 32F, (easingHP / target.maxHealth.toFloat()).coerceIn(0F, target.maxHealth.toFloat()) * (length + 32F), 36F, ColorUtils.healthColor(getHealth(target), target.maxHealth).rgb)
      */
    }

    private fun drawnewNovo(target: EntityLivingBase){
        val font = fontValue.get()
        font.FONT_HEIGHT
        val hpcolor = ColorUtils.healthColor(getHealth(target), target.maxHealth)
        val percent = target.health / target.maxHealth * 100F
        val nameLength = (font.getStringWidth(target.name)).coerceAtLeast(font.getStringWidth("${decimalFormat.format(percent)}%")).toFloat() + 10F
        val barWidth = (target.health / target.maxHealth).coerceIn(0F, target.maxHealth) * (nameLength - 2F)

        RenderUtils.drawRect(-2F, -2F, 3F + nameLength + 42F, 2F + 42F, Color(24, 24, 24, 255).rgb)
        RenderUtils.drawRect(-1F, -1F, 2F + nameLength + 42F, 1F + 42F, Color(31, 31, 31, 255).rgb)
        RenderUtils.drawHead(target.skin, 0, 0, 36, 36)
        font.drawStringWithShadow(target.name, 2F + 42F + 1F, 2F, -1)
        RenderUtils.drawRect(2F + 42F, 15F, 36F + nameLength, 25F, Color(24, 24, 24, 255).rgb)

        val animateThingy = ((getHealth(target) / target.maxHealth).roundToInt() / 100)

        RenderUtils.drawRect(2F + 42F, 15F, 2F + 42F + animateThingy, 25F, Color.black.rgb)
        RenderUtils.drawRect(2F + 42F, 15F, 2F + 42F + barWidth, 25F, hpcolor)

        font.drawStringWithShadow("${decimalFormat.format(percent)}%", 2F + 42F + (nameLength - 2F) / 2F - font.getStringWidth("${decimalFormat.format(percent)}%").toFloat() / 2F, 16F, -1)
    }

    private fun drawAstolfo(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.skyRainbow(1, 1F, 0.9F, 5.0)
        val hpPct = easingHP / target.maxHealth

        RenderUtils.drawRect(0F, 0F, 140F, 60F, Color(0, 0, 0, 110).rgb)

        // health rect
        RenderUtils.drawRect(3F, 55F, 137F, 58F, ColorUtils.reAlpha(color, 100).rgb)
        RenderUtils.drawRect(3F, 55F, 3 + (hpPct * 134F), 58F, color.rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(18, 46, 20, target)

        font.drawStringWithShadow(target.name, 37F, 6F, -1)
        GL11.glPushMatrix()
        GL11.glScalef(2F, 2F, 2F)
        font.drawString("${getHealth(target).roundToInt()} ❤", 19, 9, color.rgb)
        GL11.glPopMatrix()
    }

    private fun drawFancy(target: EntityLivingBase) {
        val font = fontValue.get()
        val width = (48 + 9 + font.getStringWidth(target.name))
            .coerceAtLeast(100)
            .toFloat()

        RenderUtils.drawRect(0f, 0f, width, 48 + 6f, Color(0, 0, 0, 150))
        //ColorUtils.healthColor(getHealth(target), target.maxHealth)
        //RenderUtils.drawRect(0f,0f,width,2f,Color(56,245,200))
        RenderUtils.drawRect(0f, 0f, width, 2f, ColorUtils.healthColor(getHealth(target), target.maxHealth))

        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            RenderUtils.drawHead(playerInfo.locationSkin, 2, 4, 48, 48)
        }

        GL11.glTranslatef(48 + 3 + 3f, 7f, 0f)

        font.drawString(target.name, 0f, 0f, Color.WHITE)
    }
    private fun drawNewFlux(target: EntityLivingBase){
        val font = fontValue.get()
        val width = (26F + font.getStringWidth(target.name)).coerceAtLeast(26F + font.getStringWidth("Health: ${decimalFormat.format(target.health)}")) + 15F
        RenderUtils.drawCircleRect(-1F, -1F, 1F + width, 47F, 1F, Color(35,35,40,230).rgb)
        //RenderUtils.drawBorder(1F, 1F, 26F, 26F, 1F, Color(115, 255, 115).rgb)
        RenderUtils.drawHead(target.skin, 1, 1, 26, 26)
        font.drawString(target.name, 30F, 6F, 0xFFFFFF,true) // Draw target name
        font.drawString("Health: ${decimalFormat.format(getHealth(target))}", 30F, 18F, 0xFFFFFF,true) // Draw target health
        // ❤
        font.drawString("❤",2F,29F,-1,true)

        //drawArmorIcon(2,38,7,7)
        var hp = ((target.health - target.maxHealth) / 2.0.pow(10.0 - 3.0)).toFloat() * RenderUtils.deltaTime.toFloat()
        // bar bg
        RenderUtils.drawRect(12F, 30F, 12F + width - 15F, 33F, Color(20, 20, 20, 255).rgb)
        RenderUtils.drawRect(12F, 40F, 12F + width - 15F, 43F, Color(20, 20, 20, 255).rgb)
        // Health bar
        if (hp < 0 || hp > target.maxHealth) {
            hp = target.health
        }
        if (hp > target.health) {
            RenderUtils.drawRect(12F, 30F, 12F + (hp / target.maxHealth) * (width - 15F), 33F, Color(231,182,0,255).rgb)
        } // Damage animation
        RenderUtils.drawRect(12F, 30F, 12F + (target.health / target.maxHealth) * (width - 15F), 33F, Color(0,224,84,255).rgb)
        if (target.totalArmorValue != 0) {
            RenderUtils.drawRect(12F, 40F, 12F + (target.totalArmorValue / 20F) * (width - 15F), 43F, Color(77,128,255,255).rgb) // Draw armor bar
        }
    }

    private fun drawNovo(target: EntityLivingBase) {
        val font = fontValue.get()
        val color = ColorUtils.healthColor(getHealth(target), target.maxHealth)
        val darkColor = ColorUtils.darker(color, 0.6F)
        val hpPos = 33F + ((getHealth(target) / target.maxHealth * 10000).roundToInt() / 100)

        RenderUtils.drawRect(0F, 0F, 140F, 40F, Color(40, 40, 40).rgb)
        font.drawString(target.name, 33, 5, Color.WHITE.rgb)
        RenderUtils.drawEntityOnScreen(20, 35, 15, target)
        RenderUtils.drawRect(hpPos, 18F, 33F + ((easingHP / target.maxHealth * 10000).roundToInt() / 100), 25F, darkColor)
        RenderUtils.drawRect(33F, 18F, hpPos, 25F, color)
        font.drawString("❤", 33, 30, Color.RED.rgb)
        font.drawString(decimalFormat.format(getHealth(target)), 43, 30, Color.WHITE.rgb)
    }

    private fun drawLiquid(target: EntityLivingBase) {
        val font = fontValue.get()
        val width = (38 + target.name.let(font::getStringWidth))
            .coerceAtLeast(118)
            .toFloat()
        // Draw rect box
        RenderUtils.drawBorderedRect(0F, 0F, width, 36F, 3F, Color(0, 0, 0, backGroundAlphaValue.get()).rgb, Color(0, 0, 0, backGroundAlphaValue.get()).rgb)

        // Damage animation
        if (easingHP > getHealth(target)) {
            RenderUtils.drawRect(0F, 34F, (easingHP / target.maxHealth) * width,
                36F, Color(252, 185, 65).rgb)
        }

        // Health bar
        RenderUtils.drawRect(0F, 34F, (getHealth(target) / target.maxHealth) * width,
            36F, ColorUtils.healthColor(getHealth(target), target.maxHealth))

        // Heal animation
        if (easingHP < getHealth(target)) {
            RenderUtils.drawRect((easingHP / target.maxHealth) * width, 34F,
                (getHealth(target) / target.maxHealth) * width, 36F, Color(44, 201, 144).rgb)
        }

        target.name.let { font.drawString(it, 36, 3, 0xffffff) }
        font.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 36, 15, 0xffffff)

        // Draw info
        RenderUtils.drawHead(target.skin, 2, 2, 30, 30)
        val playerInfo = mc.netHandler.getPlayerInfo(target.uniqueID)
        if (playerInfo != null) {
            font.drawString("Ping: ${playerInfo.responseTime.coerceAtLeast(0)}",
                36, 24, 0xffffff)
        }
    }

    private fun drawZamorozka(target: EntityLivingBase) {
        val font = fontValue.get()

        // Frame
        RenderUtils.drawCircleRect(0f, 0f, 150f, 55f, 5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawRect(7f, 7f, 35f, 40f, Color(0, 0, 0, 70).rgb)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        RenderUtils.drawEntityOnScreen(21, 38, 15, target)

        // Healthbar
        val barLength = 143 - 7f
        RenderUtils.drawCircleRect(7f, 45f, 143f, 50f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawCircleRect(7f, 45f, 7 + ((easingHP / target.maxHealth) * barLength).coerceAtLeast(5f), 50f, 2.5f, ColorUtils.rainbowWithAlpha(90).rgb)
        RenderUtils.drawCircleRect(7f, 45f, 7 + ((target.health / target.maxHealth) * barLength).coerceAtLeast(5f), 50f, 2.5f, ColorUtils.rainbow().rgb)

        // Info
        RenderUtils.drawCircleRect(43f, 15f - font.FONT_HEIGHT, 143f, 17f, (font.FONT_HEIGHT + 1) * 0.45f, Color(0, 0, 0, 70).rgb)
        font.drawCenteredString("${target.name} ${if (target.ping != -1) { "§f${target.ping}ms" } else { "" }}", 93f, 16f - font.FONT_HEIGHT, ColorUtils.rainbow().rgb, false)
        font.drawString("Health: ${decimalFormat.format(easingHP)} §7/ ${decimalFormat.format(target.maxHealth)}", 43, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)
        font.drawString("Distance: ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))}", 43, 11 + font.FONT_HEIGHT * 2, Color.WHITE.rgb)
    }

    private fun drawRise(target: EntityLivingBase) {
        val font = fontValue.get()

        RenderUtils.drawCircleRect(0f, 0f, 150f, 50f, 5f, Color(0, 0, 0, 130).rgb)

        val hurtPercent = target.hurtPercent
        val scale = if (hurtPercent == 0f) { 1f } else if (hurtPercent <0.5f) {
            1 - (0.2f * hurtPercent * 2)
        } else {
            0.8f + (0.2f * (hurtPercent - 0.5f) * 2)
        }
        val size = 30

        GL11.glPushMatrix()
        GL11.glTranslatef(5f, 5f, 0f)
        // 受伤的缩放效果
        GL11.glScalef(scale, scale, scale)
        GL11.glTranslatef(((size * 0.5f * (1 - scale)) / scale), ((size * 0.5f * (1 - scale)) / scale), 0f)
        // 受伤的红色效果
        GL11.glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
        // 绘制头部图片
        RenderUtils.quickDrawHead(target.skin, 0, 0, size, size)
        //TODO: Skin Cache
        GL11.glPopMatrix()

        font.drawString("Name ${target.name}", 40, 11, Color.WHITE.rgb)
        font.drawString("Distance ${decimalFormat.format(mc.thePlayer.getDistanceToEntityBox(target))} Hurt ${target.hurtTime}", 40, 11 + font.FONT_HEIGHT, Color.WHITE.rgb)

        // 渐变血量条
        GL11.glEnable(3042)
        GL11.glDisable(3553)
        GL11.glBlendFunc(770, 771)
        GL11.glEnable(2848)
        GL11.glShadeModel(7425)
        fun renderSideway(x: Int, x1: Int) {
            RenderUtils.quickDrawGradientSideways(x.toDouble(), 39.0, x1.toDouble(), 45.0, ColorUtils.hslRainbow(x, indexOffset = 10).rgb, ColorUtils.hslRainbow(x1, indexOffset = 10).rgb)
        }
        val stopPos = (5 + ((135 - font.getStringWidth(decimalFormat.format(target.maxHealth))) * (easingHP / target.maxHealth))).toInt()
        for (i in 5..stopPos step 5) {
            renderSideway(i, (i + 5).coerceAtMost(stopPos))
        }
        GL11.glEnable(3553)
        GL11.glDisable(3042)
        GL11.glDisable(2848)
        GL11.glShadeModel(7424)
        GL11.glColor4f(1f, 1f, 1f, 1f)

        font.drawString(decimalFormat.format(easingHP), stopPos + 5, 43 - font.FONT_HEIGHT / 2, Color.WHITE.rgb)
    }
    
    private fun designRisePraticle() {
        /*
            SpawnDelay
            FadeInTime
            FadeOutTime
            Motion
            MotionTime
            MotionDecrease
            Position
            Colour
            Size
            
            some fancy ideas
        */
    }

    private fun drawFlux(target: EntityLivingBase) {
        val font = fontValue.get()

        val width = (38 + target.name.let(Fonts.font40::getStringWidth))
            .coerceAtLeast(70)
            .toFloat()

        // draw background
        RenderUtils.drawRect(0F, 0F, width, 34F, Color(40, 40, 40).rgb)
        RenderUtils.drawRect(2F, 22F, width - 2F, 24F, Color.BLACK.rgb)
        RenderUtils.drawRect(2F, 28F, width - 2F, 30F, Color.BLACK.rgb)

        // draw bars
        RenderUtils.drawRect(2F, 22F, 2 + (easingHP / target.maxHealth) * (width - 4), 24F, Color(231, 182, 0).rgb)
        RenderUtils.drawRect(2F, 22F, 2 + (getHealth(target) / target.maxHealth) * (width - 4), 24F, Color(0, 224, 84).rgb)
        RenderUtils.drawRect(2F, 28F, 2 + (target.totalArmorValue / 20F) * (width - 4), 30F, Color(77, 128, 255).rgb)

        // draw text
        font.drawString(target.name, 22, 3, Color.WHITE.rgb)
        GL11.glPushMatrix()
        GL11.glScaled(0.7, 0.7, 0.7)
        font.drawString("Health: ${decimalFormat.format(getHealth(target))}", (22 / 0.7F).toInt(), ((4 + Fonts.font40.height) / 0.7F).toInt(), Color.WHITE.rgb)
        GL11.glPopMatrix()

        // Draw head
        RenderUtils.drawHead(target.skin, 2, 2, 16, 16)
    }

    private fun drawArris(target: EntityLivingBase) {
        val font = fontValue.get()

        val hp = decimalFormat.format(easingHP)
        val additionalWidth = font.getStringWidth("${target.name}  $hp hp").coerceAtLeast(75)
        RenderUtils.drawCircleRect(0f, 0f, 45f + additionalWidth, 40f, 3f, Color(0, 0, 0, 110).rgb)

        RenderUtils.quickDrawHead(target.skin, 5, 5, 30, 30)

        // info text
        font.drawString(target.name, 40, 5, Color.WHITE.rgb)
        "$hp hp".also {
            font.drawString(it, 40 + additionalWidth - font.getStringWidth(it), 5, Color.LIGHT_GRAY.rgb)
        }

        // hp bar
        val yPos = 5 + font.FONT_HEIGHT + 3f
        RenderUtils.drawRect(40f, yPos, 40 + (easingHP / target.maxHealth) * additionalWidth, yPos + 4, Color.GREEN.rgb)
        RenderUtils.drawRect(40f, yPos + 9, 40 + (target.totalArmorValue / 20F) * additionalWidth, yPos + 13, Color(77, 128, 255).rgb)
    }

    private fun drawTenacity(target: EntityLivingBase) {
        val font = fontValue.get()

        val additionalWidth = font.getStringWidth(target.name).coerceAtLeast(75)
        RenderUtils.drawCircleRect(0f, 0f, 45f + additionalWidth, 40f, 3f, Color(0, 0, 0, 110).rgb)

        // circle player avatar
        mc.textureManager.bindTexture(target.skin)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 8f, 8f, 8, 8, 30, 30, 64f, 64f)
        RenderUtils.drawScaledCustomSizeModalCircle(5, 5, 40f, 8f, 8, 8, 30, 30, 64f, 64f)

        // info text
        font.drawCenteredString(target.name, 40 + (additionalWidth / 2f), 5f, Color.WHITE.rgb, false)
        "${decimalFormat.format((easingHP / target.maxHealth) * 100)}%".also {
            font.drawString(it, (40f + (easingHP / target.maxHealth) * additionalWidth - font.getStringWidth(it)).coerceAtLeast(40f), 28f - font.FONT_HEIGHT, Color.WHITE.rgb, false)
        }

        // hp bar
        RenderUtils.drawCircleRect(40f, 28f, 40f + additionalWidth, 33f, 2.5f, Color(0, 0, 0, 70).rgb)
        RenderUtils.drawCircleRect(40f, 28f, 40f + (easingHP / target.maxHealth) * additionalWidth, 33f, 2.5f, ColorUtils.rainbow().rgb)
    }

    private fun getTBorder(): Border? {
        return when (modeValue.get().lowercase()) {
            "orange" -> Border(0F, 0F, 120F, 40F)
            "novoline" -> Border(0F, 0F, 140F, 40F)
            "newnovoline" -> Border(-1F, -2F, 90F, 38F)
            "newflux" -> Border(0F,-1F,90F,47F)
            "astolfo" -> Border(0F, 0F, 140F, 60F)
            "liquid" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(fontValue.get()::getStringWidth)).coerceAtLeast(118).toFloat(), 36F)
            "flux" -> Border(0F, 0F, (38 + mc.thePlayer.name.let(fontValue.get()::getStringWidth))
                .coerceAtLeast(70)
                .toFloat(), 34F)
            "rise" -> Border(0F, 0F, 150F, 50F)
            "zamorozka" -> Border(0F, 0F, 150F, 55F)
            "arris" -> Border(0F, 0F, 120F, 40F)
            "tenacity" -> Border(0F, 0F, 120F, 40F)
            else -> null
        }
    }
}
