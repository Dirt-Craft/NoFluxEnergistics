package net.dirtcraft.plugin.nofluxenergistics.data;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

public class RestrictionManager {
    private final Map<TypeKey<?>, Map<TypeKey<?>, Blacklist>> blacklistedConnections = new HashMap<>();
    private final TypeKey.BlockTypeKey blockTypeKey = new TypeKey.BlockTypeKey(null);
    private final TypeKey.ModIdKey modIdKey = new TypeKey.ModIdKey(null);

    public boolean isAllowed(Player player, Location<World> blockPos){
        String primary;
        BlockType type = blockPos.getBlockType();
        Map<TypeKey<?>, Blacklist> blacklist = blacklistedConnections.get(blockTypeKey.as(type));
        if (blacklist != null && !blacklist.isEmpty()) primary = blockTypeKey.toString();
        else {
            blacklist = blacklistedConnections.get(modIdKey.as(type));
            if (blacklist == null || blacklist.isEmpty()) return true;
            primary = modIdKey.toString();
        }
        if (!isAllowed(player, primary, blacklist, blockPos.add(+1, 0, 0))) return false;
        if (!isAllowed(player, primary, blacklist, blockPos.add(-1, 0, 0))) return false;
        if (!isAllowed(player, primary, blacklist, blockPos.add(0, +1, 0))) return false;
        if (!isAllowed(player, primary, blacklist, blockPos.add(0, -1, 0))) return false;
        if (!isAllowed(player, primary, blacklist, blockPos.add(0, 0, +1))) return false;
        return isAllowed(player, primary, blacklist, blockPos.add(0, 0, -1));
    }

    public void registerBlacklist(@NonNull String a, @NonNull String b, Blacklist blacklist) {
        TypeKey<?> aType;
        TypeKey<?> bType;
        if (a.contains(":")) {
            BlockType type = Sponge.getGame()
                    .getRegistry()
                    .getType(BlockType.class, a)
                    .orElse(null);
            if (type == null) return;
            else aType = new TypeKey.ImmutableBlockTypeKey(type);
        } else aType = new TypeKey.ImmutableModIdKey(a);
        if (b.contains(":")) {
            BlockType type = Sponge.getGame()
                    .getRegistry()
                    .getType(BlockType.class, b)
                    .orElse(null);
            if (type == null) return;
            else bType = new TypeKey.ImmutableBlockTypeKey(type);
        } else bType = new TypeKey.ImmutableModIdKey(b);
        registerBlacklist(aType, bType, blacklist);
    }

    public void clear() {
        Set<TypeKey<?>> keys = blacklistedConnections.keySet();
        for (TypeKey<?> key : keys) blacklistedConnections.remove(key);
        Blacklist.DEFAULTS.forEach(b->b.register(this));
    }

    private void registerBlacklist(@NonNull TypeKey<?> a, @NonNull TypeKey<?> b, Blacklist entry) {
        blacklistedConnections.computeIfAbsent(a, a2 -> new HashMap<>()).put(b, entry);
        blacklistedConnections.computeIfAbsent(b, b2 -> new HashMap<>()).put(a, entry);
    }

    private boolean isAllowed(Player player, String primary, Map<TypeKey<?>, Blacklist> blacklist, Location<World> blockPos){
        BlockType type = blockPos.getBlockType();
        String secondary;
        Blacklist entry = blacklist.get(blockTypeKey.as(type));
        if (entry != null) secondary = blockTypeKey.toString();
        else {
            entry = blacklist.get(modIdKey.as(type));
            if (entry == null) return true;
            secondary = modIdKey.toString();
        }
        player.sendMessage(format(entry.getWarning(primary, secondary)));
        return false;
    }

    private Text format(String message) {
        return TextSerializers.FORMATTING_CODE.deserialize(message);
    }
}
