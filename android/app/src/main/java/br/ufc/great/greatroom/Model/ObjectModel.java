package br.ufc.great.greatroom.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adyson on 22/10/15.
 */
public class ObjectModel {
    private int id;
    private String uuid;
    private String type;

    public ObjectModel() {

    }

    public ObjectModel(JSONObject json) {
        id = json.optInt("id", 0);
        uuid = json.optString("uuid", "");
        type = json.optString("type", "");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            if (id > 0)
                json.put("id", id);
            if (uuid != null && !uuid.isEmpty())
                json.put("uuid", uuid);
            if (type != null && !type.isEmpty())
                json.put("type", type);
        } catch (JSONException e) {
        }

        return json;
    }

    public void fromJson(JSONObject json) {
        id = json.optInt("id", 0);
        uuid = json.optString("uuid", "");
        type = json.optString("type", "");
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
