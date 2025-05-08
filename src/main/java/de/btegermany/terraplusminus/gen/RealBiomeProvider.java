package de.btegermany.terraplusminus.gen;

import com.google.common.cache.LoadingCache;
import lombok.Getter;
import net.buildtheearth.terraminusminus.generator.BiomesRegistry;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class RealBiomeProvider extends BiomeProvider {
    private final Map<ChunkPos, Data> cache = new ConcurrentHashMap<>();

    @Override
    public @NotNull Biome getBiome(@NotNull WorldInfo worldInfo, int x, int y, int z) {
        final World world = Bukkit.getWorld(worldInfo.getUID());

        if(world.getGenerator() instanceof RealWorldGenerator){
            final RealWorldGenerator generator = ((RealWorldGenerator) world.getGenerator());

            //Calculate the chunk position from the block position
            final ChunkPos dataChunkPos = generator.getDataChunkPosFromBlock(x, z);

            final Data data = cache.computeIfAbsent(dataChunkPos, pos -> {
                try {
                    final CachedChunkData chunkData = generator.getTerraChunkData(pos.x(), pos.z());
                    return new Data(getBiomesData(chunkData));
                }catch (Exception ex){
                    Bukkit.getLogger().log(Level.SEVERE,"[T+-] " + ex.getMessage());
                }

                return null;
            });

            if(data != null){
                final IBiome biome = data.getBiome((x - generator.getXOffset()) & 15, (z - generator.getZOffset()) & 15);

                if(data.queryCount == 255){
                    cache.remove(dataChunkPos);
                }

                //Ensure that IBiome::getBiome is not null and is of type org.bukkit.block.Biome
                if(biome.getBiome() != null && biome.getBiome() instanceof org.bukkit.block.Biome)
                    return (Biome) biome.getBiome();
            }
        }

        return Biome.PLAINS;
    }

    @Override
    public @NotNull List<Biome> getBiomes(@NotNull WorldInfo worldInfo) {
        List<Biome> biomes = new ArrayList<>();

        BiomesRegistry.REGISTRY.forEach(((s, biome) -> {
            //Ensure that IBiome::getBiome is not null and is of type org.bukkit.block.Biome,
            //and add it to the list of possible biomes that RealBiomeProvider may use
            if(biome.getBiome() != null && biome.getBiome() instanceof Biome b)
                biomes.add(b);
        }));

        return biomes;
    }

    /**
     * Compute the IBiome array from CachedChunkData.biomes, which store the numerical ids of the IBiomes
     *
     * @param data The cached chunk data for given chunk
     * @return Computed chunk IBiome array
     */
    public static IBiome[] getBiomesData(CachedChunkData data){
        IBiome[] biomes = new IBiome[16 * 16];

        for(int x = 0; x < 16; x++){
            for(int z = 0; z < 16; z++){
                biomes[x + z * 16] = RealBiomesRegistry.getBiomeByNumericalId(data.biome(x, z));
            }
        }

        return biomes;
    }

    public static class Data {
        private IBiome[] biomes;
        public int queryCount = -1;

        /**
         * Get the biome for the position within the chunk, and increment query count by one
         * @param x Local x position within chunk [0-15]
         * @param z Local z position within chunk [0-15]
         * @return The IBiome at the
         */
        public IBiome getBiome(int x, int z) {
            queryCount++;
            return biomes[x + z + 16];
        }

        public Data(IBiome[] biomes){
            this.biomes = biomes;
        }
    }
}
