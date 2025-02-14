package edu.suresh.mealmate.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.suresh.mealmate.R;
import edu.suresh.mealmate.adapters.CategoryExpandableListAdapter;
import edu.suresh.mealmate.adapters.InstructionAdapter;
import edu.suresh.mealmate.model.InstructionStep;
import edu.suresh.mealmate.utils.CustomExpandableListView;



public class AddRecipeFragment extends Fragment {

    private RecyclerView recyclerView;
    private InstructionAdapter instructionAdapter;
    private List<InstructionStep> instructionList;
    private Button addInstructionButton;
    private CustomExpandableListView expandableListView; // Use CustomExpandableListView
    private CategoryExpandableListAdapter expandableListAdapter;
    private List<String> categoryList;
    private HashMap<String, List<String>> ingredientMap;
    private List<String> selectedIngredients = new ArrayList<>();

    private EditText newIngredientInput;
    private Button addIngredientButton;
    private final String othersCategory = "🆕 Others";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.instructionRecyclerView);
        addInstructionButton = view.findViewById(R.id.addInstructionButton);
        expandableListView = view.findViewById(R.id.expandableListView); // Use CustomExpandableListView
        newIngredientInput = view.findViewById(R.id.newIngredientInput);
        addIngredientButton = view.findViewById(R.id.addIngredientButton);

        // Setup Expandable ListView for ingredients
        setupExpandableListView();

        // Setup RecyclerView for instructions
        setupRecyclerView();

        // Handle Add Instruction button click
        addInstructionButton.setOnClickListener(v -> addInstruction());

        addIngredientButton.setOnClickListener(v -> {
            String newIngredient = newIngredientInput.getText().toString().trim();

            if (!newIngredient.isEmpty()) {
                // Check if "Others" category exists, if not, add it
                if (!categoryList.contains(othersCategory)) {
                    categoryList.add(othersCategory);
                    ingredientMap.put(othersCategory, new ArrayList<>());
                }

                String ingredientWithEmoji = "🆕 " + newIngredient;

                // Add new ingredient under "Others" and check it by default
                ingredientMap.get(othersCategory).add(ingredientWithEmoji);
                selectedIngredients.add(ingredientWithEmoji); // ✅ Auto-check it

                // Notify adapter and expand the "Others" category
                expandableListAdapter.notifyDataSetChanged();
                expandableListView.expandGroup(categoryList.indexOf(othersCategory));

                // Clear input field for next entry
                newIngredientInput.setText("");
            } else {
                Toast.makeText(getContext(), "Please enter an ingredient!", Toast.LENGTH_SHORT).show();
            }
        });



        return view;
    }

    /**
     * Sets up the ExpandableListView for selecting ingredients.
     */
    private void setupExpandableListView() {
        categoryList = new ArrayList<>();
        ingredientMap = new HashMap<>();

        // Add categories
        categoryList.add("🥦 Vegetables");
        categoryList.add("🍎 Fruits");
        categoryList.add("🌾 Grains & Legumes");
        categoryList.add("🍗 Proteins");
        categoryList.add("🧀 Dairy");
        categoryList.add("🌿 Herbs & Spices");
        categoryList.add("🛢️ Oils & Condiments"); // ✅ NEW CATEGORY ADDED

        // Add ingredients under each category
        ingredientMap.put("🥦 Vegetables", List.of("🥕 Carrot", "🥦 Broccoli", "🌿 Spinach", "🍅 Tomato", "🧅 Onion", "🧄 Garlic", "🌶 Bell Pepper", "🥒 Zucchini", "🥬 Cabbage", "🥬 Kale", "🥗 Lettuce", "🥔 Cauliflower"));
        ingredientMap.put("🍎 Fruits", List.of("🍏 Apple", "🍌 Banana", "🍊 Orange", "🍓 Strawberries", "🍇 Grapes", "🥭 Mango", "🍍 Pineapple", "🍋 Lemon/Lime"));
        ingredientMap.put("🌾 Grains & Legumes", List.of("🍚 Rice", "🌾 Quinoa", "🥣 Oats", "🌰 Lentils", "🫘 Chickpeas", "🌽 Corn", "🥜 Peanuts"));
        ingredientMap.put("🍗 Proteins", List.of("🍗 Chicken", "🥩 Beef", "🐖 Pork", "🐟 Fish", "🍳 Eggs", "🌱 Tofu", "🫘 Beans"));
        ingredientMap.put("🧀 Dairy", List.of("🥛 Milk", "🍦 Yogurt", "🧀 Cheese", "🧈 Butter", "🥥 Coconut Milk", "🌱 Soy/Oat Milk"));
        ingredientMap.put("🌿 Herbs & Spices", List.of("🌿 Basil", "🌿 Oregano", "🌿 Thyme", "🌿 Rosemary", "🧂 Salt", "🌶 Chili Powder", "🟠 Turmeric", "🟡 Ginger", "🟤 Cumin"));

        // ✅ NEW: Oils & Condiments List
        ingredientMap.put("🛢️ Oils & Condiments", List.of("🫒 Olive Oil", "🥥 Coconut Oil", "🥫 Soy Sauce", "🔥 Hot Sauce", "🍯 Honey", "🥄 Mayonnaise", "🍶 Vinegar", "🧂 Salt", "🍚 Sugar"));

        expandableListAdapter = new CategoryExpandableListAdapter(getContext(), categoryList, ingredientMap, selectedIngredients);

        expandableListView.setAdapter(expandableListAdapter);

        // Handle ingredient selection
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedIngredient = ingredientMap.get(categoryList.get(groupPosition)).get(childPosition);

            if (!selectedIngredients.contains(selectedIngredient)) {
                selectedIngredients.add(selectedIngredient);
                Toast.makeText(getContext(), selectedIngredient + " added!", Toast.LENGTH_SHORT).show();
            } else {
                selectedIngredients.remove(selectedIngredient);
                Toast.makeText(getContext(), selectedIngredient + " removed!", Toast.LENGTH_SHORT).show();
            }

            return true;
        });
    }

    /**
     * Sets up the RecyclerView for dynamic instructions.
     */
    private void setupRecyclerView() {
        instructionList = new ArrayList<>();
        instructionAdapter = new InstructionAdapter(instructionList, position -> removeInstruction(position));

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(instructionAdapter);
    }

    /**
     * Adds a new instruction step dynamically.
     */
    private void addInstruction() {
        instructionList.add(new InstructionStep(instructionList.size() + 1, ""));
        instructionAdapter.notifyItemInserted(instructionList.size() - 1);
    }

    /**
     * Removes an instruction step dynamically.
     */
    private void removeInstruction(int position) {
        instructionList.remove(position);
        instructionAdapter.notifyDataSetChanged();
    }
}