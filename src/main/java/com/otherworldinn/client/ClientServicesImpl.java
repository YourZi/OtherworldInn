package com.otherworldinn.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.otherworldinn.OtherworldInn;
import com.otherworldinn.client.commission.CommissionClientManager;
import com.otherworldinn.client.dialogue.DialogueClientManager;
import com.otherworldinn.client.gui.screen.SeasonCalendarScreen;
import com.otherworldinn.client.renderer.RoomOutlineRenderer;
import com.otherworldinn.client.util.TextureUtils;
import com.otherworldinn.util.ClientServices;
import com.otherworldinn.world.dialogue.DialogueNodeView;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public final class ClientServicesImpl implements ClientServices.Hooks {
    private static final Map<String, ResourceLocation> SKIN_CACHE = new ConcurrentHashMap<>();

    @Override
    public void activateRoomOutline(int ticks) {
        RoomOutlineRenderer.activateTimedRoomOutline(ticks);
    }

    @Override
    @Nullable
    public Player getClientPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft == null ? null : minecraft.player;
    }

    @Override
    public boolean isChineseLocale() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null) {
            return true;
        }
        String locale = minecraft.getLanguageManager().getSelected();
        return locale != null && locale.toLowerCase(Locale.ROOT).startsWith("zh");
    }

    @Override
    public boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    @Override
    public List<ResourceLocation> findTexturesInFolder(String namespace, String path) {
        return TextureUtils.findTexturesInFolder(namespace, path);
    }

    @Override
    public ResourceLocation getMojangSkinTexture(String playerName, ResourceLocation fallback) {
        if (playerName == null || playerName.isBlank()) {
            return fallback;
        }
        String key = playerName.toLowerCase(Locale.ROOT);
        ResourceLocation cached = SKIN_CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        String skinUrl = fetchMojangSkinUrl(playerName);
        if (skinUrl == null || skinUrl.isBlank()) {
            return fallback;
        }
        ResourceLocation textureId =
                ResourceLocation.fromNamespaceAndPath(OtherworldInn.MODID, "mojang_skin/" + key);
        try {
            downloadAndRegisterTexture(textureId, skinUrl);
            SKIN_CACHE.put(key, textureId);
            return textureId;
        } catch (Exception e) {
            OtherworldInn.LOGGER.debug("Failed to download Mojang skin for {}", playerName, e);
            return fallback;
        }
    }

    @Override
    public void handleDialogueNode(DialogueNodeView view) {
        DialogueClientManager.handleNode(view);
    }

    @Override
    public void handleDialogueClose() {
        DialogueClientManager.handleClose();
    }

    @Override
    public void handleCommissionBoard(CompoundTag data, boolean openScreen) {
        CommissionClientManager.handleBoardData(data, openScreen);
    }

    @Override
    public void openSeasonCalendar() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.player == null || minecraft.level == null) {
            return;
        }
        minecraft.setScreen(new SeasonCalendarScreen());
    }

    private static String fetchMojangSkinUrl(String playerName) {
        try {
            // Step 1: Resolve player name to UUID
            URL profileUrlObj = new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName);
            HttpURLConnection profileConnection = (HttpURLConnection) profileUrlObj.openConnection();
            profileConnection.setConnectTimeout(5000);
            profileConnection.setReadTimeout(5000);
            if (profileConnection.getResponseCode() != 200) {
                return null;
            }
            JsonObject profileJson;
            try (InputStreamReader reader =
                    new InputStreamReader(profileConnection.getInputStream(), StandardCharsets.UTF_8)) {
                profileJson = new Gson().fromJson(reader, JsonObject.class);
            }
            String uuid = profileJson != null ? profileJson.get("id").getAsString() : null;
            if (uuid == null || uuid.isBlank()) {
                return null;
            }

            // Step 2: Get session profile including textures
            URL sessionUrlObj =
                    new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            HttpURLConnection sessionConnection = (HttpURLConnection) sessionUrlObj.openConnection();
            sessionConnection.setConnectTimeout(5000);
            sessionConnection.setReadTimeout(5000);
            if (sessionConnection.getResponseCode() != 200) {
                return null;
            }
            JsonObject sessionJson;
            try (InputStreamReader reader =
                    new InputStreamReader(sessionConnection.getInputStream(), StandardCharsets.UTF_8)) {
                sessionJson = new Gson().fromJson(reader, JsonObject.class);
            }

            // Step 3: Parse the Base64-encoded textures property
            var properties = sessionJson.getAsJsonArray("properties");
            for (var element : properties) {
                JsonObject prop = element.getAsJsonObject();
                if ("textures".equals(prop.get("name").getAsString())) {
                    String encoded = prop.get("value").getAsString();
                    String decoded = new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
                    JsonObject texturesJson = new Gson().fromJson(decoded, JsonObject.class);
                    JsonObject textures = texturesJson.getAsJsonObject("textures");
                    if (textures == null) {
                        return null;
                    }
                    JsonObject skin = textures.getAsJsonObject("SKIN");
                    if (skin == null) {
                        return null;
                    }
                    return skin.get("url").getAsString();
                }
            }
        } catch (Exception e) {
            OtherworldInn.LOGGER.debug("Failed to resolve Mojang skin for {}", playerName, e);
        }
        return null;
    }

    private static void downloadAndRegisterTexture(ResourceLocation textureId, String skinUrl)
            throws Exception {
        try (InputStream inputStream = new URL(skinUrl).openStream()) {
            NativeImage image = NativeImage.read(inputStream);
            if (image == null) {
                return;
            }
            DynamicTexture texture = new DynamicTexture(image);
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.execute(() -> minecraft.getTextureManager().register(textureId, texture));
            }
        }
    }
}
