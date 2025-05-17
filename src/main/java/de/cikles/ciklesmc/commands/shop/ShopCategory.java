package de.cikles.ciklesmc.commands.shop;

import de.cikles.ciklesmc.enchantments.Enchantments;
import de.cikles.ciklesmc.utility.Config;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static de.cikles.ciklesmc.commands.shop.Shop.createItemStack;
import static de.cikles.ciklesmc.utility.Config.Translations.*;

public enum ShopCategory {
    MAIN(Component.translatable(SHOP_TITLE_VILLAGER_TRADES),
            List.of(createItemStack(Material.ANVIL, Component.translatable(Villager.Profession.ARMORER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build())
                            .append(Component.newline()).append(Component.translatable(Villager.Profession.WEAPONSMITH.translationKey()))
                            .append(Component.newline()).append(Component.translatable(Villager.Profession.TOOLSMITH.translationKey())), false)
                    , createItemStack(Material.SMOKER, Component.translatable(Villager.Profession.BUTCHER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.CARTOGRAPHY_TABLE, Component.translatable(Villager.Profession.CARTOGRAPHER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.BREWING_STAND, Component.translatable(Villager.Profession.CLERIC.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.COMPOSTER, Component.translatable(Villager.Profession.FARMER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.BARREL, Component.translatable(Villager.Profession.FISHERMAN.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.FLETCHING_TABLE, Component.translatable(Villager.Profession.FLETCHER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.CAULDRON, Component.translatable(Villager.Profession.LEATHERWORKER.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.LECTERN, Component.translatable(Villager.Profession.LIBRARIAN.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.STONECUTTER, Component.translatable(Villager.Profession.MASON.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.LOOM, Component.translatable(Villager.Profession.SHEPHERD.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.NETHER_STAR, Component.text("⭐", Style.style().color(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.ENCHANTED_BOOK, Component.translatable(Material.ENCHANTED_BOOK.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false)
                    , createItemStack(Material.NETHERITE_SCRAP, Component.text("📖", Style.style().color(TextColor.color(0xC24492)).decoration(TextDecoration.ITALIC, false).build()), false))),
    SMITH(Component.translatable(Villager.Profession.ARMORER.translationKey()), smith()),
    BUTCHER(Component.translatable(Villager.Profession.BUTCHER.translationKey()), butcher()),
    CARTOGRAPHER(Component.translatable(Villager.Profession.CARTOGRAPHER.translationKey()), cartographer()),
    CLERIC(Component.translatable(Villager.Profession.CLERIC.translationKey()), cleric()),
    FARMER(Component.translatable(Villager.Profession.FARMER.translationKey()), farmer()),
    FISHERMAN(Component.translatable(Villager.Profession.FISHERMAN.translationKey()), fisherman()),
    FLETCHER(Component.translatable(Villager.Profession.FLETCHER.translationKey()), fletcher()),
    LEATHER_WORKER(Component.translatable(Villager.Profession.LEATHERWORKER.translationKey()), leatherWorker()),
    LIBRARIAN(Component.translatable(Villager.Profession.LIBRARIAN.translationKey()), librarian()),
    MASON(Component.translatable(Villager.Profession.MASON.translationKey()), mason()),
    SHEPHERD(Component.translatable(Villager.Profession.SHEPHERD.translationKey()), shepherd()),
    CUSTOM(Component.translatable(SHOP_SPECIAL), custom()),
    ENCHANTMENTS(Component.translatable(SHOP_ENCHANTMENT), enchantments()),
    ANCIENT_TOMES(Component.translatable(SHOP_ANCIENT_TOME), ancientTomes());

    final Component title;
    final int pages;
    private final List<?> buy;

    ShopCategory(Component title, List<?> buy) {
        this.buy = buy;
        this.pages = (buy.size() / 21F) % 1f > 0 ? (buy.size() / 21) + 1 : buy.size() / 21;
        this.title = title;
    }


    public static ShopCategory fromTranslationKey(String category) {
        if (category.equals(Villager.Profession.ARMORER.translationKey()))
            return SMITH;
        else if (category.equals(Villager.Profession.BUTCHER.translationKey()))
            return BUTCHER;
        else if (category.equals(Villager.Profession.CARTOGRAPHER.translationKey()))
            return CARTOGRAPHER;
        else if (category.equals(Villager.Profession.CLERIC.translationKey()))
            return CLERIC;
        else if (category.equals(Villager.Profession.FARMER.translationKey()))
            return FARMER;
        else if (category.equals(Villager.Profession.FISHERMAN.translationKey()))
            return FISHERMAN;
        else if (category.equals(Villager.Profession.FLETCHER.translationKey()))
            return FLETCHER;
        else if (category.equals(Villager.Profession.LEATHERWORKER.translationKey()))
            return LEATHER_WORKER;
        else if (category.equals(Villager.Profession.LIBRARIAN.translationKey()))
            return LIBRARIAN;
        else if (category.equals(Villager.Profession.MASON.translationKey()))
            return MASON;
        else if (category.equals(Villager.Profession.SHEPHERD.translationKey()))
            return SHEPHERD;
        else if (category.equals(SHOP_SPECIAL))
            return CUSTOM;
        else if (category.equals(SHOP_ENCHANTMENT))
            return ENCHANTMENTS;
        else if (category.equals(SHOP_ANCIENT_TOME))
            return ANCIENT_TOMES;
        else return MAIN;
    }

    private static List<MerchantRecipe> smith() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe coal = new MerchantRecipe(emerald(1), 9999);
        coal.addIngredient(new ItemStack(Material.COAL, 15));
        trades.add(coal);

        MerchantRecipe iron = new MerchantRecipe(emerald(1), 9999);
        iron.addIngredient(new ItemStack(Material.IRON_INGOT, 4));
        trades.add(iron);

        MerchantRecipe flint = new MerchantRecipe(emerald(1), 9999);
        flint.addIngredient(new ItemStack(Material.FLINT, 24));
        trades.add(flint);

        MerchantRecipe lavaBucked = new MerchantRecipe(emerald(1), 9999);
        lavaBucked.addIngredient(new ItemStack(Material.LAVA_BUCKET, 1));
        trades.add(lavaBucked);

        MerchantRecipe diamond = new MerchantRecipe(emerald(1), 9999);
        diamond.addIngredient(new ItemStack(Material.DIAMOND, 1));
        trades.add(diamond);

        // Sell
        trades.add(withDefaultEnchantment(Material.DIAMOND_HOE, 14, Enchantment.UNBREAKING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_AXE, 17, Enchantment.UNBREAKING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_SHOVEL, 10, Enchantment.UNBREAKING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_PICKAXE, 18, Enchantment.UNBREAKING, Enchantment.EFFICIENCY));
        trades.add(withDefaultEnchantment(Material.DIAMOND_SWORD, 13, Enchantment.UNBREAKING, Enchantment.LOOTING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_HELMET, 13, Enchantment.UNBREAKING, Enchantment.AQUA_AFFINITY));
        trades.add(withDefaultEnchantment(Material.DIAMOND_CHESTPLATE, 21, Enchantment.UNBREAKING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_LEGGINGS, 19, Enchantment.UNBREAKING));
        trades.add(withDefaultEnchantment(Material.DIAMOND_BOOTS, 13, Enchantment.UNBREAKING, Enchantment.FEATHER_FALLING));

        MerchantRecipe shield = new MerchantRecipe(new ItemStack(Material.SHIELD, 1), 9999);
        shield.addIngredient(emerald(5));
        trades.add(shield);

        MerchantRecipe bell = new MerchantRecipe(new ItemStack(Material.BELL, 1), 9999);
        bell.addIngredient(emerald(36));
        trades.add(bell);

        return trades;
    }

    private static List<MerchantRecipe> custom() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe copper = new MerchantRecipe(emerald(2), 9999);
        copper.addIngredient(new ItemStack(Material.COPPER_BLOCK, 1));
        trades.add(copper);

        // Sell

        MerchantRecipe slime = new MerchantRecipe(new ItemStack(Material.SLIME_BALL, 3), 9999);
        slime.addIngredient(new ItemStack(Material.HONEY_BLOCK, 1));
        trades.add(slime);

        return trades;
    }

    private static List<ItemStack> enchantments() {
        List<ItemStack> enchantments = new ArrayList<>();
        List<Enchantment> ciklesEnchantments = Config.enabledEnchantments().stream().map(Enchantments::getEnchantment).toList();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().filter(e -> (Config.enchantments() && e.getKey().namespace().equalsIgnoreCase("ciklesmc") && ciklesEnchantments.contains(e)) || (e.isTradeable() && !e.isCursed())).forEach(e -> {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(e, e.getMaxLevel(), true);
            meta.setMaxStackSize(e.getMaxLevel());
            item.setItemMeta(meta);
            item.setAmount(e.getMaxLevel());
            enchantments.add(item);
        });
        return enchantments;
    }

    private static List<MerchantRecipe> ancientTomes() {
        List<MerchantRecipe> trades = new ArrayList<>();
        Config.getAncientTomes().stream().map(ShopCategory::upgradeEnchantment).forEach(trades::add);
        return trades;
    }

    private static MerchantRecipe upgradeEnchantment(Enchantment enchantment) {
        ItemStack netheriteScrap = Material.NETHERITE_SCRAP.asItemType().createItemStack();
        ItemStack result = Material.NETHERITE_SCRAP.asItemType().createItemStack();
        ItemMeta resultMeta = result.getItemMeta();
        resultMeta.customName(Component.text("Ancient Tome", TextColor.color(0xC24492)));
        resultMeta.addEnchant(enchantment, 1, true);
        result.setItemMeta(resultMeta);


        MerchantRecipe recipe = new MerchantRecipe(result, 9999);
        recipe.addIngredient(emerald(enchantment.getMaxLevel() * 2 + 3));
        recipe.addIngredient(netheriteScrap);
        return recipe;
    }


    private static List<MerchantRecipe> butcher() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe rawChicken = new MerchantRecipe(emerald, 9999);
        rawChicken.addIngredient(new ItemStack(Material.CHICKEN, 14));
        trades.add(rawChicken);

        MerchantRecipe pork = new MerchantRecipe(emerald, 9999);
        pork.addIngredient(new ItemStack(Material.PORKCHOP, 7));
        trades.add(pork);

        MerchantRecipe rawRabbit = new MerchantRecipe(emerald, 9999);
        rawRabbit.addIngredient(new ItemStack(Material.RABBIT, 4));
        trades.add(rawRabbit);

        MerchantRecipe rawMutton = new MerchantRecipe(emerald, 9999);
        rawMutton.addIngredient(new ItemStack(Material.MUTTON, 7));
        trades.add(rawMutton);

        MerchantRecipe rawBeef = new MerchantRecipe(emerald, 9999);
        rawBeef.addIngredient(new ItemStack(Material.BEEF, 10));
        trades.add(rawBeef);

        MerchantRecipe coal = new MerchantRecipe(emerald, 9999);
        coal.addIngredient(new ItemStack(Material.COAL, 15));
        trades.add(coal);

        MerchantRecipe driedKelbBlock = new MerchantRecipe(emerald, 9999);
        driedKelbBlock.addIngredient(new ItemStack(Material.DRIED_KELP_BLOCK, 10));
        trades.add(driedKelbBlock);

        MerchantRecipe sweetBerries = new MerchantRecipe(emerald, 9999);
        sweetBerries.addIngredient(new ItemStack(Material.SWEET_BERRIES, 10));
        trades.add(sweetBerries);

        // Sell

        MerchantRecipe rabbitStew = new MerchantRecipe(new ItemStack(Material.RABBIT_STEW, 1), 9999);
        rabbitStew.addIngredient(emerald);
        trades.add(rabbitStew);

        MerchantRecipe cookedPorkChop = new MerchantRecipe(new ItemStack(Material.COOKED_PORKCHOP, 4), 9999);
        cookedPorkChop.addIngredient(emerald);
        trades.add(cookedPorkChop);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.COOKED_CHICKEN, 8), 9999);
        glass.addIngredient(emerald);
        trades.add(glass);


        return trades;
    }

    private static List<MerchantRecipe> cartographer() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 9999);
        paper.addIngredient(new ItemStack(Material.PAPER, 24));
        trades.add(paper);

        MerchantRecipe glassPane = new MerchantRecipe(emerald(1), 9999);
        glassPane.addIngredient(new ItemStack(Material.GLASS_PANE, 10));
        trades.add(glassPane);

        MerchantRecipe compass = new MerchantRecipe(emerald(1), 9999);
        compass.addIngredient(new ItemStack(Material.COMPASS, 1));
        trades.add(compass);

        // Sell

        MerchantRecipe bookshelf = new MerchantRecipe(new ItemStack(Material.MAP, 1), 9999);
        bookshelf.addIngredient(emerald(7));
        trades.add(bookshelf);

        MerchantRecipe itemFrame = new MerchantRecipe(new ItemStack(Material.ITEM_FRAME, 1), 9999);
        itemFrame.addIngredient(emerald(7));
        trades.add(itemFrame);

        MerchantRecipe glowItemFrame = new MerchantRecipe(new ItemStack(Material.GLOW_ITEM_FRAME, 1), 9999);
        glowItemFrame.addIngredient(emerald(9));
        trades.add(glowItemFrame);

        return trades;
    }

    private static List<MerchantRecipe> cleric() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe rottenFlesh = new MerchantRecipe(emerald(1), 9999);
        rottenFlesh.addIngredient(new ItemStack(Material.ROTTEN_FLESH, 32));
        trades.add(rottenFlesh);

        MerchantRecipe gold = new MerchantRecipe(emerald(1), 9999);
        gold.addIngredient(new ItemStack(Material.GOLD_INGOT, 3));
        trades.add(gold);

        MerchantRecipe goldBlock = new MerchantRecipe(emerald(7), 9999);
        goldBlock.addIngredient(new ItemStack(Material.GOLD_BLOCK, 2));
        trades.add(goldBlock);

        MerchantRecipe rabbitFoot = new MerchantRecipe(emerald(1), 9999);
        rabbitFoot.addIngredient(new ItemStack(Material.RABBIT_FOOT, 2));
        trades.add(rabbitFoot);

        MerchantRecipe scute = new MerchantRecipe(emerald(1), 9999);
        scute.addIngredient(new ItemStack(Material.TURTLE_SCUTE, 4));
        trades.add(scute);

        MerchantRecipe bottle = new MerchantRecipe(emerald(1), 9999);
        bottle.addIngredient(new ItemStack(Material.GLASS_BOTTLE, 9));
        trades.add(bottle);

        MerchantRecipe netherWart = new MerchantRecipe(emerald(1), 9999);
        netherWart.addIngredient(new ItemStack(Material.NETHER_WART, 22));
        trades.add(netherWart);

        // Sell

        MerchantRecipe redstone = new MerchantRecipe(new ItemStack(Material.REDSTONE_BLOCK, 2), 9999);
        redstone.addIngredient(emerald(5));
        trades.add(redstone);

        MerchantRecipe lapisLazuli = new MerchantRecipe(new ItemStack(Material.LAPIS_LAZULI, 2), 9999);
        lapisLazuli.addIngredient(emerald(1));
        trades.add(lapisLazuli);

        MerchantRecipe glowstone = new MerchantRecipe(new ItemStack(Material.GLOWSTONE, 2), 9999);
        glowstone.addIngredient(emerald(7));
        trades.add(glowstone);

        MerchantRecipe enderpearl = new MerchantRecipe(new ItemStack(Material.ENDER_PEARL, 1), 9999);
        enderpearl.addIngredient(emerald(5));
        trades.add(enderpearl);

        MerchantRecipe exp = new MerchantRecipe(new ItemStack(Material.EXPERIENCE_BOTTLE, 1), 9999);
        exp.addIngredient(emerald(3));
        trades.add(exp);

        return trades;
    }

    private static List<MerchantRecipe> farmer() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe wheat = new MerchantRecipe(emerald, 9999);
        wheat.addIngredient(new ItemStack(Material.WHEAT, 20));
        trades.add(wheat);

        MerchantRecipe beetroots = new MerchantRecipe(emerald, 9999);
        beetroots.addIngredient(new ItemStack(Material.BEETROOT, 15));
        trades.add(beetroots);

        MerchantRecipe carrot = new MerchantRecipe(emerald, 9999);
        carrot.addIngredient(new ItemStack(Material.CARROT, 22));
        trades.add(carrot);

        MerchantRecipe potato = new MerchantRecipe(emerald, 9999);
        potato.addIngredient(new ItemStack(Material.POTATO, 26));
        trades.add(potato);

        MerchantRecipe pumpkin = new MerchantRecipe(emerald, 9999);
        pumpkin.addIngredient(new ItemStack(Material.PUMPKIN, 6));
        trades.add(pumpkin);

        MerchantRecipe melon = new MerchantRecipe(emerald, 9999);
        melon.addIngredient(new ItemStack(Material.MELON, 4));
        trades.add(melon);

        // Sell

        MerchantRecipe pumpkinPie = new MerchantRecipe(new ItemStack(Material.PUMPKIN_PIE, 4), 9999);
        pumpkinPie.addIngredient(emerald);
        trades.add(pumpkinPie);

        MerchantRecipe apple = new MerchantRecipe(new ItemStack(Material.APPLE, 4), 9999);
        apple.addIngredient(emerald);
        trades.add(apple);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.COOKIE, 18), 9999);
        glass.addIngredient(emerald(3));
        trades.add(glass);

        MerchantRecipe compass = new MerchantRecipe(new ItemStack(Material.CAKE, 1), 9999);
        compass.addIngredient(emerald(4));
        trades.add(compass);

        MerchantRecipe clock = new MerchantRecipe(new ItemStack(Material.GOLDEN_CARROT, 3), 9999);
        clock.addIngredient(emerald(3));
        trades.add(clock);

        MerchantRecipe nameTag = new MerchantRecipe(new ItemStack(Material.GLISTERING_MELON_SLICE, 3), 9999);
        nameTag.addIngredient(emerald(4));
        trades.add(nameTag);


        return trades;
    }

    private static List<MerchantRecipe> fisherman() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe string = new MerchantRecipe(emerald, 9999);
        string.addIngredient(new ItemStack(Material.STRING, 14));
        trades.add(string);

        MerchantRecipe coal = new MerchantRecipe(emerald, 9999);
        coal.addIngredient(new ItemStack(Material.COAL, 10));
        trades.add(coal);

        MerchantRecipe cod = new MerchantRecipe(emerald, 9999);
        cod.addIngredient(new ItemStack(Material.COD, 15));
        trades.add(cod);

        MerchantRecipe salmon = new MerchantRecipe(emerald, 9999);
        salmon.addIngredient(new ItemStack(Material.SALMON, 13));
        trades.add(salmon);

        MerchantRecipe tropicalFish = new MerchantRecipe(emerald, 9999);
        tropicalFish.addIngredient(new ItemStack(Material.TROPICAL_FISH, 6));
        trades.add(tropicalFish);

        MerchantRecipe pufferFish = new MerchantRecipe(emerald, 9999);
        pufferFish.addIngredient(new ItemStack(Material.PUFFERFISH, 4));
        trades.add(pufferFish);

        MerchantRecipe boat = new MerchantRecipe(emerald, 9999);
        boat.addIngredient(new ItemStack(Material.OAK_BOAT, 20));
        trades.add(boat);

        // Sell

        MerchantRecipe cookedCod = new MerchantRecipe(new ItemStack(Material.COOKED_COD, 6), 9999);
        cookedCod.addIngredient(emerald);
        cookedCod.addIngredient(new ItemStack(Material.COD, 6));
        trades.add(cookedCod);

        MerchantRecipe cookedSalmon = new MerchantRecipe(new ItemStack(Material.COOKED_SALMON, 6), 9999);
        cookedSalmon.addIngredient(emerald);
        cookedSalmon.addIngredient(new ItemStack(Material.SALMON, 6));
        trades.add(cookedSalmon);

        MerchantRecipe campfire = new MerchantRecipe(new ItemStack(Material.CAMPFIRE, 1), 9999);
        campfire.addIngredient(emerald(2));
        trades.add(campfire);

        MerchantRecipe codBucked = new MerchantRecipe(new ItemStack(Material.COD_BUCKET, 1), 9999);
        codBucked.addIngredient(emerald(3));
        trades.add(codBucked);

        MerchantRecipe fishingRod = new MerchantRecipe(new ItemStack(Material.FISHING_ROD, 1), 9999);
        fishingRod.addIngredient(emerald(3));
        trades.add(fishingRod);

        return trades;
    }

    private static List<MerchantRecipe> fletcher() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 9999);
        paper.addIngredient(new ItemStack(Material.STICK, 32));
        trades.add(paper);

