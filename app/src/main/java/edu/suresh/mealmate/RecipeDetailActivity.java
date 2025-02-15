package edu.suresh.mealmate;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;
import java.util.Map;

import edu.suresh.mealmate.model.Recipe;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recipe_detail);

        Recipe recipe = getIntent().getParcelableExtra("RECIPE");

        if (recipe != null) {
            // Use the recipe object to populate the UI
            String recipeName = recipe.getRecipeName();
            String cookTime = recipe.getCookTime();
            String photoUrl = recipe.getPhotoUrl();
            Map<String, List<String>> ingredients = recipe.getIngredients();
            List<Map<String, Object>> instructions = recipe.getInstructions();


            Log.d("recipeName", recipeName);
            Log.d("cookTime Detail", cookTime);
            Log.d("photoUrl", photoUrl);
            Log.d("ingredients", ingredients.toString());
            Log.d("instructions", instructions.toString());


            // Update the UI with recipe details
            // Example: Set recipe name in a TextView
            // TextView recipeNameTextView = findViewById(R.id.recipeNameTextView);
            // recipeNameTextView.setText(recipeName);
        }

    }
}