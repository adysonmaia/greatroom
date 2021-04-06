package br.ufc.great.greatroom.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import br.ufc.great.greatroom.model.FileModel;
import br.ufc.great.greatroom.model.ObjectModel;

/**
 * Created by adyson on 05/10/15.
 */
public class JsonHelper {

    static public JSONObject mapToJson(Map<String, Object> map) {
        JSONObject json = null;
        if (null != map && map.size() > 0) {
            json = new JSONObject();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                try {
                    if (value instanceof Map) {
                        JSONObject valueJson = JsonHelper.mapToJson((Map<String, Object>) value);
                        json.put(key, valueJson);
                    } else if (value instanceof List) {
                        JSONArray valueJson = JsonHelper.listToJson((List<Object>) value);
                        json.put(key, valueJson);
                    } else {
                        json.put(key, value);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return json;
    }

    static public JSONArray listToJson(List<Object> list) {
        JSONArray json = new JSONArray();
        for (Object value : list) {
            if (value instanceof Map) {
                JSONObject valueJson = JsonHelper.mapToJson((Map<String, Object>) value);
                json.put(valueJson);
            } else if (value instanceof List) {
                JSONArray valueJson = JsonHelper.listToJson((List<Object>) value);
                json.put(valueJson);
            } else {
                json.put(value);
            }
        }
        return json;
    }

    static public JSONArray listObjectModelToJson(List<ObjectModel> list) {
        JSONArray json = new JSONArray();
        for (ObjectModel objectModel : list) {
            if (objectModel != null)
                json.put(objectModel.toJson());
        }
        return json;
    }

    static public JSONArray listFileModelToJson(List<FileModel> list) {
        JSONArray json = new JSONArray();
        for (FileModel fileModel : list) {
            if (fileModel != null)
                json.put(fileModel.toJson());
        }
        return json;
    }
}
