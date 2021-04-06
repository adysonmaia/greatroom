package br.ufc.great.greatroom.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.altbeacon.beacon.Beacon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import br.ufc.great.greatroom.model.BeaconLocationModel;
import br.ufc.great.greatroom.model.FileModel;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.model.LocationModel;
import br.ufc.great.greatroom.model.ObjectModel;
import br.ufc.great.greatroom.model.PersonModel;
import cz.msebera.android.httpclient.Header;

/**
 * Created by adyson on 05/10/15.
 */
public class ServerApi {
    private static final String TAG = ServerApi.class.getSimpleName();
    private static final String PARAM_SUCCESS = "success";
    private static final String PARAM_RESPONSE = "response";
    private static final String PARAM_ERROR_MESSAGE = "message";

    private final RequestQueue requestQueue;
    private final String url;
    private Context context;
    private AsyncHttpClient asyncHttpClient;

    public ServerApi(Context context) {
        url = "http://great-room.appspot.com/api/";
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
        asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.setEnableRedirects(true);
        asyncHttpClient.setTimeout(20 * 1000);
        asyncHttpClient.setConnectTimeout(20 * 1000);
        asyncHttpClient.setResponseTimeout(20 * 1000);
    }

    public void findGroupsNearby(final List<LocationModel> locations, final GroupsCallback callback) {
        final long startTime = System.currentTimeMillis();
        JSONObject params = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            for (LocationModel locationModel : locations) {
                jsonArray.put(locationModel.toJson());
            }
            params.put("locations", jsonArray);
        } catch (JSONException e) {
        }
        request("groups/nearby", params, new Callback() {
            @Override
            public void onSuccessResponse(Object response) {
                if (callback != null) {
                    List<GroupModel> groups = new ArrayList<>();
                    if (response instanceof JSONArray) {
                        JSONArray json = (JSONArray) response;
                        for (int i = 0; i < json.length(); i++) {
                            try {
                                groups.add(new GroupModel(json.getJSONObject(i)));
                            } catch (JSONException e) {
                            }
                        }
                    }
                    LogFileHelper.success(TAG, "findGroupsNearby;locations;" + locations.size() + ";groups;" + groups.size(), startTime);
                    callback.onSuccessResponse(groups);
                }
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                if (callback != null)
                    callback.onSuccessResponse(new ArrayList<GroupModel>());
            }
        });
    }

    public void findGroupsNearby(final Collection<Beacon> beacons, final GroupsCallback callback) {
        List<LocationModel> locations = new ArrayList<>();
        if (beacons != null && !beacons.isEmpty()) {
            Iterator<Beacon> it = beacons.iterator();
            while (it.hasNext()) {
                locations.add(new BeaconLocationModel(it.next()));
            }
        }
        findGroupsNearby(locations, callback);
    }

    public void doCheckIn(final GroupModel group, final PersonModel user) {
        doCheckIn(group, user, null);
    }

    public void doCheckIn(final GroupModel group, final PersonModel user, final CheckInCallback callback) {
        doCheckIn(group, Arrays.asList((ObjectModel) user), callback);
    }

    public void doCheckIn(final GroupModel group, final List<ObjectModel> objects, final CheckInCallback callback) {
        JSONObject params = new JSONObject();
        try {
            params.put("group", group.toJson());
            params.put("objects", JsonHelper.listObjectModelToJson(objects));
        } catch (JSONException e) {
        }
        request("group/checkin", params, new Callback() {
            @Override
            public void onSuccessResponse(Object response) {
                if (callback != null)
                    callback.onSuccessResponse();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                if (callback != null)
                    callback.onErrorResponse(errorMessage);
            }
        });
    }

    public void getCurrentPersons(final GroupModel group, final PersonsCallback callback) {
        getCurrentObjects(group, PersonModel.OBJECT_TYPE, new ObjectsCallback() {
            @Override
            public void onSuccessResponse(List<ObjectModel> list) {
                if (callback != null) {
                    List<PersonModel> listPersons = new ArrayList<>();
                    for (ObjectModel objectModel : list) {
                        if (objectModel instanceof PersonModel)
                            listPersons.add((PersonModel) objectModel);
                    }
                    callback.onSuccessResponse(listPersons);
                }
            }
        });
    }

    public void getCurrentObjects(final GroupModel group, final String objectType, final ObjectsCallback callback) {
        JSONObject params = new JSONObject();
        try {
            params.put("group", group.toJson());
            if (objectType != null && !objectType.isEmpty())
                params.put("type", objectType);
        } catch (JSONException e) {
        }
        request("group/objects/current", params, new Callback() {
            @Override
            public void onSuccessResponse(Object response) {
                if (callback != null) {
                    List<ObjectModel> objects = new ArrayList<>();
                    if (response instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) response;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (jsonObject != null) {
                                    String type = jsonObject.optString("type", "");
                                    if (PersonModel.OBJECT_TYPE.equals(type)) {
                                        objects.add(new PersonModel(jsonObject));
                                    }
                                }
                            } catch (JSONException e) {
                            }
                        }
                    }
                    callback.onSuccessResponse(objects);
                }
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                if (callback != null)
                    callback.onSuccessResponse(new ArrayList<ObjectModel>());
            }
        });
    }

    public void getFiles(final GroupModel group, final FilesCallback callback) {
        JSONObject params = new JSONObject();
        try {
            params.put("group", group.toJson());
        } catch (JSONException e) {
        }
        request("group/files", params, new Callback() {
            @Override
            public void onSuccessResponse(Object response) {
                if (callback != null) {
                    List<FileModel> list = new ArrayList<>();
                    if (response instanceof JSONArray) {
                        JSONArray jsonArray = (JSONArray) response;
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                if (jsonObject != null) {
                                    list.add(new FileModel(jsonObject));
                                }
                            } catch (JSONException e) {
                            }
                        }
                    }
                    callback.onSuccessResponse(list);
                }
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                if (callback != null)
                    callback.onSuccessResponse(new ArrayList<FileModel>());
            }
        });
    }

    public void deleteFile(final GroupModel group, final FileModel file, final FileDeleteCallback callback) {
        deleteFiles(group, Arrays.asList(file), callback);
    }

    public void deleteFiles(final GroupModel group, final List<FileModel> files, final FileDeleteCallback callback) {
        JSONObject params = new JSONObject();
        try {
            params.put("group", group.toJson());
            params.put("files", JsonHelper.listFileModelToJson(files));
        } catch (JSONException e) {
        }
        request("group/files/delete", params, new Callback() {
            @Override
            public void onSuccessResponse(Object response) {
                if (callback != null)
                    callback.onSuccessResponse();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                if (callback != null)
                    callback.onErrorResponse(errorMessage);
            }
        });
    }

    public FileModel uploadFile(final GroupModel group, final Uri uri, final FileUploadCallback callback) {
        final long startTime = System.currentTimeMillis();
        FileModel inputFileModel = null;
        try {
            final String method = "group/file/upload/";
            final String uploadUrl = url + method + group.getId();
            String fileName = "";
            String contentType = "";
            String[] projection = {MediaStore.Files.FileColumns.DISPLAY_NAME, MediaStore.Files.FileColumns.MIME_TYPE};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                int nameColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);
                int typeColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);
                cursor.moveToFirst();
                fileName = cursor.getString(nameColumnIndex);
                contentType = cursor.getString(typeColumnIndex);
            }
            if (cursor != null) {
                cursor.close();
            }
            inputFileModel = new FileModel();
            inputFileModel.setName(fileName);
            inputFileModel.setContentType(contentType);

            InputStream inputStream = context.getContentResolver().openInputStream(uri);

            final FileModel tmpFile = new FileModel();
            tmpFile.setName(fileName);
            tmpFile.setContentType(contentType);

            RequestParams params = new RequestParams();
            params.put("file", inputStream, fileName, contentType, false);
