package edu.suresh.mealmate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.suresh.mealmate.CustomProgressDialog;
import edu.suresh.mealmate.GeoTagActivity;
import edu.suresh.mealmate.GroceryActivity;
import edu.suresh.mealmate.R;
import edu.suresh.mealmate.WeeklyPlanActivity;
import edu.suresh.mealmate.adapters.MealAdapter;
import edu.suresh.mealmate.adapters.StoreAdapter;
import edu.suresh.mealmate.model.Meal;
import edu.suresh.mealmate.model.Recipe;
import edu.suresh.mealmate.model.SavedLocation;

public class HomeFragment extends Fragment implements MealAdapter.OnMealRemoveListener {

    private RecyclerView todaysMealRecyclerView, favStoreRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private TextView noMealText;
    private MaterialButton viewWeeklyPlanButton;
    private FloatingActionButton shop;
    private CustomProgressDialog customProgressDialog;

    private int completedRequests = 0;
    private int totalRequests = 0;

    private ShapeableImageView addStore;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Views
        todaysMealRecyclerView = view.findViewById(R.id.todaysMealRecyclerView);
        todaysMealRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        noMealText = view.findViewById(R.id.noMealText);
        addStore = view.findViewById(R.id.addFavStoreIcon);

        favStoreRecyclerView = view.findViewById(R.id.favStoresRecyclerView);
        viewWeeklyPlanButton = view.findViewById(R.id.viewWeeklyPlanButton);
        shop = view.findViewById(R.id.shop);
        customProgressDialog = new CustomProgressDialog(getActivity());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        favStoreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        loadDummyData();
        loadDataMealToday(true);

        // Button Listeners
        viewWeeklyPlanButton.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), WeeklyPlanActivity.class));
        });

        shop.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), GroceryActivity.class));
        });

        addStore.setOnClickListener(v -> {
            startActivity(new Intent(getContext(), GeoTagActivity.class));

        });

        return view;
    }





    private void loadDummyData() {
        List<SavedLocation> dummyData = new ArrayList<>();
        dummyData.add(new SavedLocation(
                "Fresh Mart",
                "https://example.com/store1.jpg",
                27.7172, 85.3240,
                "2.5 km",
                Arrays.asList("Tomatoes", "Potatoes", "Onions", "Red meat", "Eggs", "Origano", "Coconut oil", "Basil"),
                3
        ));
        dummyData.add(new SavedLocation(
                "Organic Foods",
                "https://example.com/store2.jpg",
                27.7150, 85.3120,
                "3 km",
                Arrays.asList("Carrots", "Broccoli"),
                2
        ));
        dummyData.add(new SavedLocation(
                "Super Mart",
                "https://example.com/store3.jpg",
                27.7180, 85.3300,
                "1.8 km",
                Arrays.asList("Milk", "Eggs", "Bread"),
                1
        ));

        StoreAdapter adapter = new StoreAdapter(requireContext(), dummyData);
        favStoreRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mealList == null || mealList.isEmpty()) {
            loadDataMealToday(false);
        }
    }

    private void loadDataMealToday(boolean showLoad) {
        completedRequests = 0;
        totalRequests = 0;

        if (showLoad) {
            customProgressDialog.show();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mealRef = db.collection("meals").document(todayDate);

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<Long> breakfastTimestamps = (List<Long>) document.get("Breakfast");
                    List<Long> lunchTimestamps = (List<Long>) document.get("Lunch");
                    List<Long> dinnerTimestamps = (List<Long>) document.get("Dinner");

                    if (breakfastTimestamps == null) breakfastTimestamps = new ArrayList<>();
                    if (lunchTimestamps == null) lunchTimestamps = new ArrayList<>();
                    if (dinnerTimestamps == null) dinnerTimestamps = new ArrayList<>();

                    totalRequests = breakfastTimestamps.size() + lunchTimestamps.size() + dinnerTimestamps.size();
                    List<Meal> allMeals = new ArrayList<>();

                    fetchAllMeals(breakfastTimestamps, lunchTimestamps, dinnerTimestamps, allMeals);
                } else {
                    updateMealRecyclerView(new ArrayList<>());
                    if (showLoad) customProgressDialog.dismiss();
                }
            } else {
                if (showLoad) customProgressDialog.dismiss();
            }
        });
    }

    private void fetchAllMeals(List<Long> breakfastTimestamps, List<Long> lunchTimestamps,
                               List<Long> dinnerTimestamps, List<Meal> allMeals) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (totalRequests == 0) {
            updateMealRecyclerView(new ArrayList<>());
            customProgressDialog.dismiss();
            return;
        }

        for (Long timestamp : breakfastTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Breakfast", allMeals);
        }
        for (Long timestamp : lunchTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Lunch", allMeals);
        }
        for (Long timestamp : dinnerTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Dinner", allMeals);
        }
    }

    private void fetchRecipeByTimestamp(FirebaseFirestore db, Long timestamp, String mealType, List<Meal> allMeals) {
        db.collection("recipes")
                .whereEqualTo("timestamp", timestamp)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                allMeals.add(new Meal(recipe, mealType));
                            }
                        }
                    }
                    checkAndUpdateRecyclerView(allMeals);
                })
                .addOnFailureListener(e -> checkAndUpdateRecyclerView(allMeals));
    }

    private void checkAndUpdateRecyclerView(List<Meal> allMeals) {
        completedRequests++;
        if (completedRequests == totalRequests) {
            updateMealRecyclerView(allMeals);
            customProgressDialog.dismiss();
        }
    }

    private void updateMealRecyclerView(List<Meal> allMeals) {
        boolean hasMeals = !allMeals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        todaysMealRecyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

        if (hasMeals) {
            mealAdapter = new MealAdapter(requireContext(), allMeals, false, this);
            todaysMealRecyclerView.setAdapter(mealAdapter);
        } else {
            todaysMealRecyclerView.setAdapter(null);
        }
    }

    @Override
    public void onMealRemove(Meal meal, int position) {
        // Implement meal removal logic if needed
    }
}