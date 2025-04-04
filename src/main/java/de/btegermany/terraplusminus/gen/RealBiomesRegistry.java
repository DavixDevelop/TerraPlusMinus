package de.btegermany.terraplusminus.gen;

import de.btegermany.terraplusminus.gen.biome.RealBiome;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.buildtheearth.terraminusminus.generator.BiomesRegistry;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import java.util.Iterator;
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
        Iterator<Biome> it = biomeRegistry.iterator();
        //Register them to BiomesRegistry.REGISTRY
        while(it.hasNext()){
            final Biome biome = it.next();
            String key = biome.getKey().toString();
            BiomesRegistry.registerBiome(new RealBiome(key, biome));
            if(!BIOME_KEYS.containsKey(key))
                BIOME_KEYS.put(key, index);
            else
                BIOME_KEYS.replace(key, index);
            index++;
        }

        //Set default biome
        BiomesRegistry.DEFAULT_BIOME = RealBiome.getByBiome(Biome.OCEAN);
    }

    public static IBiome<?> getBiomeByNumericalId(int id){
        final Optional<Map.Entry<String, Integer>> biomeKey = BIOME_KEYS.entrySet().stream().filter(x -> x.getValue().equals(id)).findFirst();
        if(biomeKey.isPresent())
            return REGISTRY.getOrDefault(biomeKey.get().getKey(), DEFAULT_BIOME);

        return DEFAULT_BIOME;
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
                        return RealBiome.getByBiome(Biome.WINDSWEPT_HILLS);
                    case "minecraft:swampland":
                        return RealBiome.getByBiome(Biome.SWAMP);
                    case "minecraft:hell":
                        return RealBiome.getByBiome(Biome.NETHER_WASTES);
                    case "minecraft:sky":
                        return RealBiome.getByBiome(Biome.THE_END);
                    case "minecraft:ice_flats":
                        return RealBiome.getByBiome(Biome.SNOWY_PLAINS);
                    case "minecraft:ice_mountains":
                        return RealBiome.getByBiome(Biome.FROZEN_PEAKS);
                    case "minecraft:mushroom_island", "minecraft:mushroom_island_shore":
                        return RealBiome.getByBiome(Biome.MUSHROOM_FIELDS);
                    case "minecraft:beaches":
                        return RealBiome.getByBiome(Biome.BEACH);
                    case "minecraft:desert_hills", "minecraft:mutated_desert":
                        return RealBiome.getByBiome(Biome.DESERT);
                    case "minecraft:forest_hills":
                        return RealBiome.getByBiome(Biome.FOREST);
                    case "minecraft:taiga_hills", "minecraft:mutated_taiga":
                        return RealBiome.getByBiome(Biome.TAIGA);
                    case "minecraft:jungle_hills":
                        return RealBiome.getByBiome(Biome.JUNGLE);
                    case "minecraft:jungle_edge", "minecraft:mutated_jungle_edge":
                        return RealBiome.getByBiome(Biome.SPARSE_JUNGLE);
                    case "minecraft:stone_beach":
                        return RealBiome.getByBiome(Biome.STONY_SHORE);
                    case "minecraft:cold_beach":
                        return RealBiome.getByBiome(Biome.SNOWY_BEACH);
                    case "minecraft:birch_forest_hills":
                        return RealBiome.getByBiome(Biome.BIRCH_FOREST);
                    case "minecraft:roofed_forest", "minecraft:mutated_roofed_forest":
                        return RealBiome.getByBiome(Biome.DARK_FOREST);
                    case "minecraft:taiga_cold", "minecraft:mutated_taiga_cold":
                        return RealBiome.getByBiome(Biome.SNOWY_TAIGA);
                    case "minecraft:taiga_cold_hills":
                        return RealBiome.getByBiome(Biome.SNOWY_SLOPES);
                    case "minecraft:redwood_taiga", "minecraft:redwood_taiga_hills", "minecraft:mutated_redwood_taiga", "minecraft:mutated_redwood_taiga_hills":
                        return RealBiome.getByBiome(Biome.OLD_GROWTH_PINE_TAIGA);
                    case "minecraft:savanna_rock", "minecraft:mutated_savanna_rock":
                        return RealBiome.getByBiome(Biome.SAVANNA_PLATEAU);
                    case "minecraft:mesa", "minecraft:mesa_clear_rock", "minecraft:mutated_mesa_rock", "minecraft:mutated_mesa_clear_rock":
                        return RealBiome.getByBiome(Biome.BADLANDS);
                    case "minecraft:mesa_rock":
                        return RealBiome.getByBiome(Biome.WOODED_BADLANDS);
                    case "minecraft:void":
                        return RealBiome.getByBiome(Biome.THE_VOID);
                    case "minecraft:mutated_plains":
                        return RealBiome.getByBiome(Biome.SUNFLOWER_PLAINS);
                    case "minecraft:mutated_extreme_hills", "minecraft:mutated_extreme_hills_with_trees":
                        return RealBiome.getByBiome(Biome.WINDSWEPT_GRAVELLY_HILLS);
                    case "minecraft:mutated_forest":
                        return RealBiome.getByBiome(Biome.FLOWER_FOREST);
                    case "minecraft:mutated_swampland":
                        return RealBiome.getByBiome(Biome.MANGROVE_SWAMP);
                    case "minecraft:mutated_ice_flats":
                        return RealBiome.getByBiome(Biome.ICE_SPIKES);
                    case "minecraft:mutated_jungle":
                        return RealBiome.getByBiome(Biome.BAMBOO_JUNGLE);
                    case "minecraft:mutated_birch_forest", "minecraft:mutated_birch_forest_hills":
                        return RealBiome.getByBiome(Biome.OLD_GROWTH_BIRCH_FOREST);
                    case "minecraft:mutated_savanna":
                        return RealBiome.getByBiome(Biome.WINDSWEPT_SAVANNA);
                    case "minecraft:mutated_mesa":
                        return RealBiome.getByBiome(Biome.ERODED_BADLANDS);
                    default:
                        return getDefault();
                }
            });
        }

        return biome;
    }

}
