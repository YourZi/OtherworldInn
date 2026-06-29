package com.otherworldinn.world.sponsor.service;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.otherworldinn.world.sponsor.SponsorAppearanceSnapshot;
import com.otherworldinn.world.sponsor.SponsorDefinition;
import com.otherworldinn.world.sponsor.SponsorProfileSourceType;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class SponsorProfileResolver {
    private static final Pattern PROFILE_ID_PATTERN = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F]{32})\"");
    private static final Pattern PROFILE_NAME_PATTERN = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern PROPERTY_VALUE_PATTERN = Pattern.compile("\"value\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern SKIN_URL_PATTERN = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern MODEL_PATTERN = Pattern.compile("\"model\"\\s*:\\s*\"(slim|classic)\"");
    private static final long SUCCESS_CACHE_TTL_MS = 6L * 60L * 60L * 1000L;
    private static final long FAILURE_CACHE_TTL_MS = 10L * 60L * 1000L;
    private static final Map<String, CacheEntry> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<Consumer<SponsorAppearanceSnapshot>>> PENDING = new ConcurrentHashMap<>();

    private SponsorProfileResolver() {}

    public static void resolveAppearanceAsync(
            SponsorDefinition definition,
            MinecraftServer server,
            Consumer<SponsorAppearanceSnapshot> callback) {
        if (definition == null || server == null || callback == null) {
            return;
        }

        String normalizedKey = definition.normalizedKey();
        if (normalizedKey.isBlank()) {
            server.execute(() -> callback.accept(SponsorAppearanceSnapshot.localFallback(definition.playerName())));
            return;
        }

        long now = System.currentTimeMillis();
        CacheEntry cached = CACHE.get(normalizedKey);
        if (cached != null && cached.isValidAt(now)) {
            server.execute(() -> callback.accept(cached.snapshot()));
            return;
        }

        List<Consumer<SponsorAppearanceSnapshot>> waitingCallbacks = PENDING.get(normalizedKey);
        if (waitingCallbacks != null) {
            waitingCallbacks.add(callback);
            return;
        }

        CopyOnWriteArrayList<Consumer<SponsorAppearanceSnapshot>> createdCallbacks = new CopyOnWriteArrayList<>();
        createdCallbacks.add(callback);
        List<Consumer<SponsorAppearanceSnapshot>> existing = PENDING.putIfAbsent(normalizedKey, createdCallbacks);
        if (existing != null) {
            existing.add(callback);
            return;
        }

        Thread thread =
                new Thread(
                        () -> {
                            SponsorAppearanceSnapshot snapshot = resolveAppearance(definition, server);
                            long ttl =
                                    snapshot.sourceType() == SponsorProfileSourceType.LOCAL_FALLBACK
                                            ? FAILURE_CACHE_TTL_MS
                                            : SUCCESS_CACHE_TTL_MS;
                            CACHE.put(normalizedKey, new CacheEntry(snapshot, System.currentTimeMillis() + ttl));
                            List<Consumer<SponsorAppearanceSnapshot>> callbacks = PENDING.remove(normalizedKey);
                            if (callbacks == null || callbacks.isEmpty()) {
                                return;
                            }
                            server.execute(() -> callbacks.forEach(consumer -> consumer.accept(snapshot)));
                        },
                        "otherworldinn-sponsor-profile-" + sanitizeThreadName(normalizedKey));
        thread.setDaemon(true);
        thread.start();
    }

    private static SponsorAppearanceSnapshot resolveAppearance(
            SponsorDefinition definition, MinecraftServer server) {
        SponsorAppearanceSnapshot mojangSnapshot = fetchFromMojang(definition.playerName());
        if (mojangSnapshot != null) {
            return mojangSnapshot;
        }

        SponsorAppearanceSnapshot livePlayerSnapshot = fetchFromLivePlayers(definition.playerName(), server);
        if (livePlayerSnapshot != null) {
            return livePlayerSnapshot;
        }

        return SponsorAppearanceSnapshot.localFallback(definition.playerName());
    }

    private static SponsorAppearanceSnapshot fetchFromMojang(String playerName) {
        try {
            String encodedName = URLEncoder.encode(playerName, StandardCharsets.UTF_8);
            String profileResponse =
                    readHttp("https://api.mojang.com/users/profiles/minecraft/" + encodedName, 3000, 3000);
            String profileId = matchGroup(PROFILE_ID_PATTERN, profileResponse);
            if (profileId == null || profileId.isBlank()) {
                return null;
            }
            String resolvedName = matchGroup(PROFILE_NAME_PATTERN, profileResponse);
            String sessionResponse =
                    readHttp(
                            "https://sessionserver.mojang.com/session/minecraft/profile/" + profileId,
                            4000,
                            4000);
            String propertyValue = matchGroup(PROPERTY_VALUE_PATTERN, sessionResponse);
            return buildSnapshotFromTextures(
                    playerName,
                    resolvedName == null || resolvedName.isBlank() ? playerName : resolvedName,
                    propertyValue,
                    SponsorProfileSourceType.MOJANG_ONLINE);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static SponsorAppearanceSnapshot fetchFromLivePlayers(String playerName, MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String liveName = player.getGameProfile().getName();
            if (liveName == null || !liveName.equalsIgnoreCase(playerName)) {
                continue;
            }
            SponsorAppearanceSnapshot snapshot =
                    buildSnapshotFromGameProfile(
                            playerName, player.getGameProfile(), SponsorProfileSourceType.LIVE_PLAYER);
            if (snapshot != null) {
                return snapshot;
            }
        }
        return null;
    }

    private static SponsorAppearanceSnapshot buildSnapshotFromGameProfile(
            String sponsorName, GameProfile profile, SponsorProfileSourceType sourceType) {
        if (profile == null) {
            return null;
        }
        String resolvedName = profile.getName();
        Collection<Property> textures = profile.getProperties().get("textures");
        if (textures == null || textures.isEmpty()) {
            return null;
        }
        Property textureProperty = textures.iterator().next();
        if (textureProperty == null || textureProperty.value() == null || textureProperty.value().isBlank()) {
            return null;
        }
        return buildSnapshotFromTextures(sponsorName, resolvedName, textureProperty.value(), sourceType);
    }

    private static SponsorAppearanceSnapshot buildSnapshotFromTextures(
            String sponsorName,
            String resolvedName,
            String texturesValue,
            SponsorProfileSourceType sourceType) {
        if (texturesValue == null || texturesValue.isBlank()) {
            return null;
        }
        try {
            String decoded = new String(Base64.getDecoder().decode(texturesValue), StandardCharsets.UTF_8);
            String skinUrl = matchGroup(SKIN_URL_PATTERN, decoded);
            if (skinUrl == null || skinUrl.isBlank()) {
                return null;
            }
            String model = matchGroup(MODEL_PATTERN, decoded);
            return new SponsorAppearanceSnapshot(
                    sponsorName,
                    resolvedName,
                    "slim".equalsIgnoreCase(model) ? "slim" : "default",
                    skinUrl.replace("\\/", "/"),
                    sourceType);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static String readHttp(String url, int connectTimeoutMs, int readTimeoutMs) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(connectTimeoutMs);
        connection.setReadTimeout(readTimeoutMs);
        connection.setUseCaches(false);
        int responseCode = connection.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
            throw new IOException("Unexpected HTTP status: " + responseCode);
        }
        try (InputStream inputStream = connection.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }

    private static String matchGroup(Pattern pattern, String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        Matcher matcher = pattern.matcher(input);
        if (!matcher.find() || matcher.groupCount() < 1) {
            return null;
        }
        return matcher.group(1);
    }

    private static String sanitizeThreadName(String text) {
        return text.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private record CacheEntry(SponsorAppearanceSnapshot snapshot, long expiresAtMs) {
        private boolean isValidAt(long currentTimeMs) {
            return this.snapshot != null && currentTimeMs < this.expiresAtMs;
        }
    }
}
