package exsistory.project.teleportgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

import static exsistory.project.teleportgui.ColorizeHandler.colorize;

public class GuiListener implements Listener {

    private final Main plugin;

    public GuiListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getInventory();
        if (clickedInventory.getHolder() instanceof Main) {
            event.setCancelled(true);
            Player target = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if (clickedItem == null || clickedItem.getType() == Material.AIR || clickedItem.getType() == Material.valueOf(plugin.getConfig().getString("teleport-gui.items.filler.material"))) return;

            UUID requesterUUID = plugin.getTeleportRequests().remove(target.getUniqueId());

            if (requesterUUID == null) return;

            Player requester = Bukkit.getPlayer(requesterUUID);

            if (requester == null || !requester.isOnline()) {
                target.sendMessage(plugin.getConfig().getString("messages.no-longer-online"));
                return;
            }

            if (clickedItem.getType() == Material.valueOf(plugin.getConfig().getString("teleport-gui.items.allow-tp.material"))) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null) {
                    PersistentDataType<String, String> type = PersistentDataType.STRING;
                    NamespacedKey key = new NamespacedKey(plugin, "button");
                    String value = meta.getPersistentDataContainer().get(key, type);
                    if (value.equals("tphere")) {
                        plugin.handleTp(requester, target);
                        target.sendMessage(colorize(plugin.getConfig().getString("messages.accepted-request")));
                        requester.sendMessage(colorize(plugin.getConfig().getString("messages.player-accepted-request").replace("%player%", target.getName())));
                    } else if (value.equals("tpahere")) {
                        plugin.handleTpahere(requester, target);
                        target.sendMessage(colorize(plugin.getConfig().getString("messages.accepted-request")));
                        requester.sendMessage(colorize(plugin.getConfig().getString("messages.player-accepted-request").replace("%player%", target.getName())));
                    }
                }
            } else if (clickedItem.getType() == Material.valueOf(plugin.getConfig().getString("teleport-gui.items.deny-tp.material"))) {
                requester.sendMessage(colorize(plugin.getConfig().getString("messages.tp-cancelled-sender")));
                target.sendMessage(colorize(plugin.getConfig().getString("messages.tp-cancelled-receiver")));
            }
            target.closeInventory();
        }
    }
}
