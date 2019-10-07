/*
 * Copyright (c) 2017, 2018, 2019 Adrian Siekierka
 *
 * This file is part of Debark.
 *
 * Debark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Debark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Debark.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.asie.debark.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

public final class BlockStateIterator {
    private static final Method WITH_PROPERTY = ObfuscationReflectionHelper.findMethod(IBlockState.class, "func_177226_a", IBlockState.class, IProperty.class, Comparable.class);

    private BlockStateIterator() {

    }

    public static Stream<IBlockState> permuteAll(IBlockState baseState) {
        return permuteBy(baseState, ImmutableList.copyOf(baseState.getPropertyKeys()));
    }

    public static Stream<IBlockState> permuteByNames(IBlockState baseState, List<String> propertyNames) {
        Set<String> propertyNameSet = new HashSet<>(propertyNames);
        List<IProperty> properties = new ArrayList<>();

        for (IProperty<?> property : baseState.getPropertyKeys()) {
            if (propertyNameSet.contains(property.getName())) {
                properties.add(property);
            }
        }

        return permuteBy(baseState, properties);
    }

    public static Stream<IBlockState> permuteBy(IBlockState baseState, List<IProperty> properties) {
        Map<IProperty, List> values = new LinkedHashMap<>();
        int permutations = 1;

        for (IProperty<?> property : properties) {
            List propertyValues = ImmutableList.copyOf(property.getAllowedValues());
            values.put(property, propertyValues);
            permutations *= propertyValues.size();
        }

        Stream.Builder<IBlockState> builder = Stream.builder();
        for (int i = 0; i < permutations; i++) {
            IBlockState state = baseState;
            int j = i;
            for (Map.Entry<IProperty, List> valueEntry : values.entrySet()) {
                int size = valueEntry.getValue().size();
                int k = j % size;
                try {
                    state = (IBlockState) WITH_PROPERTY.invoke(state, valueEntry.getKey(), valueEntry.getValue().get(k));
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }
                j /= size;
            }
            builder.accept(state);
        }
        return builder.build();
    }
}
