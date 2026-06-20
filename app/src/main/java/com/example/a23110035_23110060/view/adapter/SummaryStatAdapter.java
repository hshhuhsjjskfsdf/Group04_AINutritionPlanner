package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;

import java.util.ArrayList;
import java.util.List;

public class SummaryStatAdapter extends RecyclerView.Adapter<SummaryStatAdapter.ViewHolder> {
    public static class Stat {
        public final String title;
        public final String value;

        public Stat(String title, String value) {
            this.title = title;
            this.value = value;
        }
    }

    private final List<Stat> items = new ArrayList<>();

    public void submitList(List<Stat> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_summary_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stat stat = items.get(position);
        holder.title.setText(stat.title);
        holder.value.setText(stat.value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView value;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textStatTitle);
            value = itemView.findViewById(R.id.textStatValue);
        }
    }
}
