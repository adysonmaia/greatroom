package br.ufc.great.greatroom.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import br.ufc.great.greatroom.util.DateHelper;

/**
 * Created by adyson on 05/10/15.
 */
public class PersonModel extends ObjectModel {
    public static final String OBJECT_TYPE = "PERSON";

    private String name;
    private String email;
    private String imageUrl;
    private Date lastCheckIn;

    public PersonModel() {
        setType(OBJECT_TYPE);
    }

    public PersonModel(JSONObject json) {
        super(json);
        name = json.optString("name", "");
        email = json.optString("email", "");
        imageUrl = json.optString("image_url", "");
        if (json.has("checkin_date")) {
            lastCheckIn = DateHelper.fromRFC2822(json.optString("checkin_date", ""));
        } else {
            lastCheckIn = null;
        }
        setType(OBJECT_TYPE);
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = super.toJson();
        try {
            json.put("name", name);
            json.put("email", email);
            json.put("image_url", imageUrl);
        } catch (JSONException e) {
        }

        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getLastCheckInDate() {
        return lastCheckIn;
    }

    public void setLastCheckInDate(Date lastCheckIn) {
        this.lastCheckIn = lastCheckIn;
    }
}
