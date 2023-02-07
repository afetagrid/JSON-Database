package server;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Database {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    private final Gson gson = new Gson();
    private final String databasePath;

    public Database(String path) {
        this.databasePath = path;
    }

    private Map<String, String> fileJsonToMap() {
        try {
            readLock.lock();
            String json = new String(Files.readAllBytes(Path.of(databasePath)));
            readLock.unlock();
            if (!json.equals("")) {
                Type typeOfHashMap = new TypeToken<Map<String, String>>(){}.getType();
                return gson.fromJson(json, typeOfHashMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<>();
    }
    private void writeToFileMappedJson(Map<String, String> mappedJson) {
        String json = gson.toJson(mappedJson);
        try (FileWriter fileWriter = new FileWriter(databasePath)) {
            writeLock.lock();
            fileWriter.write(json);
            writeLock.unlock();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean set(JsonElement key, JsonElement value) {
        Map<String, String> mappedJson = fileJsonToMap();
        if (key.isJsonPrimitive()) {
            mappedJson.put(key.getAsString(), value.toString());
        } else {
            List<String> composedKey = List.of(key.toString().replaceAll("[\\[\\]\"]", "").split(","));
            JsonObject jsonObject = JsonParser.parseString(mappedJson.get(composedKey.get(0))).getAsJsonObject();
            JsonObject insideJson = jsonObject;
            for (String s : composedKey.subList(1, composedKey.size() - 1)) {
                insideJson = insideJson.get(s).getAsJsonObject();
            }
            if (value.isJsonPrimitive()) {
                insideJson.addProperty(composedKey.get(composedKey.size() - 1), value.getAsString());
            } else {
                insideJson.add(composedKey.get(composedKey.size() - 1), value);
            }
            mappedJson.put(composedKey.get(0), jsonObject.toString());
        }
        writeToFileMappedJson(mappedJson);
        return true;
    }
    public String get(JsonElement key) {
        Map<String, String> mappedJson = fileJsonToMap();
        List<String> composedKey = List.of(key.toString().replaceAll("[\\[\\]\"]", "").split(","));
        try {
            JsonObject jsonObject = JsonParser.parseString(mappedJson.get(composedKey.get(0))).getAsJsonObject();
            JsonElement jsonToReturn = null;
            for (String s : composedKey.subList(1, composedKey.size())) {
                if (jsonObject.get(s) == null) {
                    return null;
                }
                if (jsonObject.get(s).isJsonPrimitive()) {
                    jsonToReturn = jsonObject.get(s);
                } else {
                    jsonObject = jsonObject.get(s).getAsJsonObject();
                    jsonToReturn = jsonObject;
                }
            }
            if (jsonToReturn == null) {
                jsonToReturn = jsonObject;
            }
            return gson.toJson(jsonToReturn);
        } catch (Exception e) {
            return null;
        }
    }
    public boolean delete(JsonElement key) {
        Map<String, String> mappedJson = fileJsonToMap();
        if (key.isJsonPrimitive()) {
            if (mappedJson.containsKey(key.getAsString())) {
                mappedJson.remove(key.getAsString());
                writeToFileMappedJson(mappedJson);
                return true;
            }
        } else {
            List<String> composedKey = List.of(key.toString().replaceAll("[\\[\\]\"]", "").split(","));
            JsonObject jsonObject = JsonParser.parseString(mappedJson.get(composedKey.get(0))).getAsJsonObject();
            JsonObject insideJson = jsonObject;
            for (String s : composedKey.subList(1, composedKey.size() - 1)) {
                insideJson = insideJson.get(s).getAsJsonObject();
            }
            if (insideJson.get(composedKey.get(composedKey.size() - 1)) != null) {
                insideJson.remove(composedKey.get(composedKey.size() - 1));
                mappedJson.put(composedKey.get(0), jsonObject.toString());
                writeToFileMappedJson(mappedJson);
                return true;
            }
        }
        return false;
    }
}
