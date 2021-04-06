package br.ufc.great.greatroom.view.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.AppController;
import br.ufc.great.greatroom.controller.GroupController;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.util.AndroidHelper;
import br.ufc.great.greatroom.util.ServerApi;

public class GroupActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    public static final String GROUP_PARAM = "group_data";
    private static final int ITEM_INDEX_PERSONS = 0;
    private static final int ITEM_INDEX_FILES = 1;

    private FloatingActionButton fab;
    private GroupModel group;
    private ProgressDialog progressDialog;
    private AppController appController;
    private GroupController groupController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        appController = AppController.getInstance();
        groupController = GroupController.getInstance();

        String[] items = getResources().getStringArray(R.array.group_content);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, items);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Fazendo Check-in");
        progressDialog.setMessage(group.getName());
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doCheckIn();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = null;
        switch (position) {
            case ITEM_INDEX_PERSONS:
                intent = new Intent(this, PersonsActivity.class);
                intent.putExtra(PersonsActivity.GROUP_PARAM, group.toJson().toString());
                break;
            case ITEM_INDEX_FILES:
                intent = new Intent(this, FilesActivity.class);
                intent.putExtra(FilesActivity.GROUP_PARAM, group.toJson().toString());
                break;
        }
        if (intent != null)
            startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public int getResourceColor(int id) {
        if (AndroidHelper.isMinMarshmallow())
            return getResources().getColor(id, null);
        else
            return getResources().getColor(id);
    }

    private void updateUi() {
        if (appController.getUser() != null) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }

        if (groupController.hasGroupChecked(group)) {
            int color = getResourceColor(R.color.colorPrimary);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
        } else {
            int color = getResourceColor(R.color.colorAccent);
            fab.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    private void doCheckIn() {
        progressDialog.show();
        groupController.doCheckIn(group, new ServerApi.CheckInCallback() {
            @Override
            public void onSuccessResponse() {
                Toast.makeText(GroupActivity.this, "Check-in realizado", Toast.LENGTH_SHORT).show();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        updateUi();
                    }
                });
            }

            @Override
            public void onErrorResponse(String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        updateUi();
                    }
                });
            }
        });
    }
}
