package br.ufc.great.greatroom.view.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import br.ufc.great.greatroom.R;
import br.ufc.great.greatroom.controller.AppController;
import br.ufc.great.greatroom.model.PersonModel;

/**
 * Created by belmondorodrigues on 26/10/2015.
 */
public class PersonsAdapter extends BaseAdapter {
    private List<PersonModel> items;
    private Context context;

    private Activity activity;
    private LayoutInflater inflater;

    private String[] bgColors;

    public PersonsAdapter(Context context) {
        this.items = new ArrayList<>();
        this.context = context;
    }

    public void updateEntries(List<PersonModel> entries) {
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
    public Object getItem(int location) {
        return items.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.activity_persons_item, parent, false);
        }

        PersonModel person = items.get(position);
        NetworkImageView imageView = (NetworkImageView) view.findViewById(R.id.person_image);
        TextView nameView = (TextView) view.findViewById(R.id.person_name);
        TextView timeView = (TextView) view.findViewById(R.id.checkin_time);
        nameView.setText(person.getName());
        if (person.getImageUrl() != null && !person.getImageUrl().isEmpty())
            imageView.setImageUrl(person.getImageUrl(), AppController.getInstance().getImageLoader());
        else
            imageView.setImageUrl("", AppController.getInstance().getImageLoader());
        if (person.getLastCheckInDate() != null) {
            Date now = new Date();
            long diff = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - person.getLastCheckInDate().getTime());
            String timeText = "último check-in: ";
            if (diff <= 0)
                timeText += "agora";
            else if (diff == 1)
                timeText += diff + " minuto";
            else
                timeText += diff + " minutos";
            timeView.setText(timeText);
            timeView.setVisibility(View.VISIBLE);
        } else {
            timeView.setVisibility(View.INVISIBLE);
        }

        return view;
    }

}