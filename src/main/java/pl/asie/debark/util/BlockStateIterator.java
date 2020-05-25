/*
 * Copyright (c) 2017, 2018, 2019 Adrian Siekierka
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
