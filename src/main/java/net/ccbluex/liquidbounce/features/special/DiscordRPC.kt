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

object DiscordRPC {
    private val ipcClient = IPCClient(814078085066522655)
    private val timestamp = OffsetDateTime.now()
    private var running = false

    fun run() {
        ipcClient.setListener(object : IPCListener {
            override fun onReady(client: IPCClient?) {
                running = true
                thread {
                    while (running) {
                        update()
                        try {
                            Thread.sleep(1300L)
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
        builder.setDetails("Working on : FAKECRACKMEAAAAAAAAA")
        // ServerUtils.getRemoteIp().also {
            builder.setState("Method : init()V")
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
