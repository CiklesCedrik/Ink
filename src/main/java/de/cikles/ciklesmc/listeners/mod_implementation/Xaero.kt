package de.cikles.ciklesmc.listeners.mod_implementation

import com.google.common.io.ByteStreams
import de.cikles.ciklesmc.core.CiklesMC
import de.cikles.ciklesmc.listeners.mod_implementation.ModImplementation.XAERO_MINIMAP
import de.cikles.ciklesmc.listeners.mod_implementation.ModImplementation.XAERO_WORLD_MAP
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.messaging.PluginMessageListener
import java.io.IOException


class Xaero : PluginMessageListener, Listener {
    // Waypoint update = 0x41 | delete 0x52
    private val connect: Byte = 0x48
    private val worldUpdate: Byte = 0x57
    private val players: ArrayList<Player> = ArrayList()

    @EventHandler
    fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        if (!players.contains(event.player)) return
        sendPlayerWorldId(event.player, XAERO_WORLD_MAP)
        sendPlayerWorldId(event.player, XAERO_MINIMAP)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerDisconnect(event: PlayerQuitEvent) {
        players.remove(event.player)
    }

    private fun sendPlayerWorldId(player: Player, channel: String) {
        try {
            val bytes = ByteStreams.newDataOutput()
            bytes.writeByte(0)
            bytes.writeChar(worldUpdate.toInt())
            bytes.writeInt(player.world.uid.hashCode())
            player.sendPluginMessage(CiklesMC.getInstance(), channel, bytes.toByteArray())
        } catch (e: IOException) {
            CiklesMC.getInstance().slF4JLogger.info("Failed to send world update packet to player: {}", player.name, e)
        }
    }


    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        if ((channel.contentEquals(XAERO_MINIMAP) || channel.contentEquals(XAERO_WORLD_MAP)) && message[2] == connect) {
            CiklesMC.getInstance().slF4JLogger.info("Player {} joined with a modded client (Xaero).", player.name)

            players.add(player)

            sendPlayerWorldId(player, XAERO_MINIMAP)
            sendPlayerWorldId(player, XAERO_WORLD_MAP)
        }
    }
}