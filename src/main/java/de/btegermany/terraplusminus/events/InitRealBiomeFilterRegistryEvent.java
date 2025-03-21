package de.btegermany.terraplusminus.events;

import lombok.NonNull;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.generator.biome.IEarthBiomeFilter;
import net.buildtheearth.terraminusminus.util.OrderedRegistry;

public class InitRealBiomeFilterRegistryEvent extends InitRealEarthRegistryEvent<IEarthBiomeFilter> {
    protected InitRealBiomeFilterRegistryEvent(@NonNull EarthGeneratorSettings settings, @NonNull OrderedRegistry<IEarthBiomeFilter> registry) {
        super(settings, registry);
    }
}
