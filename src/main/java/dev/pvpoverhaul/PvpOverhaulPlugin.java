package dev.pvpoverhaul;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

public final class PvpOverhaulPlugin extends JavaPlugin implements Listener {

    private ItemRegistry items;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        items = new ItemRegistry(this);
        items.registerRecipes();

        var pm = getServer().getPluginManager();
        pm.registerEvents(new SpearMaceListener(this, items), this);
        pm.registerEvents(new SwapListener(this), this);
        pm.registerEvents(new CrystalListener(this, items), this);
        pm.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (items != null) items.unregisterRecipes();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!getConfig().getBoolean("resource-pack.enabled", false)) return;
        String url = getConfig().getString("resource-pack.url", "");
        String sha1 = getConfig().getString("resource-pack.sha1", "");
        if (url.isBlank() || sha1.length() != 40) {
            getLogger().warning("resource-pack.enabled is true but url/sha1 are not set correctly.");
            return;
        }
        e.getPlayer().setResourcePack(url, HexFormat.of().parseHex(sha1),
                Component.text("PvP Overhaul textures"), getConfig().getBoolean("resource-pack.required", true));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Component.text("/pvp reload | /pvp give <item> [player]", NamedTextColor.YELLOW));
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                items.unregisterRecipes();
                reloadConfig();
                items.registerRecipes();
                sender.sendMessage(Component.text("Config reloaded.", NamedTextColor.GREEN));
            }
            case "give" -> {
                if (args.length < 2 || !ItemRegistry.IDS.contains(args[1].toLowerCase())) {
                    sender.sendMessage(Component.text("Items: " + String.join(", ", ItemRegistry.IDS), NamedTextColor.RED));
                    return true;
                }
                Player target = args.length >= 3 ? getServer().getPlayer(args[2])
                        : (sender instanceof Player p ? p : null);
                if (target == null) {
                    sender.sendMessage(Component.text("Specify an online player.", NamedTextColor.RED));
                    return true;
                }
                target.getInventory().addItem(items.create(args[1].toLowerCase()));
                sender.sendMessage(Component.text("Gave " + args[1] + " to " + target.getName(), NamedTextColor.GREEN));
            }
            default -> sender.sendMessage(Component.text("Unknown subcommand.", NamedTextColor.RED));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) out.addAll(List.of("reload", "give"));
        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) out.addAll(ItemRegistry.IDS);
        return out.stream().filter(s -> s.startsWith(args[args.length - 1].toLowerCase())).toList();
    }
}
