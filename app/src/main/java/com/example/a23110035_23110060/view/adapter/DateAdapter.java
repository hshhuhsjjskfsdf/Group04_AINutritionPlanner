package com.example.a23110035_23110060.view.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    private final List<Calendar> dates = new ArrayList<>();
    private Calendar selectedDate;
    private final OnDateSelectedListener listener;
    private final SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
    private final SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("d", Locale.US);

    public DateAdapter(OnDateSelectedListener listener) {
        this.listener = listener;
        this.selectedDate = Calendar.getInstance();
    }

    public void setDates(List<Calendar> newDates, Calendar selected) {
        this.dates.clear();
        this.dates.addAll(newDates);
        this.selectedDate = (Calendar) selected.clone();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_week, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Calendar date = dates.get(position);
        holder.textDayOfWeek.setText(dayOfWeekFormat.format(date.getTime()));
        holder.textDayOfMonth.setText(dayOfMonthFormat.format(date.getTime()));

        boolean isSelected = isSameDay(date, selectedDate);
        
        if (isSelected) {
            holder.cardDate.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
            holder.textDayOfWeek.setTextColor(Color.WHITE);
            holder.textDayOfMonth.setTextColor(Color.WHITE);
            holder.indicator.setVisibility(View.VISIBLE);
        } else {
            holder.cardDate.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.surface_soft));
            holder.textDayOfWeek.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            holder.textDayOfMonth.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_primary));
            holder.indicator.setVisibility(View.INVISIBLE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (!isSelected) {
                selectedDate = (Calendar) date.clone();
                notifyDataSetChanged();
                listener.onDateSelected(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dates.size();
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardDate;
        TextView textDayOfWeek, textDayOfMonth;
        View indicator;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDate = itemView.findViewById(R.id.cardDate);
            textDayOfWeek = itemView.findViewById(R.id.textDayOfWeek);
            textDayOfMonth = itemView.findViewById(R.id.textDayOfMonth);
            indicator = itemView.findViewById(R.id.indicator);
        }
    }
}
