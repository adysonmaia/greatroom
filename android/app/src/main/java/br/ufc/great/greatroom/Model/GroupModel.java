package br.ufc.great.greatroom.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adyson on 05/10/15.
 */
public class GroupModel {
    private int id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean checked;
    private double nearbyFactor;

    public GroupModel() {

    }

    public GroupModel(int id, String name, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.checked = false;
        this.nearbyFactor = 0.0;
    }

    public GroupModel(JSONObject json) {
        id = json.optInt("id", 0);
        name = json.optString("name", "");
        description = json.optString("description", "");
        imageUrl = json.optString("image_url", "");
        nearbyFactor = json.optDouble("nearby", 0.0);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("image_url", imageUrl);
            json.put("nearby", nearbyFactor);
        } catch (JSONException e) {
        }
        return json;
    }

    public boolean equals(GroupModel group) {
        return (group != null && id == group.id);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public double getNearbyFactor() {
        return nearbyFactor;
    }

    public void setNearbyFactor(double nearbyFactor) {
        this.nearbyFactor = nearbyFactor;
    }
}
