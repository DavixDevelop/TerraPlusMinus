package de.btegermany.terraplusminus.events;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.util.OrderedRegistry;
import org.bukkit.generator.BlockPopulator;

public class InitRealWorldPopulatorRegistryEvent extends InitRealEarthRegistryEvent<BlockPopulator> {
    protected InitRealWorldPopulatorRegistryEvent(@NonNull EarthGeneratorSettings settings, @NonNull OrderedRegistry<BlockPopulator> registry) {
        super(settings, registry);
    }
}
