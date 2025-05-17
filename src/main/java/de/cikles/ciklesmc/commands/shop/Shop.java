package de.cikles.ciklesmc.commands.shop;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.*;

import static de.cikles.ciklesmc.utility.Config.Translations.*;

public class Shop extends LiteralArgumentBuilder<CommandSourceStack> implements com.mojang.brigadier.Command<CommandSourceStack>, Listener {

    private static final String SHOP_TITLE = "Shop - ";
    private static final Set<Enchantment> TREASURE_ENCHANTMENTS = Set.of(Enchantment.MENDING, Enchantment.FROST_WALKER, Enchantment.SOUL_SPEED, Enchantment.SWIFT_SNEAK, Enchantment.BINDING_CURSE, Enchantment.VANISHING_CURSE);

    public Shop() {
        super("shop");
        this.requires(ctx -> ctx.getExecutor() instanceof Player).executes(this);
    }

    static ItemStack createItemStack(@NotNull Material material, @NotNull Component displayName, boolean hideTooltip) {
        ItemStack it = material.asItemType().createItemStack();
        ItemMeta meta = it.getItemMeta();
        meta.displayName(displayName);
        meta.setHideTooltip(hideTooltip);
        it.setItemMeta(meta);
        return it;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), t -> {
            if (context.getSource().getExecutor() instanceof Player player) inventory(ShopCategory.MAIN, 1, player);
        });
        return Command.SINGLE_SUCCESS;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getView().title() instanceof TextComponent text)) return;
        if (!(Objects.equals(text.content(), SHOP_TITLE) && Objects.equals(text.color(), NamedTextColor.AQUA))) return;
        if (event.getView().getBottomInventory().equals(event.getClickedInventory())) {
            if (event.getClick().isShiftClick()) event.setCancelled(true);
            return;
        }
        if (event.getClickedInventory() == null) return;
        if (event.getClickedInventory().getType().equals(InventoryType.MERCHANT))
            return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null) return;
        event.setCancelled(true);
        Bukkit.getAsyncScheduler().runNow(CiklesMC.getInstance(), t -> {
            ShopCategory category = ShopCategory.fromTranslationKey(((TranslatableComponent) text.children().get(0)).key());
            int page = text.children().size() == 1 ? 1 : Integer.parseInt(((TextComponent) text.children().get(1)).content().split("/")[0].substring(2));
            clicked(player, category, clicked, page);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTradeSelect(TradeSelectEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!(event.getView().title() instanceof TextComponent text)) return;
        if (!(Objects.equals(text.content(), SHOP_TITLE) && Objects.equals(text.color(), NamedTextColor.AQUA))) return;
        if (!(event.getMerchant().getRecipe(event.getIndex()).getResult().getItemMeta().displayName() instanceof TextComponent textComponent))
            return;
        if (textComponent.content().equals(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_MAIN_PAGE, player.locale())).format(null))) {
            event.setCancelled(true);
            inventory(ShopCategory.MAIN, 1, player);
        }
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onAnvilUse(PrepareAnvilEvent event) {
        if (event.getInventory().getFirstItem() == null) return;

        ItemStack first = event.getInventory().getFirstItem();
        ItemMeta firstMeta = first.getItemMeta();

        if (isAncientTome(first)) {
            event.setResult(null);
            return;
        }
        if (event.getInventory().getSecondItem() == null) return;
        ItemStack second = event.getInventory().getSecondItem();

        ItemMeta secondMeta = second.getItemMeta();

        HashMap<Enchantment, Integer> appliedEnchantments = new HashMap<>();

        if (isAncientTome(second)) {
            if (first.getType().equals(Material.ENCHANTED_BOOK) || first.getType().equals(Material.BOOK)) {
                event.setResult(null);
                return;
            }
            getEnchantments(firstMeta, appliedEnchantments);
            Enchantment target = secondMeta.getEnchants().keySet().stream().findAny().orElse(null);
            if (target == null || !appliedEnchantments.containsKey(target)) {
                event.setResult(null);
                return;
            }
            if (appliedEnchantments.get(target) != target.getMaxLevel()) {
                event.setResult(null);
                return;
            }
            appliedEnchantments.remove(target);

            event.setResult(createResultItem(first, firstMeta, appliedEnchantments, target));
            event.getView().setRepairCost(35);
        }
        appliedEnchantments.clear();

        if (event.getInventory().getResult() == null) return;

        ItemStack result = event.getInventory().getResult();
        ItemMeta resultMeta = result.getItemMeta();

        getEnchantments(resultMeta, appliedEnchantments);
        getEnchantments(firstMeta, appliedEnchantments);
        getEnchantments(secondMeta, appliedEnchantments);

        if (resultMeta instanceof EnchantmentStorageMeta enchantmentStorageMeta)
            appliedEnchantments.forEach((enchantment, level) -> enchantmentStorageMeta.addStoredEnchant(enchantment, level, true));
        else appliedEnchantments.forEach((enchantment, level) -> resultMeta.addEnchant(enchantment, level, true));
        result.setItemMeta(resultMeta);
        event.setResult(result);
    }

    private boolean isAncientTome(ItemStack item) {
        return !(!item.getType().equals(Material.NETHERITE_SCRAP) || !(item.getItemMeta().displayName() instanceof TextComponent text) || !text.content().equals("Ancient Tome") || !text.color().equals(TextColor.color(0xC24492)));
    }

    private ItemStack createResultItem(ItemStack first, ItemMeta firstMeta, Map<Enchantment, Integer> appliedEnchantments, Enchantment target) {
        ItemStack result = first.getType().asItemType().createItemStack();
        ItemMeta meta = result.getItemMeta();
        meta.displayName(firstMeta.displayName());
        if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
            enchantmentStorageMeta.addStoredEnchant(target, target.getMaxLevel() + 1, true);
            appliedEnchantments.forEach((enchantment, lvl) -> enchantmentStorageMeta.addStoredEnchant(enchantment, lvl, true));
        } else {
            meta.addEnchant(target, target.getMaxLevel() + 1, true);
            appliedEnchantments.forEach((enchantment, lvl) -> meta.addEnchant(enchantment, lvl, true));
        }
        if (firstMeta instanceof Damageable damageable && result instanceof Damageable resultDamageable) {
            resultDamageable.setDamage(damageable.getDamage());
            resultDamageable.setMaxDamage(damageable.getMaxDamage());
        }
        result.setItemMeta(meta);
        return result;
    }

    private void getEnchantments(ItemMeta meta, Map<Enchantment, Integer> enchantments) {
        enchantments.putAll(meta.getEnchants());
        if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta)
            enchantmentStorageMeta.getStoredEnchants().forEach((enchantment, level) -> {
                if (!enchantments.containsKey(enchantment) || enchantments.get(enchantment) < level)
                    enchantments.put(enchantment, level);
            });
    }


    private void inventory(@NotNull ShopCategory category, @Range(from = 1, to = 5) int page, @NotNull Player player) {
        Component title = Component.text(SHOP_TITLE, NamedTextColor.AQUA).append(category.title);
        if (category.pages > 1) title = title.append(Component.text(" (" + page + "/" + category.pages + ")"));
        if (!category.isMerchant()) {
            int items = Math.clamp((category.size() - (page - 1) * 21), 0, 21);
            int lines = 1;
            if (items > 14) lines = 3;
            else if (items > 7) lines = 2;
            int size = 27 + lines * 9;
            Inventory inventory = player.getServer().createInventory(null, size, title);

            fillContents(inventory, player, category, items, size, lines, page);

            player.getScheduler().run(CiklesMC.getInstance(), t -> player.openInventory(inventory), null);
        } else {
            Merchant merchant = Bukkit.getServer().createMerchant(title);
            ItemStack item = createItemStack(Material.COMPASS, Component.text(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_MAIN_PAGE, player.locale())).format(null), Style.style().decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_AQUA).build()), false);

            MerchantRecipe main = new MerchantRecipe(item, 1);
            main.addIngredient(item);
            List<MerchantRecipe> list = new ArrayList<>(category.recipe());
            list.add(0, main);

            merchant.setRecipes(list);
            player.getScheduler().run(CiklesMC.getInstance(), t -> player.openMerchant(merchant, true), null);
        }

    }

    private void fillContents(Inventory inventory, Player player, ShopCategory category, int items, int size, int lines, int page) {
        ItemStack[] background = new ItemStack[size];
        Arrays.fill(background, createItemStack(Material.GRAY_STAINED_GLASS_PANE, Component.empty(), true));
        inventory.setContents(background);
        inventory.setItem(size - 6, createItemStack(Material.OAK_BUTTON, Component.text(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_PREVIOUS_PAGE, player.locale())).format(null), Style.style().decoration(TextDecoration.ITALIC, false).build()), false));
        inventory.setItem(size - 5, createItemStack(Material.COMPASS, Component.text(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_MAIN_PAGE, player.locale())).format(null), Style.style().decoration(TextDecoration.ITALIC, false).build()), false));
        inventory.setItem(size - 4, createItemStack(Material.OAK_BUTTON, Component.text(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_NEXT_PAGE, player.locale())).format(null), Style.style().decoration(TextDecoration.ITALIC, false).build()), false));

        int line = 0;
        for (int i = lines * 9 - items; i < lines * 9; i++)
            if (i % 9 != 0 && i % 9 != 8)
                inventory.setItem(9 + i, createItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, Component.empty(), true));
        for (int i = 0; i < items; i++) {
            if (i % 7 == 0) line++;
            inventory.setItem(line * 9 + 1 + i % 7, category.itemStack().get((page - 1) * 21 + i));
        }
    }

    private void inventory(@NotNull Enchantment enchantment, @NotNull Player player) {
        Merchant merchant = Bukkit.getServer().createMerchant(Component.text(SHOP_TITLE, NamedTextColor.AQUA).append(ShopCategory.ENCHANTMENTS.title));
        List<MerchantRecipe> trades = new ArrayList<>();
        // Sell
        for (int i = 1; i <= enchantment.getMaxLevel(); i++) {

            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(enchantment, i, true);
            item.setItemMeta(meta);

            MerchantRecipe book = new MerchantRecipe(item, 9999);
            book.addIngredient(new ItemStack(Material.EMERALD, Math.min(64, 2 + 7 * i * (TREASURE_ENCHANTMENTS.contains(enchantment) ? 2 : 1))));
            book.addIngredient(new ItemStack(Material.BOOK, 1));
            trades.add(book);
        }
        ItemStack item0 = createItemStack(Material.COMPASS, Component.text(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_MAIN_PAGE, player.locale())).format(null), Style.style().decoration(TextDecoration.ITALIC, false).color(NamedTextColor.DARK_AQUA).build()), false);
        MerchantRecipe main = new MerchantRecipe(item0, 1);
        main.addIngredient(item0);
        trades.add(0, main);
        merchant.setRecipes(trades);

        player.getScheduler().run(CiklesMC.getInstance(), t -> player.openMerchant(merchant, true), null);
    }

    private void clicked(@NotNull Player player, @NotNull ShopCategory category, @NotNull ItemStack clicked, int page) {
        if (clicked.getItemMeta() != null && clicked.getItemMeta().displayName() instanceof TextComponent textComponent) {
            if (clicked.getType().equals(Material.COMPASS)) {
                if (textComponent.content().equals(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_MAIN_PAGE, player.locale())).format(null))) {
                    inventory(ShopCategory.MAIN, 1, player);
                    return;
                }
            } else if (clicked.getType().equals(Material.OAK_BUTTON)) {
                if (textComponent.content().equals(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_PREVIOUS_PAGE, player.locale())).format(null))) {
                    inventory(category, Math.max(1, page - 1), player);
                    return;
                } else if (textComponent.content().equals(Objects.requireNonNull(CiklesMC.translationRegistry.translate(SHOP_NEXT_PAGE, player.locale())).format(null))) {
                    inventory(category, Math.min(category.pages, page + 1), player);
                    return;
                }
            }
        }
        switch (category) {
            case MAIN:
                mainPage(player, clicked);
                break;
            case ENCHANTMENTS:
                enchantmentPage(player, clicked);
                break;
            default:
                break;
        }
    }

    private void mainPage(Player player, ItemStack clicked) {
        switch (clicked.getType()) {
            case ANVIL:
                inventory(ShopCategory.SMITH, 1, player);
                break;
            case SMOKER:
                inventory(ShopCategory.BUTCHER, 1, player);
                break;
            case CARTOGRAPHY_TABLE:
                inventory(ShopCategory.CARTOGRAPHER, 1, player);
                break;
            case BREWING_STAND:
                inventory(ShopCategory.CLERIC, 1, player);
                break;
            case COMPOSTER:
                inventory(ShopCategory.FARMER, 1, player);
                break;
            case BARREL:
                inventory(ShopCategory.FISHERMAN, 1, player);
                break;
            case FLETCHING_TABLE:
                inventory(ShopCategory.FLETCHER, 1, player);
                break;
            case CAULDRON:
                inventory(ShopCategory.LEATHER_WORKER, 1, player);
                break;
            case LECTERN:
                inventory(ShopCategory.LIBRARIAN, 1, player);
                break;
            case STONECUTTER:
                inventory(ShopCategory.MASON, 1, player);
                break;
            case LOOM:
                inventory(ShopCategory.SHEPHERD, 1, player);
                break;
            case NETHER_STAR:
                inventory(ShopCategory.CUSTOM, 1, player);
                break;
            case ENCHANTED_BOOK:
                inventory(ShopCategory.ENCHANTMENTS, 1, player);
                break;
            case NETHERITE_SCRAP:
                inventory(ShopCategory.ANCIENT_TOMES, 1, player);
                break;
            default:
                break;
        }
    }

    private void enchantmentPage(Player player, ItemStack clicked) {
        Enchantment ench = clicked.getItemMeta().getEnchants().keySet().stream().findAny().orElseGet(() -> {
            if (clicked.getItemMeta() instanceof EnchantmentStorageMeta meta)
                return meta.getStoredEnchants().keySet().stream().findAny().orElse(null);
            return null;
        });
        if (ench == null) return;
        inventory(ench, player);

    }
}