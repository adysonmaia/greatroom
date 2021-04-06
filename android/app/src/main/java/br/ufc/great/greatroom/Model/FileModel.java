package br.ufc.great.greatroom.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by adyson on 15/11/15.
 */
public class FileModel {
    private String name;
    private String extension;
    private String contentType;
    private String url;
    private String localPath;
    private long size;

    public FileModel() {

    }

    public FileModel(JSONObject json) {
        name = json.optString("name");
        extension = json.optString("extension");
        contentType = json.optString("type");
        url = json.optString("url");
        size = json.optLong("size");
    }


    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("name", name);
            json.put("extension", extension);
            json.put("type", contentType);
            json.put("url", url);
            json.put("size", size);
        } catch (JSONException e) {
        }
        return json;
    }

    public File getLocalFile() {
        if (localPath == null || localPath.isEmpty())
            return null;
        else
            return new File(localPath);
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

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
