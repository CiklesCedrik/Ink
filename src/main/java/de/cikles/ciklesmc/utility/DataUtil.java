package de.cikles.ciklesmc.utility;

import com.google.common.collect.Lists;
import de.cikles.ciklesmc.core.CiklesMC;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.ListPersistentDataType;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("unused")
public class DataUtil {
    public static final PersistentDataType<long[], java.util.UUID> UUID = new PersistentDataType<>() {
        @Override
        public @NotNull Class<long[]> getPrimitiveType() {
            return long[].class;
        }

        @Override
        public @NotNull Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public long @NotNull [] toPrimitive(java.util.@NotNull UUID complex, @NotNull PersistentDataAdapterContext context) {
            return new long[]{complex.getMostSignificantBits(), complex.getLeastSignificantBits()};
        }

        @Override
        public java.util.@NotNull UUID fromPrimitive(long @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {
            return new UUID(primitive[0], primitive[1]);
        }

    };
    public static final PersistentDataType<String, ItemStack> ITEM_STACK = new PersistentDataType<>() {
        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<ItemStack> getComplexType() {
            return ItemStack.class;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull ItemStack complex, @NotNull PersistentDataAdapterContext context) {
            return complex.isEmpty() ? "air" : Base64.getEncoder().encodeToString(complex.serializeAsBytes());
        }

        @Override
        public @NotNull ItemStack fromPrimitive(@NotNull String primitive, @NotNull PersistentDataAdapterContext context) {
            return primitive.equals("air") ? ItemStack.empty() : ItemStack.deserializeBytes(Base64.getDecoder().decode(primitive));
        }
    };
    public static final NamespacedKey DATA_KEY = NamespacedKey.fromString("data", CiklesMC.getInstance());
    private static final String MESSAGE_VALUE_NOT_FOUND = "Failed to get value";
    public static final ListPersistentDataType<PersistentDataContainer, ItemStack> INVENTORY_CONTENTS = new ListPersistentDataType<>() {
        @NotNull
        private final PersistentDataType<PersistentDataContainer, ItemStack> innerType = new PersistentDataType<>() {

            @Override
            public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }

            @Override
            public @NotNull Class<ItemStack> getComplexType() {
                return ItemStack.class;
            }

            @Override
            public @NotNull PersistentDataContainer toPrimitive(@NotNull ItemStack complex, @NotNull PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                DataUtil.set(container, DATA_KEY, ITEM_STACK.toPrimitive(complex, context), PersistentDataType.STRING);
                return container;
            }

            @Override
            public @NotNull ItemStack fromPrimitive(@NotNull PersistentDataContainer primitive, @NotNull PersistentDataAdapterContext context) {
                return ITEM_STACK.fromPrimitive(DataUtil.get(primitive, DATA_KEY, PersistentDataType.STRING), context);
            }
        };

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Class<List<PersistentDataContainer>> getPrimitiveType() {
            return (Class<List<PersistentDataContainer>>) (Object) List.class;
        }

        @Override
        @SuppressWarnings("unchecked")
        public @NotNull Class<List<ItemStack>> getComplexType() {
            return (Class<List<ItemStack>>) (Object) List.class;
        }

        @NotNull
        @Override
        public List<PersistentDataContainer> toPrimitive(@NotNull List<ItemStack> complex, @NotNull PersistentDataAdapterContext context) {
            return Lists.transform(complex, s -> innerType.toPrimitive(s, context));
        }

        @NotNull
        @Override
        public List<ItemStack> fromPrimitive(@NotNull List<PersistentDataContainer> primitive, @NotNull PersistentDataAdapterContext context) {
            return Lists.transform(primitive, s -> innerType.fromPrimitive(s, context));
        }

        @Override
        public @NotNull PersistentDataType<PersistentDataContainer, ItemStack> elementType() {
            return this.innerType;
        }
    };

    private DataUtil() {
    }

    public static <P, V> void set(PersistentDataContainer container, NamespacedKey key, V value, PersistentDataType<P, V> type) {
        container.set(key, type, value);
    }

    @NotNull
    public static <P, V> V get(PersistentDataContainer container, NamespacedKey key, PersistentDataType<P, V> type) {
        if (container.has(key, type)) return Objects.requireNonNull(container.get(key, type), MESSAGE_VALUE_NOT_FOUND);
        throw new NullPointerException(MESSAGE_VALUE_NOT_FOUND);
    }


    @Subst("")
    @NotNull
    public static <P, V> V getOrDefault(PersistentDataContainer container, NamespacedKey key, V defaultValue, PersistentDataType<P, V> type) {
        if (container.has(key, type)) return DataUtil.get(container, key, type);
        set(container, key, defaultValue, type);
        return defaultValue;
    }

    @NotNull
    public static PersistentDataContainer getSubContainer(Entity entity, NamespacedKey key) {
        return getSubContainer(entity.getPersistentDataContainer(), key);
    }

    @NotNull
    public static PersistentDataContainer getSubContainer(PersistentDataContainer container, NamespacedKey key) {
        return getOrDefault(container, key, container.getAdapterContext().newPersistentDataContainer(), PersistentDataType.TAG_CONTAINER);
    }

    @NotNull
    public static List<PersistentDataContainer> getSubArrayContainer(Entity entity, NamespacedKey key) {
        return getSubArrayContainer(entity.getPersistentDataContainer(), key);
    }

    public static <P, V> void set(Entity entity, NamespacedKey key, V value, PersistentDataType<P, V> type) {
        set(entity.getPersistentDataContainer(), key, value, type);
    }

    public static <P, V> V get(Entity entity, NamespacedKey key, PersistentDataType<P, V> type) {
        return get(entity.getPersistentDataContainer(), key, type);
    }

    @NotNull
    public static <P, V> V getOrDefault(Entity entity, NamespacedKey key, V defaultValue, PersistentDataType<P, V> type) {
        return getOrDefault(entity.getPersistentDataContainer(), key, defaultValue, type);
    }

    @NotNull
    public static List<PersistentDataContainer> getSubArrayContainer(PersistentDataContainer container, NamespacedKey key) {
        return getOrDefault(container, key, new ArrayList<>(), PersistentDataType.LIST.dataContainers());
    }
}
