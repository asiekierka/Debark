package pl.asie.debark.util;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class SpriteUtils {
    private SpriteUtils() {

    }

    public static int[] getFrameDataOrThrow(TextureAtlasSprite sprite) {
        try {
            if (sprite.getFrameCount() <= 0) {
                throw new RuntimeException("Could not read texture data for " + sprite.getIconName() + "! - invalid frame count " + sprite.getFrameCount() + "!");
            }
            int[][] data = sprite.getFrameTextureData(0);
            if (data == null || data.length <= 0 || data[0] == null) {
                throw new RuntimeException("Could not read texture data for " + sprite.getIconName() + " - frame 0 array missing!");
            }
            return data[0];
        } catch (Exception e) {
            throw new RuntimeException("Could not read texture data for " + sprite.getIconName() + "!", e);
        }
    }
}
