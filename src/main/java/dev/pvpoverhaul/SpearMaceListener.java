package dev.pvpoverhaul;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

/** Lunge: a sprinting melee hit with the Spear-Mace deals bonus damage and knockback. */
public final class SpearMaceListener implements Listener {

    private final PvpOverhaulPlugin plugin;
    private final ItemRegistry items;

    public SpearMaceListener(PvpOverhaulPlugin plugin, ItemRegistry items) {
        this.plugin = plugin;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent e) {
        if (e.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity victim)) return;
        if (!items.is(attacker.getInventory().getItemInMainHand(), ItemRegistry.SPEAR_MACE)) return;
        if (!plugin.getConfig().getBoolean("spear-mace.lunge.enabled", true)) return;
        if (!attacker.isSprinting()) return;

        e.setDamage(e.getDamage() + plugin.getConfig().getDouble("spear-mace.lunge.bonus-damage", 3.0));

        double kb = plugin.getConfig().getDouble("spear-mace.lunge.knockback", 0.9);
        Vector dir = victim.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
        if (dir.lengthSquared() > 1.0E-6) {
            dir.normalize().multiply(kb).setY(0.25);
            victim.setVelocity(victim.getVelocity().add(dir));
        }
        attacker.getWorld().playSound(victim.getLocation(), Sound.ITEM_TRIDENT_HIT, 1.0f, 0.8f);
    }
}
