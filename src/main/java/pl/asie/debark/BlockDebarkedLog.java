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

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import pl.asie.debark.old.UCWBlockAccess;

import javax.annotation.Nullable;

public class BlockDebarkedLog extends BlockLog {
    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 3);
    private final IBlockState[] parents;

    public BlockDebarkedLog(IBlockState[] parents) {
        super();
        this.parents = parents;
    }

    public IBlockState getParentState(int variant) {
        if (variant < 0 || variant >= parents.length) {
            DebarkMod.logger.error("Mod requested parent block state for " + this + " with invalid variant number " + variant + "!");
            return parents[0];
        } else {
            return parents[variant];
        }
    }

    public IBlockState getParentState(IBlockState myState) {
        return getParentState(myState.getValue(VARIANT));
    }

    public ItemStack getParentStack(int variant) {
        if (variant < 0 || variant >= parents.length) {
            DebarkMod.logger.error("Mod requested parent item sstack for " + this + " with invalid variant number " + variant + "!");
            return parents[0].getBlock().getItem(null, null, parents[0]);
        }
        return parents[variant].getBlock().getItem(null, null, parents[variant]);
    }

    public int getVariantCount() {
        return parents.length;
    }

    public String getLocalizedName(int variant) {
        return I18n.translateToLocalFormatted("tile.debark.debarked.name", getParentStack(variant).getDisplayName());
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getStateFromMeta(meta << 2).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.fromFacingAxis(facing.getAxis()));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BlockLog.LOG_AXIS, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.values()[meta & 3]).withProperty(VARIANT, meta >> 2);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(BlockLog.LOG_AXIS).ordinal() | (state.getValue(VARIANT) << 2);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (int i = 0; i < getVariantCount(); i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState parent = getParentState(world.getBlockState(pos));
        return parent.getBlock().getFlammability(new UCWBlockAccess(world, ImmutableMap.of(pos, parent)), pos, face);
    }

    @Override
    public boolean isFlammable(IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState parent = getParentState(world.getBlockState(pos));
        return parent.getBlock().isFlammable(new UCWBlockAccess(world, ImmutableMap.of(pos, parent)), pos, face);
    }

    @Override
    public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
        IBlockState parent = getParentState(world.getBlockState(pos));
        return parent.getBlock().getFireSpreadSpeed(new UCWBlockAccess(world, ImmutableMap.of(pos, parent)), pos, face);
    }
}
