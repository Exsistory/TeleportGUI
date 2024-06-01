package exsistory.project.teleportgui;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static exsistory.project.teleportgui.ColorizeHandler.colorize;

public class Main extends JavaPlugin implements Listener, InventoryHolder, CommandExecutor {
    public HashMap<UUID, UUID> teleportRequests;
    public HashMap<UUID, Boolean> tpaToggles;
    private Inventory gui;

    private CommandHandler commandHandler;

    @Override
    public void onEnable() {
        int pluginId = 22050;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new Metrics.SimplePie("chart_id", () -> "TeleportGUI"));
        teleportRequests = new HashMap<>();
        tpaToggles = new HashMap<>();
        new PlaceholderHandler(this).register();
        getCommand("tpa").setExecutor(new TpaCommand(this));
        getCommand("tpahere").setExecutor(new TpahereCommand(this));
        getCommand("tg").setExecutor(this);
        getCommand("teleportgui").setExecutor(this);
        commandHandler = new CommandHandler(this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        getServer().getPluginManager().registerEvents(new CommandHandler(this), this);
        commandHandler.registerTpaCommands();
        commandHandler.registerTpaHereCommands();
        commandHandler.registerTpaToggleCommands();
        saveDefaultConfig();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public Inventory getInventory() {
        return gui;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            List<String> aliases = getConfig().getStringList("commands.tpatoggle");
            if (label.equalsIgnoreCase("tpatoggle") || aliases.stream().anyMatch(alias -> label.equalsIgnoreCase(alias))) {
                boolean currentState = tpaToggles.getOrDefault(player.getUniqueId(), true);
                tpaToggles.put(player.getUniqueId(), !currentState);
                player.sendMessage(colorize(currentState ? colorize(getConfig().getString("messages.tp-toggle-deny")) : colorize(getConfig().getString("messages.tp-toggle-allow"))));
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("teleportgui.admin")) {
                    reloadConfig();
                    player.sendMessage(colorize(getConfig().getString("messages.reload-plugin")));
                } else {
                    player.sendMessage(colorize("&cNo permission!"));
                }
                return true;
            }
        }
        return false;
    }

    public HashMap<UUID, UUID> getTeleportRequests() {
        return teleportRequests;
    }

    public boolean floodgateOrGeyser() {
        if (getConfig().getString("main-settings.mode").equalsIgnoreCase("GEYSER")) {
            return false;
        } else if (getConfig().getString("main-settings.mode").equalsIgnoreCase("FLOODGATE")){
            return true;
        }
        return false;
    }

    public void openTpaGui(Player target, Player requester, Boolean tpahere) {
            boolean enableExpiration = getConfig().getBoolean("expire-time.enable");
            if (enableExpiration) {
                String timeFormat = getConfig().getString("expire-time.time");
                long expirationSeconds = convertTimeToSeconds(timeFormat);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                            requester.sendMessage(colorize(getConfig().getString("messages.tp-request-expired")));
                            target.closeInventory();
                            teleportRequests.remove(target.getUniqueId(), requester.getUniqueId());
                    }
                }.runTaskLater(this, expirationSeconds * 20);
            }
            if (floodgateOrGeyser()) {
                if (FloodgateApi.getInstance().isFloodgatePlayer(target.getUniqueId())) {
                    openBedrockTpaGui(target, requester, tpahere);
                    return;
                }
            } else {
                if (GeyserApi.api().isBedrockPlayer(target.getUniqueId())) {
                    openBedrockTpaGui(target, requester, tpahere);
                    return;
                }
            }
            FileConfiguration config = getConfig();
            String title;
            if (tpahere) {
                title = colorize(config.getString("teleport-gui.settings.tphere-title").replace("%player%", requester.getName()));
            } else {
                title = colorize(config.getString("teleport-gui.settings.tp-title").replace("%player%", requester.getName()));
            }
            int slots = config.getInt("teleport-gui.settings.slots");
            gui = Bukkit.createInventory(this, slots, title);

            if (config.getBoolean("teleport-gui.items.filler.enable")) {
                ItemStack filler = createItem(config, "teleport-gui.items.filler");
                String mode = config.getString("teleport-gui.items.filler.mode");
                if (mode.equalsIgnoreCase("FILL")) {
                    for (int i = 0; i < slots; i++) {
                        gui.setItem(i, filler);
                    }
                } else if (mode.equalsIgnoreCase("BORDER")) {
                    for (int i = 0; i < slots; i++) {
                        if (i < 9 || i >= slots - 9 || i % 9 == 0 || i % 9 == 8) {
                            gui.setItem(i, filler);
                        }
                    }
                }
            }

            ItemStack allowTp = createItem(config, "teleport-gui.items.allow-tp");
            ItemMeta allowTpMeta = allowTp.getItemMeta();
            if (tpahere) {
                allowTpMeta.getPersistentDataContainer().set(new NamespacedKey(this, "button"), PersistentDataType.STRING, "tpahere");
                allowTp.setItemMeta(allowTpMeta);
            } else {
                allowTpMeta.getPersistentDataContainer().set(new NamespacedKey(this, "button"), PersistentDataType.STRING, "tphere");
                allowTp.setItemMeta(allowTpMeta);
            }
            int allowTpSlot = config.getInt("teleport-gui.items.allow-tp.slot");
            gui.setItem(allowTpSlot, allowTp);

            ItemStack denyTp = createItem(config, "teleport-gui.items.deny-tp");
            int denyTpSlot = config.getInt("teleport-gui.items.deny-tp.slot");
            gui.setItem(denyTpSlot, denyTp);

            target.openInventory(gui);
            teleportRequests.put(target.getUniqueId(), requester.getUniqueId());
    }

    private long convertTimeToSeconds(String timeFormat) {
        String[] parts = timeFormat.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        long seconds = 0;
        for (int i = 0; i < parts.length; i += 2) {
            int value = Integer.parseInt(parts[i].trim());
            String unit = parts[i + 1].trim().toLowerCase();
            switch (unit) {
                case "s":
                    seconds += value;
                    break;
                case "m":
                    seconds += TimeUnit.MINUTES.toSeconds(value);
                    break;
                case "h":
                    seconds += TimeUnit.HOURS.toSeconds(value);
                    break;
                case "d":
                    seconds += TimeUnit.DAYS.toSeconds(value);
                    break;
                default:
                    break;
            }
        }
        return seconds;
    }

    private ItemStack createItem(FileConfiguration config, String path) {
        Material material = Material.valueOf(config.getString(path + ".material"));
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(colorize(config.getString(path + ".name")));
        List<String> lore = colorize(config.getStringList(path + ".lore"));
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void handleTp(Player player, Player target) {
        for (BukkitTask task : getServer().getScheduler().getPendingTasks()) {
            if (task.getOwner().equals(this)) {
                task.cancel();
            }
        }

        FileConfiguration config = getConfig();
        int delay = config.getInt("teleport-delay.default");

        for (String key : config.getConfigurationSection("teleport-delay.custom-times").getKeys(false)) {
            if (player.hasPermission("teleportgui.delay." + key)) {
                delay = config.getInt("teleport-delay.custom-times." + key);
                break;
            }
        }

        String mode = config.getString("teleport-delay.mode");
        boolean cancelOnMove = config.getBoolean("teleport-delay.cancel-on-move");
        Location initialLocation = player.getLocation();

        final int finalDelay = delay;

        new BukkitRunnable() {
            int countdown = finalDelay;

            @Override
            public void run() {
                if (!player.isOnline() || !target.isOnline() || (cancelOnMove && playerMoved(player, initialLocation))) {
                    player.sendMessage(colorize(getConfig().getString("messages.tp-move-cancelled")));
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    player.teleport(target);
                    player.sendMessage(colorize(getConfig().getString("messages.tp-successful")));
                    cancel();
                    return;
                }

                String message = colorize(getConfig().getString("messages.teleporting-in").replace("%time%", Integer.toString(countdown)));
                if (mode.equalsIgnoreCase("ACTION")) {
                    player.sendActionBar(message);
                } else if (mode.equalsIgnoreCase("TITLE")) {
                    player.sendTitle("", message, 10, 20, 10);
                } else {
                    player.sendMessage(message);
                }

                countdown--;
            }
        }.runTaskTimer(this, 0, 20);
    }

    public void handleTpahere(Player player, Player target) {
        for (BukkitTask task : getServer().getScheduler().getPendingTasks()) {
            if (task.getOwner().equals(this)) {
                task.cancel();
            }
        }
        FileConfiguration config = getConfig();
        int delay = config.getInt("teleport-delay.default");

        for (String key : config.getConfigurationSection("teleport-delay.custom-times").getKeys(false)) {
            if (player.hasPermission("teleportgui.delay." + key)) {
                delay = config.getInt("teleport-delay.custom-times." + key);
                break;
            }
        }

        String mode = config.getString("teleport-delay.mode");
        boolean cancelOnMove = config.getBoolean("teleport-delay.cancel-on-move");
        Location initialLocation = player.getLocation();

        final int finalDelay = delay;

        new BukkitRunnable() {
            int countdown = finalDelay;

            @Override
            public void run() {
                if (!player.isOnline() || !target.isOnline() || (cancelOnMove && playerMoved(player, initialLocation))) {
                    player.sendMessage(colorize(getConfig().getString("messages.tp-move-cancelled")));
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    target.teleport(player);
                    teleportRequests.remove(target.getUniqueId(), player.getUniqueId());
                    player.sendMessage(colorize(getConfig().getString("messages.tp-successful")));
                    cancel();
                    return;
                }

                String message = colorize(getConfig().getString("messages.teleporting-in").replace("%time%", Integer.toString(countdown)));
                if (mode.equalsIgnoreCase("ACTION")) {
                    target.sendActionBar(message);
                } else if (mode.equalsIgnoreCase("TITLE")) {
                    target.sendTitle("", message, 10, 20, 10);
                } else {
                    target.sendMessage(message);
                }

                countdown--;
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void openBedrockTpaGui(Player target, Player requester, Boolean tpahere) {
        String title = tpahere ? getConfig().getString("teleport-gui-bedrock.settings.tp-title") : getConfig().getString("teleport-gui-bedrock.settings.tphere-title");
        SimpleForm modal = SimpleForm.builder()
                .title(colorize(title))
                .content(colorize(getConfig().getString("teleport-gui-bedrock.settings.description").replace("%player", requester.getName())))
                .button(colorize(getConfig().getString("teleport-gui-bedrock.buttons.accept.name")),
                        getConfig().getBoolean("teleport-gui-bedrock.buttons.accept.image.enable") &&
                                getConfig().getString("teleport-gui-bedrock.buttons.accept.image.mode").equalsIgnoreCase("URL") ?
                                FormImage.Type.URL : FormImage.Type.PATH,
                        getConfig().getBoolean("teleport-gui-bedrock.buttons.accept.image.enable") ?
                                (getConfig().getString("teleport-gui-bedrock.buttons.accept.image.mode").equalsIgnoreCase("URL") ?
                                        getConfig().getString("teleport-gui-bedrock.buttons.accept.image.url") :
                                        getConfig().getString("teleport-gui-bedrock.buttons.accept.image.path")) : null)
                .button(colorize(getConfig().getString("teleport-gui-bedrock.buttons.deny.name")),
                        getConfig().getBoolean("teleport-gui-bedrock.buttons.deny.image.enable") &&
                                getConfig().getString("teleport-gui-bedrock.buttons.deny.image.mode").equalsIgnoreCase("URL") ?
                                FormImage.Type.URL : FormImage.Type.PATH,
                        getConfig().getBoolean("teleport-gui-bedrock.buttons.deny.image.enable") ?
                                (getConfig().getString("teleport-gui-bedrock.buttons.deny.image.mode").equalsIgnoreCase("URL") ?
                                        getConfig().getString("teleport-gui-bedrock.buttons.deny.image.url") :
                                        getConfig().getString("teleport-gui-bedrock.buttons.deny.image.path")) : null)
                .validResultHandler(((formResponse, Player) -> {
                        if (Player.clickedButtonId() == 0) {
                            if (tpahere && teleportRequests.containsValue(requester.getUniqueId())) {
                                handleTpahere(requester, target);
                            } else if (teleportRequests.containsValue(requester.getUniqueId())) {
                                handleTp(requester, target);
                            }
                            if (!teleportRequests.containsValue(requester.getUniqueId())) {
                                target.sendMessage(colorize(getConfig().getString("messages.tp-request-expired")));
                            }
                        } else {
                            if (teleportRequests.containsValue(requester.getUniqueId())) {
                                target.sendMessage(colorize(getConfig().getString("messages.tp-cancelled-receiver")));
                                requester.sendMessage(colorize(getConfig().getString("messages.tp-cancelled-sender")));
                                teleportRequests.remove(target.getUniqueId(), requester.getUniqueId());
                            }
                        }
                }))
                .build();
        try {
            if (floodgateOrGeyser()) {
                FloodgateApi.getInstance().sendForm(target.getUniqueId(), modal);
            } else {
                GeyserApi.api().sendForm(target.getUniqueId(), modal);
            }
        } catch (Exception e) {
            getLogger().severe("Hmm seems that you have the incorrect option selected in your config! Check what your using and if its GEYSER OR FLOODGATE!");
        }
    }

    private boolean playerMoved(Player player, Location initialLocation) {
        Location currentLocation = player.getLocation();
        return currentLocation.getX() != initialLocation.getX() ||
                currentLocation.getY() != initialLocation.getY() ||
                currentLocation.getZ() != initialLocation.getZ();
    }
}
