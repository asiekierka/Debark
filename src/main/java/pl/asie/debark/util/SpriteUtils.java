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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import pl.asie.debark.DebarkMod;

import java.util.function.Function;

public final class SpriteUtils {
    private static final int[] MISSINGNO_DATA = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            MISSINGNO_DATA[i] = ((((i >> 3) ^ (i >> 7)) & 1) != 0) ? 0xFFFF00FF : 0xFF000000;
        }
    }

    private SpriteUtils() {

    }

    public static boolean isMissingno(TextureAtlasSprite sprite) {
        return "missingno".equals(sprite.getIconName()) || "minecraft:missingno".equals(sprite.getIconName());
    }

    public static TextureAtlasSprite loadSpriteOrWarn(ResourceLocation location, Function<ResourceLocation, TextureAtlasSprite> getter) {
        TextureAtlasSprite sprite = getter.apply(location);
        if (sprite == null) {
            sprite = getter.apply(TextureMap.LOCATION_MISSING_TEXTURE);
            if (sprite == null) {
                throw new RuntimeException("Could not load " + location + " or fallback!");
            }
        }
        if (isMissingno(sprite)) {
            DebarkMod.logger.error("Could not locate texture " + location + "!");
        }
        return sprite;
    }

    public static int[] getFrameDataOrWarn(TextureAtlasSprite sprite) {
        if (isMissingno(sprite)) {
            return MISSINGNO_DATA;
        }
        int[][] data = null;
        if (sprite.getFrameCount() <= 0) {
            DebarkMod.logger.error("Could not read texture data for " + sprite.getIconName() + "! - invalid frame count " + sprite.getFrameCount() + "!");
            return MISSINGNO_DATA;
        }
        try {
            data = sprite.getFrameTextureData(0);
        } catch (Exception e) {
            DebarkMod.logger.error("Could not read texture data for " + sprite.getIconName() + "!", e);
            return MISSINGNO_DATA;
        }
        if (data == null || data.length <= 0 || data[0] == null || data[0].length <= 0) {
            DebarkMod.logger.error("Could not read texture data for " + sprite.getIconName() + " - frame 0 array missing!");
            return MISSINGNO_DATA;
        }
        return data[0];
    }
}
