package edu.suresh.mealmate.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import edu.suresh.mealmate.R;
import edu.suresh.mealmate.model.SavedLocation;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private final List<SavedLocation> storeList;

    public StoreAdapter(Context context, List<SavedLocation> storeList) {
        this.context = context;
        this.storeList = storeList;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        SavedLocation store = storeList.get(position);

        // Store Name and Distance
        holder.storeName.setText(store.getName());
        holder.storeDistance.setText(store.getDistance() + " away");

        // Display Matching Count Badge
        int matchingCount = store.getMatchingCount();
        if (matchingCount > 0) {
            holder.matchingCount.setVisibility(View.VISIBLE);
            holder.matchingCount.setText(String.valueOf(matchingCount));
        } else {
            holder.matchingCount.setVisibility(View.GONE);
        }

        // Display Available Ingredients as Chips
        holder.chipsContainer.removeAllViews();
        List<String> ingredients = store.getAvailableIngredients();
        for (String ingredient : ingredients) {
            TextView chip = new TextView(context);
            chip.setText(ingredient);
            chip.setBackground(context.getDrawable(R.drawable.chip_background));
            chip.setPadding(24, 12, 24, 12);
            chip.setTextColor(context.getColor(R.color.on_surface));
            chip.setTextSize(12);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(8, 0, 8, 0);
            chip.setLayoutParams(layoutParams);
            holder.chipsContainer.addView(chip);
        }

        // Load Image using Glide
        Glide.with(context)
                .load(store.getImageUrl())
                .placeholder(R.drawable.saved_store)
                .into(holder.storeImage);

        // Handle Get Directions Button
        holder.getDirectionsButton.setOnClickListener(v -> {
            String geoUri = "google.navigation:q=" + store.getLatitude() + "," + store.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
            intent.setPackage("com.google.android.apps.maps");
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    public static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView storeImage;
        TextView storeName, storeDistance, matchingCount;
        LinearLayout chipsContainer;
        Button getDirectionsButton;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeImage = itemView.findViewById(R.id.storeImage);
            storeName = itemView.findViewById(R.id.storeName);
            storeDistance = itemView.findViewById(R.id.storeDistance);
            matchingCount = itemView.findViewById(R.id.matchingCount);
            chipsContainer = itemView.findViewById(R.id.chipsContainer);
            getDirectionsButton = itemView.findViewById(R.id.getDirectionsButton);
        }
    }
}
