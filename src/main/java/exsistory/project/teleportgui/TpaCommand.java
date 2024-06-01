package exsistory.project.teleportgui;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static exsistory.project.teleportgui.ColorizeHandler.colorize;

public class TpaCommand implements CommandExecutor {

    private final Main plugin;

    public TpaCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> aliases = plugin.getConfig().getStringList("commands.tpa");
        if (label.equalsIgnoreCase("tpa") || aliases.stream().anyMatch(alias -> label.equalsIgnoreCase(alias))) {
        if (!(sender instanceof Player)) return false;
        if (args.length != 1) return false;

        Player requester = (Player) sender;

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            requester.sendMessage(colorize(plugin.getConfig().getString("messages.player-not-found")));
            return false;
        }

        if (target == requester) {
            requester.sendMessage(colorize(plugin.getConfig().getString("messages.tp-self")));
            return false;
        }

        if (plugin.tpaToggles.getOrDefault(target.getUniqueId(), true)) {
            plugin.openTpaGui(target, requester, false);
            plugin.teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
            requester.sendMessage(colorize(plugin.getConfig().getString("messages.tp-request-sent").replace("%player%", target.getName())));
            return true;
        } else {
            requester.sendMessage(colorize(plugin.getConfig().getString("messages.not-accepting-requests").replace("%player%", target.getName())));
            return true;
        }
    }
        return false;
    }
}
