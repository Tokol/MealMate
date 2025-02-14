package edu.suresh.mealmate.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;


import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import edu.suresh.mealmate.EditProfileFromDashboard;
import edu.suresh.mealmate.R;
import edu.suresh.mealmate.home.MainActivity;
import edu.suresh.mealmate.utils.AgeCalculate;


public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView tvUserName, tvGender, tvMobileNumber, ageTv;
    private FirebaseFirestore db;
    String userId;
    private View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = rootView.findViewById(R.id.profileImage);
        tvUserName = rootView.findViewById(R.id.tvUserName);
        tvGender = rootView.findViewById(R.id.tvGender);
        tvMobileNumber = rootView.findViewById(R.id.tvMobileNumber);
        ageTv = rootView.findViewById(R.id.tvAge);
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = rootView.findViewById(R.id.profileToolbar);

        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.profile_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_profile) {
                openEditProfile();
                return true;
            } else if (item.getItemId() == R.id.action_sign_out) {
                signOutUser();
                return true;
            }
            return false;
        });



        loadUserData();

        loadUserDataRemote();

        return rootView;
    }


    void openEditProfile(){
        Intent intent = new Intent(getActivity(), EditProfileFromDashboard.class);
        editProfileLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == AppCompatActivity.RESULT_OK) {
                            loadUserData(); //  Reload profile when returning from EditProfile
                            loadUserDataRemote(); // Fetch latest data from Firebase
                        }
                    });

    void signOutUser() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setCancelable(false) // ✅ Prevents dismissing by tapping outside
                .setPositiveButton("Yes", (dialog, which) -> {
                    // ✅ Firebase Sign Out
                    FirebaseAuth.getInstance().signOut();

                    // ✅ Clear SharedPreferences
                    SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.clear(); // Clears all saved values
                    editor.apply();

                    // ✅ Navigate to MainActivity and clear backstack
                    Intent intent = new Intent(requireActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevents going back to Profile
                    startActivity(intent);
                    requireActivity().finish(); // ✅ Ensures ProfileFragment is fully removed
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // ✅ Fix: Properly dismisses dialog
                .show();
    }



    private void loadUserData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
         userId = sharedPreferences.getString("USER_ID", null);
        String name = sharedPreferences.getString("USER_NAME", null);
        String mobile = sharedPreferences.getString("USER_MOBILE", null);
        String dob = sharedPreferences.getString("USER_DOB", null);
        String gender = sharedPreferences.getString("USER_GENDER", null);
        String photoUrl = sharedPreferences.getString("USER_PHOTO", null);

        tvUserName.setText(name);
        tvGender.setText(gender);
        tvMobileNumber.setText(mobile);
        if (dob != null && !dob.isEmpty()) {
            AgeCalculate ageCalculate = new AgeCalculate();
            int age = ageCalculate.calculateAge(dob);
            ageTv.setText(String.valueOf(age) + " years");
        } else {
            ageTv.setText("N/A");
        }

        if (!photoUrl.isEmpty()) {
            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_men) // Fallback image
                    .error(R.drawable.profile_border) // Error case
                    .into(profileImage);
        }
    }




private void loadUserDataRemote() {
 // Replace with actual User ID
    if (userId == null) {
        showSnackbar("User ID not found in SharedPreferences");
        return;
    }


    DocumentReference docRef = db.collection("Users").document(userId);
    docRef.get().addOnSuccessListener(documentSnapshot -> {
        if (!isAdded()) {
            return; //
        }

        if (documentSnapshot.exists()) {
            String name = documentSnapshot.getString("name");
            String gender = documentSnapshot.getString("gender");
            String mobile = documentSnapshot.getString("mobile");
            String dob = documentSnapshot.getString("dob");
            String photoUrl = documentSnapshot.getString("photoUrl");

            SharedPreferences sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putString("USER_NAME", name);
            editor.putString("USER_MOBILE", mobile);
            editor.putString("USER_DOB", dob);
            editor.putString("USER_GENDER", gender);
            editor.putString("USER_PHOTO", photoUrl);

            loadUserData();

        } else {
            showSnackbar("User data not found on firebase");
        }
    }).addOnFailureListener(e -> showSnackbar("Failed to load data from firebase"));
}

    private void showSnackbar(String message) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
    }

}