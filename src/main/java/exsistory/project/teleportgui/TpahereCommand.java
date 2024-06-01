package exsistory.project.teleportgui;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static exsistory.project.teleportgui.ColorizeHandler.colorize;

public class TpahereCommand implements CommandExecutor {

    private final Main plugin;

    public TpahereCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> aliases = plugin.getConfig().getStringList("commands.tpahere");
        if (label.equalsIgnoreCase("tpahere") || aliases.stream().anyMatch(alias -> label.equalsIgnoreCase(alias))) {
            if (!(sender instanceof Player)) return false;
            if (args.length != 1) return false;

            Player target = (Player) sender;

            Player requester = Bukkit.getPlayer(args[0]);
            if (requester == null || !requester.isOnline()) {
                requester.sendMessage(colorize(plugin.getConfig().getString("messages.player-not-found")));
                return false;
            }

            if (target == requester) {
                requester.sendMessage(colorize(plugin.getConfig().getString("messages.tp-self")));
                return false;
            }

            if (plugin.tpaToggles.getOrDefault(target.getUniqueId(), true)) {
                plugin.openTpaGui(requester, target, true);
                plugin.teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
                target.sendMessage(colorize(plugin.getConfig().getString("messages.tphere-request-sent").replace("%player%", requester.getName())));
                return true;
            } else {
                requester.sendMessage(colorize(plugin.getConfig().getString("messages.not-accepting-requests").replace("%player%", target.getName())));
                return true;
            }
        }
        return false;
    }
}
