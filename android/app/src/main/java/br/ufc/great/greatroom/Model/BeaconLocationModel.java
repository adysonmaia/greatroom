package br.ufc.great.greatroom.model;

import org.altbeacon.beacon.Beacon;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adyson on 05/10/15.
 */
public class BeaconLocationModel implements LocationModel {
    Beacon beacon;
    String uuid;
    double distance;

    public BeaconLocationModel(Beacon beacon) {
        this.beacon = beacon;
        this.uuid = beacon.getId1().toString();
        this.distance = beacon.getDistance();
    }

    public BeaconLocationModel(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public String getType() {
        return "ibeacon";
    }

    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("type", getType().toLowerCase());
            json.put("uuid", getUuid());
            json.put("distance", getDistance());
        } catch (JSONException e) {

        }
        return json;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }
}
