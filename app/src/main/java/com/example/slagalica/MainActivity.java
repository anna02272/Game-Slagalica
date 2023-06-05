package com.example.slagalica;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.example.slagalica.games.AsocijacijeActivity;
import com.example.slagalica.games.KoZnaZnaActivity;
import com.example.slagalica.games.KorakPoKorakActivity;
import com.example.slagalica.games.SpojniceActivity;
import com.example.slagalica.login_registration.RegistrationLoginActivity;
import com.example.slagalica.menu.FriendsFragment;
import com.example.slagalica.menu.HomeFragment;
import com.example.slagalica.menu.NotificationFragment;
import com.example.slagalica.menu.ProfileFragment;
import com.example.slagalica.menu.RangFragment;
import com.example.slagalica.menu.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            Button buttonRegister = findViewById(R.id.register);
            Button buttonStartGame = findViewById(R.id.startgame);

            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, RegistrationLoginActivity.class);

                    startActivity(intent);
                }
            });
            buttonStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, AsocijacijeActivity.class);

                    startActivity(intent);
                }


            });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.menu_open_nav, R.string.menu_close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace
                    (R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.menu_home);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.menu_profile:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new ProfileFragment()).commit();
                break;
            case R.id.menu_notification:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new NotificationFragment()).commit();
                break;
            case R.id.menu_friends:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new FriendsFragment()).commit();
                break;
            case R.id.menu_rang:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new RangFragment()).commit();
                break;
            case R.id.menu_settings:
                getSupportFragmentManager().beginTransaction().replace
                        (R.id.fragment_container, new SettingsFragment()).commit();
                break;
            case R.id.menu_logout:
                Toast.makeText(this, "Logout!", Toast.LENGTH_SHORT).show();
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}