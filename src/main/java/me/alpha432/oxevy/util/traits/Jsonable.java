package me.alpha432.oxevy.util.traits;

import com.google.gson.JsonElement;

public interface Jsonable {
    JsonElement toJson();

    void fromJson(JsonElement element);

    default String getFileName() {
        return "";
    }
}
