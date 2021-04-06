package br.ufc.great.greatroom.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.ufc.great.greatroom.model.FileModel;

/**
 * Created by adyson on 15/11/15.
 */
public class FilesAdapter extends BaseAdapter {
    private List<FileModel> items;
    private Context context;

    public FilesAdapter(Context context) {
        this.items = new ArrayList<>();
        this.context = context;
    }

    public void updateEntries(List<FileModel> entries) {
        if (null == entries)
            items.clear();
        else
            items = entries;
        notifyDataSetInvalidated();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        FileModel file = items.get(position);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(file.getName() + "." + file.getExtension());

        return view;
    }
}
