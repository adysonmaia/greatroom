package br.ufc.great.greatroom.view.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.List;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.AppController;
import br.ufc.great.greatroom.controller.GroupController;
import br.ufc.great.greatroom.model.GroupModel;

/**
 * Created by adyson on 06/10/15.
 */
public class GroupsAdapter extends BaseAdapter {
    private List<GroupModel> items;
    private Context context;
    private GroupController groupController;

    public GroupsAdapter(Context context) {
        this.items = new ArrayList<>();
        this.context = context;
        this.groupController = GroupController.getInstance();
    }

    public void updateEntries(List<GroupModel> entries) {
        items.clear();
        if (entries != null)
            items.addAll(entries);
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
                    .inflate(R.layout.fragment_groups_item, parent, false);
        }

        GroupModel group = items.get(position);
        NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.group_image);
        TextView nameView = (TextView) view.findViewById(R.id.group_name);
        ImageView checkedView = (ImageView) view.findViewById(R.id.group_checked);
        nameView.setText(group.getName());
        if (groupController.hasGroupChecked(group)) {
            nameView.setTypeface(null, Typeface.BOLD);
            checkedView.setVisibility(View.VISIBLE);
        } else {
            nameView.setTypeface(null, Typeface.NORMAL);
            checkedView.setVisibility(View.INVISIBLE);
        }
        if (group.getImageUrl() != null && !group.getImageUrl().isEmpty())
            imageView.setImageUrl(group.getImageUrl(), AppController.getInstance().getImageLoader());
        else
            imageView.setImageUrl("", AppController.getInstance().getImageLoader());

        return view;
    }

}
