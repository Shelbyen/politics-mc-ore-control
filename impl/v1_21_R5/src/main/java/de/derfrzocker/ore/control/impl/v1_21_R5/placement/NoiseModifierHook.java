package de.derfrzocker.ore.control.impl.v1_21_R5.placement;

import de.derfrzocker.feature.api.FeaturePlacementModifier;
import de.derfrzocker.feature.api.PlacementModifierConfiguration;
import de.derfrzocker.feature.common.feature.placement.NoiseModifier;
import de.derfrzocker.feature.common.feature.placement.configuration.NoiseConfiguration;
import de.derfrzocker.ore.control.api.Biome;
import de.derfrzocker.ore.control.api.OreControlManager;
import de.derfrzocker.ore.control.api.PlacementModifierHook;
import de.derfrzocker.ore.control.api.config.Config;
import de.derfrzocker.ore.control.api.config.ConfigManager;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_21_R5.generator.CraftLimitedRegion;
import org.bukkit.craftbukkit.v1_21_R5.util.RandomSourceWrapper;
import org.bukkit.util.BlockVector;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class NoiseModifierHook extends PlacementModifier implements PlacementModifierHook<NoiseConfiguration> {

    private final Map<String, PlacementModifierConfiguration> cache = new ConcurrentHashMap<>();
    private final NoiseModifier noiseModifier;
    private final ConfigManager configManager;
    private final Biome biome;
    private final NamespacedKey namespacedKey;

    public NoiseModifierHook(@NotNull OreControlManager oreControlManager, @NotNull Biome biome, @NotNull NamespacedKey namespacedKey) {
        this.configManager = oreControlManager.getConfigManager();
        this.noiseModifier = (NoiseModifier) oreControlManager.getRegistries().getPlacementModifierRegistry().get(NoiseModifier.KEY).get();
        this.biome = biome;
        this.namespacedKey = namespacedKey;

        oreControlManager.addValueChangeListener(cache::clear);
    }

    @Override
    public FeaturePlacementModifier<NoiseConfiguration> getPlacementModifier() { return noiseModifier; }

    @Override
    public Biome getBiome() { return biome; }

    @Override
    public @NotNull NamespacedKey getKey() { return noiseModifier.getKey(); }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos blockPos) {
        PlacementModifierConfiguration configuration = cache.computeIfAbsent(context.getLevel().getMinecraftWorld().getWorld().getName(), this::loadConfig);
        if (configuration == null) {
            return Stream.of(blockPos);
        }

        CraftLimitedRegion limitedRegion = new CraftLimitedRegion(context.getLevel(), new ChunkPos(blockPos));
        Stream<BlockVector> pos = noiseModifier.getPositions(context.getLevel().getMinecraftWorld().getWorld(), new RandomSourceWrapper.RandomWrapper(random), new BlockVector(blockPos.getX(), blockPos.getY(), blockPos.getZ()), limitedRegion, (NoiseConfiguration) configuration);

        limitedRegion.breakLink();

        return pos.map(blockVector -> new BlockPos(blockVector.getBlockX(), blockVector.getBlockY(), blockVector.getBlockZ()));
    }

    private PlacementModifierConfiguration loadConfig(String name) {
        Config config = configManager.getGenerationConfig(configManager.getOrCreateConfigInfo(name), biome, namespacedKey).orElse(null);

        if (config == null) {
            return null;
        }

        if (config.getPlacements() == null) {
            return null;
        }

        return config.getPlacements().get(noiseModifier);
    }

    @Override
    public PlacementModifierType<?> type() {
        return null;
    }
}
