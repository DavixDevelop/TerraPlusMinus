package de.btegermany.terraplusminus.gen.populate.tree;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import de.btegermany.terraplusminus.Terraplusminus;
import de.btegermany.terraplusminus.gen.CustomBiomeProvider;
import de.btegermany.terraplusminus.gen.RealWorldGenerator;
import de.btegermany.terraplusminus.gen.populate.RealWorldPopulator;
import net.buildtheearth.terraminusminus.generator.CachedChunkData;
import net.buildtheearth.terraminusminus.generator.EarthGeneratorPipelines;
import net.buildtheearth.terraminusminus.generator.data.TreeCoverBaker;
import net.buildtheearth.terraminusminus.substitutes.BlockState;
import net.daporkchop.lib.common.reference.ReferenceStrength;
import net.daporkchop.lib.common.reference.cache.Cached;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

public class TreePopulator extends RealWorldPopulator {
    public static final Cached<byte[]> RNG_CACHE = Cached.threadLocal(() -> new byte[16 * 16], ReferenceStrength.SOFT);
    boolean generateTrees; // Should Trees be added to the Terrain
    boolean useBiomeFromDataset;
    String surface;

    // List of Possible trees by type
    HashMap<String, ArrayList<ArrayList<TreeBlock>>> trees = new HashMap<>();

    public TreePopulator() {
        this.generateTrees = Terraplusminus.config.getBoolean("generate_trees");
        this.useBiomeFromDataset = Terraplusminus.config.getBoolean("biomes.use_dataset");
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
        if(!generateTrees)
            return;



        final World world = Bukkit.getWorld(worldInfo.getName());

        try {

            byte[] treeCover = data.getCustom(EarthGeneratorPipelines.KEY_DATA_TREE_COVER, TreeCoverBaker.FALLBACK_TREE_DENSITY);
            byte[] rng = RNG_CACHE.get();

            Biome[] biomes = CustomBiomeProvider.getBiomesData(data);

            for (int i = 0, dx = 0; dx < 16 >> 1; dx++) {
                for (int dz = 0; dz < 16 >> 1; dz++, i++) {
                    if ((rng[i] & 0xFF) < (treeCover[(((x * 16) & 0xF) << 4) | ((z * 16) & 0xF)] & 0xFF)) {
                        int valueX = random.nextInt(15) + 1; // Depending on the size of the tree this should be changed
                        int valueZ = random.nextInt(15) + 1;
                        int groundY = 0;
                        int waterY = 0;
                        BlockState state = data.surfaceBlock(0, 0);

                        try {
                            groundY = data.groundHeight(valueX, valueZ);
                            waterY = data.waterHeight(valueX, valueZ);
                            state = data.surfaceBlock(valueX, valueZ);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                        if (groundY < waterY) {
                            continue;
                        }


                        Biome biome = CustomBiomeProvider.parseDefaultBiome();

                        Location loc = new Location(world, valueX + x * 16, groundY + 1 + yOffset, valueZ + z * 16);
                        if (groundY + yOffset < world.getMaxHeight() - 35 && groundY + yOffset > world.getMinHeight() && state == null) {
                            if (useBiomeFromDataset)
                                biome = biomes[dx + dz * 16];

                            if (biome == Biome.DESERT || biome == Biome.SAVANNA || biome == Biome.SAVANNA_PLATEAU) // desert, savanna and savanna plateau
                                generateCustomTree(limitedRegion, loc, "savanna");
                            else if (biome == Biome.FLOWER_FOREST) // flower forest
                                generateCustomTree(limitedRegion, loc, "oak", "birch");
                            else if (biome == Biome.TAIGA) // taiga
                                generateCustomTree(limitedRegion, loc, "spruce");
                            else if (biome == Biome.SNOWY_SLOPES || biome == Biome.SNOWY_PLAINS || biome == Biome.ICE_SPIKES) {// snowy regions
                                // TODO: trees with snow
                            } else
                                generateCustomTree(limitedRegion, loc, "oak", "birch");
                        }
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateCustomTree(LimitedRegion limitedRegion, Location loc, String... types) {

        ArrayList<ArrayList<TreeBlock>> trees = new ArrayList<>();
        for (String type : types) {
            this.trees.get(type).forEach((tree) -> {
                trees.add(tree);
            });
        }

        // Random Tree
        if (trees.size() == 0) return;

        int randTree = (new Random()).nextInt(trees.size());
        if (randTree < 0) randTree = 0;
        if (randTree > trees.size() - 1) randTree = trees.size() - 1;
        ArrayList<TreeBlock> tree = trees.get(randTree);

        int originX = loc.getBlockX();
        int originY = loc.getBlockY();
        int originZ = loc.getBlockZ();


        // Rotate Tree Randomly
        Random rand = new Random();
        int angle = rand.nextInt(4) * 90;

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