/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.Metrics.SimplePie
import net.minecraft.util.IChatComponent
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.Display
import oshi.SystemInfo
import oshi.software.os.windows.nt.CentralProcessor
import java.io.File
import java.util.*
object ClientUtils : MinecraftInstance() {
    private val logger = LogManager.getLogger("Client")
    val osType: EnumOSType = EnumOSType.UNKNOWN
    var inDevMode = System.getProperty("dev-mode") != null


    /**
     * the hardware id used to identify in bstats
     */
    val hardwareUuid: UUID

    init {
        hardwareUuid = UUID.randomUUID()
    }

    fun buildMetrics() {
        // delete files generated by old metrics
/*        val bsUuidFile = File("BS_UUID")
        if (bsUuidFile.exists())
            bsUuidFile.delete()*/

        // build metrics
        // dead metrics
        // val metrics = Metrics(LiquidBounce.CLIENT_NAME, 11076, LiquidBounce.CLIENT_VERSION, hardwareUuid.toString(), true)
/*        val metrics = Metrics(LiquidBounce.CLIENT_NAME, 11076, "UnnamedAntiLeaks#2021.6a (EZ Crack)", "UnnamedAntiLeaks#"+hardwareUuid.toString(), true)

        metrics.addCustomChart(SimplePie("config_name") {
            LiquidBounce.configManager.nowConfig
        })
        metrics.addCustomChart(SimplePie("server_address") {
            ServerUtils.getRemoteIp()
        })*/
    }

    fun logInfo(msg: String) {
        logger.info("光復香港，時代革命"+msg)
    }

    fun logWarn(msg: String) {
        logger.warn(msg)
    }

    fun logError(msg: String) {
        logger.error(msg)
    }

    fun logError(msg: String, t: Throwable) {
        logger.error(msg, t)
    }

    fun logDebug(msg: String) {
        logger.debug(msg)
    }
    const val epicTitle = "ATTENTION CITIZEN! 口口口口门 口口門 口口门 法口口 口口口 口口口口 口口口口口 六口口口門口口 The Square 口口門大口口 口右口鬥口 口灣 口口民口 口口 口口口 口疆口口口口口區 口口口口口獎 口口口 口口 口口 2020口口口口口口 ，2020口口口 ，2018口口口口口口，198口口口口口，1999口口口 ，2019口口口口口口, 2018口口口口，2015 7口口口口口，2015口口口口口 口口口，2017口口口口，2008口口口口 口主 反口 反口口 暴口 騷口 擾口 抗口 示口游口 口輪大口 口口弟子 胡耀邦 口紫陽 還口口民 和平口口 激流口口 大口口口報 九口口共口口 獨口 專口 壓口 拷口 口摘器口 口口人口 口口彩 口口門 法口口 口洪志"
    fun setTitle() {
//        Display.setTitle("光復香港，時代革命 动态网自由门 天安門 天安门 法輪功 李洪志 Free Tibet 六四天安門事件 The Tiananmen Square protests of 1989 天安門大屠殺 The Tiananmen Square Massacre 反右派鬥爭 The Anti-Rightist Struggle 大躍進政策 The Great Leap Forward 文化大革命 The Great Proletarian Cultural Revolution 人權 Human Rights 民運 Democratization 自由 Freedom 獨立 Independence 多黨制 Multi-party system 台灣 臺灣 Taiwan Formosa 中華民國 Republic of China 西藏 土伯特 唐古特 Tibet 達賴喇嘛 Dalai Lama 法輪功 Falun Dafa 新疆維吾爾自治區 The Xinjiang Uyghur Autonomous Region 諾貝爾和平獎 Nobel Peace Prize 劉暁波 Liu Xiaobo 民主 言論 思想 反共 反革命 抗議 運動 騷亂 暴亂 騷擾 擾亂 抗暴 平反 維權 示威游行 李洪志 法輪大法 大法弟子 強制斷種 強制堕胎 民族淨化 人體實驗 肅清 胡耀邦 趙紫陽 魏京生 王丹 還政於民 和平演變 激流中國 北京之春 大紀元時報 九評論共産黨 獨裁 專制 壓制 統一 監視 鎮壓 迫害 侵略 掠奪 破壞 拷問 屠殺 活摘器官 誘拐 買賣人口 遊進 走私 毒品 賣淫 春畫 賭博 六合彩 天安門 天安门 法輪功 李洪志 Winnie the Pooh 劉曉波")
        Display.setTitle(epicTitle)
//        Display.setTitle("${LiquidBounce.CLIENT_NAME} ${LiquidBounce.CLIENT_VERSION} (${LiquidBounce.CLIENT_BRANCH}) | ${LiquidBounce.CLIENT_WEBSITE}")
    }

    fun setTitle(stats:String) {
        Display.setTitle(epicTitle)

//        Display.setTitle("${LiquidBounce.CLIENT_NAME} ${LiquidBounce.CLIENT_VERSION} (${LiquidBounce.CLIENT_BRANCH}) | ${LiquidBounce.CLIENT_WEBSITE} - "+stats)
//        Display.setTitle("光復香港，時代革命 动态网自由门 天安門 天安门 法輪功 李洪志 Free Tibet 六四天安門事件 The Tiananmen Square protests of 1989 天安門大屠殺 The Tiananmen Square Massacre 反右派鬥爭 The Anti-Rightist Struggle 大躍進政策 The Great Leap Forward 文化大革命 The Great Proletarian Cultural Revolution 人權 Human Rights 民運 Democratization 自由 Freedom 獨立 Independence 多黨制 Multi-party system 台灣 臺灣 Taiwan Formosa 中華民國 Republic of China 西藏 土伯特 唐古特 Tibet 達賴喇嘛 Dalai Lama 法輪功 Falun Dafa 新疆維吾爾自治區 The Xinjiang Uyghur Autonomous Region 諾貝爾和平獎 Nobel Peace Prize 劉暁波 Liu Xiaobo 民主 言論 思想 反共 反革命 抗議 運動 騷亂 暴亂 騷擾 擾亂 抗暴 平反 維權 示威游行 李洪志 法輪大法 大法弟子 強制斷種 強制堕胎 民族淨化 人體實驗 肅清 胡耀邦 趙紫陽 魏京生 王丹 還政於民 和平演變 激流中國 北京之春 大紀元時報 九評論共産黨 獨裁 專制 壓制 統一 監視 鎮壓 迫害 侵略 掠奪 破壞 拷問 屠殺 活摘器官 誘拐 買賣人口 遊進 走私 毒品 賣淫 春畫 賭博 六合彩 天安門 天安门 法輪功 李洪志 Winnie the Pooh 劉曉波")
    }

    fun displayAlert(message: String) {
        displayChatMessage("§8[" + LiquidBounce.COLORED_NAME + "§8] §f" + message)
    }

    fun displayChatMessage(message: String) {
        if (mc.thePlayer == null) {
            logger.info("(MCChat)$message")
            return
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("text", message)
        mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()))
    }

    enum class EnumOSType(val friendlyName: String) {
        WINDOWS("win"), LINUX("linux"), MACOS("mac"), UNKNOWN("unk");
    }
}
