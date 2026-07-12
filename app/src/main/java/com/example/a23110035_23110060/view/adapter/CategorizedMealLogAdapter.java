package com.example.a23110035_23110060.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.io.File;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.a23110035_23110060.R;
import com.example.a23110035_23110060.data.local.MealLogEntity;

import com.example.a23110035_23110060.helper.CsvImportHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CategorizedMealLogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnMealLogActionListener {
        void onEdit(MealLogEntity log);
        void onDelete(MealLogEntity log);
    }

    private final List<Object> items = new ArrayList<>();
    private final OnMealLogActionListener listener;

    public CategorizedMealLogAdapter(OnMealLogActionListener listener) {
        this.listener = listener;
    }

    public void submitData(List<MealLogEntity> logs) {
        items.clear();
        if (logs == null || logs.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        addSection("Bữa sáng", "Breakfast", logs);
        addSection("Bữa trưa", "Lunch", logs);
        addSection("Bữa tối", "Dinner", logs);
        addSection("Bữa phụ", "Snack", logs);
        addSection("Khác", "Other", logs);

        notifyDataSetChanged();
    }

    private void addSection(String title, String type, List<MealLogEntity> allLogs) {
        List<MealLogEntity> sectionLogs = new ArrayList<>();
        double totalCal = 0;
        for (MealLogEntity log : allLogs) {
            if (type.equalsIgnoreCase(log.mealType)) {
                sectionLogs.add(log);
                totalCal += log.calories;
            }
        }

        if (!sectionLogs.isEmpty()) {
            items.add(new HeaderItem(title, totalCal));
            items.addAll(sectionLogs);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof HeaderItem ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_group_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_log, parent, false);
            return new ItemViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            HeaderItem header = (HeaderItem) items.get(position);
            ((HeaderViewHolder) holder).bind(header);
        } else if (holder instanceof ItemViewHolder) {
            MealLogEntity log = (MealLogEntity) items.get(position);
            ((ItemViewHolder) holder).bind(log, listener);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title, calories;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textGroupTitle);
            calories = itemView.findViewById(R.id.textGroupCalories);
        }

        void bind(HeaderItem item) {
            title.setText(item.title.toUpperCase());
            calories.setText(String.format(Locale.US, "%.0f kcal", item.totalCalories));
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name, calories, portion, time;
        TextView textProteinItem, textCarbsItem, textFatItem;
        ImageView image;
        ImageButton menu;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textFoodName);
            portion = itemView.findViewById(R.id.textPortion);
            textProteinItem = itemView.findViewById(R.id.textProteinItem);
            textCarbsItem = itemView.findViewById(R.id.textCarbsItem);
            textFatItem = itemView.findViewById(R.id.textFatItem);
            calories = itemView.findViewById(R.id.textCalories);
            time = itemView.findViewById(R.id.textTime);
            image = itemView.findViewById(R.id.image_meal_thumbnail);
            menu = itemView.findViewById(R.id.btnMenu);
        }

        void bind(MealLogEntity log, OnMealLogActionListener listener) {
            name.setText(CsvImportHelper.formatFoodLabel(log.foodName));
            calories.setText(String.format(Locale.US, "%.0f kcal", log.calories));
            portion.setText(log.serving);
            
            textProteinItem.setText(String.format(Locale.US, "%.1fg", log.protein));
            textCarbsItem.setText(String.format(Locale.US, "%.1fg", log.carbs));
            textFatItem.setText(String.format(Locale.US, "%.1fg", log.fat));
            
            // Format time from createdAt
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", Locale.US);
            time.setText(sdf.format(new java.util.Date(log.createdAt)));

            Glide.with(itemView).clear(image);
            image.setImageDrawable(null);
            image.setColorFilter(null);
            image.setVisibility(View.VISIBLE);

            if (log.imageUrl != null && !log.imageUrl.trim().isEmpty()) {
                Glide.with(itemView)
                     .load(log.imageUrl)
                     .placeholder(R.drawable.ic_meal_log_placeholder)
                     .error(R.drawable.ic_meal_log_placeholder)
                     .centerCrop()
                     .into(image);
            } else if (log.imagePath != null && !log.imagePath.trim().isEmpty() && new File(log.imagePath).exists()) {
                Glide.with(itemView)
                     .load(new File(log.imagePath))
                     .placeholder(R.drawable.ic_meal_log_placeholder)
                     .error(R.drawable.ic_meal_log_placeholder)
                     .centerCrop()
                     .into(image);
            } else {
                image.setImageResource(R.drawable.ic_meal_log_placeholder);
                image.setColorFilter(null);
            }

            menu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenu().add("Sửa");
                popup.getMenu().add("Xóa");
                popup.setOnMenuItemClickListener(item -> {
                    if ("Sửa".equals(item.getTitle())) {
                        listener.onEdit(log);
                    } else if ("Xóa".equals(item.getTitle())) {
                        listener.onDelete(log);
                    }
                    return true;
                });
                popup.show();
            });
        }
    }

    static class HeaderItem {
        String title;
        double totalCalories;

        HeaderItem(String title, double totalCalories) {
            this.title = title;
            this.totalCalories = totalCalories;
        }
    }
}
