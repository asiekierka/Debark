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
