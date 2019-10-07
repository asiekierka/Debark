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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.TRSRTransformation;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

public final class ModelDataLookup {
	private static class FakeSprite extends TextureAtlasSprite {
		protected FakeSprite(ResourceLocation spriteName) {
			super(spriteName.toString());
			setIconWidth(16);
			setIconHeight(16);
			initSprite(16, 16, 0, 0, false);
			setFramesTextureData(fakeTextureFrameList);
		}
	}

	private static final List<int[][]> fakeTextureFrameList;
	public static final BufferedImage missingNo;

	static {
		fakeTextureFrameList = ImmutableList.of(new int[1][256]);

		missingNo = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < 256; i++) {
			missingNo.setRGB(i & 15, i >> 4, ((i ^ (i >> 4)) & 8) != 0 ? 0xFF000000 : 0xFFFF00FF);
		}
	}

	private ModelDataLookup() {

	}

	public static ResourceLocation getLocation(IBlockState state, EnumFacing facing, IModel model) {
		String domain = state.getBlock().getRegistryName().getNamespace();

		if ("forestry".equals(domain)) {
			String[] name = state.getBlock().getRegistryName().getPath().split("\\.", 2);
			IProperty variantProp = state.getBlock().getBlockState().getProperty("variant");
			if (variantProp != null) {
				String variant = variantProp.getName(state.getValue(variantProp));
				if (name.length == 2) {
					switch (name[0]) {
						case "planks":
							return new ResourceLocation("forestry", "blocks/wood/planks." + variant);
						case "logs":
							if (facing.getAxis() == EnumFacing.Axis.Y) {
								return new ResourceLocation("forestry", "blocks/wood/heart." + variant);
							} else {
								return new ResourceLocation("forestry", "blocks/wood/bark." + variant);
							}
					}
				}
			}
		} else if ("extratrees".equals(domain)) {
			String[] name = state.getBlock().getRegistryName().getPath().split("\\.", 2);
			IProperty variantProp = state.getBlock().getBlockState().getProperty("variant");
			if (variantProp != null) {
				String variant = variantProp.getName(state.getValue(variantProp));
				if (name.length == 2) {
					switch (name[0]) {
						case "planks":
							return new ResourceLocation("extratrees", "blocks/planks/" + variant);
						case "logs":
							if (facing.getAxis() == EnumFacing.Axis.Y) {
								return new ResourceLocation("extratrees", "blocks/logs/" + variant + "_trunk");
							} else {
								return new ResourceLocation("extratrees", "blocks/logs/" + variant + "_bark");
							}
					}
				}
			}
		}

		try {
			// some mods have a skewed particle texture
			IBakedModel bakedModel = model.bake(TRSRTransformation.identity(), DefaultVertexFormats.ITEM, FakeSprite::new);
			Collection<BakedQuad> quadList = bakedModel.getQuads(state, facing, 0);
			if (!quadList.isEmpty()) {
				return new ResourceLocation(quadList.iterator().next().getSprite().getIconName());
			} else {
				quadList = bakedModel.getQuads(state, null, 0);
				for (BakedQuad q : quadList) {
					if (q.getFace() == EnumFacing.UP) {
						return new ResourceLocation(q.getSprite().getIconName());
					}
				}
			}
		} catch (Exception e) {
			// pass
		}

		return TextureMap.LOCATION_MISSING_TEXTURE;
	}
}
