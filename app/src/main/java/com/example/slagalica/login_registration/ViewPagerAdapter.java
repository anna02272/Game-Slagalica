package com.example.slagalica.login_registration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.slagalica.login_registration.LoginFragment;
import com.example.slagalica.login_registration.RegistrationFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1){
            return new RegistrationFragment();
        }
        return new LoginFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}