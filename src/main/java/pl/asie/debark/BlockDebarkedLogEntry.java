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

package pl.asie.debark;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

public class BlockDebarkedLogEntry {
    private final BlockDebarkedLog block;
    private final int variant;

    public BlockDebarkedLogEntry(BlockDebarkedLog block, int variant) {
        this.block = block;
        this.variant = variant;
    }

    public BlockDebarkedLog getBlock() {
        return block;
    }

    public int getVariant() {
        return variant;
    }

    public IBlockState getBlockState() {
        return block.getDefaultState().withProperty(BlockDebarkedLog.VARIANT, variant);
    }

    public ItemStack getItemStack() {
        return new ItemStack(block, 1, variant);
    }

    public String getTextureKey() {
        return block.getRegistryName().getPath() + "_" + variant;
    }
}
