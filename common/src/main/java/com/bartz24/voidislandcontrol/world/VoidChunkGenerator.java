package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.logic.SpawnHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import com.bartz24.voidislandcontrol.config.VicConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Empty chunks. Structure steps still run when {@code structures} is true, which is how
 * void nether / end keep fortresses and end cities without terrain.
 */
public class VoidChunkGenerator extends ChunkGenerator {
    public static final ResourceLocation ID = new ResourceLocation("voidislandcontrol", "void");

    public static final Codec<VoidChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter(ChunkGenerator::getBiomeSource),
                    Codec.BOOL.optionalFieldOf("structures", false).forGetter(VoidChunkGenerator::structures)
            ).apply(instance, instance.stable(VoidChunkGenerator::new)));

    private final boolean structures;

    public VoidChunkGenerator(BiomeSource biomeSource, boolean structures) {
        super(biomeSource);
        this.structures = structures;
    }

    public boolean structures() {
        return structures;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(WorldGenRegion level, long seed, RandomState random, BiomeManager biomeManager,
                             StructureManager structureManager, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion level, StructureManager structureManager, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structureManager, ChunkAccess chunk) {
        int surface = VicConfig.islandSettings.islandYLevel - 3;
        int half = Math.max(1, VicConfig.islandSettings.islandSize / 2);
        int wx = chunk.getPos().getMinBlockX();
        int wz = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int bx = wx + x;
                int bz = wz + z;
                if (Math.abs(bx) > half || Math.abs(bz) > half) continue;
                chunk.setBlockState(pos.set(bx, surface, bz), Blocks.GRASS_BLOCK.defaultBlockState(), false);
                chunk.setBlockState(pos.set(bx, surface - 1, bz), Blocks.BEDROCK.defaultBlockState(), false);
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor level, RandomState random) {
        return level.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState random) {
        return new NoiseColumn(level.getMinBuildHeight(), new BlockState[0]);
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState random, BlockPos pos) {
        info.add("Void Island Control");
    }

    @Override
    public void applyBiomeDecoration(WorldGenLevel level, ChunkAccess chunk, StructureManager structureManager) {
        if (placeStructures(level)) super.applyBiomeDecoration(level, chunk, structureManager);
    }

    private boolean placeStructures(WorldGenLevel level) {
        if (!(level instanceof WorldGenRegion region)) return structures;
        var key = region.getLevel().dimension();
        if (Level.NETHER.equals(key)) return VicConfig.worldGenSettings.netherVoidStructures;
        if (Level.END.equals(key)) return VicConfig.worldGenSettings.endVoidStructures;
        return structures;
    }
}
