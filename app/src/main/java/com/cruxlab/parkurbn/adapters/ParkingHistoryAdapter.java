package com.cruxlab.parkurbn.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cruxlab.parkurbn.R;
import com.cruxlab.parkurbn.model.History;
import com.cruxlab.parkurbn.tools.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by alla on 4/24/17.
 */

public class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.ViewHolder> {

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMM yyyy", Locale.US);

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAddress, tvCost, tvDate, tvCard, tvTime;
        ImageView ivMap;

        ViewHolder(View view) {
            super(view);
            tvAddress = ((TextView) view.findViewById(R.id.tv_address));
            tvCost = (TextView) view.findViewById(R.id.tv_cost);
            tvTime = (TextView) view.findViewById(R.id.tv_time);
            tvDate = (TextView) view.findViewById(R.id.tv_date);
            tvCard = (TextView) view.findViewById(R.id.tv_card);
            ivMap = (ImageView) view.findViewById(R.id.iv_map_screenshot);
        }
    }

    private List<History> history;
    private Context context;

    public ParkingHistoryAdapter(Context context) {
        history = new ArrayList<>();
        this.context = context;
    }

    public void setHistory(List<History> history) {
        this.history = history;
    }

    @Override
    public ParkingHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_history, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        History item = history.get(position);

        try {
            if (item.getParkingDate() != null) {
                Date date = formatter.parse(item.getParkingDate());
                holder.tvDate.setText(dateFormatter.format(date));
                holder.tvTime.setText(Converter.minsToTimeAMPM(item.getStartParkingTime()) + " — " +
                        Converter.minsToTimeAMPM(item.getEndParkingTime()));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.tvAddress.setText(item.getAddress());
        holder.tvCard.setText(item.getCard() == null ? item.getPaypalEmail() : item.getCard());
        holder.tvCost.setText("$" + item.getAmount());

        Glide.with(context).load(item.getMapUrl()).into(holder.ivMap);
    }

    @Override
    public int getItemCount() {
        return history.size();
    }
}