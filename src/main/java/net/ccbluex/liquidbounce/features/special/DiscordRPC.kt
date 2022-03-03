package net.ccbluex.liquidbounce.features.special

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.minecraft.client.Minecraft
import org.json.JSONObject
import java.time.OffsetDateTime
import kotlin.concurrent.thread
import net.ccbluex.liquidbounce.utils.misc.RandomUtils

object DiscordRPC {
    private val ipcClient = IPCClient(895930191866634281)
    private val timestamp = OffsetDateTime.now().minusYears(RandomUtils.nextInt(100,1000).toLong())
    private var running = false

    fun run() {
        ipcClient.setListener(object : IPCListener {
            override fun onReady(client: IPCClient?) {
                running = true
                thread {
                    while (running) {
                        update()
                        try {
                            Thread.sleep(5000L)
                        } catch (ignored: InterruptedException) {
                        }
                    }
                }
            }

            override fun onClose(client: IPCClient?, json: JSONObject?) {
                running = false
            }
        })
        ipcClient.connect()
    }

    private fun update() {
        val builder = RichPresence.Builder()
        builder.setStartTimestamp(timestamp)
        // builder.setLargeImage("cfb8fe2fe9169dc68f7f8c1236b885")
        builder.setDetails("207.180.208.166, 207.180.208.166, 51.222.2.8")
        // ServerUtils.getRemoteIp().also {
        builder.setState("Cloudflare can't protect your website lol")
        // presence.largeImageKey = "java"
        // presence.largeImageText = "JByteMod"
        // }

        // Check ipc client is connected and send rpc
        if (ipcClient.status == PipeStatus.CONNECTED)
            ipcClient.sendRichPresence(builder.build())
    }

    fun stop() {
        ipcClient.close()
    }
}
