package dev.pvpoverhaul;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;

/** Creates and recognises every custom item. Items are identified by a PDC tag, never by name. */
public final class ItemRegistry {

    public static final String SPEAR_MACE = "spear_mace";
    public static final String DETONATOR = "crystal_detonator";
    public static final String BLAST_WARD = "blast_ward";
    public static final Set<String> IDS = Set.of(SPEAR_MACE, DETONATOR, BLAST_WARD);

    private final PvpOverhaulPlugin plugin;
    private final NamespacedKey idKey;

    public ItemRegistry(PvpOverhaulPlugin plugin) {
        this.plugin = plugin;
        this.idKey = new NamespacedKey(plugin, "item_id");
    }

    /** Returns the custom item id, or null for vanilla items. */
    public String idOf(ItemStack stack) {
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) return null;
        return stack.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
    }

    public boolean is(ItemStack stack, String id) {
        return id.equals(idOf(stack));
    }

    public ItemStack create(String id) {
        FileConfiguration cfg = plugin.getConfig();
        return switch (id) {
            case SPEAR_MACE -> {
                Material base = Material.matchMaterial(cfg.getString("spear-mace.base-material", "MACE"));
                if (base == null) base = Material.MACE;
                ItemStack item = base(base, id, "Spear-Mace", NamedTextColor.GOLD,
                        List.of("Lunge while sprinting for bonus damage", "and knockback. Keeps the smash attack."));
                ItemMeta meta = item.getItemMeta();
                // Custom modifiers REPLACE vanilla defaults, so damage/speed are re-declared here.
                meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(
                        new NamespacedKey(plugin, "spear_mace_damage"),
                        cfg.getDouble("spear-mace.attack-damage-bonus", 5.0),
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                meta.addAttributeModifier(Attribute.ATTACK_SPEED, new AttributeModifier(
                        new NamespacedKey(plugin, "spear_mace_speed"),
                        cfg.getDouble("spear-mace.attack-speed-modifier", -3.4),
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, new AttributeModifier(
                        new NamespacedKey(plugin, "spear_mace_reach"),
                        cfg.getDouble("spear-mace.reach-bonus", 1.0),
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND));
                item.setItemMeta(meta);
                yield item;
            }
            case DETONATOR -> base(Material.BLAZE_ROD, id, "Crystal Detonator", NamedTextColor.LIGHT_PURPLE,
                    List.of("Right-click to detonate end crystals", "that YOU placed within range."));
            case BLAST_WARD -> base(Material.ECHO_SHARD, id, "Blast Ward", NamedTextColor.AQUA,
                    List.of("Hold in your offhand to dampen", "explosion damage. Consumed on use."));
            default -> throw new IllegalArgumentException("Unknown item: " + id);
        };
    }

    private ItemStack base(Material mat, String id, String name, NamedTextColor color, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        meta.lore(lore.stream().map(l -> (Component) Component.text(l, NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)).toList());
        meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
        // Points at assets/pvp/items/<id>.json in the resource pack.
        meta.setItemModel(new NamespacedKey("pvp", id));
        item.setItemMeta(meta);
        return item;
    }

    public void registerRecipes() {
        FileConfiguration cfg = plugin.getConfig();
        if (cfg.getBoolean("spear-mace.craftable", true)) {
            ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, SPEAR_MACE), create(SPEAR_MACE));
            r.shape(" B ", " M ", " B ");
            r.setIngredient('B', Material.BREEZE_ROD);
            r.setIngredient('M', Material.MACE);
            plugin.getServer().addRecipe(r);
        }
        if (cfg.getBoolean("crystal-detonator.craftable", true)) {
            ShapedRecipe r = new ShapedRecipe(new NamespacedKey(plugin, DETONATOR), create(DETONATOR));
            r.shape(" C ", " R ", " B ");
            r.setIngredient('C', Material.END_CRYSTAL);
            r.setIngredient('R', Material.REDSTONE_BLOCK);
            r.setIngredient('B', Material.BLAZE_ROD);
            plugin.getServer().addRecipe(r);
        }
        if (cfg.getBoolean("blast-ward.craftable", true)) {
            ShapelessRecipe r = new ShapelessRecipe(new NamespacedKey(plugin, BLAST_WARD), create(BLAST_WARD));
            r.addIngredient(1, Material.OBSIDIAN);
            r.addIngredient(3, Material.AMETHYST_SHARD);
            plugin.getServer().addRecipe(r);
        }
    }

    public void unregisterRecipes() {
        for (String id : IDS) plugin.getServer().removeRecipe(new NamespacedKey(plugin, id));
    }
}
