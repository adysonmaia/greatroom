package br.ufc.great.greatroom.view.activity;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.PubSubController;
import br.ufc.great.greatroom.model.FileModel;
import br.ufc.great.greatroom.model.GroupModel;
import br.ufc.great.greatroom.util.Downloader;
import br.ufc.great.greatroom.util.ServerApi;
import br.ufc.great.greatroom.view.adapter.FilesAdapter;

public class FilesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {
    public static final String GROUP_PARAM = "group_data";

    private SwipeRefreshLayout swipeRefreshLayout;
    private View loadingView;
    private ServerApi serverApi;
    private GroupModel group;
    private FilesAdapter adapter;
    private PubSubController pubSubController;
    private ProgressDialog uploadProgressDialog;
    private ProgressDialog deleteProgressDialog;
    private ProgressDialog downloadProgressDialog;
    private BroadcastReceiver filesChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
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

        toolbar.setSubtitle(group.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pubSubController = PubSubController.getInstance();
        filesChangedListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                findFiles();
            }
        };

        serverApi = new ServerApi(getApplicationContext());
        loadingView = findViewById(R.id.loading);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);
        adapter = new FilesAdapter(this);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        registerForContextMenu(listView);

        uploadProgressDialog = new ProgressDialog(this);
        uploadProgressDialog.setTitle("Enviando arquivo");
        uploadProgressDialog.setMessage("");
        uploadProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        uploadProgressDialog.setProgress(0);
        uploadProgressDialog.setMax(100);

        deleteProgressDialog = new ProgressDialog(this);
        deleteProgressDialog.setTitle("Apagando arquivo");
        deleteProgressDialog.setMessage("");
        deleteProgressDialog.setIndeterminate(true);
        deleteProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        downloadProgressDialog = new ProgressDialog(this);
        downloadProgressDialog.setTitle("Baixando arquivo");
        downloadProgressDialog.setMessage("");
        downloadProgressDialog.setIndeterminate(true);
        downloadProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooser();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pubSubController.subscribeToGroupEvent(PubSubController.EVENT_GROUP_FILES_CHANGED,
                group, filesChangedListener);
        findFiles();
    }

    @Override
    protected void onPause() {
        pubSubController.unsubscribeToGroupEvent(PubSubController.EVENT_GROUP_FILES_CHANGED,
                group, filesChangedListener);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            doUpload(data.getData());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == android.R.id.list) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_files_list, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.menu_delete:
                FileModel file = (FileModel) adapter.getItem(info.position);
                if (file != null) {
                    deleteFile(file);
                }
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onRefresh() {
        findFiles();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        FileModel fileModel = (FileModel) adapter.getItem(position);
        DownloadFileTask downloadTask = new DownloadFileTask();
        downloadTask.execute(fileModel);
    }

    private void findFiles() {
        if (!swipeRefreshLayout.isRefreshing())
            swipeRefreshLayout.setRefreshing(true);
        loadingView.setVisibility(View.VISIBLE);

        serverApi.getFiles(group, new ServerApi.FilesCallback() {
            @Override
            public void onSuccessResponse(final List<FileModel> list) {
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

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("application/pdf");
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Selecione o arquivo"), 0);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Gerenciador de arquivos não encontrados", Toast.LENGTH_SHORT).show();
        }
    }

    private void doUpload(Uri uri) {
        uploadProgressDialog.setProgress(0);

        FileModel inputFile = serverApi.uploadFile(group, uri, new ServerApi.FileUploadCallback() {
            @Override
            public void onSuccessResponse(final FileModel file) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgressDialog.dismiss();
                        findFiles();
                    }
                });
            }

            @Override
            public void onErrorResponse(final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgressDialog.dismiss();
                        String message = "Erro ao enviar o arquivo: " + errorMessage;
                        Toast.makeText(FilesActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onProgressResponse(final float progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadProgressDialog.setProgress((int) progress * 100);
                    }
                });
            }
        });

        if (inputFile != null) {
            uploadProgressDialog.setMessage(inputFile.getName());
            uploadProgressDialog.show();
        }
    }

    private void deleteFile(FileModel file) {
        deleteProgressDialog.setMessage(file.getName());
        deleteProgressDialog.show();
        serverApi.deleteFile(group, file, new ServerApi.FileDeleteCallback() {
            @Override
            public void onSuccessResponse() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteProgressDialog.dismiss();
                        findFiles();
                    }
                });
            }

            @Override
            public void onErrorResponse(final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteProgressDialog.dismiss();
                        findFiles();
                        String message = "Erro ao apagar o arquivo: " + errorMessage;
                        Toast.makeText(FilesActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private class DownloadFileTask extends AsyncTask<FileModel, Void, FileModel> {

        @Override
        protected FileModel doInBackground(FileModel... files) {
            FileModel fileModel = files[0];
            String fileName = fileModel.getName() + "." + fileModel.getExtension();
            String localPath = Environment.getExternalStorageDirectory() + "/greatRoom/groups/" + group.getId() + "/files/" + fileName;
            fileModel.setLocalPath(localPath);
            File file = fileModel.getLocalFile();

            if (file.exists() && file.isFile())
                return fileModel;
            try {
                File dir = file.getParentFile();
                if (dir != null && !dir.exists())
                    dir.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    downloadProgressDialog.show();
                }
            });

            Downloader.downloadFile(fileModel.getUrl(), file);
            return fileModel;
        }

        protected void onPostExecute(final FileModel fileModel) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Uri fileUri = Uri.fromFile(fileModel.getLocalFile());
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, fileModel.getContentType());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    if (downloadProgressDialog.isShowing())
                        downloadProgressDialog.dismiss();

                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(FilesActivity.this, "Nenhuma aplicação encontrada para abrir o arquivo", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
