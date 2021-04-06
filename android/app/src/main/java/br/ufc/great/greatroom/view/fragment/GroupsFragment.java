package br.ufc.great.greatroom.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.GroupController;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.view.activity.GroupActivity;
import br.ufc.great.greatroom.view.adapter.GroupsAdapter;


/**
 * Created by belmondorodrigues on 22/09/2015.
 */
public class GroupsFragment extends ListFragment {

    private GroupController groupController;
    private GroupsAdapter listAdapter;
    private View loadingView;
    private BroadcastReceiver groupsChangedListener;

    public GroupsFragment() {
        // Required empty public constructor
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        loadingView = view.findViewById(R.id.loading);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listAdapter = new GroupsAdapter(getActivity());
        groupController = GroupController.getInstance();

        ListView listView = getListView();
        listView.setAdapter(listAdapter);

        groupsChangedListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadGroups();
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        loadGroups();
    }

    @Override
    public void onResume() {
        super.onResume();
        groupController.registerEventReceiver(GroupController.EVENT_GROUPS_CHANGED, groupsChangedListener);
        groupController.registerEventReceiver(GroupController.EVENT_GROUP_CHECKED, groupsChangedListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        groupController.unregisterEventReceiver(groupsChangedListener);
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent(getActivity().getApplicationContext(), GroupActivity.class);
        GroupModel groupModel = (GroupModel) listAdapter.getItem(position);
        intent.putExtra(GroupActivity.GROUP_PARAM, groupModel.toJson().toString());
        startActivity(intent);
    }

    private void loadGroups() {
        List<GroupModel> list = groupController.getGroups();

        // TODO teste, apagar depois
        /*if (list.isEmpty()) {
            GroupModel groupModel = new GroupModel();
            groupModel.setId(2);
            groupModel.setName("GREat Room");
            groupModel.setImageUrl("https://storage.googleapis.com/greatroom/images/great.png");
            list.add(groupModel);

            groupModel = new GroupModel();
            groupModel.setId(1);
            groupModel.setName("Teste");
            groupModel.setImageUrl("");
            list.add(groupModel);
        }*/

        if (list.isEmpty())
            loadingView.setVisibility(View.VISIBLE);
        else
            loadingView.setVisibility(View.GONE);
        listAdapter.updateEntries(list);
    }
}