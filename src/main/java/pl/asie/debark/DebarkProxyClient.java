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

package pl.asie.debark;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pl.asie.debark.messy.DebarkTopSprite;
import pl.asie.debark.messy.LogColoredSprite;
import pl.asie.debark.messy.StrippedBarkColoredSprite;
import pl.asie.debark.util.ModelDataLookup;
import pl.asie.debark.util.ModelLoaderEarlyView;
import pl.asie.debark.util.ResourceUtils;

public class DebarkProxyClient extends DebarkProxyCommon {
    @Override
    public void preInit() {
        super.preInit();
    }

    @SubscribeEvent
    public void modelRegister(ModelRegistryEvent event) {
        DebarkMod.blocksMap.forEach((state, debarkedBlock) -> {
            ModelResourceLocation invModelLocation = new ModelResourceLocation(debarkedBlock.getBlock().getRegistryName(), "axis=y,variant=" + debarkedBlock.getVariant());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(debarkedBlock.getBlock()), debarkedBlock.getVariant(), invModelLocation);
        });
    }

    @SubscribeEvent
    public void modelBake(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "block/cube_column"));

            DebarkMod.blocksMap.forEach((state, debarkedBlock) -> {
                ResourceLocation blockSide = new ResourceLocation("debark", "blocks/debark_side_" + debarkedBlock.getTextureKey());
                ResourceLocation blockTop = new ResourceLocation("debark", "blocks/debark_top_" + debarkedBlock.getTextureKey());

                IModel retexturedModel = baseModel.retexture(
                        ImmutableMap.of(
                                "side", blockSide.toString(),
                                "end", blockTop.toString(),
                                "#side", blockSide.toString(),
                                "#end", blockTop.toString()
                        )
                );

                IModel retexturedModelBark = baseModel.retexture(
                        ImmutableMap.of(
                                "side", blockSide.toString(),
                                "end", blockSide.toString(),
                                "#side", blockSide.toString(),
                                "#end", blockSide.toString()
                        )
                );

                for (BlockLog.EnumAxis axis : BlockLog.EnumAxis.values()) {
                    IModel srcModel = axis == BlockLog.EnumAxis.NONE ? retexturedModelBark : retexturedModel;
                    ModelRotation rotation = ModelRotation.X0_Y0;
                    switch (axis) {
                        case X: rotation = ModelRotation.X90_Y90; break;
                        case Z: rotation = ModelRotation.X90_Y0; break;
                    }
                    IBakedModel bakedModel = srcModel.bake(rotation, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
                    ModelResourceLocation bakedModelLocation = new ModelResourceLocation(debarkedBlock.getBlock().getRegistryName(), "axis=" + axis.getName() + ",variant=" + debarkedBlock.getVariant());
                    event.getModelRegistry().putObject(bakedModelLocation, bakedModel);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void registerTextures(TextureStitchEvent.Pre event) {
        ModelLoaderEarlyView modelLoaderEarlyView = new ModelLoaderEarlyView();
        ResourceLocation templateSide = new ResourceLocation("debark", "blocks/debark_side");
        ResourceLocation templateTop = new ResourceLocation("debark", "blocks/debark_top");

        if (!ResourceUtils.textureExists(templateTop)) {
            event.getMap().setTextureEntry(new DebarkTopSprite("debark:blocks/debark_top", new ResourceLocation("minecraft", "blocks/log_oak_top")));
        }

        DebarkMod.blocksMap.forEach((state, debarkedBlock) -> {
            IModel model = modelLoaderEarlyView.getModel(state);
            if (model == null) {
                System.out.println("AAAAAAAAAA");
                return;
            }

            ResourceLocation logTopLocation = ModelDataLookup.getLocation(state, EnumFacing.UP, model);
            ResourceLocation logSideLocation = ModelDataLookup.getLocation(state, EnumFacing.NORTH, model);

            ResourceLocation blockSide = new ResourceLocation("debark", "blocks/debark_side_" + debarkedBlock.getTextureKey());
            ResourceLocation blockTop = new ResourceLocation("debark", "blocks/debark_top_" + debarkedBlock.getTextureKey());

            if (!ResourceUtils.textureExists(blockSide)) {
                if (ResourceUtils.textureExists(templateSide)) {
                    event.getMap().setTextureEntry(new LogColoredSprite(blockSide.toString(), logTopLocation, templateSide));
                } else {
                    event.getMap().setTextureEntry(new StrippedBarkColoredSprite(blockSide.toString(), logTopLocation, logSideLocation));
                }
            }
            if (!ResourceUtils.textureExists(blockTop)) {
                event.getMap().setTextureEntry(new LogColoredSprite(blockTop.toString(), logTopLocation, templateTop));
            }
        });
    }
}
