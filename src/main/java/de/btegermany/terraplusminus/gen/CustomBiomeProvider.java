package de.btegermany.terraplusminus.gen;

import de.btegermany.terraplusminus.Terraplusminus;
import de.btegermany.terraplusminus.data.KoppenClimateData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.projection.GeographicProjection;
import net.buildtheearth.terraminusminus.projection.OutOfProjectionBoundsException;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.substitutes.TerraBukkit;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class CustomBiomeProvider extends BiomeProvider {
    private final Map<ChunkPos, Data> cache = new ConcurrentHashMap<>();

    private final List<Biome> biomeList = new ArrayList<>();

    public CustomBiomeProvider() {
        //Populate the biomeList from the Paper Biome Registry,
        //as well as pre-cache the T-- Biome cache via TerraBukkit
        final Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);

        for(Biome biome : biomeRegistry) {
            net.buildtheearth.terraminusminus.substitutes.Biome _biome = TerraBukkit.fromBukkitBiome(biome);
            biomeList.add(TerraBukkit.toBukkitBiome(_biome));
        }
    }

    private RealWorldGenerator generator;

    @NotNull
    @Override
    public Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        if (Terraplusminus.config.getBoolean("biomes.use_dataset")) {

            if(generator == null) {
                final World world = Bukkit.getWorld(worldInfo.getUID());

                if(world != null && world.getGenerator() instanceof RealWorldGenerator) {
                    generator = ((RealWorldGenerator) world.getGenerator());

                    return getBiomeFromTerraPipeline(x, z);
                }
            } else {
                return getBiomeFromTerraPipeline(x, z);
            }

            /*double[] coords;
            try {
                coords = this.projection.toGeo(x, z);
            } catch (OutOfProjectionBoundsException ignored) {
                return Biome.PLAINS;
            }
            try {
                biomeData = this.climateData.getAsync(coords[0], coords[1]).get();
                return koppenDataToBukkitBiome(biomeData);
            } catch (InterruptedException | ExecutionException | OutOfProjectionBoundsException e) {
                e.printStackTrace();
            }*/
        }
        return parseDefaultBiome();
    }

    @NotNull
    @Override
    public List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        return biomeList;
    }

    /**
     * Get the Biome for the location from the Terra pipeline, ex the Koppen biome filter, legacy terra filter, etc..
     * @param x The X position of the block
     * @param z The Z position of the block
     * @return The Biome at the position or the default biome
     */
    private Biome getBiomeFromTerraPipeline(int x, int z) {
        //Calculate the chunk position from the block position
        final ChunkPos dataChunkPos = generator.getDataChunkPosFromBlock(x, z);

        final Data data = cache.computeIfAbsent(dataChunkPos, pos -> {
           try {
               final CachedChunkData chunkData = generator.getTerraChunkData(pos.x(), pos.z());
               return new Data(getBiomesData(chunkData));
           } catch (Exception ex) {
               Bukkit.getLogger().log(Level.SEVERE,"[T+-] " + ex.getMessage());
           }

           return null;
        });

        if(data != null) {
            final Biome biome = data.getBiome(x, z);

            //Remove the temp chunk data once the biome provider processes the chunk
            if(data.queryCount == 255) {
                cache.remove(dataChunkPos);
            }

            return biome;
        }

        return parseDefaultBiome();
    }

    public static Biome koppenDataToBukkitBiome(double koppenData) {
        switch ((int) koppenData) {
            case 0 -> {
                return Biome.OCEAN;
            }
            case 1, 12 -> {
                return Biome.JUNGLE;
            }
            case 2 -> {
                return Biome.BAMBOO_JUNGLE;
            }
            case 3, 11 -> {
                return Biome.SPARSE_JUNGLE;
            }
            case 4, 7, 5 -> {
                return Biome.DESERT;
            }
            case 6 -> {
                return Biome.SAVANNA;
            }
            case 8 -> {
                return Biome.PLAINS;
            }
            case 9 -> {
                return Biome.SUNFLOWER_PLAINS;
            }
            case 10 -> {
                return Biome.BEACH;
            }
            case 13 -> {
                return Biome.WINDSWEPT_GRAVELLY_HILLS;
            }
            case 14, 15 -> {
                return Biome.FLOWER_FOREST;
            }
            case 16 -> {
                return Biome.WINDSWEPT_HILLS;
            }
            case 17 -> {
                return Biome.SAVANNA_PLATEAU;
            }
            case 18 -> {
                return Biome.WOODED_BADLANDS;
            }
            case 19 -> {
                return Biome.SNOWY_TAIGA;
            }
            case 20 -> {
                return Biome.OLD_GROWTH_PINE_TAIGA;
            }
            case 21, 22 -> {
                return Biome.SWAMP;
            }
            case 23, 24 -> {
                return Biome.OLD_GROWTH_SPRUCE_TAIGA;
            }
            case 25 -> {
                return Biome.FOREST;
            }
            case 26 -> {
                return Biome.DARK_FOREST;
            }
            case 27 -> {
                return Biome.TAIGA;
            }
            case 28 -> {
                return Biome.SNOWY_SLOPES;
            }
            case 29 -> {
                return Biome.SNOWY_PLAINS;
            }
            case 30 -> {
                return Biome.ICE_SPIKES;
            }
            default -> {
                return Biome.PLAINS;
            }
        }
    }

    /**
     * Compute the Biome array from CachedChunkData.biomes, where each biome stores an Identity (namespace:key) of the biome
     *
     * @param data The cached chunk data for given chunk
     * @return Computed chunk Biome array
     */
    public static Biome[] getBiomesData(CachedChunkData data) {
        Biome[] biomes = new Biome[16 * 16];

        for(int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                biomes[x + z * 16] = TerraBukkit.toBukkitBiome(data.biome(x, z));
            }
        }

        return biomes;
    }

    public static Biome parseDefaultBiome() {
        final String FALLBACK_BIOME = "minecraft:plains";

        String biomeName = Terraplusminus.config.getString("biomes.biome");
        if (biomeName == null || biomeName.isBlank()) {
            biomeName = FALLBACK_BIOME;
        } else {
            biomeName = biomeName.toLowerCase();
            if (!biomeName.contains(":")) {
                biomeName = "minecraft:" + biomeName;
            }
        }

        var biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        return biomeRegistry.get(Key.key(biomeName));
    }

    public static class Data {
        private Biome[] biomes;
        public int queryCount = -1;

        /**
         * Get the biome for the position within the chunk, and increment query count by one
         * @param x Local x position within chunk [0-15]
         * @param z Local z position within chunk [0-15]
         * @return The IBiome at the
         */
        public Biome getBiome(int x, int z) {
            queryCount++;
            return biomes[x + z + 16];
        }

        public Data(Biome[] biomes){
            this.biomes = biomes;
        }
    }
}
