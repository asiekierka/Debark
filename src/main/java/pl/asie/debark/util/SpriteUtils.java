package pl.asie.debark.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class SpriteUtils {
    private SpriteUtils() {

    }

    public static int[] getFrameDataOrThrow(TextureAtlasSprite sprite) {
        int[][] data = sprite.getFrameTextureData(0);
        if (data == null || data.length <= 0 || data[0] == null) {
            throw new RuntimeException("Could not read texture data for " + sprite.getIconName() + "!");
        }
        return data[0];
    }
}
