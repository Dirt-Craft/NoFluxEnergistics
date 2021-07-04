package net.dirtcraft.plugin.nofluxenergistics.data;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.plugin.PluginContainer;

public abstract class TypeKey<T> {
    protected T key;

    @Override
    public final int hashCode() {
        return key.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        return key.equals(other);
    }

    protected final String splitCamelCase(String string) {
        return string.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    static class ImmutableBlockTypeKey extends TypeKey<BlockType> {
        ImmutableBlockTypeKey(BlockType type) {
            this.key = type;
        }
    }

    static final class BlockTypeKey extends ImmutableBlockTypeKey {
        BlockTypeKey(BlockType type) {
            super(type);
        }

        BlockTypeKey as(BlockType type) {
            this.key = type;
            return this;
        }

        @Override
        public String toString() {
            return key.getTranslation().get();
        }
    }

    static class ImmutableModIdKey extends TypeKey<String> {
        ImmutableModIdKey(String modId) {
            this.key = modId;
        }
    }

    static final class ModIdKey extends ImmutableModIdKey {
        String name = null;
        ModIdKey(String modId) {
            super(modId);
        }

        ModIdKey as(BlockType type) {
            this.key = type.getId().split(":")[0];
            return this;
        }

        @Override
        public String toString() {
                String modName = Sponge.getGame().getPluginManager()
                        .getPlugin(key)
                        .map(PluginContainer::getName)
                        .orElse(key);
                return name = String.format("items from %s", modName);
        }
    }
}
