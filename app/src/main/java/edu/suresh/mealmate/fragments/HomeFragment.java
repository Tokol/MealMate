package edu.suresh.mealmate.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.suresh.mealmate.CustomProgressDialog;
import edu.suresh.mealmate.R;
import edu.suresh.mealmate.WeeklyPlanActivity;
import edu.suresh.mealmate.adapters.MealAdapter;
import edu.suresh.mealmate.model.Meal;
import edu.suresh.mealmate.model.Recipe;


public class HomeFragment extends Fragment implements MealAdapter.OnMealRemoveListener {

    private RecyclerView todaysMealRecyclerView;
    private MealAdapter mealAdapter;
    private List<Meal> mealList;
    private TextView noMealText;
    private MaterialButton viewWeeklyPlanButton;

    CustomProgressDialog customProgressDialog;

    private int completedRequests = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView
        todaysMealRecyclerView = view.findViewById(R.id.todaysMealRecyclerView);
        todaysMealRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        noMealText = view.findViewById(R.id.noMealText);
        viewWeeklyPlanButton = view.findViewById(R.id.viewWeeklyPlanButton);

        customProgressDialog = new CustomProgressDialog(getActivity());


        // Load Dummy Data
        //loadDummyMeals();
        loadDataMealToday(true);


        viewWeeklyPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WeeklyPlanActivity.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataMealToday(false);
    }

    private void loadDataMealToday(boolean showLoad) {
        if(showLoad){
            customProgressDialog.show();
        }
        // Step 1: Get today's date in "YYYY-MM-DD" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = dateFormat.format(new Date());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference mealRef = db.collection("meals").document(todayDate);

        mealRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();


                Log.d("Documents", document.toString());
                if (document.exists()) {
                    List<Long> breakfastTimestamps = (List<Long>) document.get("Breakfast");
                    List<Long> lunchTimestamps = (List<Long>) document.get("Lunch");
                    List<Long> dinnerTimestamps = (List<Long>) document.get("Dinner");

                    // ✅ Ensure Lists Are Not Null (Prevents Skipping)
                    if (breakfastTimestamps == null) breakfastTimestamps = new ArrayList<>();
                    if (lunchTimestamps == null) lunchTimestamps = new ArrayList<>();
                    if (dinnerTimestamps == null) dinnerTimestamps = new ArrayList<>();

                    // ✅ Create a combined list of meals
                    List<Meal> allMeals = new ArrayList<>();

                    Log.d("Firestore", "Breakfast Timestamps: " + breakfastTimestamps);
                    Log.d("Firestore", "Lunch Timestamps: " + lunchTimestamps);
                    Log.d("Firestore", "Dinner Timestamps: " + dinnerTimestamps);

                    // ✅ Fetch All Meals & Ensure RecyclerView Updates After All Requests Finish
                    fetchAllMeals(breakfastTimestamps, lunchTimestamps, dinnerTimestamps, allMeals);
                } else {
                    Log.d("Firestore", "No meal plan found for today: " + todayDate);
                    updateMealRecyclerView(new ArrayList<>()); // Clear RecyclerView if no meals
                    if (showLoad){
                        customProgressDialog.dismiss();
                    }
                }
            } else {
                Log.e("Firestore", "Failed to load meals", task.getException());
                if (showLoad){
                    customProgressDialog.dismiss();
                }
            }
        });
    }
    private void fetchAllMeals(List<Long> breakfastTimestamps, List<Long> lunchTimestamps,
                               List<Long> dinnerTimestamps, List<Meal> allMeals) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        int totalRequests = breakfastTimestamps.size() + lunchTimestamps.size() + dinnerTimestamps.size();

        Log.d("fetchedDataSize", totalRequests+"");
        if (totalRequests == 0) {
            updateMealRecyclerView(new ArrayList<>()); // ✅ No meals, clear RecyclerView
            customProgressDialog.dismiss();
            return;
        }




        // ✅ Fetch all recipes & update list dynamically
        for (Long timestamp : breakfastTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Breakfast", allMeals, totalRequests);
        }
        for (Long timestamp : lunchTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Lunch", allMeals, totalRequests);
        }
        for (Long timestamp : dinnerTimestamps) {
            fetchRecipeByTimestamp(db, timestamp, "Dinner", allMeals, totalRequests);
        }
    }


