package dev.pvpoverhaul;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/** Repurposes the offhand-swap key (F): trades your held item with a fixed hotbar slot. */
public final class SwapListener implements Listener {

    private final PvpOverhaulPlugin plugin;

    public SwapListener(PvpOverhaulPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwap(PlayerSwapHandItemsEvent e) {
        if (!plugin.getConfig().getBoolean("swap.enabled", true)) return;
        Player p = e.getPlayer();
        if (plugin.getConfig().getBoolean("swap.require-sneak", false) && !p.isSneaking()) return;

        int slot = Math.max(0, Math.min(8, plugin.getConfig().getInt("swap.slot", 8)));
        PlayerInventory inv = p.getInventory();
        int current = inv.getHeldItemSlot();
        if (slot == current) return;

        ItemStack held = inv.getItem(current);
        ItemStack other = inv.getItem(slot);
        if ((held == null || held.getType().isAir()) && (other == null || other.getType().isAir())) return;

        e.setCancelled(true); // stop the vanilla main-hand <-> offhand swap
        inv.setItem(current, other);
        inv.setItem(slot, held);
        p.updateInventory();
        p.playSound(p.getLocation(), Sound.ITEM_ARMOR_EQUIP_IRON, 0.5f, 1.5f);
    }
}
