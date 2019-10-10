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
import java.util.Random;
import java.util.function.Function;

public class StrippedBarkColoredSprite extends CustomSprite {
    private final ResourceLocation logTop;
    private final ResourceLocation logSide;

    public StrippedBarkColoredSprite(String spriteName, ResourceLocation logTop, ResourceLocation logSide) {
        super(spriteName);
        this.logTop = logTop;
        this.logSide = logSide;
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return ImmutableSet.of(logTop, logSide);
    }

    private float[] getGammaCorrectedLumaRange(TextureAtlasSprite baseTex, int offset16) {
        float minL = Float.MAX_VALUE;
        float maxL = Float.MIN_VALUE;

        double A = 0;
        double B = 0;
        int count = 0;

        int offset = offset16 * baseTex.getIconWidth() / 16;
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

        minL = (float) Math.pow(minL / 100f, 2.2) * 100f;
        maxL = (float) Math.pow(maxL / 100f, 2.2) * 100f;

        return new float[] { minL, maxL, (float) A, (float) B };
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        TextureAtlasSprite baseTex = SpriteUtils.loadSpriteOrWarn(logSide, textureGetter);
        float[] gcrMiddle = getGammaCorrectedLumaRange(SpriteUtils.loadSpriteOrWarn(logTop, textureGetter), 2);
        float[] gcrSide = getGammaCorrectedLumaRange(baseTex, 0);
        float[] gcrLeft = new float[] { -0.28125f, -0.234375f };

        gcrLeft[0] = (gcrLeft[0] * (gcrMiddle[1] - gcrMiddle[0])) + gcrMiddle[0];
        gcrLeft[1] = (gcrLeft[1] * (gcrMiddle[1] - gcrMiddle[0])) + gcrMiddle[0];
        if (gcrLeft[0] < 0f) gcrLeft[0] = 0f;
        if (gcrLeft[1] < 0f) gcrLeft[1] = 0f;

        // recolor template texture
        int[] templateData = new int[baseTex.getIconWidth() * baseTex.getIconHeight()];
        int[] templateInput = SpriteUtils.getFrameDataOrWarn(baseTex);
        for (int ix = 0; ix < baseTex.getIconWidth(); ix++) {
            // adapt luma
            // the range is from ~92 to ~98 on the leftmost side, turning into the log top range on the 1/4th
            // we also want to make the middle color less sensitive
            float offset = (Math.abs(7.5f - ix) - 2.5f);
            if (offset < 0f) offset = 0f;
            offset = 1f - (offset / 5f);
            offset = (float) Math.pow(offset, 0.65);
            float minL = gcrLeft[0] * (1 - offset) + (gcrMiddle[0] * 0.75f + gcrMiddle[1] * 0.25f) * offset;
            float maxL = gcrLeft[1] * (1 - offset) + (gcrMiddle[1] * 0.75f + gcrMiddle[0] * 0.25f) * offset;

            minL /= 100f;
            maxL /= 100f;

            for (int iy = 0; iy < baseTex.getIconHeight(); iy++) {
                int ip = iy * baseTex.getIconWidth() + ix;
                int pixel = templateInput[ip];
                float[] lab = UCWColorspaceUtils.XYZtoLAB(UCWColorspaceUtils.sRGBtoXYZ(UCWColorspaceUtils.fromInt(pixel)));
                float lum = (float) Math.pow(lab[0] / 100f, 2.2) * 100f;
                // luma is in the gcrSide range
                lum = ((lum - gcrSide[0]) / (gcrSide[1] - gcrSide[0]));
                // luma is in the 0..1 range
                lum = (lum * (maxL - minL)) + minL;
                // luma is in the minL..maxL range (still 0..1)
                lum = (float) Math.pow(lum, 1 / 2.2) * 100f;
                // luma is now proper, i think?
                lab[0] = lum;
                lab[1] = gcrMiddle[2];
                lab[2] = gcrMiddle[3];
                templateData[ip] = UCWColorspaceUtils.asInt(UCWColorspaceUtils.XYZtosRGB(UCWColorspaceUtils.LABtoXYZ(lab))) | 0xFF000000;
            }
        }

        setIconWidth(baseTex.getIconWidth());
        setIconHeight(baseTex.getIconHeight());
        addFrameTextureData(templateData);

        return false;
    }
}
