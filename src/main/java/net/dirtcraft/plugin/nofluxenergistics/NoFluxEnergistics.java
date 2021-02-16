package net.dirtcraft.plugin.nofluxenergistics;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(
        id = "nofluxenergistics",
        name = "NoFluxEnergistics",
        description = "Simple plugin to not allow configured blocks to touch each other.",
        authors = "juliann"
)
public class NoFluxEnergistics {

    private static final String AE2 = "AppliedEnergistics2";
    private static final String FLUX_NETWORKS = "FluxNetworks";
    private static final String PROJECT_E = "ProjectE";
    private static final String YABBA = "Yabba";
    private static final String STORAGE_DRAWERS = "StorageDrawers";

    private static final String BONSAI_POT = "bonsaitrees:bonsaipot";
    private static final String ITEM_DUCTS = "thermaldynamics:duct_32";

    private static final String MINECRAFT_HOPPER = "minecraft:hopper";
    private static final String COBBLE_GEN = "tp:cobblegen_block";
    private static final String COBBLE_GEN_2 = "tp:iron_cobblegen_block";
    private static final String COBBLE_GEN_3 = "tp:diamond_cobblegen_block";
    private static final String COBBLE_GEN_4 = "tp:blaze_cobblegen_block";
    private static final String COBBLE_GEN_5 = "tp:emerald_cobblegen_block";

    private static final String BLACK_HOLE = "industrialforegoing:black_hole";

    @Inject
    private Logger logger;

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Object cause) {
        if (!(cause instanceof Player)) return;
        Player player = (Player) cause;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            BlockSnapshot blockSnapshot = transaction.getFinal();
            checkBlocks(blockSnapshot, event, player);
        }
    }

    public void checkBlocks(BlockSnapshot blockSnapshot, ChangeBlockEvent.Place event, MessageReceiver source) {
        if (isNextTo(blockSnapshot, AE2, FLUX_NETWORKS)) notifyPlayer(event, source, AE2, FLUX_NETWORKS);
        if (isNextTo(blockSnapshot, AE2, PROJECT_E)) notifyPlayer(event, source, AE2, PROJECT_E);
        if (isNextTo(blockSnapshot, AE2, YABBA)) notifyPlayer(event, source, AE2, YABBA);
        if (isNextTo(blockSnapshot, AE2, STORAGE_DRAWERS)) notifyPlayer(event, source, AE2, STORAGE_DRAWERS);

        if (isNextTo(blockSnapshot, AE2, BLACK_HOLE)) notifyPlayer(event, source,
                "&7You &ccannot &7place items from &6Applied Energistics 2 &7next to &6Black Hole Storage&7 items" +
                        "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!");
        if (isNextTo(blockSnapshot, BONSAI_POT, ITEM_DUCTS)) notifyPlayer(event, source,
                "&7You &ccannot &7place &6Bonsai Pots&7 next to &6ItemDucts" +
                        "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!");
        if (isNextTo(blockSnapshot, MINECRAFT_HOPPER, COBBLE_GEN, COBBLE_GEN_2, COBBLE_GEN_3, COBBLE_GEN_4, COBBLE_GEN_5)) notifyPlayer(event, source,
                "&7You &ccannot &7place &6Hoppers&7 next to &6Cobblestone Generators" +
                        "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!");
    }

    private boolean isNextTo(BlockSnapshot blockSnapshot, String mainBlock, String... itemIds) {
        final List<String> ids = Arrays.stream(itemIds).map(String::toLowerCase).collect(Collectors.toList());

        final String mainBlockId = mainBlock.toLowerCase();

        BlockState block = blockSnapshot.getExtendedState();
        String blockId = block.getId().toLowerCase();

        boolean match = false;
        for (String id : ids)
            if (blockId.startsWith(mainBlockId) || blockId.startsWith(id.toLowerCase())) {
                match = true;
                break;
            }

        if (!match) return false;

        Optional<Location<World>> optionalLocation = blockSnapshot.getLocation();
        if (!optionalLocation.isPresent()) {
            logger.error("Could not retrieve block location for block transaction: " + blockSnapshot.toContainer());
            return false;
        }
        Location<World> blockLocation = optionalLocation.get();

        final List<Location<World>> locations = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            if (i == 0) continue;
            locations.add(blockLocation.add(i, 0, 0));
            locations.add(blockLocation.add(0, i, 0));
            locations.add(blockLocation.add(0, 0, i));
        }

        for (Location<World> location : locations) {
            String locationId = location.getBlock().getId().toLowerCase().trim();
            for (String id : ids) if (
                    (blockId.startsWith(mainBlockId) && locationId.startsWith(id)) ||
                    (blockId.startsWith(id) && locationId.startsWith(mainBlockId))) return true;
        }

        return false;
    }

    private void notifyPlayer(ChangeBlockEvent.Place event, MessageReceiver source, String id1, String id2) {
        event.setCancelled(true);
        id1 = splitCamelCase(id1);
        id2 = splitCamelCase(id2);
        source.sendMessage(format("&7You &ccannot &7place items from &6" + id1 + " &7next to items from &6" + id2 + "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!"));
    }

    private void notifyPlayer(ChangeBlockEvent.Place event, MessageReceiver source, String message) {
        event.setCancelled(true);
        source.sendMessage(format(message));
    }

    public static Text format(String message) {
        return TextSerializers.FORMATTING_CODE.deserialize(message);
    }

    private static String splitCamelCase(String string) {
        return string.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }
}
