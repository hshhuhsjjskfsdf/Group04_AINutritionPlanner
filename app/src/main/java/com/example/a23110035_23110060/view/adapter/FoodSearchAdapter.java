package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.FoodEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodSearchAdapter extends RecyclerView.Adapter<FoodSearchAdapter.ViewHolder> {
    public interface OnFoodClickListener {
        void onFoodClick(FoodEntity food);
    }

    private final List<FoodEntity> items = new ArrayList<>();
    private final OnFoodClickListener listener;

    public FoodSearchAdapter(OnFoodClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<FoodEntity> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodEntity item = items.get(position);
        holder.name.setText(item.dishName);
        holder.info.setText(String.format(Locale.US, "%.0f kcal | P %.1fg • C %.1fg • F %.1fg",
                item.calories, item.protein, item.carbs, item.fat));
        holder.serving.setText(item.serving);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView info;
        TextView serving;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textFoodName);
            info = itemView.findViewById(R.id.textFoodInfo);
            serving = itemView.findViewById(R.id.textFoodServing);
        }
    }
}
