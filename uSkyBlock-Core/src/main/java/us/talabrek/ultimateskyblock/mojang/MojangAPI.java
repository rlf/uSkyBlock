/**
 * Features include:
 * <pre>
 *     * Mojang API auto-throttling when receiving HTTP 429 (TOO MANY REQUESTS)
 *     * Incremental consumer strategy (consume each response, instead of blocking for everything).
 * </pre>
 *
 * Inspired by https://gist.github.com/evilmidget38/26d70114b834f71fb3b4
 */
package us.talabrek.ultimateskyblock.mojang;


import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import dk.lockfuglsang.minecraft.util.TimeUtil;
import us.talabrek.ultimateskyblock.util.UUIDUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static dk.lockfuglsang.minecraft.po.I18nUtil.tr;

public class MojangAPI {
    private static final Logger log = Logger.getLogger(MojangAPI.class.getName());
    private static final int PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private static final String NAME_URL = "https://api.mojang.com/users/profiles/minecraft/{0}?at=0";
    private static final int MAX_RETRIES = 3;
    // Mojang API has a cap at 600 requests per 10 minutes (1 per second).
    private static final long THROTTLE_SUCCESS = 200L;
    private static final long THROTTLE_CONSERVATIVE = 1000L;
    private static final long THROTTLE_FAILURE = 3000L;
    private static final int BAD_REQUEST = 400;
    private static final int TOO_MANY_REQUESTS = 429;
    private static final long FAILURE_WINDOW = 60000;

    private static long tStart = 0;
    private static long numRequests = 0;

    private final JSONParser jsonParser = new JSONParser();

    private int failuresInRow = 0;
    private long lastFailure = 0;

    public MojangAPI() {
    }

    public void fetchUUIDs(List<String> names, NameUUIDConsumer consumer, ProgressCallback callback) {
        if (consumer == null) {
            throw new IllegalArgumentException("consumer cannot be null");
        }
        if (isOnlineMode()) {
            try {
                fetchCurrent(names, consumer, callback);
                callback.complete(true);
            } catch (Exception e) {
                callback.error("" + e);
                callback.complete(false);
            }
        } else {
            fetchOfflineMode(names, consumer, callback);
        }
    }

    private boolean isOnlineMode() {
        return Bukkit.getOnlineMode();
    }

    private void fetchOfflineMode(List<String> names, NameUUIDConsumer consumer, ProgressCallback callback) {
        int failed = 0;
        int success = 0;
        int total = names.size();
        for (String name : names) {
            //noinspection deprecation
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            Map<String, UUID> map = new HashMap<>();
            map.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
            consumer.success(map);
            success++;
            if (callback != null) {
                callback.progress(success+failed, failed, total, "OfflineMode");
            }
        }
        if (callback != null) {
            callback.complete(true);
        }
    }

    private void fetchCurrent(List<String> names, NameUUIDConsumer consumer, ProgressCallback callback) throws Exception {
        int requests = (int) Math.ceil(names.size() / PROFILES_PER_REQUEST);
        int failed = 0;
        for (int i = 0; i < requests; i++) {
            int fromIndex = i * PROFILES_PER_REQUEST;
            int toIndex = Math.min((i + 1) * 100, names.size());
            List<String> segment = names.subList(fromIndex, toIndex);
            HttpURLConnection connection = createPostConnection();
            String body = JSONArray.toJSONString(segment);
            writeBody(connection, body);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Map<String, UUID> tempMap = new HashMap<>();
                JSONArray array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                for (Object profile : array) {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = getUUID(id);
                    tempMap.put(name, uuid);
                }
                int missing = segment.size() - tempMap.size();
                consumer.success(tempMap);
                if (missing > 0) {
                    List<String> unknown = new ArrayList<>(segment);
                    unknown.removeAll(tempMap.keySet());
                    consumer.unknown(unknown);
                }
                if (callback != null) {
                    failed += missing;
                    callback.progress(toIndex, failed, names.size(), "Online");
                }
            } else if (responseCode == TOO_MANY_REQUESTS) {
                i--;
            } else if (responseCode == BAD_REQUEST) {
                if (callback != null) {
                    callback.error(connection.getResponseMessage());
                }
                if (consumer != null) {
                    consumer.unknown(segment);
                }
                log.warning(connection.getResponseMessage() + "\nBODY:\n" + body);
            } else {
                i--; // retry segment
                if (callback != null) {
                    callback.error(connection.getResponseMessage());
                }
            }
            throttle(responseCode, i != requests - 1, callback);
        }
    }

    private void throttle(int responseCode, boolean hasMore, ProgressCallback callback) throws InterruptedException {
        if (!hasMore) {
            return;
        }
        long now = System.currentTimeMillis();
        if (responseCode == TOO_MANY_REQUESTS) {
            lastFailure = now;
            failuresInRow++;
            long throttle = THROTTLE_FAILURE*failuresInRow;
            callback.error(tr("Too many requests for Mojangs API ({0} within {1}), sleeping {2}",
                    numRequests,
                    TimeUtil.millisAsString(now -tStart),
                    TimeUtil.millisAsShort(throttle)));
            Thread.sleep(throttle);
        } else {
            if (lastFailure > now - FAILURE_WINDOW) {
                Thread.sleep(THROTTLE_CONSERVATIVE);
            } else {
                Thread.sleep(THROTTLE_SUCCESS);
            }
            failuresInRow = 0;
        }
    }

    private static HttpURLConnection createGetConnection(String name) throws IOException {
        URL url = new URL(MessageFormat.format(NAME_URL, name));
        stat();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(false);
        return connection;
    }

    private static HttpURLConnection createPostConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        stat();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }

    private static void stat() {
        if (tStart == 0) {
            tStart = System.currentTimeMillis();
        }
        numRequests++;
    }

    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes("UTF-8"));
        stream.flush();
        stream.close();
    }

    private static UUID getUUID(String id) {
        return UUIDUtil.fromString(id);
    }
}