package de.cikles.ciklesmc.listeners.mod_implementation;

import de.cikles.ciklesmc.core.CiklesMC;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.messaging.Messenger;

public class ModImplementation implements Listener {
    public static final String XAERO_WORLD_MAP = "xaeroworldmap:main";
    public static final String XAERO_MINIMAP = "xaerominimap:main";
    public static final String OPEN_PARTIES = "openpartiesandclaims:main";
    public static final String DELETE_ITEMS_PACKET = "roughlyenoughitems:delete_item";
    public static final String CREATE_ITEMS_PACKET = "roughlyenoughitems:create_item";
    public static final String CREATE_ITEMS_HOTBAR_PACKET = "roughlyenoughitems:create_item_hotbar";
    public static final String CREATE_ITEMS_GRAB_PACKET = "roughlyenoughitems:create_item_grab";
    public static final String CREATE_ITEMS_MESSAGE_PACKET = "roughlyenoughitems:ci_msg";
    public static final String MOVE_ITEMS_NEW_PACKET = "roughlyenoughitems:move_items_new";
    REI rei = new REI();
    Xaero xaero = new Xaero();

    public void registerOutgoingChannels(Messenger messenger) {
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), XAERO_WORLD_MAP);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), XAERO_MINIMAP);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), DELETE_ITEMS_PACKET);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_PACKET);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_HOTBAR_PACKET);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_GRAB_PACKET);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_MESSAGE_PACKET);
        messenger.registerOutgoingPluginChannel(CiklesMC.getInstance(), MOVE_ITEMS_NEW_PACKET);
    }

    public void registerIncomingChannels(Messenger messenger) {
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), XAERO_MINIMAP, xaero);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), DELETE_ITEMS_PACKET, rei);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_PACKET, rei);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_HOTBAR_PACKET, rei);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_GRAB_PACKET, rei);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), CREATE_ITEMS_MESSAGE_PACKET, rei);
        messenger.registerIncomingPluginChannel(CiklesMC.getInstance(), MOVE_ITEMS_NEW_PACKET, rei);
    }

    public void registerListeners(PluginManager pluginManager) {
        pluginManager.registerEvents(xaero, CiklesMC.getInstance());
        pluginManager.registerEvents(rei, CiklesMC.getInstance());
    }
}