//    private void fetchRecipeByTimestamp(FirebaseFirestore db, Long timestamp, String mealType,
//                                        List<Meal> allMeals, int totalRequests) {
//        db.collection("recipes").document(String.valueOf(timestamp))
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        Recipe recipe = documentSnapshot.toObject(Recipe.class);
//                        if (recipe != null) {
//                            allMeals.add(new Meal(recipe, mealType)); // ✅ Add to combined list
//                        }
//                    }
//                    checkAndUpdateRecyclerView(allMeals, totalRequests);
//                })
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Error fetching recipe", e);
//                    checkAndUpdateRecyclerView(allMeals, totalRequests);
//                });
//    }

    private void fetchRecipeByTimestamp(FirebaseFirestore db, Long timestamp, String mealType,
                                        List<Meal> allMeals, int totalRequests) {
        Log.d("Firestore", "Querying recipe where timestamp = " + timestamp);

        db.collection("recipes")
                .whereEqualTo("timestamp", timestamp)  // ✅ Query by field instead of document ID
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Recipe recipe = document.toObject(Recipe.class);
                            if (recipe != null) {
                                allMeals.add(new Meal(recipe, mealType));
                                Log.d("Firestore", "Added recipe: " + recipe.getRecipeName());
                            }
                        }
                    } else {
                        Log.e("Firestore", "No recipe found with timestamp: " + timestamp);
                    }
                    checkAndUpdateRecyclerView(allMeals, totalRequests);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching recipe", e);
                    checkAndUpdateRecyclerView(allMeals, totalRequests);
                });
    }

    private void checkAndUpdateRecyclerView(List<Meal> allMeals, int totalRequests) {
        completedRequests++;

        if (completedRequests == totalRequests) { // ✅ Ensure all requests finish before updating UI
            updateMealRecyclerView(allMeals);
            customProgressDialog.dismiss();
        }
    }



    private void fetchRecipes(List<Long> timestamps, String mealType, List<Meal> allAvilableMeals) {
        if (timestamps == null || timestamps.isEmpty()) {
            Log.d("Firestore", mealType + " has no meals today.");
            updateMealRecyclerView(allAvilableMeals);  // Ensure UI updates even if empty
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
            customProgressDialog.show();
        for (Long timestamp : timestamps) {
            db.collection("recipes").document(String.valueOf(timestamp))
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Recipe recipe = documentSnapshot.toObject(Recipe.class); // ✅ Corrected
                            if (recipe != null) {
                                allAvilableMeals.add(new Meal(recipe, mealType)); // ✅ Fixed

                                Log.d("Recipe Fetch", "Loaded recipe: " + recipe.getRecipeName());

                                // ✅ Update RecyclerView after adding a meal
                                customProgressDialog.dismiss();
                                updateMealRecyclerView(allAvilableMeals);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {Log.e("Firestore", "Error fetching recipe", e);});
        }



    }



    private void updateMealRecyclerView(List<Meal> allMeals) {
        Log.d("mealUpdate", "Updating RecyclerView with " + allMeals.size() + " meals.");
        Log.d("RecyclerView Update", "Updating RecyclerView with " + allMeals.size() + " meals.");

        boolean hasMeals = !allMeals.isEmpty();
        noMealText.setVisibility(hasMeals ? View.GONE : View.VISIBLE);
        todaysMealRecyclerView.setVisibility(hasMeals ? View.VISIBLE : View.GONE);

        if (hasMeals) {
            todaysMealRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

            MealAdapter mealAdapter = new MealAdapter(getContext(), allMeals, false, this);
            todaysMealRecyclerView.setAdapter(mealAdapter);

            // ✅ Ensure RecyclerView updates
            mealAdapter.notifyDataSetChanged();
            todaysMealRecyclerView.setHasFixedSize(false); // 🔥 Prevent UI issues when size changes
        } else {
            Log.e("RecyclerView", "No meals found, clearing RecyclerView.");
            todaysMealRecyclerView.setAdapter(null); // ✅ Clear adapter when empty
        }
    }

    @Override
    public void onMealRemove(Meal meal, int position) {
        //NOTHING TO DO
    }


//    private void loadDummyMeals() {
//        mealList = new ArrayList<>();
//         mealList.add(new Meal("Thakali khana", R.drawable.thakali, "Breakfast"));
//         mealList.add(new Meal("MoMo", R.drawable.momo, "Lunch"));
//         mealList.add(new Meal("Pani Puri", R.drawable.panipuri, "Dinner"));
//
//        if (mealList.isEmpty()) {
//            noMealText.setVisibility(View.VISIBLE); // Show "No Meal Plan for Today"
//            todaysMealRecyclerView.setVisibility(View.GONE); // Hide RecyclerView
//        } else {
//            noMealText.setVisibility(View.GONE); // Hide "No Meal Plan" Text
//            todaysMealRecyclerView.setVisibility(View.VISIBLE); // Show RecyclerView
//            mealAdapter = new MealAdapter(getContext(), mealList,false);
//            todaysMealRecyclerView.setAdapter(mealAdapter);
//        }
//        mealAdapter = new MealAdapter(getContext(), mealList,false);
//        todaysMealRecyclerView.setAdapter(mealAdapter);
//    }
}
