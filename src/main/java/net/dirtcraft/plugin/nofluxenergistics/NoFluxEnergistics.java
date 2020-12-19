package net.dirtcraft.plugin.nofluxenergistics;

import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
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

    private static final String BLACK_HOLE = "industrialforegoing:black_hole";

    @Inject
    private Logger logger;

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Object cause) {
        if (!(cause instanceof Player)) return;
        Player player = (Player) cause;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            if (isNextTo(transaction, AE2, FLUX_NETWORKS)) notifyPlayer(event, player, AE2, FLUX_NETWORKS);
            if (isNextTo(transaction, AE2, PROJECT_E)) notifyPlayer(event, player, AE2, PROJECT_E);
            if (isNextTo(transaction, AE2, YABBA)) notifyPlayer(event, player, AE2, YABBA);
            if (isNextTo(transaction, AE2, STORAGE_DRAWERS)) notifyPlayer(event, player, AE2, STORAGE_DRAWERS);

            if (isNextTo(transaction, AE2, BLACK_HOLE)) notifyPlayer(event, player,
                    "&7You &ccannot &7place items from &6Applied Energistics 2 &7next to &6Black Hole Storage&7 items" +
                            "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!\"");
        }
    }

    private boolean isNextTo(Transaction<BlockSnapshot> transaction, String... itemIds) {
        List<String> ids = Arrays.stream(itemIds).map(String::toLowerCase).collect(Collectors.toList());

        BlockState block = transaction.getFinal().getExtendedState();
        String blockId = block.getId().toLowerCase();

        boolean match = false;
        for (String id : ids)
            if (blockId.startsWith(id)) {
                match = true;
                break;
            }
        if (!match) return false;

        Optional<Location<World>> optionalLocation = transaction.getFinal().getLocation();
        if (!optionalLocation.isPresent()) {
            logger.error("Could not retrieve block location for block transaction: " + transaction.toContainer());
            return false;
        }
        Location<World> blockLocation = optionalLocation.get();

        List<Location<World>> locations = new ArrayList<>();
        for (int i = -1; i < 2; i++) {
            if (i == 0) continue;
            locations.add(blockLocation.add(i, 0, 0));
            locations.add(blockLocation.add(0, i, 0));
            locations.add(blockLocation.add(0, 0, i));
        }

        for (Location<World> location : locations) {
            String locationId = location.getBlock().getId().toLowerCase();
            for (String id : ids) if (!locationId.startsWith(blockId.split(":")[0]) && locationId.startsWith(id)) return true;
        }

        return false;
    }

    private void notifyPlayer(ChangeBlockEvent.Place event, Player player, String id1, String id2) {
        event.setCancelled(true);
        id1 = splitCamelCase(id1);
        id2 = splitCamelCase(id2);
        player.sendMessage(format("&7You &ccannot &7place items from &6" + id1 + " &7next to items from &6" + id2 + "\n&7This is to prevent severe lag and/or server crashes so please use an alternative!"));
    }

    private void notifyPlayer(ChangeBlockEvent.Place event, Player player, String message) {
        event.setCancelled(true);
        player.sendMessage(format(message));
    }

    private static Text format(String message) {
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
