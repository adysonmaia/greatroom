package br.ufc.great.greatroom.model;

import org.json.JSONObject;

/**
 * Created by adyson on 05/10/15.
 */
public interface LocationModel {
    JSONObject toJson();

    String getType();
}
