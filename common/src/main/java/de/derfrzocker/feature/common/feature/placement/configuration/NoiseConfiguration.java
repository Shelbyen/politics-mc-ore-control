package de.derfrzocker.feature.common.feature.placement.configuration;

import de.derfrzocker.feature.api.FeaturePlacementModifier;
import de.derfrzocker.feature.api.PlacementModifierConfiguration;
import de.derfrzocker.feature.api.Setting;
import de.derfrzocker.feature.api.Value;
import de.derfrzocker.feature.common.value.bool.BooleanType;
import de.derfrzocker.feature.common.value.bool.BooleanValue;
import de.derfrzocker.feature.common.value.number.FloatType;
import de.derfrzocker.feature.common.value.number.FloatValue;
import de.derfrzocker.feature.common.value.number.IntegerType;
import de.derfrzocker.feature.common.value.number.IntegerValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class NoiseConfiguration implements PlacementModifierConfiguration {

    public final static Set<Setting> SETTINGS;
    public final static Setting ACTIVATE = new Setting("activate", BooleanType.class);
    public final static Setting SEED = new Setting("seed", IntegerType.class);
    public final static Setting OCTAVES = new Setting("octaves", IntegerType.class);
    public final static Setting FREQUENCY = new Setting("frequency", FloatType.class);
    public final static Setting AMPLITUDE = new Setting("amplitude", FloatType.class);


    static {
        Set<Setting> settings = new LinkedHashSet<>();
        settings.add(ACTIVATE);
        settings.add(SEED);
        settings.add(OCTAVES);
        settings.add(FREQUENCY);
        settings.add(AMPLITUDE);
        SETTINGS = Collections.unmodifiableSet(settings);
    }

    private final FeaturePlacementModifier<?> placementModifier;
    private BooleanValue activate;
    private IntegerValue seed;
    private IntegerValue octaves;
    private FloatValue frequency;
    private FloatValue amplitude;

    private boolean dirty = false;

    public NoiseConfiguration(FeaturePlacementModifier<?> placementModifier, BooleanValue activate, IntegerValue seed, IntegerValue octaves, FloatValue frequency, FloatValue amplitude) {
        this.placementModifier = placementModifier;
        this.activate = activate;
        this.seed = seed;
        this.octaves = octaves;
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    public BooleanValue getActivate() {
        return activate;
    }

    public IntegerValue getSeed() {
        return seed;
    }

    public IntegerValue getOctaves() {
        return octaves;
    }

    public FloatValue getFrequency() {
        return frequency;
    }

    public FloatValue getAmplitude() {
        return amplitude;
    }

    @NotNull
    @Override
    public FeaturePlacementModifier<?> getOwner() {
        return placementModifier;
    }

    @NotNull
    @Override
    public Set<Setting> getSettings() {
        return SETTINGS;
    }

    @Override
    public Value<?, ?, ?> getValue(@NotNull Setting setting) {
        if (setting == ACTIVATE) {
            return getActivate();
        } else if (setting == SEED) {
            return getSeed();
        } else if (setting == OCTAVES) {
            return getOctaves();
        } else if (setting == FREQUENCY) {
            return getFrequency();
        } else if (setting == AMPLITUDE) {
            return getAmplitude();
        }

        throw new IllegalArgumentException(String.format("Setting '%s' is not in the configuration '%s'", setting, getClass().getSimpleName()));
    }

    @Override
    public void setValue(@NotNull Setting setting, Value<?, ?, ?> value) {
        if (setting == ACTIVATE) {
            activate = (BooleanValue) value;
            dirty = true;
            return;
        } else if (setting == SEED) {
            seed = (IntegerValue) value;
            dirty = true;
            return;
        } else if (setting == OCTAVES) {
            octaves = (IntegerValue) value;
            dirty = true;
            return;
        } else if (setting == FREQUENCY) {
            frequency = (FloatValue) value;
            dirty = true;
            return;
        } else if (setting == AMPLITUDE) {
            amplitude = (FloatValue) value;
            dirty = true;
            return;
        }

        throw new IllegalArgumentException(String.format("Setting '%s' is not in the configuration '%s'", setting, getClass().getSimpleName()));
    }

    @Override
    public boolean isDirty() {
        if (dirty) {
            return true;
        }

        return activate != null && activate.isDirty() || seed != null && seed.isDirty() || octaves != null && octaves.isDirty() || frequency != null && frequency.isDirty() || amplitude != null && amplitude.isDirty();
    }

    @Override
    public void saved() {
        dirty = false;

        if (activate != null) {
            activate.saved();
        }
        if (seed != null) {
            seed.saved();
        }
        if (octaves != null) {
            octaves.saved();
        }
        if (frequency != null) {
            frequency.saved();
        }
        if (amplitude != null) {
            amplitude.saved();
        }
    }
}
