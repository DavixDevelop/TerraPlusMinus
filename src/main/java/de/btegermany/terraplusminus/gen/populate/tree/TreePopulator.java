package de.btegermany.terraplusminus.gen.populate.tree;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import de.btegermany.terraplusminus.Terraplusminus;
import de.btegermany.terraplusminus.gen.RealBiomeProvider;
import de.btegermany.terraplusminus.gen.RealBiomesRegistry;
import de.btegermany.terraplusminus.gen.RealWorldGenerator;
import de.btegermany.terraplusminus.gen.biome.RealBiome;
import de.btegermany.terraplusminus.gen.populate.RealWorldPopulator;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.ChunkDataLoader;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorSettings;
import net.buildtheearth.terraminusminus.generator.data.TreeCoverBaker;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.buildtheearth.terraminusminus.substitutes.ChunkPos;
import net.buildtheearth.terraminusminus.substitutes.IBiome;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class TreePopulator extends RealWorldPopulator {
    public static final Cached<byte[]> RNG_CACHE = Cached.threadLocal(() -> new byte[16 * 16], ReferenceStrength.SOFT);
    String surface;

    // List of Possible trees by type
    HashMap<String, ArrayList<ArrayList<TreeBlock>>> trees = new HashMap<>();

    public TreePopulator() {
        this.surface = Terraplusminus.config.getString("surface_material");

        // Load Trees from customTrees.json
        JsonObject treeTypes = getJSONObject();
        final int[] treeCount = {0};
        treeTypes.entrySet().forEach(treeSizes -> {

            trees.put(treeSizes.getKey(), new ArrayList<>());

            Bukkit.getLogger().log(Level.INFO, "[T+-] Loading Tree Type " + treeSizes.getKey());

            treeSizes.getValue().getAsJsonObject().entrySet().forEach(treeNames -> {

                treeNames.getValue().getAsJsonObject().entrySet().forEach(tree -> {

                    treeCount[0]++;
                    ArrayList<TreeBlock> treeBlocks = new ArrayList<>();

                    tree.getValue().getAsJsonObject().get("blocks").getAsJsonArray().forEach(treeBlockElement -> {

                        JsonObject treeBlock = treeBlockElement.getAsJsonObject();
                        treeBlocks.add(new TreeBlock(treeBlock.get("x").getAsInt(), treeBlock.get("y").getAsInt(), treeBlock.get("z").getAsInt(), Material.getMaterial(treeBlock.get("material").getAsString())));

                    });

                    trees.get(treeSizes.getKey()).add(treeBlocks);

                });

            });

        });
        Bukkit.getLogger().log(Level.INFO, "[T+-] Finished loading " + treeCount[0] + " custom trees");

    }

    @Override
    public void populate(@NotNull WorldInfo worldInfo, @NotNull Random random, @NotNull int x, @NotNull int z, @NotNull int xOffset, @NotNull int zOffset, @NotNull int yOffset, @NotNull LimitedRegion limitedRegion, @NotNull CachedChunkData data, @NotNull RealWorldGenerator worldGenerator) {
        if(!Terraplusminus.config.getBoolean("generate_trees"))
            return;

        final World world = Bukkit.getWorld(worldInfo.getName());

        byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);
        byte[] rng = RNG_CACHE.get();

        IBiome[] biomes = RealBiomeProvider.getBiomesData(data);

        random.nextBytes(rng);

        for (int i = 0, dx = 0; dx < 8; dx++) {
            for (int dz = 0; dz < 8; dz++, i++) {
                //Actual block position
                final int blockX = (x * 16) + dx * 2;
                final int blockZ = (z * 16) + dz * 2;

                //Position within chunk [0-15]
                final int localX = blockX & 0xF;
                final int localZ = blockZ & 0xF;

                if ((rng[i] & 0xFF) < (treeCover[(localX << 4) | localZ] & 0xFF)) {
                    int groundY = data.groundHeight(localX, localZ);
                    int waterY = data.waterHeight(localX, localZ);

                    if (groundY < waterY) {
                        continue;
                    }

                    BlockState state = data.surfaceBlock(localX, localZ);

                    Location loc = new Location(world, blockX, groundY + 1 + yOffset, blockZ);
                    if (groundY + yOffset < world.getMaxHeight() - 35 && groundY + yOffset > world.getMinHeight() && state == null) {
                        Biome biome = (Biome) biomes[dx + dz * 16].getBiome();



                        if(biome == Biome.DESERT || biome ==  Biome.SAVANNA || biome ==  Biome.SAVANNA_PLATEAU) // desert, savanna and savanna plateau
                            generateCustomTree(limitedRegion, loc, random, "savanna");
                        else if(biome == Biome.FLOWER_FOREST) // flower forest
                            generateCustomTree(limitedRegion, loc, random,"oak", "birch");
                        else if(biome == Biome.TAIGA) // taiga
                            generateCustomTree(limitedRegion, loc, random, "spruce");
                        else if(biome == Biome.SNOWY_SLOPES || biome == Biome.SNOWY_PLAINS || biome == Biome.ICE_SPIKES) {// snowy regions
                                // TODO: trees with snow
                        }
                        else
                            generateCustomTree(limitedRegion, loc, random, "oak", "birch");
                    }
                }
            }
        }
    }

    public void generateCustomTree(LimitedRegion limitedRegion, Location loc, Random random, String... types) {

        ArrayList<ArrayList<TreeBlock>> trees = new ArrayList<>();
        for (String type : types) {
            trees.addAll(this.trees.get(type));
        }

        // Random Tree
        if (trees.isEmpty()) return;

        int randTree = random.nextInt(trees.size());
        ArrayList<TreeBlock> tree = trees.get(randTree);

        int originX = loc.getBlockX();
        int originY = loc.getBlockY();
        int originZ = loc.getBlockZ();


        // Rotate Tree Randomly
        int angle = random.nextInt(4) * 90;

        // Place Tree
        for (TreeBlock block : tree) {
            int x = block.getX();
            int z = block.getZ();
            if (angle == 90) {
                int temp = x;
                x = -z;
                z = temp;
            } else if (angle == 180) {
                x = -x;
                z = -z;
            } else if (angle == 270) {
                int temp = x;
                x = z;
                z = -temp;
            }
            limitedRegion.setType(originX + x, originY + block.getY(), originZ + z, block.getMaterial());
        }
    }

    public JsonObject getJSONObject() {
        InputStream is = getClass().getClassLoader().getResourceAsStream("assets/terraplusminus/data/customTrees.json");

        JsonReader reader;
        reader = new JsonReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        JsonParser parser = new JsonParser();
        JsonElement jsonElement = parser.parse(reader);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.get("trees").getAsJsonObject();
    }

}