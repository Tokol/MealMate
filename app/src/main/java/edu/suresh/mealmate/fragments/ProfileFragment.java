package edu.suresh.mealmate.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;

import edu.suresh.mealmate.R;
import edu.suresh.mealmate.utils.AgeCalculate;


public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private TextView tvUserName, tvGender, tvMobileNumber, ageTv;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        profileImage = view.findViewById(R.id.profileImage);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvGender = view.findViewById(R.id.tvGender);
        tvMobileNumber = view.findViewById(R.id.tvMobileNumber);
        ageTv = view.findViewById(R.id.tvAge);

        Toolbar toolbar = view.findViewById(R.id.profileToolbar);
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

        return view;
    }


    void openEditProfile(){

    }

    void signOutUser(){

    }

    private void loadUserData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("USER_ID", null);
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

}