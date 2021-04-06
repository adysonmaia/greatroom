package br.ufc.great.greatroom.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.model.PersonModel;
import br.ufc.great.greatroom.util.LogFileHelper;
import br.ufc.great.greatroom.util.ServerApi;

/**
 * Created by adyson on 30/11/15.
 */
public class GroupController {
    private static final String TAG = GroupController.class.getSimpleName();
    private static final String EVENT_PREFIX = GroupController.class.getCanonicalName() + ".";
    public static final String EVENT_GROUPS_CHANGED = EVENT_PREFIX + "groups_changed";
    public static final String EVENT_GROUP_CHECKED = EVENT_PREFIX + "group_checked";
    private static GroupController instance = null;

    private Context context;
    private AppController appController;
    private ServerApi serverApi;
    private LocalBroadcastManager localBroadcastManager;
    private GroupModel checkedGroup;
    private List<GroupModel> groups;

    public GroupController() {
        appController = AppController.getInstance();
        context = appController.getApplicationContext();
        localBroadcastManager = LocalBroadcastManager.getInstance(context);
        serverApi = new ServerApi(context);
        groups = new ArrayList<>();
        checkedGroup = null;
    }

    public static GroupController getInstance() {
        if (null == instance)
            instance = new GroupController();
        return instance;
    }

    public void registerEventReceiver(String event, BroadcastReceiver broadcastReceiver) {
        localBroadcastManager.registerReceiver(broadcastReceiver, new IntentFilter(event));
    }

    public void unregisterEventReceiver(BroadcastReceiver broadcastReceiver) {
        localBroadcastManager.unregisterReceiver(broadcastReceiver);
    }

    public boolean hasGroupChecked(GroupModel groupModel) {
        return checkedGroup != null && checkedGroup.equals(groupModel);
    }

    public void doCheckIn(final GroupModel group) {
        doCheckIn(group, null);
    }

    public void doCheckIn(final GroupModel group, final ServerApi.CheckInCallback callback) {
        PersonModel user = appController.getUser();
        if (user == null) {
            group.setChecked(false);
            checkedGroup = null;
            if (callback != null)
                callback.onErrorResponse("Usuário não conectado");
            return;
        }

        serverApi.doCheckIn(group, user, new ServerApi.CheckInCallback() {
            @Override
            public void onSuccessResponse() {
                checkedGroup = group;
                checkedGroup.setChecked(true);
                localBroadcastManager.sendBroadcast(new Intent(EVENT_GROUP_CHECKED));
                if (callback != null)
                    callback.onSuccessResponse();
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                group.setChecked(false);
                checkedGroup = null;
                if (callback != null)
                    callback.onErrorResponse(errorMessage);
            }
        });
    }

    public void setGroups(List<GroupModel> groups) {
        final long startTime = System.currentTimeMillis();
        this.groups.clear();
        this.groups.addAll(groups);

        GroupModel currentGroup = null;
        double maxNearby = 0.0;
        for (GroupModel groupModel : groups) {
            if (groupModel.getNearbyFactor() >= maxNearby) {
                maxNearby = groupModel.getNearbyFactor();
                currentGroup = groupModel;
            }
        }
        final int groupId = (currentGroup != null) ? currentGroup.getId() : 0;
        if (currentGroup != null) {
            doCheckIn(currentGroup, new ServerApi.CheckInCallback() {
                @Override
                public void onSuccessResponse() {
                    LogFileHelper.success(TAG, "setGroups-doCheckIn;"+groupId, startTime);
                }

                @Override
                public void onErrorResponse(String errorMessage) {
                    LogFileHelper.error(TAG, "setGroups-doCheckIn;"+groupId, startTime, errorMessage);
                }
            });
        }

        localBroadcastManager.sendBroadcast(new Intent(EVENT_GROUPS_CHANGED));
    }

    public List<GroupModel> getGroups() {
        return groups;
    }

    public GroupModel getCheckedGroup() {
        return checkedGroup;
    }
}
