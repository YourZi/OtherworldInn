package com.otherworldinn.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

public class TextureUtils {
    public static List<ResourceLocation> findTexturesInFolder(String namespace, String path) {
        List<ResourceLocation> textures = new ArrayList<>();
        // 获取 ResourceManager
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return textures;

        ResourceManager manager = mc.getResourceManager();

        // 扫描指定路径下的所有 png 文件
        Map<ResourceLocation, ?> resources =
                manager.listResources(
                        path,
                        loc ->
                                loc.getNamespace().equals(namespace)
                                        && loc.getPath().endsWith(".png"));

        textures.addAll(resources.keySet());

        // 排序以确保顺序一致
        textures.sort(ResourceLocation::compareTo);

        return textures;
    }
}
