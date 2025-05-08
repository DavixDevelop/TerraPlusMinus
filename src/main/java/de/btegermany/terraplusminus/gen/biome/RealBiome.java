package de.btegermany.terraplusminus.gen.biome;

import de.btegermany.terraplusminus.gen.RealBiomesRegistry;
import net.buildtheearth.terraminusminus.generator.BiomesRegistry;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import org.bukkit.block.Biome;

public class RealBiome implements IBiome<Biome> {
    private final String biomeKey;
    private final Biome biome;

    public RealBiome(String biomeKey, Biome biome){
        this.biomeKey = biomeKey;
        this.biome = biome;
    }

    @Override
    public int getNumericId() {
        return RealBiomesRegistry.BIOME_KEYS.get(biomeKey);
    }

    @Override
    public String getId() {
        return biomeKey;
    }

    @Override
    public Biome getBiome() {
        return biome;
    }

    /**
     * Return the biome handler from the Biomes registry
     * @param biome And instance of a Biome
     * @return The biome handler stored in the BiomesRegistry
     */
    public static IBiome<?> fromRegistry(Biome biome){
        String key = biome.getKey().toString();

        //If Biome was not yet registered in BiomesRegistry, register it
        if(!BiomesRegistry.REGISTRY.containsKey(key)){
            BiomesRegistry.registerBiome(new RealBiome(key, biome));
            RealBiomesRegistry.BIOME_KEYS.put(key, RealBiomesRegistry.BIOME_KEYS.size());
        }

        return BiomesRegistry.getById(key);
    }
}
