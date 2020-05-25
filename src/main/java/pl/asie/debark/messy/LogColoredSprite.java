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

package pl.asie.debark.messy;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import pl.asie.debark.old.UCWColorspaceUtils;
import pl.asie.debark.util.CustomSprite;
import pl.asie.debark.util.SpriteUtils;

import java.util.Collection;
import java.util.function.Function;

public class LogColoredSprite extends CustomSprite {
    private final ResourceLocation base;
    private final ResourceLocation template;

    public LogColoredSprite(String spriteName, ResourceLocation base, ResourceLocation template) {
        super(spriteName);
        this.base = base;
        this.template = template;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of(base, template);
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        TextureAtlasSprite baseTex = SpriteUtils.loadSpriteOrWarn(base, textureGetter);
        TextureAtlasSprite templateTex = SpriteUtils.loadSpriteOrWarn(template, textureGetter);

        float minL = Float.MAX_VALUE;
        float maxL = Float.MIN_VALUE;
        double A = 0;
        double B = 0;
        int count = 0;

        int offset = (baseTex.getIconWidth() + 7) / 8;

        int[] baseData = SpriteUtils.getFrameDataOrWarn(baseTex);
        for (int iy = offset; iy < baseTex.getIconHeight() - offset; iy++) {
            for (int ix = offset; ix < baseTex.getIconWidth() - offset; ix++) {
                int pixel = baseData[iy * baseTex.getIconWidth() + ix];
                float[] lab = UCWColorspaceUtils.XYZtoLAB(UCWColorspaceUtils.sRGBtoXYZ(UCWColorspaceUtils.fromInt(pixel)));
                if (lab[0] < minL) minL = lab[0];
                if (lab[0] > maxL) maxL = lab[0];
                A += lab[1]; B += lab[2];
                count++;
            }
        }

        assert count >= 1;
        A /= count;
        B /= count;

        // recolor template texture
        int[] templateData = new int[templateTex.getIconWidth() * templateTex.getIconHeight()];
        int[] templateInput = SpriteUtils.getFrameDataOrWarn(templateTex);
        for (int i = 0; i < templateData.length; i++) {
            int oldPixel = templateInput[i];
            float[] scaledPixel = UCWColorspaceUtils.fromInt(oldPixel);
            float l = (UCWColorspaceUtils.sRGBtoLuma(scaledPixel));
            l = (float) Math.pow(l / 100f, 2.2) * 100f;
            l = (((l / 50f) - 1f) * (maxL - minL)) + minL;
            if (l < 0f) l = 0f;
            else if (l > 100f) l = 100f;
            float[] lab = new float[] { l, (float) A, (float) B };
            templateData[i] = UCWColorspaceUtils.asInt(UCWColorspaceUtils.XYZtosRGB(UCWColorspaceUtils.LABtoXYZ(lab))) | 0xFF000000;
        }

        setIconWidth(templateTex.getIconWidth());
        setIconHeight(templateTex.getIconHeight());
        addFrameTextureData(templateData);

        return false;
    }
}
