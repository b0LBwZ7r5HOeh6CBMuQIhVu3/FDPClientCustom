/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

/*
 *
 *  * FDPClient Hacked Client
 *  * A shit open source mixin-based injection hacked client for Minecraft using Minecraft Forge based on LiquidBounce.
 *  * DeleteFDP.today
 *
 */

/*
 * LiquidBounce+ Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/WYSI-Foundation/LiquidBouncePlus/
 */
package net.ccbluex.liquidbounce.launch.data.legacyui

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.GuiBackground
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.AnimationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.EaseUtils
import net.minecraft.client.gui.*
import net.minecraftforge.fml.client.GuiModList
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.util.ResourceLocation
import java.awt.Color

import org.lwjgl.opengl.GL11

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {

    val bigLogo = ResourceLocation("fdpclient/big.png")

    var slideX: Float = 0F
    var fade: Float = 0F

    var sliderX: Float = 0F

    companion object {
        var useParallax = true
    }

    override fun initGui() {
        slideX = 0F
        fade = 0F
        sliderX = 0F
        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val creditInfo = "mainmenu by WYSI-Foundation."
        drawBackground(0)
        GL11.glPushMatrix()
        renderSwitchButton()
        Fonts.font35.drawStringWithShadow("UpdateLog", 2F, 12F, Color(0, 128, 255, 240).rgb)
        Fonts.font35.drawStringWithShadow("- the copy of this ▉▉▉▉▉▉▉ is from the ▉▉▉, and cracked fully.", 2F, 12F + (11 * 1), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- you can run this under offline without any problems.", 2F, 12F + (11 * 2), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("+ ▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉", 2F, 12F + (11 * 3), Color(128, 255, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- ▉▉▉▉▉▉'s uncrackable client died. congratulations. btw happy new year.", 2F, 12F + (11 * 4), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("+ ", 2F, 12F + (11 * 5), Color(128, 255, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- 这个版本的▉▉▉▉▉▉▉是来自▉▉▉的，并且完全离线破解。", 2F, 12F + (11 * 6), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- 祝▉▉▉▉▉▉的舔狗们，新年快乐，这是晚到的新年礼物。", 2F, 12F + (11 * 7), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- ▉▉▉▉▉ ▉▉▉ cracked by ▉▉▉▉▉▉▉▉▉", 2F, 12F + (11 * 11), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- How to Install ▉▉▉▉▉▉:", 2F, 12F + (11 * 12), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- 1. Go to %appdata%/.minecraft and drop the folders I included (mods and versions)", 2F, 12F + (11 * 13), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- 2. Open your minecraft launcher, select the ▉▉▉▉▉ I included (1.8.9)", 2F, 12F + (11 * 14), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- 4. Run Forge.", 2F, 12F + (11 * 15), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- NOTE: This client is a ▉▉▉▉▉ mod, don't try to run it like a version. Just drop the folders I included (\"mods\" containing  the client, and \"versions\" containing the forge).", 2F, 12F + (11 * 16), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("- Enjoy, I guess..", 2F, 12F + (11 * 17), Color(255, 128, 128, 240).rgb)
        Fonts.font35.drawStringWithShadow("/ ▉▉▉▉▉▉▉▉▉▉▉e▉▉▉▉▉▉▉▉▉", 2F, 12F + (11 * 8), Color(255, 255, 128, 240).rgb)
        for (i in 0..50 step 10) {
            Fonts.font35.drawStringWithShadow("- buy ▉▉▉▉▉▉▉▉▉▉.", 25F, 12F + (11 * i), Color(255, 128, 128, 200).rgb)
        }
//        for (i2 in 500 DownTo 0 step 11) {
//            Fonts.font35.drawStringWithShadow("- cracked by ▉▉▉▉▉▉▉▉▉▉.", 25F + (11 * i2), 12F + i2, Color(255, 128, 128, 200).rgb)
//        }
        Fonts.font35.drawStringWithShadow("${LiquidBounce.CLIENT_NAME} build ${LiquidBounce.CLIENT_VERSION}", 2F, height - 12F, -1)
        Fonts.font35.drawStringWithShadow(creditInfo, width - 3F - Fonts.font35.getStringWidth(creditInfo), height - 12F, -1)
        if (useParallax) moveMouseEffect(mouseX, mouseY, 10F)
        GlStateManager.disableAlpha()
        RenderUtils.drawImage2(bigLogo, width / 2F - 50F, height / 2F - 90F, 100, 100)
        GlStateManager.enableAlpha()
        renderBar(mouseX, mouseY, partialTicks)
        GL11.glPopMatrix()
        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (mouseButton != 0) return

        if (isMouseHover(2F, height - 22F, 28F, height - 12F, mouseX, mouseY))
            useParallax = !useParallax

        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F
        var index: Int = 0
        for (icon in ImageButton.values()) {
            if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY))
                when (index) {
                    0 -> mc.displayGuiScreen(GuiSelectWorld(this))
                    1 -> mc.displayGuiScreen(GuiMultiplayer(this))
                    2 -> mc.displayGuiScreen(GuiAltManager(this))
                    3 -> mc.displayGuiScreen(GuiOptions(this, this.mc.gameSettings))
                    4 -> mc.displayGuiScreen(GuiModList(this))
                    5 -> mc.displayGuiScreen(GuiBackground(this))
                    6 -> mc.shutdown()
                }

            index++
        }

        super.mouseClicked(mouseX, mouseY, mouseButton)
    }

    fun moveMouseEffect(mouseX: Int, mouseY: Int, strength: Float) {
        val mX = mouseX - width / 2
        val mY = mouseY - height / 2
        val xDelta = mX.toFloat() / (width / 2).toFloat()
        val yDelta = mY.toFloat() / (height / 2).toFloat()

        GL11.glTranslatef(xDelta * strength, yDelta * strength, 0F)
    }

    fun renderSwitchButton() {
        sliderX += if (useParallax) 2F else -2F
        if (sliderX > 12F) sliderX = 12F
        else if (sliderX < 0F) sliderX = 0F
        Fonts.font35.drawStringWithShadow("Parallax", 28F, height - 25F, -1)
        RenderUtils.drawRoundedRect(4F, height - 24F, 22F, height - 18F, 3F, if (useParallax) Color(0, 111, 255, 255).rgb else Color(140, 140, 140, 255).rgb)
        RenderUtils.drawRoundedRect(2F + sliderX, height - 26F, 12F + sliderX, height - 16F, 5F, Color.white.rgb)
    }

    fun renderBar(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val staticX = width / 2F - 120F
        val staticY = height / 2F + 20F

        RenderUtils.drawRoundedRect(staticX, staticY, staticX + 240F, staticY + 20F, 10F, Color(255, 255, 255, 100).rgb)

        var index: Int = 0
        var shouldAnimate = false
        var displayString: String? = null
        var moveX = 0F
        for (icon in ImageButton.values()) {
            if (isMouseHover(staticX + 40F * index, staticY, staticX + 40F * (index + 1), staticY + 20F, mouseX, mouseY)) {
                shouldAnimate = true
                displayString = icon.buttonName
                moveX = staticX + 40F * index
            }
            index++
        }

        if (displayString != null)
            Fonts.font35.drawCenteredString(displayString!!, width / 2F, staticY + 30F, -1)
        else
            Fonts.font35.drawCenteredString("love skid", width / 2F, staticY + 30F, -1)

        if (shouldAnimate) {
            if (fade == 0F)
                slideX = moveX
            else
                slideX = AnimationUtils.animate(moveX, slideX, 0.5F * (1F - partialTicks))

            fade += 10F
            if (fade >= 100F) fade = 100F
        } else {
            fade -= 10F
            if (fade <= 0F) fade = 0F

            slideX = AnimationUtils.animate(staticX, slideX, 0.5F * (1F - partialTicks))
        }

        if (fade != 0F)
            RenderUtils.drawRoundedRect(slideX, staticY, slideX + 40F, staticY + 20F, 10F, Color(1F, 1F, 1F, fade / 100F * 0.6F).rgb)

        index = 0
        GlStateManager.disableAlpha()
        for (i in ImageButton.values()) {
            RenderUtils.drawImage2(i.texture, staticX + 40F * index + 11F, staticY + 1F, 18, 18)
            index++
        }
        GlStateManager.enableAlpha()
    }

    fun isMouseHover(x: Float, y: Float, x2: Float, y2: Float, mouseX: Int, mouseY: Int): Boolean = mouseX >= x && mouseX < x2 && mouseY >= y && mouseY < y2

    enum class ImageButton(val buttonName: String, val texture: ResourceLocation) {
        Single("Singleplayer", ResourceLocation("fdpclient/menu/singleplayer.png")),
        Multi("Multiplayer", ResourceLocation("fdpclient/menu/multiplayer.png")),
        Alts("Alts", ResourceLocation("fdpclient/menu/alt.png")),
        Settings("Settings", ResourceLocation("fdpclient/menu/settings.png")),
        Mods("Mods/Customize", ResourceLocation("fdpclient/menu/mods.png")),
        Background("Background", ResourceLocation("fdpclient/menu/background.png")),
        Exit("Exit", ResourceLocation("fdpclient/menu/exit.png"))
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {}
}