//            asyncHttpClient.setLoggingEnabled(true);
//            asyncHttpClient.setLoggingLevel(LogInterface.DEBUG);
            asyncHttpClient.post(context, uploadUrl, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (null == callback)
                        return;
                    try {
                        boolean success = response.optBoolean(PARAM_SUCCESS, false);
                        if (!success || !response.has(PARAM_RESPONSE)) {
                            String errorMessage = response.optString(PARAM_ERROR_MESSAGE, "Request failed");
                            throw new Exception(errorMessage);
                        }
                        Object responseObject = response.get(PARAM_RESPONSE);
                        if (responseObject != null && responseObject instanceof JSONObject) {
                            FileModel fileModel = new FileModel((JSONObject) responseObject);

                            doSuccessLog(method, startTime);
                            callback.onSuccessResponse(fileModel);
                        } else {
                            throw new Exception("Request failed");
                        }
                    } catch (Exception e) {
                        doErrorLog(method, startTime, e.getMessage());
                        callback.onErrorResponse(e.getMessage());
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable error, JSONObject errorResponse) {
                    String errorMessage = "Request failed";
                    if (errorResponse != null) {
                        try {
                            if (errorResponse.has(PARAM_ERROR_MESSAGE)) {
                                errorMessage = errorResponse.optString(PARAM_ERROR_MESSAGE, errorMessage);
                            } else if (error != null) {
                                errorMessage = error.getMessage();
                            }
                        } catch (Exception e) {
                            errorMessage = e.getMessage();
                        }
                    }

                    doErrorLog(method, startTime, errorMessage);
                    if (callback != null) {
                        callback.onErrorResponse(errorMessage);
                    }
                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    if (callback != null)
                        callback.onProgressResponse((float) totalSize / bytesWritten);
                }
            });
        } catch (Exception e) {
            if (callback != null)
                callback.onErrorResponse(e.getMessage());
        }

        return inputFileModel;
    }

    protected void request(final String method, final JSONObject params, final Callback callback) {
        final long startTime = System.currentTimeMillis();
        String path = url + method;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, path, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        long stopTime = System.currentTimeMillis();
                        long elapsedTime = stopTime - startTime;

                        if (null == callback)
                            return;
                        try {
                            boolean success = response.optBoolean(PARAM_SUCCESS, false);
                            if (!success || !response.has(PARAM_RESPONSE)) {
                                String errorMessage = response.optString(PARAM_ERROR_MESSAGE, "Request failed");
                                throw new Exception(errorMessage);
                            }
                            doSuccessLog(method, startTime);
                            callback.onSuccessResponse(response.get(PARAM_RESPONSE));
                        } catch (Exception e) {
                            doErrorLog(method, startTime, e.getMessage());
                            callback.onErrorResponse(e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, "onErrorResponse " + method, error);
                        doErrorLog(method, startTime, error.getMessage());
                        if (null != callback)
                            callback.onErrorResponse(error.getMessage());
                    }
                }
        ) {

        };
        requestQueue.add(request);
    }

    protected void doSuccessLog(String method, long startTime) {
        LogFileHelper.success(TAG, method, startTime);
    }

    protected void doErrorLog(String method, long startTime, String errorMessage) {
        LogFileHelper.error(TAG, method, startTime, errorMessage);
    }

    protected interface Callback {
        void onSuccessResponse(Object response);

        void onErrorResponse(String errorMessage);
    }

    public interface GroupsCallback {
        void onSuccessResponse(List<GroupModel> list);
    }

    public interface ObjectsCallback {
        void onSuccessResponse(List<ObjectModel> list);
    }

    public interface PersonsCallback {
        void onSuccessResponse(List<PersonModel> list);
    }

    public interface FilesCallback {
        void onSuccessResponse(List<FileModel> list);
    }

    public interface FileUploadCallback {
        void onSuccessResponse(FileModel file);

        void onErrorResponse(String errorMessage);

        void onProgressResponse(float progress);
    }

    public interface FileDeleteCallback {
        void onSuccessResponse();

        void onErrorResponse(String errorMessage);
    }

    public interface CheckInCallback {
        void onSuccessResponse();

        void onErrorResponse(String errorMessage);
    }
}
