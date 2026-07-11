package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.MealLogEntity;
import com.example.a23110035_23110060.helper.DateHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TodayMealAdapter extends RecyclerView.Adapter<TodayMealAdapter.ViewHolder> {
    private final List<MealLogEntity> items = new ArrayList<>();

    public void submitList(List<MealLogEntity> data) {
        items.clear();
        if (data != null) {
            // Take only up to 3 items
            int count = Math.min(data.size(), 3);
            for (int i = 0; i < count; i++) {
                items.add(data.get(i));
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_compact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealLogEntity item = items.get(position);
        holder.textMealType.setText(item.mealType);
        holder.textFoodName.setText(item.foodName);
        holder.textCalories.setText(String.format(Locale.US, "%.0f kcal", item.calories));
        
        // Show time if available in createdAt or just use a placeholder
        String time = DateHelper.formatTime(item.createdAt);
        holder.textTime.setText(time);

        // Load image
        holder.imgMeal.setColorFilter(null);
        if (item.imageUrl != null && !item.imageUrl.trim().isEmpty()) {
            Glide.with(holder.itemView)
                    .load(item.imageUrl)
                    .placeholder(R.drawable.ic_nav_diary)
                    .error(R.drawable.ic_nav_diary)
                    .centerCrop()
                    .into(holder.imgMeal);
        } else if (item.imagePath != null && !item.imagePath.trim().isEmpty() && new File(item.imagePath).exists()) {
            Glide.with(holder.itemView)
                    .load(new File(item.imagePath))
                    .placeholder(R.drawable.ic_nav_diary)
                    .error(R.drawable.ic_nav_diary)
                    .centerCrop()
                    .into(holder.imgMeal);
        } else {
            holder.imgMeal.setImageResource(R.drawable.ic_nav_diary);
            holder.imgMeal.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.primary));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMeal;
        TextView textMealType, textFoodName, textTime, textCalories;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgMeal = itemView.findViewById(R.id.imgMeal);
            textMealType = itemView.findViewById(R.id.textMealType);
            textFoodName = itemView.findViewById(R.id.textFoodName);
            textTime = itemView.findViewById(R.id.textTime);
            textCalories = itemView.findViewById(R.id.textCalories);
        }
    }
}
