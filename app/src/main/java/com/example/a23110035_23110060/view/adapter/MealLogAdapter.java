package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.io.File;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.MealLogEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MealLogAdapter extends RecyclerView.Adapter<MealLogAdapter.ViewHolder> {
    public interface OnDeleteClickListener {
        void onDelete(MealLogEntity mealLog);
    }

    private final List<MealLogEntity> items = new ArrayList<>();
    private final OnDeleteClickListener listener;

    public MealLogAdapter(OnDeleteClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<MealLogEntity> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MealLogEntity item = items.get(position);
        holder.name.setText(item.foodName);
        holder.portion.setText(item.mealType + " • " + item.logDate + (item.isSynced ? " • Đã sync" : " • Chờ sync"));
        holder.macros.setText(String.format(Locale.US, "P %.1fg • C %.1fg • F %.1fg",
                item.protein, item.carbs, item.fat));
        holder.calories.setText(String.format(Locale.US, "%.0f kcal", item.calories));
        holder.time.setText(item.logDate); // Or specific time field if exists
        
        Glide.with(holder.itemView).clear(holder.imageMealThumbnail);
        holder.imageMealThumbnail.setImageDrawable(null);
        holder.imageMealThumbnail.setVisibility(View.GONE);

        if (item.imageUrl != null && !item.imageUrl.trim().isEmpty()) {
            holder.imageMealThumbnail.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView)
                 .load(item.imageUrl)
                 .placeholder(R.drawable.ic_empty_bowl)
                 .error(R.drawable.ic_empty_bowl)
                 .centerCrop()
                 .into(holder.imageMealThumbnail);
        } else if (item.imagePath != null && !item.imagePath.trim().isEmpty() && new File(item.imagePath).exists()) {
            holder.imageMealThumbnail.setVisibility(View.VISIBLE);
            Glide.with(holder.itemView)
                 .load(new File(item.imagePath))
                 .placeholder(R.drawable.ic_empty_bowl)
                 .error(R.drawable.ic_empty_bowl)
                 .centerCrop()
                 .into(holder.imageMealThumbnail);
        }
        
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
        TextView portion;
        TextView macros;
        TextView calories;
        TextView time;
        View delete;
        ImageView imageMealThumbnail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textFoodName);
            portion = itemView.findViewById(R.id.textPortion);
            macros = itemView.findViewById(R.id.textMacros);
            calories = itemView.findViewById(R.id.textCalories);
            time = itemView.findViewById(R.id.textTime);
            delete = itemView.findViewById(R.id.btnMenu);
            imageMealThumbnail = itemView.findViewById(R.id.image_meal_thumbnail);
        }
    }
}
