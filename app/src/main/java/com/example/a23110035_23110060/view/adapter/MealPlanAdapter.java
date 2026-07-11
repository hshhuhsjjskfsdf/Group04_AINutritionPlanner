package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.MealPlanEntity;

import com.example.a23110035_23110060.helper.CsvImportHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanAdapter.ViewHolder> {
    public interface OnPlanActionListener {
        void onDelete(MealPlanEntity plan);
        void onToggleCompleted(MealPlanEntity plan, boolean completed);
        void onEdit(MealPlanEntity plan);
    }

    private final List<MealPlanEntity> items = new ArrayList<>();
    private final OnPlanActionListener listener;

    public MealPlanAdapter(OnPlanActionListener listener) {
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
        holder.name.setText(CsvImportHelper.formatFoodLabel(item.foodName));
        holder.calories.setText(String.format(Locale.US, "%.0f kcal", item.calories));
        
        holder.textProteinPlan.setText(String.format(Locale.US, "%.1fg", item.protein));
        holder.textCarbsPlan.setText(String.format(Locale.US, "%.1fg", item.carbs));
        holder.textFatPlan.setText(String.format(Locale.US, "%.1fg", item.fat));
        
        if (item.portion != null && !item.portion.isEmpty()) {
            holder.portion.setText(item.portion);
            holder.portion.setVisibility(View.VISIBLE);
        } else {
            holder.portion.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleCompleted(item, isChecked);
            }
        });

        if (item.note != null && !item.note.isEmpty()) {
            holder.note.setText("Ghi chú: " + item.note);
            holder.note.setVisibility(View.VISIBLE);
        } else {
            holder.note.setVisibility(View.GONE);
        }

        holder.btnMenu.setOnClickListener(v -> {
            if (listener != null) {
                // For now, let's just trigger delete or edit via a simple way
                // In a real app, this might show a PopupMenu
                android.widget.PopupMenu popup = new android.widget.PopupMenu(v.getContext(), v);
                popup.getMenu().add("Sửa");
                popup.getMenu().add("Xóa");
                popup.setOnMenuItemClickListener(menuItem -> {
                    if ("Sửa".equals(menuItem.getTitle())) {
                        listener.onEdit(item);
                    } else if ("Xóa".equals(menuItem.getTitle())) {
                        listener.onDelete(item);
                    }
                    return true;
                });
                popup.show();
            }
        });
        
        // Visual indicator for completed
        holder.itemView.setAlpha(item.isCompleted ? 0.6f : 1.0f);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, calories, note, portion;
        TextView textProteinPlan, textCarbsPlan, textFatPlan;
        CheckBox checkBox;
        ImageButton btnMenu;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textPlanName);
            calories = itemView.findViewById(R.id.textPlanCalories);
            textProteinPlan = itemView.findViewById(R.id.textProteinPlan);
            textCarbsPlan = itemView.findViewById(R.id.textCarbsPlan);
            textFatPlan = itemView.findViewById(R.id.textFatPlan);
            note = itemView.findViewById(R.id.textPlanNote);
            portion = itemView.findViewById(R.id.textPlanPortion);
            checkBox = itemView.findViewById(R.id.checkCompleted);
            btnMenu = itemView.findViewById(R.id.btnMenuPlan);
        }
    }
}
