package com.cruxlab.parkurbn.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cruxlab.parkurbn.R;

/**
 * Adapter for titles to customize drawer items.
 * <p>
 * Created by alla on 4/25/17.
 */

public class MenuAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private static final int[] titles = {R.string.my_account, R.string.vehicles, R.string.parking_history, R.string.feedback};
    private static final int[] icons = {R.drawable.ic_menu_my_account, R.drawable.ic_menu_vehicles,
            R.drawable.ic_history, R.drawable.ic_feedback};
    private static final int[] cityTitles = {R.string.manhattan_beach, R.string.berkeley, R.string.seattle, R.string.santa_monica};

    public MenuAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return titles.length + cityTitles.length;
    }

    @Override
    public Object getItem(int i) {
        if (i < titles.length) {
            return titles[i];
        } else {
            return cityTitles[i - titles.length];
        }
    }

    @Override
    public long getItemId(int i) {
        if (i < titles.length) {
            return titles[i];
        } else {
            return cityTitles[i - titles.length];
        }
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.item_menu, viewGroup, false);
        }
        TextView title = (TextView) view.findViewById(R.id.title);
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        if (i < titles.length) {
            title.setText(context.getResources().getString(titles[i]));
            title.setTextSize(20);
            icon.setVisibility(View.VISIBLE);
            icon.setImageResource(icons[i]);
        } else {
            title.setText(context.getResources().getString(cityTitles[i - titles.length]));
            title.setTextSize(14);
            icon.setVisibility(View.GONE);
        }
        return view;
    }
}