        MerchantRecipe string = new MerchantRecipe(emerald(1), 9999);
        string.addIngredient(new ItemStack(Material.STRING, 14));
        trades.add(string);

        MerchantRecipe feather = new MerchantRecipe(emerald(1), 9999);
        feather.addIngredient(new ItemStack(Material.FEATHER, 24));
        trades.add(feather);

        MerchantRecipe tripwireHook = new MerchantRecipe(emerald(1), 9999);
        tripwireHook.addIngredient(new ItemStack(Material.TRIPWIRE_HOOK, 8));
        trades.add(tripwireHook);

        // Sell

        MerchantRecipe bow = new MerchantRecipe(new ItemStack(Material.BOW, 1), 9999);
        bow.addIngredient(emerald(2));
        trades.add(bow);

        MerchantRecipe crossbow = new MerchantRecipe(new ItemStack(Material.CROSSBOW, 1), 9999);
        crossbow.addIngredient(emerald(3));
        trades.add(crossbow);

        MerchantRecipe flint = new MerchantRecipe(new ItemStack(Material.FLINT, 10), 9999);
        flint.addIngredient(emerald(1));
        flint.addIngredient(new ItemStack(Material.GRAVEL, 10));
        trades.add(flint);


        return trades;
    }

    private static List<MerchantRecipe> leatherWorker() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe leather = new MerchantRecipe(emerald(1), 9999);
        leather.addIngredient(new ItemStack(Material.LEATHER, 6));
        trades.add(leather);

        MerchantRecipe flint = new MerchantRecipe(emerald(1), 9999);
        flint.addIngredient(new ItemStack(Material.FLINT, 24));
        trades.add(flint);

        MerchantRecipe rabbitHide = new MerchantRecipe(emerald(1), 9999);
        rabbitHide.addIngredient(new ItemStack(Material.RABBIT_HIDE, 9));
        trades.add(rabbitHide);

        MerchantRecipe turtleScute = new MerchantRecipe(emerald(1), 9999);
        turtleScute.addIngredient(new ItemStack(Material.TURTLE_SCUTE, 4));
        trades.add(turtleScute);

        // Sell

        MerchantRecipe horseArmor = new MerchantRecipe(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1), 9999);
        horseArmor.addIngredient(emerald(6));
        trades.add(horseArmor);

        MerchantRecipe saddle = new MerchantRecipe(new ItemStack(Material.SADDLE, 1), 9999);
        saddle.addIngredient(emerald(6));
        trades.add(saddle);


        return trades;
    }

    private static List<MerchantRecipe> librarian() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 9999);
        paper.addIngredient(new ItemStack(Material.PAPER, 24));
        trades.add(paper);

        MerchantRecipe book = new MerchantRecipe(emerald(1), 9999);
        book.addIngredient(new ItemStack(Material.BOOK, 4));
        trades.add(book);

        MerchantRecipe ink = new MerchantRecipe(emerald(1), 9999);
        ink.addIngredient(new ItemStack(Material.INK_SAC, 5));
        trades.add(ink);

        MerchantRecipe bookAndQuill = new MerchantRecipe(emerald(1), 9999);
        bookAndQuill.addIngredient(new ItemStack(Material.WRITABLE_BOOK, 2));
        trades.add(bookAndQuill);

        // Sell

        MerchantRecipe bookshelf = new MerchantRecipe(new ItemStack(Material.BOOKSHELF, 3), 9999);
        bookshelf.addIngredient(emerald(6));
        trades.add(bookshelf);

        MerchantRecipe lantern = new MerchantRecipe(new ItemStack(Material.LANTERN, 1), 9999);
        lantern.addIngredient(emerald(1));
        trades.add(lantern);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.GLASS, 4), 9999);
        glass.addIngredient(emerald(1));
        trades.add(glass);

        MerchantRecipe compass = new MerchantRecipe(new ItemStack(Material.COMPASS, 1), 9999);
        compass.addIngredient(emerald(4));
        trades.add(compass);

        MerchantRecipe clock = new MerchantRecipe(new ItemStack(Material.CLOCK, 1), 9999);
        clock.addIngredient(emerald(5));
        trades.add(clock);

        MerchantRecipe nameTag = new MerchantRecipe(new ItemStack(Material.NAME_TAG, 1), 9999);
        nameTag.addIngredient(emerald(20));
        trades.add(nameTag);


        return trades;
    }

    private static List<MerchantRecipe> mason() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe clay = new MerchantRecipe(emerald(1), 9999);
        clay.addIngredient(new ItemStack(Material.CLAY_BALL, 10));
        trades.add(clay);

        MerchantRecipe quartz = new MerchantRecipe(emerald(1), 9999);
        quartz.addIngredient(new ItemStack(Material.QUARTZ, 12));
        trades.add(quartz);

        MerchantRecipe stone = new MerchantRecipe(emerald(1), 9999);
        stone.addIngredient(new ItemStack(Material.STONE, 20));
        trades.add(stone);

        MerchantRecipe andesit = new MerchantRecipe(emerald(1), 9999);
        andesit.addIngredient(new ItemStack(Material.ANDESITE, 10));
        trades.add(andesit);

        MerchantRecipe diorite = new MerchantRecipe(emerald(1), 9999);
        diorite.addIngredient(new ItemStack(Material.DIORITE, 10));
        trades.add(diorite);

        MerchantRecipe granite = new MerchantRecipe(emerald(1), 9999);
        granite.addIngredient(new ItemStack(Material.GRANITE, 10));
        trades.add(granite);


        MerchantRecipe polishedAndesit = new MerchantRecipe(emerald(1), 9999);
        polishedAndesit.addIngredient(new ItemStack(Material.POLISHED_ANDESITE, 4));
        trades.add(polishedAndesit);

        MerchantRecipe polishedDiorite = new MerchantRecipe(emerald(1), 9999);
        polishedDiorite.addIngredient(new ItemStack(Material.POLISHED_DIORITE, 4));
        trades.add(polishedDiorite);

        MerchantRecipe polishedGranite = new MerchantRecipe(emerald(1), 9999);
        polishedGranite.addIngredient(new ItemStack(Material.POLISHED_GRANITE, 4));
        trades.add(polishedGranite);

        return trades;
    }

    private static List<MerchantRecipe> shepherd() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe whiteWool = new MerchantRecipe(emerald(1), 9999);
        whiteWool.addIngredient(new ItemStack(Material.WHITE_WOOL, 18));
        trades.add(whiteWool);

        MerchantRecipe whiteDye = new MerchantRecipe(emerald(1), 9999);
        whiteDye.addIngredient(new ItemStack(Material.WHITE_DYE, 12));
        trades.add(whiteDye);

        // Sell

        MerchantRecipe bed = new MerchantRecipe(new ItemStack(Material.RED_BED, 1), 9999);
        bed.addIngredient(emerald(3));
        trades.add(bed);

        MerchantRecipe painting = new MerchantRecipe(new ItemStack(Material.PAINTING, 3), 9999);
        painting.addIngredient(emerald(2));
        trades.add(painting);

        return trades;
    }

    private static MerchantRecipe withDefaultEnchantment(Material material, int baseCost, Enchantment... enchantments) {
        ItemStack item = material.asItemType().createItemStack();
        ItemMeta meta = item.getItemMeta();
        int level = -3;
        for (Enchantment enchantment : enchantments) {
            meta.addEnchant(enchantment, enchantment.getMaxLevel(), true);
            level += enchantment.getMaxLevel();
        }
        item.setItemMeta(meta);

        MerchantRecipe trade = new MerchantRecipe(item, 9999);
        trade.addIngredient(emerald(Math.clamp(baseCost + level * 2L, baseCost, 35)));
        return trade;
    }


    private static ItemStack emerald(int amount) {
        return new ItemStack(Material.EMERALD, amount);
    }

    @SuppressWarnings("unchecked")
    public List<MerchantRecipe> recipe() {
        if (isMerchant())
            return (List<MerchantRecipe>) buy;
        return List.of();
    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> itemStack() {
        if (!isMerchant())
            return (List<ItemStack>) buy;
        return List.of();
    }

    public int size() {
        return buy.size();
    }

    public boolean isMerchant() {
        return !buy.isEmpty() && buy.get(0) instanceof MerchantRecipe;
    }

}
