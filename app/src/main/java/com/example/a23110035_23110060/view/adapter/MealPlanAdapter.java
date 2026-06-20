package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.MealPlanEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {
    public interface OnDeleteClickListener {
        void onDelete(MealPlanEntity mealPlan);
    }

    private final List<MealPlanEntity> items = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public MealPlanAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MealPlanEntity> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealPlanEntity item = items.get(position);
        holder.name.setText(item.foodName);
        holder.meta.setText(item.mealType + " • " + item.planDate + (item.isSynced ? " • Đã sync" : " • Chờ sync"));
        holder.macros.setText(String.format(Locale.US, "%.0f kcal | P %.1fg • C %.1fg • F %.1fg",
                item.calories, item.protein, item.carbs, item.fat));
        holder.note.setText(item.note == null || item.note.isEmpty() ? "Không có ghi chú" : item.note);
        holder.delete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView meta;
        TextView macros;
        TextView note;
        Button delete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textPlanName);
            meta = itemView.findViewById(R.id.textPlanMeta);
            macros = itemView.findViewById(R.id.textPlanMacros);
            note = itemView.findViewById(R.id.textPlanNote);
            delete = itemView.findViewById(R.id.buttonDeletePlan);
        }
    }
}
