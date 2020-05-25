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

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import pl.asie.debark.DebarkMod;

import java.lang.reflect.Field;
import java.util.Map;

public class ModelLoaderEarlyView {
    private ModelLoader loader;
    private Map<ModelResourceLocation, IModel> secretSauce = null;
    private BlockModelShapes blockModelShapes = null;

    public ModelLoaderEarlyView() {
        // don't tell lex
        try {
            Class c = Class.forName("net.minecraftforge.client.model.ModelLoader$VanillaLoader");
            Field f = c.getDeclaredField("INSTANCE");
            f.setAccessible(true);
            Object o = f.get(null);
            f = c.getDeclaredField("loader");
            f.setAccessible(true);
            loader = (ModelLoader) f.get(o);
            f = ModelLoader.class.getDeclaredField("stateModels");
            f.setAccessible(true);
            secretSauce = (Map<ModelResourceLocation, IModel>) f.get(loader);
            f = ObfuscationReflectionHelper.findField(ModelBakery.class, "field_177610_k");
            f.setAccessible(true);
            blockModelShapes = (BlockModelShapes) f.get(loader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public IModel getModel(IBlockState state) {
        Map<IBlockState, ModelResourceLocation> variants = blockModelShapes.getBlockStateMapper().getVariants(state.getBlock());

        if (variants != null) {
            ModelResourceLocation loc = variants.get(state);
            if (loc != null) {
                return secretSauce.get(loc);
            }
        }

        return null;
    }
}
