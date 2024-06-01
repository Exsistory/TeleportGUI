package exsistory.project.teleportgui;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlaceholderHandler extends PlaceholderExpansion {

    private final Main plugin;

    public PlaceholderHandler(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getAuthor() {
        return "Exsistory";
    }

    @Override
    public String getIdentifier() {
        return "teleportgui";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        switch (identifier) {
            case "toggle_status":
                return toggleStatus(player);
            default:
                return null;
        }
    }

    private String toggleStatus(Player player) {
        UUID playerID = player.getUniqueId();
        if (plugin.tpaToggles.containsKey(playerID)) {
            if (plugin.tpaToggles.getOrDefault(playerID, true)) {
                return "false";
            } else {
                return "true";
            }
        }
        return "true";
    }
}
