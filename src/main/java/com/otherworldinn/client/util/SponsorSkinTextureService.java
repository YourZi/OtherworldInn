package com.otherworldinn.client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.otherworldinn.OtherworldInn;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

public final class SponsorSkinTextureService {
    private static final ResourceLocation DEFAULT_FALLBACK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    OtherworldInn.MODID, "textures/entity/guest/ordinary_guest/1.png");
    private static final Map<String, ResourceLocation> DOWNLOADED_TEXTURES = new ConcurrentHashMap<>();
    private static final Set<String> PENDING_TEXTURES = ConcurrentHashMap.newKeySet();

    private SponsorSkinTextureService() {}

    public static ResourceLocation resolveFallbackTexture(String sponsorName) {
        if (sponsorName == null || sponsorName.isBlank()) {
            return DEFAULT_FALLBACK_TEXTURE;
        }
        ResourceLocation candidate =
                ResourceLocation.fromNamespaceAndPath(
                        OtherworldInn.MODID, "textures/entity/guest/sponsor_guest/" + sponsorName + ".png");
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null && minecraft.getResourceManager().getResource(candidate).isPresent()) {
            return candidate;
        }
        return DEFAULT_FALLBACK_TEXTURE;
    }

    public static ResourceLocation getResolvedTexture(
            String sponsorName, String skinUrl, ResourceLocation fallbackTexture) {
        if (skinUrl == null || skinUrl.isBlank()) {
            return fallbackTexture;
        }
        String cacheKey = buildCacheKey(sponsorName, skinUrl);
        ResourceLocation cached = DOWNLOADED_TEXTURES.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        if (!PENDING_TEXTURES.add(cacheKey)) {
            return fallbackTexture;
        }
        Thread thread =
                new Thread(
                        () -> {
                            try {
                                downloadTexture(cacheKey, sponsorName, skinUrl);
                            } catch (Throwable ignored) {
                            } finally {
                                PENDING_TEXTURES.remove(cacheKey);
                            }
                        },
                        "otherworldinn-sponsor-skin-" + sanitizePathComponent(sponsorName));
        thread.setDaemon(true);
        thread.start();
        return fallbackTexture;
    }

    private static void downloadTexture(String cacheKey, String sponsorName, String skinUrl) throws Exception {
        ResourceLocation textureId =
                ResourceLocation.fromNamespaceAndPath(
                        OtherworldInn.MODID,
                        "sponsor_skin/"
                                + sanitizePathComponent(sponsorName)
                                + "_"
                                + Integer.toHexString(skinUrl.hashCode()));
        try (InputStream inputStream = new URL(skinUrl).openStream()) {
            NativeImage image = NativeImage.read(inputStream);
            if (image == null) {
                return;
            }
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft == null) {
                return;
            }
            minecraft.execute(
                    () -> {
                        minecraft.getTextureManager().register(textureId, texture);
                        DOWNLOADED_TEXTURES.put(cacheKey, textureId);
                    });
        }
    }

    private static String buildCacheKey(String sponsorName, String skinUrl) {
        return sanitizePathComponent(sponsorName) + "|" + skinUrl;
    }

    private static String sanitizePathComponent(String text) {
        if (text == null || text.isBlank()) {
            return "unknown";
        }
        return text.trim().toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
    }
}
