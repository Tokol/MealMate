package edu.suresh.mealmate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;
import java.util.Map;

import edu.suresh.mealmate.adapters.InstructionDetailAdapter;

import edu.suresh.mealmate.home.DashboardActivity;
import edu.suresh.mealmate.home.MainActivity;
import edu.suresh.mealmate.home.RecipeEditActivity;
import edu.suresh.mealmate.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    private ViewPager2 instructionsViewPager;
    private MaterialButton prevButton, nextButton;
    private InstructionDetailAdapter instructionsAdapter;

    private ImageView recipeImage;
    TextView recipeNameTv, cookTime, totalIngredients;
    private LinearLayout ingredientsContainer;
    Recipe recipe;
    CustomProgressDialog customProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Retrieve the Recipe object from the Intent
         recipe = getIntent().getParcelableExtra("RECIPE");
        recipeImage = findViewById(R.id.recipeImage);
        recipeNameTv = findViewById(R.id.recipeName);
        cookTime = findViewById(R.id.cookTime);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);

        customProgressDialog = new CustomProgressDialog(RecipeDetailActivity.this);
        if (recipe != null) {
            // Set up ViewPager2 for instructions
            loadIngredients(recipe.getIngredients());
            instructionsViewPager = findViewById(R.id.instructionsViewPager);
            instructionsAdapter = new InstructionDetailAdapter(this, recipe.getInstructions());
            instructionsViewPager.setAdapter(instructionsAdapter);

            String imageUrl = recipe.getPhotoUrl();
            if (!imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).into(recipeImage);
            }
            recipeNameTv.setText(recipe.getRecipeName());
            cookTime.setText(recipe.getCookTime()+ "Minutes");


            // Set up navigation buttons
            prevButton = findViewById(R.id.prevButton);
            nextButton = findViewById(R.id.nextButton);

            prevButton.setOnClickListener(v -> {
                if (instructionsViewPager.getCurrentItem() > 0) {
                    instructionsViewPager.setCurrentItem(instructionsViewPager.getCurrentItem() - 1);
                }
            });

            nextButton.setOnClickListener(v -> {
                if (instructionsViewPager.getCurrentItem() < instructionsAdapter.getItemCount() - 1) {
                    instructionsViewPager.setCurrentItem(instructionsViewPager.getCurrentItem() + 1);
                }
            });
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.recipe_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_recipe) {
                openEditRecipe();
                return true;
            } else if (item.getItemId() == R.id.action_del_recipe) {
                deleteRecipe();
                return true;
            }
            return false;
        });


    }

    void openEditRecipe() {
        if (recipe != null) {  // ✅ Ensure recipe is not null before sending
            Intent intent = new Intent(RecipeDetailActivity.this, RecipeEditActivity.class);
            intent.putExtra("RECIPE", recipe);
           // Log.d("IntentDebug", "Navigating to RecipeEditActivity");

            startActivity(intent);
        } else {
           // Log.e("IntentDebug", "Error: Recipe is null!");
            showSnackbar("Error: Recipe data is missing!");
        }
    }


    void deleteRecipe(){

        new MaterialAlertDialogBuilder(RecipeDetailActivity.this)
                .setTitle("Delete")
                .setMessage("Are you sure you want to Delete this recipe?")
                .setCancelable(false) // Prevents dismissing by tapping outside
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Firebase Sign Out
                    customProgressDialog.show();
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    db.collection("recipes")
                            .whereEqualTo("timestamp", recipe.getTimestamp())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // Delete the document
                                        db.collection("recipes").document(document.getId())
                                                .delete()
                                                .addOnSuccessListener(aVoid ->
                                                        showSnackbar("Document deleted successfully!")

                                                )
                                                .addOnFailureListener(e ->
                                                        showSnackbar("Error deleting document: \" + e")

                                                );
                                    }
                                } else {
                                    showSnackbar("No document found with timestamp: \" + timestampToDelete");
                                    //System.out.println("No document found with timestamp: " + timestampToDelete);
                                }
                            });
                    customProgressDialog.dismiss();

                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.putExtra("FRAGMENT_INDEX", 0); // Send index for HomeFragment
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Properly dismisses dialog
                .show();


    }


    private void loadIngredients(Map<String, List<String>> ingredients) {
        ingredientsContainer.removeAllViews(); // Clear any previous data

        for (Map.Entry<String, List<String>> entry : ingredients.entrySet()) {
            String category = entry.getKey(); // Example: "Vegetables"
            List<String> ingredientList = entry.getValue(); // Example: ["Spinach", "Carrots"]

            // 🏷 Create Category Header
            TextView categoryTitle = new TextView(this);
            categoryTitle.setText(category);
            categoryTitle.setTextSize(16);
            categoryTitle.setTextColor(getResources().getColor(R.color.on_surface));
            categoryTitle.setPadding(0, 16, 0, 8);
            ingredientsContainer.addView(categoryTitle);

            // 🏷 Create a ChipGroup for Ingredients
            ChipGroup chipGroup = new ChipGroup(this);
            chipGroup.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            // ✅ Add Chips for Each Ingredient
            for (String ingredient : ingredientList) {
                Chip chip = new Chip(this);
                chip.setText(ingredient);
                chip.setChipBackgroundColorResource(R.color.on_surface_variant);
                chip.setTextColor(getResources().getColor(R.color.white));
                chip.setChipCornerRadius(12f);
                chipGroup.addView(chip);
            }

            // 🏷 Add ChipGroup to the Ingredients Container
            ingredientsContainer.addView(chipGroup);
        }



    }


    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.primary_variant))
                .setTextColor(getResources().getColor(R.color.white))
                .show();
    }


}