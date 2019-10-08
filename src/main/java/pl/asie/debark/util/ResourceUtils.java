package pl.asie.debark.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public final class ResourceUtils {
    private ResourceUtils() {

    }

    public static boolean textureExists(ResourceLocation loc) {
        IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
        try (IResource resource = resourceManager.getResource(new ResourceLocation(loc.getNamespace(), "textures/" + loc.getPath() + ".png"))) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
