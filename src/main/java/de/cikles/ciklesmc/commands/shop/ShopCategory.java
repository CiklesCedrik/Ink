package de.cikles.ciklesmc.commands.shop;

import de.cikles.ciklesmc.core.CiklesMC;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static de.cikles.ciklesmc.commands.shop.Shop.createItemStack;
import static de.cikles.ciklesmc.utility.Config.Translations.SHOP_ENCHANTMENT;
import static de.cikles.ciklesmc.utility.Config.Translations.SHOP_TITLE_VILLAGER_TRADES;

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
                    , createItemStack(Material.ENDER_CHEST, Component.text("=?", NamedTextColor.GOLD), false)
                    , createItemStack(Material.ENCHANTED_BOOK, Component.translatable(Material.ENCHANTED_BOOK.translationKey(), Style.style().decoration(TextDecoration.ITALIC, false).build()), false))),
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
    CUSTOM(Component.text("=?", NamedTextColor.GOLD), custom()),
    ENCHANTMENTS(Component.translatable(SHOP_ENCHANTMENT), enchantments());

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
        else if (category.equals(SHOP_ENCHANTMENT))
            return ENCHANTMENTS;
        else return MAIN;
    }

    private static List<MerchantRecipe> smith() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe coal = new MerchantRecipe(emerald(1), 50);
        coal.addIngredient(new ItemStack(Material.COAL, 15));
        trades.add(coal);

        MerchantRecipe iron = new MerchantRecipe(emerald(1), 50);
        iron.addIngredient(new ItemStack(Material.IRON_INGOT, 4));
        trades.add(iron);

        MerchantRecipe flint = new MerchantRecipe(emerald(1), 50);
        flint.addIngredient(new ItemStack(Material.FLINT, 24));
        trades.add(flint);

        MerchantRecipe lavaBucked = new MerchantRecipe(emerald(1), 50);
        lavaBucked.addIngredient(new ItemStack(Material.LAVA_BUCKET, 1));
        trades.add(lavaBucked);

        MerchantRecipe diamond = new MerchantRecipe(emerald(1), 50);
        diamond.addIngredient(new ItemStack(Material.DIAMOND, 1));
        trades.add(diamond);

        // Sell
        MerchantRecipe shield = new MerchantRecipe(new ItemStack(Material.SHIELD, 1), 50);
        shield.addIngredient(emerald(5));
        trades.add(shield);

        MerchantRecipe bell = new MerchantRecipe(new ItemStack(Material.BELL, 1), 50);
        bell.addIngredient(emerald(36));
        trades.add(bell);

        return trades;
    }

    private static List<MerchantRecipe> butcher() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe rawChicken = new MerchantRecipe(emerald, 50);
        rawChicken.addIngredient(new ItemStack(Material.CHICKEN, 14));
        trades.add(rawChicken);

        MerchantRecipe pork = new MerchantRecipe(emerald, 50);
        pork.addIngredient(new ItemStack(Material.PORKCHOP, 7));
        trades.add(pork);

        MerchantRecipe rawRabbit = new MerchantRecipe(emerald, 50);
        rawRabbit.addIngredient(new ItemStack(Material.RABBIT, 4));
        trades.add(rawRabbit);

        MerchantRecipe rawMutton = new MerchantRecipe(emerald, 50);
        rawMutton.addIngredient(new ItemStack(Material.MUTTON, 7));
        trades.add(rawMutton);

        MerchantRecipe rawBeef = new MerchantRecipe(emerald, 50);
        rawBeef.addIngredient(new ItemStack(Material.BEEF, 10));
        trades.add(rawBeef);

        MerchantRecipe coal = new MerchantRecipe(emerald, 50);
        coal.addIngredient(new ItemStack(Material.COAL, 15));
        trades.add(coal);

        MerchantRecipe driedKelbBlock = new MerchantRecipe(emerald, 50);
        driedKelbBlock.addIngredient(new ItemStack(Material.DRIED_KELP_BLOCK, 10));
        trades.add(driedKelbBlock);

        MerchantRecipe sweetBerries = new MerchantRecipe(emerald, 50);
        sweetBerries.addIngredient(new ItemStack(Material.SWEET_BERRIES, 10));
        trades.add(sweetBerries);

        // Sell

        MerchantRecipe rabbitStew = new MerchantRecipe(new ItemStack(Material.RABBIT_STEW, 1), 50);
        rabbitStew.addIngredient(emerald);
        trades.add(rabbitStew);

        MerchantRecipe cookedPorkChop = new MerchantRecipe(new ItemStack(Material.COOKED_PORKCHOP, 4), 50);
        cookedPorkChop.addIngredient(emerald);
        trades.add(cookedPorkChop);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.COOKED_CHICKEN, 8), 50);
        glass.addIngredient(emerald);
        trades.add(glass);


        return trades;
    }

    private static List<MerchantRecipe> cartographer() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 50);
        paper.addIngredient(new ItemStack(Material.PAPER, 24));
        trades.add(paper);

        MerchantRecipe glassPane = new MerchantRecipe(emerald(1), 50);
        glassPane.addIngredient(new ItemStack(Material.GLASS_PANE, 10));
        trades.add(glassPane);

        MerchantRecipe compass = new MerchantRecipe(emerald(1), 50);
        compass.addIngredient(new ItemStack(Material.COMPASS, 1));
        trades.add(compass);

        // Sell

        MerchantRecipe bookshelf = new MerchantRecipe(new ItemStack(Material.MAP, 1), 50);
        bookshelf.addIngredient(emerald(7));
        trades.add(bookshelf);

        MerchantRecipe itemFrame = new MerchantRecipe(new ItemStack(Material.ITEM_FRAME, 1), 50);
        itemFrame.addIngredient(emerald(7));
        trades.add(itemFrame);

        MerchantRecipe glowItemFrame = new MerchantRecipe(new ItemStack(Material.GLOW_ITEM_FRAME, 1), 50);
        glowItemFrame.addIngredient(emerald(9));
        trades.add(glowItemFrame);

        return trades;
    }

    private static List<MerchantRecipe> cleric() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe rottenFlesh = new MerchantRecipe(emerald(1), 50);
        rottenFlesh.addIngredient(new ItemStack(Material.ROTTEN_FLESH, 32));
        trades.add(rottenFlesh);

        MerchantRecipe gold = new MerchantRecipe(emerald(1), 50);
        gold.addIngredient(new ItemStack(Material.GOLD_INGOT, 3));
        trades.add(gold);

        MerchantRecipe goldBlock = new MerchantRecipe(emerald(7), 50);
        goldBlock.addIngredient(new ItemStack(Material.GOLD_BLOCK, 2));
        trades.add(goldBlock);

        MerchantRecipe rabbitFoot = new MerchantRecipe(emerald(1), 50);
        rabbitFoot.addIngredient(new ItemStack(Material.RABBIT_FOOT, 2));
        trades.add(rabbitFoot);

        MerchantRecipe scute = new MerchantRecipe(emerald(1), 50);
        scute.addIngredient(new ItemStack(Material.TURTLE_SCUTE, 4));
        trades.add(scute);

        MerchantRecipe bottle = new MerchantRecipe(emerald(1), 50);
        bottle.addIngredient(new ItemStack(Material.GLASS_BOTTLE, 9));
        trades.add(bottle);

        MerchantRecipe netherWart = new MerchantRecipe(emerald(1), 50);
        netherWart.addIngredient(new ItemStack(Material.NETHER_WART, 22));
        trades.add(netherWart);

        // Sell

        MerchantRecipe redstone = new MerchantRecipe(new ItemStack(Material.REDSTONE_BLOCK, 2), 50);
        redstone.addIngredient(emerald(5));
        trades.add(redstone);

        MerchantRecipe lapisLazuli = new MerchantRecipe(new ItemStack(Material.LAPIS_LAZULI, 2), 50);
        lapisLazuli.addIngredient(emerald(1));
        trades.add(lapisLazuli);

        MerchantRecipe glowstone = new MerchantRecipe(new ItemStack(Material.GLOWSTONE, 2), 50);
        glowstone.addIngredient(emerald(7));
        trades.add(glowstone);

        MerchantRecipe enderpearl = new MerchantRecipe(new ItemStack(Material.ENDER_PEARL, 1), 50);
        enderpearl.addIngredient(emerald(5));
        trades.add(enderpearl);

        MerchantRecipe exp = new MerchantRecipe(new ItemStack(Material.EXPERIENCE_BOTTLE, 1), 50);
        exp.addIngredient(emerald(3));
        trades.add(exp);

        return trades;
    }

    private static List<MerchantRecipe> farmer() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe wheat = new MerchantRecipe(emerald, 50);
        wheat.addIngredient(new ItemStack(Material.WHEAT, 20));
        trades.add(wheat);

        MerchantRecipe beetroots = new MerchantRecipe(emerald, 50);
        beetroots.addIngredient(new ItemStack(Material.BEETROOT, 15));
        trades.add(beetroots);

        MerchantRecipe carrot = new MerchantRecipe(emerald, 50);
        carrot.addIngredient(new ItemStack(Material.CARROT, 22));
        trades.add(carrot);

        MerchantRecipe potato = new MerchantRecipe(emerald, 50);
        potato.addIngredient(new ItemStack(Material.POTATO, 26));
        trades.add(potato);

        MerchantRecipe pumpkin = new MerchantRecipe(emerald, 50);
        pumpkin.addIngredient(new ItemStack(Material.PUMPKIN, 6));
        trades.add(pumpkin);

        MerchantRecipe melon = new MerchantRecipe(emerald, 50);
        melon.addIngredient(new ItemStack(Material.MELON, 4));
        trades.add(melon);

        // Sell

        MerchantRecipe pumpkinPie = new MerchantRecipe(new ItemStack(Material.PUMPKIN_PIE, 4), 50);
        pumpkinPie.addIngredient(emerald);
        trades.add(pumpkinPie);

        MerchantRecipe apple = new MerchantRecipe(new ItemStack(Material.APPLE, 4), 50);
        apple.addIngredient(emerald);
        trades.add(apple);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.COOKIE, 18), 50);
        glass.addIngredient(emerald(3));
        trades.add(glass);

        MerchantRecipe compass = new MerchantRecipe(new ItemStack(Material.CAKE, 1), 50);
        compass.addIngredient(emerald(4));
        trades.add(compass);

        MerchantRecipe clock = new MerchantRecipe(new ItemStack(Material.GOLDEN_CARROT, 3), 50);
        clock.addIngredient(emerald(3));
        trades.add(clock);

        MerchantRecipe nameTag = new MerchantRecipe(new ItemStack(Material.GLISTERING_MELON_SLICE, 3), 50);
        nameTag.addIngredient(emerald(4));
        trades.add(nameTag);


        return trades;
    }

    private static List<MerchantRecipe> fisherman() {
        List<MerchantRecipe> trades = new ArrayList<>();

        ItemStack emerald = emerald(1);

        // Buy
        MerchantRecipe string = new MerchantRecipe(emerald, 50);
        string.addIngredient(new ItemStack(Material.STRING, 14));
        trades.add(string);

        MerchantRecipe coal = new MerchantRecipe(emerald, 50);
        coal.addIngredient(new ItemStack(Material.COAL, 10));
        trades.add(coal);

        MerchantRecipe cod = new MerchantRecipe(emerald, 50);
        cod.addIngredient(new ItemStack(Material.COD, 15));
        trades.add(cod);

        MerchantRecipe salmon = new MerchantRecipe(emerald, 50);
        salmon.addIngredient(new ItemStack(Material.SALMON, 13));
        trades.add(salmon);

        MerchantRecipe tropicalFish = new MerchantRecipe(emerald, 50);
        tropicalFish.addIngredient(new ItemStack(Material.TROPICAL_FISH, 6));
        trades.add(tropicalFish);

        MerchantRecipe pufferFish = new MerchantRecipe(emerald, 50);
        pufferFish.addIngredient(new ItemStack(Material.PUFFERFISH, 4));
        trades.add(pufferFish);

        MerchantRecipe boat = new MerchantRecipe(emerald, 50);
        boat.addIngredient(new ItemStack(Material.OAK_BOAT, 20));
        trades.add(boat);

        // Sell

        MerchantRecipe cookedCod = new MerchantRecipe(new ItemStack(Material.COOKED_COD, 6), 50);
        cookedCod.addIngredient(emerald);
        cookedCod.addIngredient(new ItemStack(Material.COD, 6));
        trades.add(cookedCod);

        MerchantRecipe cookedSalmon = new MerchantRecipe(new ItemStack(Material.COOKED_SALMON, 6), 50);
        cookedSalmon.addIngredient(emerald);
        cookedSalmon.addIngredient(new ItemStack(Material.SALMON, 6));
        trades.add(cookedSalmon);

        MerchantRecipe campfire = new MerchantRecipe(new ItemStack(Material.CAMPFIRE, 1), 50);
        campfire.addIngredient(emerald(2));
        trades.add(campfire);

        MerchantRecipe codBucked = new MerchantRecipe(new ItemStack(Material.COD_BUCKET, 1), 50);
        codBucked.addIngredient(emerald(3));
        trades.add(codBucked);

        MerchantRecipe fishingRod = new MerchantRecipe(new ItemStack(Material.FISHING_ROD, 1), 50);
        fishingRod.addIngredient(emerald(3));
        trades.add(fishingRod);

        return trades;
    }

    private static List<MerchantRecipe> fletcher() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 50);
        paper.addIngredient(new ItemStack(Material.STICK, 32));
        trades.add(paper);

        MerchantRecipe string = new MerchantRecipe(emerald(1), 50);
        string.addIngredient(new ItemStack(Material.STRING, 14));
        trades.add(string);

        MerchantRecipe feather = new MerchantRecipe(emerald(1), 50);
        feather.addIngredient(new ItemStack(Material.FEATHER, 24));
        trades.add(feather);

        MerchantRecipe tripwireHook = new MerchantRecipe(emerald(1), 50);
        tripwireHook.addIngredient(new ItemStack(Material.TRIPWIRE_HOOK, 8));
        trades.add(tripwireHook);

        // Sell

        MerchantRecipe bow = new MerchantRecipe(new ItemStack(Material.BOW, 1), 50);
        bow.addIngredient(emerald(2));
        trades.add(bow);

        MerchantRecipe crossbow = new MerchantRecipe(new ItemStack(Material.CROSSBOW, 1), 50);
        crossbow.addIngredient(emerald(3));
        trades.add(crossbow);

        MerchantRecipe flint = new MerchantRecipe(new ItemStack(Material.FLINT, 10), 50);
        flint.addIngredient(emerald(1));
        flint.addIngredient(new ItemStack(Material.GRAVEL, 10));
        trades.add(flint);


        return trades;
    }

    private static List<MerchantRecipe> leatherWorker() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe leather = new MerchantRecipe(emerald(1), 50);
        leather.addIngredient(new ItemStack(Material.LEATHER, 6));
        trades.add(leather);

        MerchantRecipe flint = new MerchantRecipe(emerald(1), 50);
        flint.addIngredient(new ItemStack(Material.FLINT, 24));
        trades.add(flint);

        MerchantRecipe rabbitHide = new MerchantRecipe(emerald(1), 50);
        rabbitHide.addIngredient(new ItemStack(Material.RABBIT_HIDE, 9));
        trades.add(rabbitHide);

        MerchantRecipe turtleScute = new MerchantRecipe(emerald(1), 50);
        turtleScute.addIngredient(new ItemStack(Material.TURTLE_SCUTE, 4));
        trades.add(turtleScute);

        // Sell

        MerchantRecipe horseArmor = new MerchantRecipe(new ItemStack(Material.LEATHER_HORSE_ARMOR, 1), 50);
        horseArmor.addIngredient(emerald(6));
        trades.add(horseArmor);

        MerchantRecipe saddle = new MerchantRecipe(new ItemStack(Material.SADDLE, 1), 50);
        saddle.addIngredient(emerald(6));
        trades.add(saddle);


        return trades;
    }

    private static List<MerchantRecipe> librarian() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe paper = new MerchantRecipe(emerald(1), 50);
        paper.addIngredient(new ItemStack(Material.PAPER, 24));
        trades.add(paper);

        MerchantRecipe book = new MerchantRecipe(emerald(1), 50);
        book.addIngredient(new ItemStack(Material.BOOK, 4));
        trades.add(book);

        MerchantRecipe ink = new MerchantRecipe(emerald(1), 50);
        ink.addIngredient(new ItemStack(Material.INK_SAC, 5));
        trades.add(ink);

        MerchantRecipe bookAndQuill = new MerchantRecipe(emerald(1), 50);
        bookAndQuill.addIngredient(new ItemStack(Material.WRITABLE_BOOK, 2));
        trades.add(bookAndQuill);

        // Sell

        MerchantRecipe bookshelf = new MerchantRecipe(new ItemStack(Material.BOOKSHELF, 3), 50);
        bookshelf.addIngredient(emerald(6));
        trades.add(bookshelf);

        MerchantRecipe lantern = new MerchantRecipe(new ItemStack(Material.LANTERN, 1), 50);
        lantern.addIngredient(emerald(1));
        trades.add(lantern);

        MerchantRecipe glass = new MerchantRecipe(new ItemStack(Material.GLASS, 4), 50);
        glass.addIngredient(emerald(1));
        trades.add(glass);

        MerchantRecipe compass = new MerchantRecipe(new ItemStack(Material.COMPASS, 1), 50);
        compass.addIngredient(emerald(4));
        trades.add(compass);

        MerchantRecipe clock = new MerchantRecipe(new ItemStack(Material.CLOCK, 1), 50);
        clock.addIngredient(emerald(5));
        trades.add(clock);

        MerchantRecipe nameTag = new MerchantRecipe(new ItemStack(Material.NAME_TAG, 1), 50);
        nameTag.addIngredient(emerald(20));
        trades.add(nameTag);


        return trades;
    }

    private static List<MerchantRecipe> mason() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe clay = new MerchantRecipe(emerald(1), 50);
        clay.addIngredient(new ItemStack(Material.CLAY_BALL, 10));
        trades.add(clay);

        MerchantRecipe quartz = new MerchantRecipe(emerald(1), 50);
        quartz.addIngredient(new ItemStack(Material.QUARTZ, 12));
        trades.add(quartz);

        MerchantRecipe stone = new MerchantRecipe(emerald(1), 50);
        stone.addIngredient(new ItemStack(Material.STONE, 20));
        trades.add(stone);

        MerchantRecipe andesit = new MerchantRecipe(emerald(1), 50);
        andesit.addIngredient(new ItemStack(Material.ANDESITE, 10));
        trades.add(andesit);

        MerchantRecipe diorite = new MerchantRecipe(emerald(1), 50);
        diorite.addIngredient(new ItemStack(Material.DIORITE, 10));
        trades.add(diorite);

        MerchantRecipe granite = new MerchantRecipe(emerald(1), 50);
        granite.addIngredient(new ItemStack(Material.GRANITE, 10));
        trades.add(granite);


        MerchantRecipe polishedAndesit = new MerchantRecipe(emerald(1), 50);
        polishedAndesit.addIngredient(new ItemStack(Material.POLISHED_ANDESITE, 4));
        trades.add(polishedAndesit);

        MerchantRecipe polishedDiorite = new MerchantRecipe(emerald(1), 50);
        polishedDiorite.addIngredient(new ItemStack(Material.POLISHED_DIORITE, 4));
        trades.add(polishedDiorite);

        MerchantRecipe polishedGranite = new MerchantRecipe(emerald(1), 50);
        polishedGranite.addIngredient(new ItemStack(Material.POLISHED_GRANITE, 4));
        trades.add(polishedGranite);

        return trades;
    }

    private static List<MerchantRecipe> shepherd() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe whiteWool = new MerchantRecipe(emerald(1), 50);
        whiteWool.addIngredient(new ItemStack(Material.WHITE_WOOL, 18));
        trades.add(whiteWool);

        MerchantRecipe whiteDye = new MerchantRecipe(emerald(1), 50);
        whiteDye.addIngredient(new ItemStack(Material.WHITE_DYE, 12));
        trades.add(whiteDye);

        // Sell

        MerchantRecipe bed = new MerchantRecipe(new ItemStack(Material.RED_BED, 1), 50);
        bed.addIngredient(emerald(3));
        trades.add(bed);

        MerchantRecipe painting = new MerchantRecipe(new ItemStack(Material.PAINTING, 3), 50);
        painting.addIngredient(emerald(2));
        trades.add(painting);

        return trades;
    }

    private static List<MerchantRecipe> custom() {
        List<MerchantRecipe> trades = new ArrayList<>();

        // Buy
        MerchantRecipe copper = new MerchantRecipe(emerald(2), 50);
        copper.addIngredient(new ItemStack(Material.COPPER_BLOCK, 1));
        trades.add(copper);

        // Sell

        MerchantRecipe slime = new MerchantRecipe(new ItemStack(Material.SLIME_BALL, 2), 50);
        slime.addIngredient(new ItemStack(Material.HONEY_BLOCK, 1));
        trades.add(slime);

        trades.add(upgradeEnchantment(Enchantment.SHARPNESS));
        trades.add(upgradeEnchantment(Enchantment.SWEEPING_EDGE));
        trades.add(upgradeEnchantment(Enchantment.LOOTING));
        trades.add(upgradeEnchantment(Enchantment.FORTUNE));
        trades.add(upgradeEnchantment(Enchantment.UNBREAKING));

        trades.add(upgradeEnchantment(Enchantment.PROTECTION));
        trades.add(upgradeEnchantment(Enchantment.RESPIRATION));
        trades.add(upgradeEnchantment(Enchantment.FEATHER_FALLING));
        trades.add(upgradeEnchantment(Enchantment.DEPTH_STRIDER));
        trades.add(upgradeEnchantment(Enchantment.SWIFT_SNEAK));

        ItemStack elytraItem = Material.ELYTRA.asItemType().createItemStack();
        ItemMeta meta = elytraItem.getItemMeta();
        meta.addEnchant(Enchantment.PROTECTION, 5, true);
        meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("armor", CiklesMC.getInstance())), 8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
        meta.addAttributeModifier(Attribute.ARMOR_TOUGHNESS, new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("armor_toughness", CiklesMC.getInstance())), 3, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
        meta.addAttributeModifier(Attribute.KNOCKBACK_RESISTANCE, new AttributeModifier(Objects.requireNonNull(NamespacedKey.fromString("knockback_resistance", CiklesMC.getInstance())), 1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST));
        elytraItem.setItemMeta(meta);

        MerchantRecipe elytra = new MerchantRecipe(elytraItem, 50);
        elytra.addIngredient(Material.NETHERITE_INGOT.asItemType().createItemStack(2));
        elytra.addIngredient(new ItemStack(Material.ELYTRA));
        trades.add(elytra);

        return trades;
    }

    private static MerchantRecipe upgradeEnchantment(Enchantment enchantment) {
        ItemStack book = Material.ENCHANTED_BOOK.asItemType().createItemStack();
        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
        bookMeta.addStoredEnchant(enchantment, enchantment.getMaxLevel(), true);
        book.setItemMeta(bookMeta);
        ItemStack result = Material.ENCHANTED_BOOK.asItemType().createItemStack();
        EnchantmentStorageMeta resultMeta = (EnchantmentStorageMeta) book.getItemMeta();
        resultMeta.addStoredEnchant(enchantment, enchantment.getMaxLevel() + 1, true);
        result.setItemMeta(resultMeta);

        MerchantRecipe recipe = new MerchantRecipe(result, 50);
        recipe.addIngredient(emerald(12));
        recipe.addIngredient(book);
        return recipe;
    }

    private static ItemStack emerald(int amount) {
        return new ItemStack(Material.EMERALD, amount);
    }

    private static List<ItemStack> enchantments() {
        List<ItemStack> enchantments = new ArrayList<>();
        RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).stream().filter(e -> e.getKey().namespace().equalsIgnoreCase("ciklesmc") || (e.isTradeable() && !e.isCursed())).forEach(e -> {
            ItemStack item = new ItemStack(Material.ENCHANTED_BOOK, 1);
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
            meta.addStoredEnchant(e, 1, true);
            meta.setMaxStackSize(e.getMaxLevel());
            item.setItemMeta(meta);
            item.setAmount(e.getMaxLevel());
            enchantments.add(item);
        });
        return enchantments;
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
