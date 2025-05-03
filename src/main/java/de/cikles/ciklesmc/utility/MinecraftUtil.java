package de.cikles.ciklesmc.utility;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.cikles.ciklesmc.core.CiklesMC;
import org.jetbrains.annotations.NotNull;

import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.UUID;

@SuppressWarnings("unused")
public class MinecraftUtil {

    private MinecraftUtil() {
    }

    public static String getHeadURL(UUID uuid) {
        return "https://mc-heads.net/avatar/" + getPlayerName(uuid);
    }

    // Programmed by Cedrik
    public static @NotNull String getPlayerName(@NotNull UUID uuid) {
        try {
            URL url = URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid).toURL();
            JsonObject json = new GsonBuilder().create().fromJson(new InputStreamReader(url.openStream()), JsonObject.class);
            String name;
            if (json != null) name = String.valueOf(json.get("name"));
            else {
                url = URI.create("https://api.mojang.com/user/profiles/" + uuid + "/names").toURL();
                JsonArray array = new GsonBuilder().create().fromJson(new InputStreamReader(url.openStream()), JsonArray.class);
                name = (array.get(array.size() - 1).getAsJsonObject()).get("name").getAsString();
            }
            return name.replaceAll("\\W", "");
        } catch (Exception e) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Failed to get player-name", e);
        }
        throw new UnknownPlayerException("player_uuid is not valid");
    }

    public static @NotNull UUID getPlayerUniqueId(@NotNull String playerName) {
        try {
            URL url = URI.create("https://api.mojang.com/users/profiles/minecraft/" + playerName).toURL();
            JsonObject json = new GsonBuilder().create().fromJson(new InputStreamReader(url.openStream()), JsonObject.class);
            if (json != null) return fromUndashed(json.get("id").toString());
        } catch (Exception e) {
            CiklesMC.getInstance().getSLF4JLogger().warn("Failed to get player-uuid", e);
        }
        return fromUndashed(playerName.replace("-", ""));
    }

    public static UUID fromUndashed(@NotNull String string) {
        return new UUID(Long.parseUnsignedLong(string.substring(0, 16), 16), Long.parseUnsignedLong(string.substring(16), 16));
    }

    private static class UnknownPlayerException extends RuntimeException {
        private UnknownPlayerException(String message) {
            super(message);
        }
    }
}
