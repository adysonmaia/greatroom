package br.ufc.great.greatroom.view.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import org.json.JSONObject;

import java.util.List;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.PubSubController;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.model.PersonModel;
import br.ufc.great.greatroom.util.ServerApi;
import br.ufc.great.greatroom.view.adapter.PersonsAdapter;

/**
 * Created by belmondorodrigues on 26/10/2015.
 */
public class PersonsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final String GROUP_PARAM = "group_data";

    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView;
    private PersonsAdapter adapter;
    private ServerApi serverApi;
    private GroupModel group;
    private PubSubController pubSubController;
    private BroadcastReceiver objectsChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persons);

        try {
            Intent intent = getIntent();
            Bundle data = intent.getExtras();
            String json = data.getString(GROUP_PARAM);
            group = new GroupModel(new JSONObject(json));
        } catch (Exception e) {
            finish();
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(group.getName());
        }

        pubSubController = PubSubController.getInstance();
        objectsChangedListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                findPersons();
            }
        };

        serverApi = new ServerApi(getApplicationContext());
        loadingView = findViewById(R.id.loading);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        adapter = new PersonsAdapter(this);
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pubSubController.subscribeToGroupEvent(PubSubController.EVENT_GROUP_OBJECTS_CHANGED,
                group, objectsChangedListener);
        findPersons();
    }

    @Override
    protected void onPause() {
        pubSubController.unsubscribeToGroupEvent(PubSubController.EVENT_GROUP_OBJECTS_CHANGED,
                group, objectsChangedListener);
        super.onPause();
    }

    @Override
    public void onRefresh() {
        findPersons();
    }

    public void findPersons() {
        if (!swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(true);
        loadingView.setVisibility(View.VISIBLE);

        serverApi.getCurrentPersons(group, new ServerApi.PersonsCallback() {
            public void onSuccessResponse(final List<PersonModel> list) {
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        loadingView.setVisibility(View.GONE);
                        adapter.updateEntries(list);
                    }
                });
            }
        });
    }
}
