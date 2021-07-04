package net.dirtcraft.plugin.nofluxenergistics;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.dirtcraft.plugin.nofluxenergistics.data.Blacklist;
import net.dirtcraft.plugin.nofluxenergistics.data.RestrictionManager;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
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
    @Inject
    @DefaultConfig(sharedRoot = false)
    private ConfigurationLoader<CommentedConfigurationNode> loader;
    private ConfigurationOptions options;
    private CommentedConfigurationNode node = null;
    private List<Blacklist> config;
    private RestrictionManager manager;

    @Listener
    public void onStartup(GameConstructionEvent event){
        this.manager = new RestrictionManager();
        options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        this.load();
        this.save();
    }

    @Listener
    public void onStartup(GamePostInitializationEvent event){
        Blacklist.DEFAULTS.forEach(b->b.register(manager));
        config.forEach(b->b.register(manager));
    }

    @Listener
    public void onBlockPlace(ChangeBlockEvent.Place event, @Root Object cause) {
        if (!(cause instanceof Player)) return;
        Player player = (Player) cause;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            BlockSnapshot blockSnapshot = transaction.getFinal();
            Location<World> loc = blockSnapshot.getLocation().orElse(null);
            if (loc == null) return;
            event.setCancelled(!manager.isAllowed(player, loc));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void load() {
        try {
            node = loader.load(options);
            config = node.getValue(new TypeToken<List<Blacklist>>(){}, config);
            loader.save(node);
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public void save(){
        if (node == null) return;
        try {
            loader.save(node.setValue(new TypeToken<List<Blacklist>>(){}, config));
        } catch (IOException | ObjectMappingException exception) {
            exception.printStackTrace();
        }
    }
}
