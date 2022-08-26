package net.ccbluex.liquidbounce.features.module.modules.movement.speeds.other

import net.ccbluex.liquidbounce.features.module.modules.movement.speeds.SpeedMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.event.PacketEvent
import java.util.concurrent.LinkedBlockingQueue
import net.minecraft.network.Packet

class VulcanTimer : SpeedMode("VulcanTimer") {
    private var stage = false
    private val packets = LinkedBlockingQueue<Packet<*>>()

    override fun onPreMotion() {
        if (MovementUtils.isMoving()) {
            if (/*mc.thePlayer.ticksExisted % 3 == 0 || */mc.thePlayer.ticksExisted % 4 == 0 || mc.thePlayer.ticksExisted % 5 == 0 || mc.thePlayer.ticksExisted % 7 == 0 || mc.thePlayer.ticksExisted % 8 == 0 || mc.thePlayer.ticksExisted % 9 == 0 || mc.thePlayer.ticksExisted % 10 == 0) {
                mc.timer.timerSpeed = 2.8f
                stage = true
            }else{
                mc.timer.timerSpeed = 0.4f
                stage = false
            }
        }
    }
    override fun onPacket(event: PacketEvent) {
        if(stage) {
            if(!event.isServerSide()){
                event.cancelEvent()
                packets.add(event.packet)
            }
        } else {
            while (!packets.isEmpty()) {
                mc.netHandler.networkManager.sendPacket(packets.take())
            }
        }
        
    }
}