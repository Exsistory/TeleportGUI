package exsistory.project.teleportgui;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public final class CommandHandler implements Listener {

    private final Main plugin;

    public CommandHandler(Main plugin) {
        this.plugin = plugin;
    }

    public void registerTpaCommands() {
        try {
            final Field bukkitCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(plugin.getServer());

            FileConfiguration config = plugin.getConfig();
            List<String> customAliases = config.getStringList("commands.tpa");

            for (String alias : customAliases) {
                PluginCommand aliasCommand = null;

                try {
                    Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                    constructor.setAccessible(true);
                    aliasCommand = constructor.newInstance(alias, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (aliasCommand != null) {
                    aliasCommand.setExecutor(new TpaCommand(plugin));
                    commandMap.register(alias, aliasCommand);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerTpaHereCommands() {
        try {
            final Field bukkitCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(plugin.getServer());

            FileConfiguration config = plugin.getConfig();
            List<String> customAliases = config.getStringList("commands.tpahere");

            for (String alias : customAliases) {
                PluginCommand aliasCommand = null;

                try {
                    Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                    constructor.setAccessible(true);
                    aliasCommand = constructor.newInstance(alias, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (aliasCommand != null) {
                    aliasCommand.setExecutor(new TpahereCommand(plugin));
                    commandMap.register(alias, aliasCommand);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void registerTpaToggleCommands() {
        try {
            final Field bukkitCommandMap = plugin.getServer().getClass().getDeclaredField("commandMap");

            bukkitCommandMap.setAccessible(true);
            CommandMap commandMap = (CommandMap) bukkitCommandMap.get(plugin.getServer());

            FileConfiguration config = plugin.getConfig();
            List<String> customAliases = config.getStringList("commands.tpatoggle");

            for (String alias : customAliases) {
                PluginCommand aliasCommand = null;

                try {
                    Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
                    constructor.setAccessible(true);
                    aliasCommand = constructor.newInstance(alias, plugin);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (aliasCommand != null) {
                    aliasCommand.setExecutor(plugin);
                    commandMap.register(alias, aliasCommand);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
