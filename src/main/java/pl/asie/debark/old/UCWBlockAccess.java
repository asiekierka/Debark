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

package pl.asie.debark.old;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.Map;

public class UCWBlockAccess implements IBlockAccess {
	private final IBlockAccess parent;
	private final Map<BlockPos, IBlockState> diffMap;

	public UCWBlockAccess(IBlockAccess parent, Map<BlockPos, IBlockState> diffMap) {
		this.parent = parent;
		this.diffMap = diffMap;
	}

	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return parent.getTileEntity(pos);
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return parent.getCombinedLight(pos, lightValue);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		IBlockState state = diffMap.get(pos);
		if (state == null) {
			state = parent.getBlockState(pos);
		}
		return state;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return parent.isAirBlock(pos);
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return parent.getBiome(pos);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return parent.getStrongPower(pos, direction);
	}

	@Override
	public WorldType getWorldType() {
		return parent.getWorldType();
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return parent.isSideSolid(pos, side, _default);
	}
}
