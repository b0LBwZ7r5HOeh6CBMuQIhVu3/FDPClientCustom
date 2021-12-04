package net.ccbluex.liquidbounce.ui.client.hud.element.elements

import net.ccbluex.liquidbounce.ui.client.hud.element.Border
import net.ccbluex.liquidbounce.ui.client.hud.element.Element
import net.ccbluex.liquidbounce.ui.client.hud.element.ElementInfo
import net.ccbluex.liquidbounce.ui.client.hud.element.Side
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FontValue
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;


/**
 * CustomHUD text element
 *
 * Allows to draw custom text
 */
@ElementInfo(name = "QQLogo", blur = true)
class QQLogo(
    x: Double = 75.0,
    y: Double = 110.0,
    scale: Float = 1F,
    side: Side = Side(Side.Horizontal.MIDDLE, Side.Vertical.DOWN)
) : Element(x, y, scale, side) {
    private val fontValue = FontValue("Font", Fonts.font35)

    override fun drawElement(partialTicks: Float): Border {
        val font = fontValue.get()
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDepthMask(false);
        OpenGlHelper.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        glColor4f(1, 1, 1, 1);
        // mc.getTextureManager().bindTexture(new ResourceLocation("什么玩意妈妈暴毙是不是当年我把你婊子妈的臭逼用日本武士刀割下来炖烂放到离心机里把你妈的臭逼加上子宫用一百万转每秒的转速给你离心出来你这个废物东西一样我看你不三不四不痛不痒的一直虚空对线是不是想念牛鞭跟你妈的臭逼炖在一起的B味道了啊"));
        // Gui.drawModalRectWithCustomSizedTexture(0, 0, 0, 0, 60, 60, 60, 60);
        glDepthMask(true);
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        final int color = new Color(45,45,45).getRGB();
        for(float i = 26;i <= 42; ++i)
            RenderUtils.drawOutFullCircle(31.5F, 30, i, color, 5);
        RenderUtils.drawOutFullCircle(31.5F, 30, 27, new Color(53,141,204).getRGB(), 2);
        RenderUtils.drawGradientSideways(60F, 30F, 180F, 45F, new Color(45,45,45, 255).getRGB(), new Color(45,45,45, 0).getRGB());
        RenderUtils.drawOutFullCircle(31.5F, 30, 44, new Color(0,230,0).getRGB(), 3,-7F, 320F);
        RenderUtils.drawGradientSideways(60F, 1F, 200F, 26.5F, new Color(45,45,45, 255).getRGB(), new Color(45,45,45, 0).getRGB());
        font.drawString(mc.getSession().getUsername() + " | " + Math.round(mc.thePlayer.getHealth()) + "hp", 80F, 8F, new Color(200, 200, 200).getRGB());
        font.drawString(String.valueOf(mc.thePlayer.getFoodStats().getFoodLevel()), 90F, 31F, -1, false);
        font.drawString("Food", 105F, 31F, new Color(236,161,4).getRGB(), false);
        return new Border(this, this.getPositionX() - 15, this.getPositionY() - 18, this.getPositionX() + 190, this.getPositionY() + 78);
    }
}
