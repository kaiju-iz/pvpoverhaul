package dev.pvpoverhaul;

import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Crystal Detonator (remote-detonate your own crystals) and Blast Ward (offhand explosion dampener). */
public final class CrystalListener implements Listener {

    private final PvpOverhaulPlugin plugin;
    private final ItemRegistry items;
    /** crystal entity UUID -> placer UUID */
    private final Map<UUID, UUID> owners = new ConcurrentHashMap<>();
    /** player UUID -> server tick a ward was last consumed, so one blast only eats one ward */
    private final Map<UUID, Integer> lastWardTick = new ConcurrentHashMap<>();

    public CrystalListener(PvpOverhaulPlugin plugin, ItemRegistry items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlace(EntityPlaceEvent e) {
        if (e.getEntity() instanceof EnderCrystal c && e.getPlayer() != null) {
            owners.put(c.getUniqueId(), e.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onRemove(EntityRemoveEvent e) {
        owners.remove(e.getEntity().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack held = p.getInventory().getItemInMainHand();
        if (!items.is(held, ItemRegistry.DETONATOR)) return;
        e.setCancelled(true);
        if (p.hasCooldown(held)) return;

        double radius = plugin.getConfig().getDouble("crystal-detonator.radius", 16.0);
        float power = (float) plugin.getConfig().getDouble("crystal-detonator.power", 6.0);
        int detonated = 0;
        for (EnderCrystal c : p.getWorld().getNearbyEntitiesByType(EnderCrystal.class, p.getLocation(), radius)) {
            if (!p.getUniqueId().equals(owners.get(c.getUniqueId()))) continue;
            Location at = c.getLocation();
            c.remove();
            at.getWorld().createExplosion(at, power, false, true, p);
            detonated++;
        }
        if (detonated > 0) {
            p.setCooldown(held, plugin.getConfig().getInt("crystal-detonator.cooldown-ticks", 40));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onExplosionDamage(EntityDamageEvent e) {
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION && e.getCause() != DamageCause.BLOCK_EXPLOSION) return;
        if (!(e.getEntity() instanceof Player p)) return;
        ItemStack off = p.getInventory().getItemInOffHand();
        if (!items.is(off, ItemRegistry.BLAST_WARD)) return;

        double reduction = plugin.getConfig().getDouble("blast-ward.damage-reduction", 0.4);
        e.setDamage(e.getDamage() * (1.0 - reduction));

        if (plugin.getConfig().getBoolean("blast-ward.consume-per-hit", true)) {
            int tick = plugin.getServer().getCurrentTick();
            Integer last = lastWardTick.put(p.getUniqueId(), tick);
            if (last == null || last != tick) {
                off.setAmount(off.getAmount() - 1);
                p.getInventory().setItemInOffHand(off.getAmount() <= 0 ? null : off);
            }
        }
    }
}
