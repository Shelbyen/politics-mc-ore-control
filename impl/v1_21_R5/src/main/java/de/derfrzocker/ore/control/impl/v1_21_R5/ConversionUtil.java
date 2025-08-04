package de.derfrzocker.ore.control.impl.v1_21_R5;

import de.derfrzocker.feature.common.value.number.FixedFloatValue;
import de.derfrzocker.feature.common.value.number.IntegerValue;
import de.derfrzocker.feature.common.value.number.integer.FixedDoubleToIntegerValue;
import de.derfrzocker.feature.common.value.number.integer.biased.BiasedToBottomIntegerValue;
import de.derfrzocker.feature.common.value.number.integer.clamped.ClampedIntegerValue;
import de.derfrzocker.feature.common.value.number.integer.clamped.ClampedNormalIntegerValue;
import de.derfrzocker.feature.common.value.number.integer.uniform.UniformIntegerValue;
import de.derfrzocker.feature.common.value.number.integer.weighted.WeightedListIntegerValue;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.valueproviders.ClampedInt;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;
import net.minecraft.util.valueproviders.WeightedListInt;

public final class ConversionUtil {

    private ConversionUtil() {
    }

    public static IntegerValue convert(IntProvider intProvider) {
        try {
            if (intProvider.getType() == IntProviderType.CONSTANT) {
                return new FixedDoubleToIntegerValue(intProvider.getMinValue());
            } else if (intProvider.getType() == IntProviderType.UNIFORM) {
                return new UniformIntegerValue(new FixedDoubleToIntegerValue(intProvider.getMinValue()), new FixedDoubleToIntegerValue(intProvider.getMaxValue()));
            } else if (intProvider.getType() == IntProviderType.BIASED_TO_BOTTOM) {
                return new BiasedToBottomIntegerValue(new FixedDoubleToIntegerValue(intProvider.getMinValue()), new FixedDoubleToIntegerValue(intProvider.getMaxValue()));
            } else if (intProvider.getType() == IntProviderType.CLAMPED) {
                Field sourceField = ClampedInt.class.getDeclaredField(NMSReflectionNames.CLAMPED_INT_SOURCE);
                Field minField = ClampedInt.class.getDeclaredField(NMSReflectionNames.CLAMPED_INT_MIN_INCLUSIVE);
                Field maxField = ClampedInt.class.getDeclaredField(NMSReflectionNames.CLAMPED_INT_MAX_INCLUSIVE);
                sourceField.setAccessible(true);
                minField.setAccessible(true);
                maxField.setAccessible(true);

                IntProvider source = (IntProvider) sourceField.get(intProvider);
                int min = (int) minField.get(intProvider);
                int max = (int) maxField.get(intProvider);
                return new ClampedIntegerValue(convert(source), new FixedDoubleToIntegerValue(min), new FixedDoubleToIntegerValue(max));
            } else if (intProvider.getType() == IntProviderType.WEIGHTED_LIST) {
                Field distributionField = WeightedListInt.class.getDeclaredField(NMSReflectionNames.WEIGHTED_LIST_INT_DISTRIBUTION);
                distributionField.setAccessible(true);
                WeightedList<IntProvider> distribution = (WeightedList<IntProvider>) distributionField.get(intProvider);

                Map<IntegerValue, IntegerValue> values = new LinkedHashMap<>();
                for (Weighted<IntProvider> wrapper : distribution.unwrap()) {
                    values.put(convert(wrapper.value()), new FixedDoubleToIntegerValue(wrapper.weight()));
                }

                return new WeightedListIntegerValue(values);
            } else if (intProvider.getType() == IntProviderType.CLAMPED_NORMAL) {
                Field meanField = ClampedNormalInt.class.getDeclaredField(NMSReflectionNames.CLAMPED_NORMAL_INT_MEAN);
                Field deviationField = ClampedNormalInt.class.getDeclaredField(NMSReflectionNames.CLAMPED_NORMAL_INT_DEVIATION);
                meanField.setAccessible(true);
                deviationField.setAccessible(true);

                float mean = (float) meanField.get(intProvider);
                float deviation = (float) meanField.get(intProvider);
                int min = intProvider.getMinValue();
                int max = intProvider.getMaxValue();
                return new ClampedNormalIntegerValue(new FixedFloatValue(mean), new FixedFloatValue(deviation), new FixedDoubleToIntegerValue(min), new FixedDoubleToIntegerValue(max));
            } else {
                throw new UnsupportedOperationException(String.format("No integer value equivalent for IntProvider '%s'", intProvider));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
