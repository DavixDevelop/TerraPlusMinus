package de.btegermany.terraplusminus.gen.populate;

import de.btegermany.terraplusminus.gen.RealWorldGenerator;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RealWorldPopulator extends BlockPopulator {
    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull LimitedRegion limitedRegion) {
        final World world = Bukkit.getWorld(worldInfo.getUID());

        if(!(world.getGenerator() instanceof RealWorldGenerator))
            return;

        final RealWorldGenerator generator = ((RealWorldGenerator) world.getGenerator());
        final ChunkPos dataChunkPos = generator.getDataChunkPosFromChunk(chunkX, chunkZ);

        generator.getTerraChunkDataAsync(dataChunkPos.x(), dataChunkPos.z())
                .thenAccept(data -> {
                    if(data != null){
                        populate(worldInfo, random, chunkX, chunkZ, generator.getXOffset(), generator.getZOffset(), generator.getYOffset(), limitedRegion, data);
                    }
                });
    }

    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, @NotNull int x, @NotNull int z, @NotNull int xOffset, @NotNull int zOffset, @NotNull int yOffset, @NotNull LimitedRegion limitedRegion, @NotNull CachedChunkData chunkData) {}
}
