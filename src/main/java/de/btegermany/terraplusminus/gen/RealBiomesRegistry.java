package de.btegermany.terraplusminus.gen;

import de.btegermany.terraplusminus.gen.biome.RealBiome;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.buildtheearth.terraminusminus.generator.BiomesRegistry;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RealBiomesRegistry extends BiomesRegistry {
    public static Map<String, Integer> BIOME_KEYS = new ConcurrentHashMap<>();
    private static boolean initialized = false;

    public RealBiomesRegistry(){
        if(!initialized){
            init();
            initialized = true;
        }
    }

    /**
     * Initialize the biomes registry, by first populating it from the Paper's Biome registry.
     * and then overriding the legacy 1.12.2 biomes with equivalent/similar biomes from 1.13+
     */
    public void init(){
        //Get all registered biomes
        final Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);

        Integer index = 0;
        //Register them to BiomesRegistry.REGISTRY
        for (Biome biome : biomeRegistry) {
            String key = biome.getKey().toString();
            BiomesRegistry.registerBiome(new RealBiome(key, biome));
            BIOME_KEYS.put(key, index);
            index++;
        }

        //Override the legacy 1.12.2 biomes in the registry with equivalent/similar biomes from 1.13+
        overrideLegacyBiomes();
    }

    private void overrideLegacyBiomes() {
        overrideLegacy("minecraft:extreme_hills", Biome.WINDSWEPT_HILLS);
        overrideLegacy("minecraft:smaller_extreme_hills", Biome.WINDSWEPT_HILLS);
        overrideLegacy("minecraft:extreme_hills_with_trees", Biome.WINDSWEPT_HILLS);
        overrideLegacy("minecraft:swampland", Biome.SWAMP);
        overrideLegacy("minecraft:hell", Biome.NETHER_WASTES);
        overrideLegacy("minecraft:sky", Biome.THE_END);
        overrideLegacy("minecraft:ice_flats", Biome.SNOWY_PLAINS);
        overrideLegacy("minecraft:ice_mountains", Biome.FROZEN_PEAKS);
        overrideLegacy("minecraft:mushroom_island", Biome.MUSHROOM_FIELDS);
        overrideLegacy("minecraft:mushroom_island_shore", Biome.MUSHROOM_FIELDS);
        overrideLegacy("minecraft:beaches", Biome.BEACH);
        overrideLegacy("minecraft:desert_hills", Biome.DESERT);
        overrideLegacy("minecraft:mutated_desert", Biome.DESERT);
        overrideLegacy("minecraft:forest_hills", Biome.FOREST);
        overrideLegacy("minecraft:taiga_hills", Biome.TAIGA);
        overrideLegacy("minecraft:mutated_taiga", Biome.TAIGA);
        overrideLegacy("minecraft:jungle_hills", Biome.JUNGLE);
        overrideLegacy("minecraft:jungle_edge", Biome.SPARSE_JUNGLE);
        overrideLegacy("minecraft:mutated_jungle_edge", Biome.SPARSE_JUNGLE);
        overrideLegacy("minecraft:stone_beach", Biome.STONY_SHORE);
        overrideLegacy("minecraft:cold_beach", Biome.SNOWY_BEACH);
        overrideLegacy("minecraft:birch_forest_hills", Biome.BIRCH_FOREST);
        overrideLegacy("minecraft:roofed_forest", Biome.DARK_FOREST);
        overrideLegacy("minecraft:mutated_roofed_forest", Biome.DARK_FOREST);
        overrideLegacy("minecraft:taiga_cold", Biome.SNOWY_TAIGA);
        overrideLegacy("minecraft:mutated_taiga_cold", Biome.SNOWY_TAIGA);
        overrideLegacy("minecraft:taiga_cold_hills", Biome.SNOWY_SLOPES);
        overrideLegacy("minecraft:redwood_taiga", Biome.OLD_GROWTH_PINE_TAIGA);
        overrideLegacy("minecraft:redwood_taiga_hills", Biome.OLD_GROWTH_PINE_TAIGA);
        overrideLegacy("minecraft:mutated_redwood_taiga", Biome.OLD_GROWTH_PINE_TAIGA);
        overrideLegacy("minecraft:mutated_redwood_taiga_hills", Biome.OLD_GROWTH_PINE_TAIGA);
        overrideLegacy("minecraft:savanna_rock", Biome.SAVANNA_PLATEAU);
        overrideLegacy("minecraft:mutated_savanna_rock", Biome.SAVANNA_PLATEAU);
        overrideLegacy("minecraft:mesa", Biome.BADLANDS);
        overrideLegacy("minecraft:mesa_clear_rock", Biome.BADLANDS);
        overrideLegacy("minecraft:mutated_mesa_rock", Biome.BADLANDS);
        overrideLegacy("minecraft:mutated_mesa_clear_rock", Biome.BADLANDS);
        overrideLegacy("minecraft:mesa_rock", Biome.WOODED_BADLANDS);
        overrideLegacy("minecraft:void", Biome.THE_VOID);
        overrideLegacy("minecraft:mutated_plains", Biome.SUNFLOWER_PLAINS);
        overrideLegacy("minecraft:mutated_extreme_hills", Biome.WINDSWEPT_GRAVELLY_HILLS);
        overrideLegacy("minecraft:mutated_extreme_hills_with_trees", Biome.WINDSWEPT_GRAVELLY_HILLS);
        overrideLegacy("minecraft:mutated_forest", Biome.FLOWER_FOREST);
        overrideLegacy("minecraft:mutated_swampland", Biome.MANGROVE_SWAMP);
        overrideLegacy("minecraft:mutated_ice_flats", Biome.ICE_SPIKES);
        overrideLegacy("minecraft:mutated_jungle", Biome.BAMBOO_JUNGLE);
        overrideLegacy("minecraft:mutated_birch_forest", Biome.OLD_GROWTH_BIRCH_FOREST);
        overrideLegacy("minecraft:mutated_birch_forest_hills", Biome.OLD_GROWTH_BIRCH_FOREST);
        overrideLegacy("minecraft:mutated_savanna", Biome.WINDSWEPT_SAVANNA);
        overrideLegacy("minecraft:mutated_mesa", Biome.ERODED_BADLANDS);
    }

    private void overrideLegacy(String biomeID, Biome biome) {
        REGISTRY.put(biomeID, RealBiome.fromRegistry(biome));
    }

    /**
     * Get the corresponding RealBiome instance from the registry via It's numerical ID
     * @param id The numerical ID of the IBiome
     * @return The corresponding RealBiome instance
     */
    public static IBiome<?> getBiomeByNumericalId(int id){
        final Optional<Map.Entry<String, Integer>> biomeKey = BIOME_KEYS.entrySet().stream().filter(x -> x.getValue().equals(id)).findFirst();
        if(biomeKey.isPresent())
            return REGISTRY.getOrDefault(biomeKey.get().getKey(), getDefault());

        return getDefault();
    }

    /**
     * Maps the given IBiome to a registered RealBiome instance.
     * <p>
     * If the biome is null or its internal biome is null, the default biome is returned.
     * If the biome is already an instance of RealBiome, it gets returned as it is.
     * Otherwise, this method ensures that every vanilla or custom biome
     * has a corresponding RealBiome instance, even if it wasn't
     * explicitly registered during initialization in RealBiomesRegistry::init.
     * <p>
     * This handles cases where custom or Paper biomes may not have been
     * registered yet by the time the plugin accesses them, by lazy
     * registering them to maintain consistency and avoid null mappings.
     *
     * @param biome The biome to map
     * @return Registered RealBiome from the registry, or the default mappings
     */
    @Override
    public IBiome<?> map(IBiome biome){
        if(biome == null || biome.getBiome() == null)
            return getDefault();

        if(biome instanceof RealBiome)
            return biome;

        return REGISTRY.computeIfAbsent(biome.getId(), key -> {
          if(biome.getBiome() instanceof org.bukkit.block.Biome)
              return new RealBiome(key, (Biome) biome.getBiome());
          return getDefault();
        });
    }

}
