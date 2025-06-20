package dev.puzzleshq.CRArchiveBot.old.utils;

import org.hjson.JsonValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HJsonUtils {

    public static Object convertJsonValue(JsonValue value) {
        if (value.isObject()) {
            Map<String, Object> map = new LinkedHashMap<>();
            for (String name : value.asObject().names()) {
                map.put(name, convertJsonValue(value.asObject().get(name)));
            }
            return map;
        } else if (value.isArray()) {
            List<Object> list = new ArrayList<>();
            for (JsonValue item : value.asArray()) {
                list.add(convertJsonValue(item));
            }
            return list;
        } else if (value.isNull()) {
            return null;
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.asInt();
        } else {
            return value.asString();
        }
    }

}
