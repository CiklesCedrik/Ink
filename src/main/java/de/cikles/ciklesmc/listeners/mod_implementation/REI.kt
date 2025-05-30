package de.cikles.ciklesmc.listeners.mod_implementation

import com.google.gson.Gson
import de.cikles.ciklesmc.listeners.mod_implementation.ModImplementation.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.messaging.PluginMessageListener


class REI : PluginMessageListener, Listener {
    private val GSON = Gson()
    private val reiFailedCheatItems = "text.rei.failed_cheat_items"
    private val reiNoPermissionCheat = "text.rei.no_permission_cheat"
    override fun onPluginMessageReceived(channel: String, player: Player, message: ByteArray) {
        when (channel) {
            DELETE_ITEMS_PACKET, CREATE_ITEMS_GRAB_PACKET, MOVE_ITEMS_NEW_PACKET, CREATE_ITEMS_PACKET, CREATE_ITEMS_HOTBAR_PACKET -> {
                if (!player.isOp) {
                    player.sendMessage(Component.translatable(reiNoPermissionCheat, NamedTextColor.RED))
                    return
                }
                player.sendMessage(Component.translatable(reiFailedCheatItems))

            }
        }
    }
}