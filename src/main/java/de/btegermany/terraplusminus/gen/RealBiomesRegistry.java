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
    }

    public static IBiome<?> getBiomeByNumericalId(int id){
        final Optional<Map.Entry<String, Integer>> biomeKey = BIOME_KEYS.entrySet().stream().filter(x -> x.getValue().equals(id)).findFirst();
        if(biomeKey.isPresent())
            return REGISTRY.getOrDefault(biomeKey.get().getKey(), getDefault());

        return getDefault();
    }

    private final Map<String, IBiome<?>> KeyToIBiome = new ConcurrentHashMap<>();

    @Override
    public IBiome<?> map(IBiome biome){
        if(biome.getBiome() == null)
            return getDefault();

        if (!(biome.getBiome() instanceof org.bukkit.block.Biome)){
            return KeyToIBiome.computeIfAbsent(biome.getId(), key -> {
                switch (biome.getId()){
                    case "minecraft:extreme_hills", "minecraft:smaller_extreme_hills", "minecraft:extreme_hills_with_trees":
                        return RealBiome.fromRegistry(Biome.WINDSWEPT_HILLS);
                    case "minecraft:swampland":
                        return RealBiome.fromRegistry(Biome.SWAMP);
                    case "minecraft:hell":
                        return RealBiome.fromRegistry(Biome.NETHER_WASTES);
                    case "minecraft:sky":
                        return RealBiome.fromRegistry(Biome.THE_END);
                    case "minecraft:ice_flats":
                        return RealBiome.fromRegistry(Biome.SNOWY_PLAINS);
                    case "minecraft:ice_mountains":
                        return RealBiome.fromRegistry(Biome.FROZEN_PEAKS);
                    case "minecraft:mushroom_island", "minecraft:mushroom_island_shore":
                        return RealBiome.fromRegistry(Biome.MUSHROOM_FIELDS);
                    case "minecraft:beaches":
                        return RealBiome.fromRegistry(Biome.BEACH);
                    case "minecraft:desert_hills", "minecraft:mutated_desert":
                        return RealBiome.fromRegistry(Biome.DESERT);
                    case "minecraft:forest_hills":
                        return RealBiome.fromRegistry(Biome.FOREST);
                    case "minecraft:taiga_hills", "minecraft:mutated_taiga":
                        return RealBiome.fromRegistry(Biome.TAIGA);
                    case "minecraft:jungle_hills":
                        return RealBiome.fromRegistry(Biome.JUNGLE);
                    case "minecraft:jungle_edge", "minecraft:mutated_jungle_edge":
                        return RealBiome.fromRegistry(Biome.SPARSE_JUNGLE);
                    case "minecraft:stone_beach":
                        return RealBiome.fromRegistry(Biome.STONY_SHORE);
                    case "minecraft:cold_beach":
                        return RealBiome.fromRegistry(Biome.SNOWY_BEACH);
                    case "minecraft:birch_forest_hills":
                        return RealBiome.fromRegistry(Biome.BIRCH_FOREST);
                    case "minecraft:roofed_forest", "minecraft:mutated_roofed_forest":
                        return RealBiome.fromRegistry(Biome.DARK_FOREST);
                    case "minecraft:taiga_cold", "minecraft:mutated_taiga_cold":
                        return RealBiome.fromRegistry(Biome.SNOWY_TAIGA);
                    case "minecraft:taiga_cold_hills":
                        return RealBiome.fromRegistry(Biome.SNOWY_SLOPES);
                    case "minecraft:redwood_taiga", "minecraft:redwood_taiga_hills", "minecraft:mutated_redwood_taiga", "minecraft:mutated_redwood_taiga_hills":
                        return RealBiome.fromRegistry(Biome.OLD_GROWTH_PINE_TAIGA);
                    case "minecraft:savanna_rock", "minecraft:mutated_savanna_rock":
                        return RealBiome.fromRegistry(Biome.SAVANNA_PLATEAU);
                    case "minecraft:mesa", "minecraft:mesa_clear_rock", "minecraft:mutated_mesa_rock", "minecraft:mutated_mesa_clear_rock":
                        return RealBiome.fromRegistry(Biome.BADLANDS);
                    case "minecraft:mesa_rock":
                        return RealBiome.fromRegistry(Biome.WOODED_BADLANDS);
                    case "minecraft:void":
                        return RealBiome.fromRegistry(Biome.THE_VOID);
                    case "minecraft:mutated_plains":
                        return RealBiome.fromRegistry(Biome.SUNFLOWER_PLAINS);
                    case "minecraft:mutated_extreme_hills", "minecraft:mutated_extreme_hills_with_trees":
                        return RealBiome.fromRegistry(Biome.WINDSWEPT_GRAVELLY_HILLS);
                    case "minecraft:mutated_forest":
                        return RealBiome.fromRegistry(Biome.FLOWER_FOREST);
                    case "minecraft:mutated_swampland":
                        return RealBiome.fromRegistry(Biome.MANGROVE_SWAMP);
                    case "minecraft:mutated_ice_flats":
                        return RealBiome.fromRegistry(Biome.ICE_SPIKES);
                    case "minecraft:mutated_jungle":
                        return RealBiome.fromRegistry(Biome.BAMBOO_JUNGLE);
                    case "minecraft:mutated_birch_forest", "minecraft:mutated_birch_forest_hills":
                        return RealBiome.fromRegistry(Biome.OLD_GROWTH_BIRCH_FOREST);
                    case "minecraft:mutated_savanna":
                        return RealBiome.fromRegistry(Biome.WINDSWEPT_SAVANNA);
                    case "minecraft:mutated_mesa":
                        return RealBiome.fromRegistry(Biome.ERODED_BADLANDS);
                    default:
                        return getDefault();
                }
            });
        }

        return biome;
    }

}
