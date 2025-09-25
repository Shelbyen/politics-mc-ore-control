package de.derfrzocker.feature.common.feature.placement;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.derfrzocker.feature.api.*;
import de.derfrzocker.feature.api.util.Parser;
import de.derfrzocker.feature.common.feature.placement.configuration.NoiseConfiguration;
import de.derfrzocker.feature.common.value.bool.BooleanType;
import de.derfrzocker.feature.common.value.bool.BooleanValue;
import de.derfrzocker.feature.common.value.number.FloatType;
import de.derfrzocker.feature.common.value.number.FloatValue;
import de.derfrzocker.feature.common.value.number.IntegerType;
import de.derfrzocker.feature.common.value.number.IntegerValue;
import org.bukkit.NamespacedKey;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.util.BlockVector;
import org.bukkit.util.noise.SimplexNoiseGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NoiseModifier implements FeaturePlacementModifier<NoiseConfiguration> {
    public static final NamespacedKey KEY = NamespacedKey.fromString("feature:noise_gen");

    private final Parser<PlacementModifierConfiguration> parser;

    public NoiseModifier(Registries registries) {
        parser = new Parser<>() {
            @Override
            public JsonElement toJson(PlacementModifierConfiguration v) {
                NoiseConfiguration value = (NoiseConfiguration) v;
                JsonObject jsonObject = new JsonObject();

                if (value.getActivate() != null) {
                    JsonObject entry = value.getActivate().getValueType().getParser().toJson(value.getActivate()).getAsJsonObject();
                    entry.addProperty("noise_activate_type", value.getActivate().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }
                if (value.getMaxCount() != null) {
                    JsonObject entry = value.getMaxCount().getValueType().getParser().toJson(value.getMaxCount()).getAsJsonObject();
                    entry.addProperty("max-count", value.getMaxCount().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }
                if (value.getSeed() != null) {
                    JsonObject entry = value.getSeed().getValueType().getParser().toJson(value.getSeed()).getAsJsonObject();
                    entry.addProperty("seed_type", value.getSeed().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }
                if (value.getOctaves() != null) {
                    JsonObject entry = value.getOctaves().getValueType().getParser().toJson(value.getOctaves()).getAsJsonObject();
                    entry.addProperty("octaves_type", value.getOctaves().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }
                if (value.getFrequency() != null) {
                    JsonObject entry = value.getFrequency().getValueType().getParser().toJson(value.getFrequency()).getAsJsonObject();
                    entry.addProperty("frequency_type", value.getFrequency().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }
                if (value.getAmplitude() != null) {
                    JsonObject entry = value.getAmplitude().getValueType().getParser().toJson(value.getAmplitude()).getAsJsonObject();
                    entry.addProperty("amplitude_type", value.getAmplitude().getValueType().getKey().toString());
                    jsonObject.add("noise", entry);
                }

                return jsonObject;
            }

            @Override
            public NoiseConfiguration fromJson(JsonElement jsonElement) {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                BooleanValue activate = null;

                IntegerValue maxCount = null;
                IntegerValue seed = null;
                IntegerValue octaves = null;
                FloatValue frequency = null;
                FloatValue amplitude = null;

                if (jsonObject.has("noise")) {
                    JsonObject entry = jsonObject.getAsJsonObject("noise");
                    activate = registries.getValueTypeRegistry(BooleanType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("noise_activate_type").getAsString())).get().getParser().fromJson(entry);

                    maxCount = registries.getValueTypeRegistry(IntegerType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("max-count_type").getAsString())).get().getParser().fromJson(entry);
                    seed = registries.getValueTypeRegistry(IntegerType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("seed_type").getAsString())).get().getParser().fromJson(entry);
                    octaves = registries.getValueTypeRegistry(IntegerType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("octaves_type").getAsString())).get().getParser().fromJson(entry);
                    frequency = registries.getValueTypeRegistry(FloatType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("frequency_type").getAsString())).get().getParser().fromJson(entry);
                    amplitude = registries.getValueTypeRegistry(FloatType.class).get(NamespacedKey.fromString(entry.getAsJsonPrimitive("amplitude_type").getAsString())).get().getParser().fromJson(entry);
                }

                return new NoiseConfiguration(NoiseModifier.this, activate, seed, octaves, maxCount, frequency, amplitude);
            }
        };
    }

    @NotNull
    @Override
    public Set<Setting> getSettings() {
        return NoiseConfiguration.SETTINGS;
    }

    @NotNull
    @Override
    public Configuration createEmptyConfiguration() {
        return new NoiseConfiguration(this, null, null, null, null, null, null);
    }

    @NotNull
    @Override
    public Parser<PlacementModifierConfiguration> getParser() {
        return parser;
    }

    @NotNull
    @Override
    public NoiseConfiguration merge(@NotNull PlacementModifierConfiguration first, @NotNull PlacementModifierConfiguration second) {
        return new NoiseConfiguration(this,
                ((NoiseConfiguration) first).getActivate() != null ? ((NoiseConfiguration) first).getActivate() : ((NoiseConfiguration) second).getActivate(),

                ((NoiseConfiguration) first).getMaxCount() != null ? ((NoiseConfiguration) first).getMaxCount() : ((NoiseConfiguration) second).getMaxCount(),
                ((NoiseConfiguration) first).getSeed() != null ? ((NoiseConfiguration) first).getSeed() : ((NoiseConfiguration) second).getSeed(),
                ((NoiseConfiguration) first).getOctaves() != null ? ((NoiseConfiguration) first).getOctaves() : ((NoiseConfiguration) second).getOctaves(),
                ((NoiseConfiguration) first).getFrequency() != null ? ((NoiseConfiguration) first).getFrequency() : ((NoiseConfiguration) second).getFrequency(),
                ((NoiseConfiguration) first).getAmplitude() != null ? ((NoiseConfiguration) first).getAmplitude() : ((NoiseConfiguration) second).getAmplitude());
    }

    @NotNull
    @Override
    public Stream<BlockVector> getPositions(@NotNull WorldInfo worldInfo, @NotNull Random random, @NotNull BlockVector position, @NotNull LimitedRegion limitedRegion, @NotNull NoiseConfiguration configuration) {
        if (configuration.getActivate() == null || !configuration.getActivate().getValue(worldInfo, random, position, limitedRegion) || configuration.getMaxCount() == null || configuration.getSeed() == null || configuration.getOctaves() == null || configuration.getFrequency() == null || configuration.getAmplitude() == null) {
            return Stream.of(position);
        }

        double noiseValue = SimplexNoiseGenerator.getNoise(
                position.getX(),
                position.getZ(),
                configuration.getOctaves().getValue(worldInfo, random, position, limitedRegion),
                configuration.getFrequency().getValue(worldInfo, random, position, limitedRegion),
                configuration.getAmplitude().getValue(worldInfo, random, position, limitedRegion));

        return IntStream.range(0, (int)(noiseValue * configuration.getMaxCount().getValue(worldInfo, random, position, limitedRegion))).mapToObj((var1x) -> position);
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return KEY;
    }
}
