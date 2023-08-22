package com.example.slagalica;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.slagalica.config.SocketHandler;
import com.example.slagalica.games.KoZnaZnaActivity;
import com.example.slagalica.games.SpojniceActivity;
import com.example.slagalica.login_registration.RegistrationLoginActivity;
import com.example.slagalica.menu.FriendsFragment;
import com.example.slagalica.menu.HomeFragment;
import com.example.slagalica.menu.NotificationFragment;
import com.example.slagalica.menu.ProfileFragment;
import com.example.slagalica.menu.RangFragment;
import com.example.slagalica.menu.SettingsFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    public static Socket socket;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            SocketHandler.setSocket();

            socket = SocketHandler.getSocket();
            socket.connect();

             FirebaseAuth auth = FirebaseAuth.getInstance();
            Button buttonRegister = findViewById(R.id.register);
            Button buttonStartGameGuest = findViewById(R.id.startgameguest);
            Button buttonStartGame = findViewById(R.id.startgame);
            View navView = findViewById(R.id.nav_view);

            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                buttonRegister.setVisibility(View.GONE);
                buttonStartGameGuest.setVisibility(View.GONE);
                buttonStartGame.setVisibility(View.VISIBLE);
                navView.setVisibility(View.VISIBLE);
            } else {
                buttonRegister.setVisibility(View.VISIBLE);
                buttonStartGameGuest.setVisibility(View.VISIBLE);
                buttonStartGame.setVisibility(View.GONE);
                navView.setVisibility(View.GONE);
            }
            buttonRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(MainActivity.this, RegistrationLoginActivity.class);

                    startActivity(intent);
                }
            });
            buttonStartGameGuest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    firebaseDatabase.getReference("points/guest_points").setValue(0);
                    Intent intent = new Intent(MainActivity.this, KoZnaZnaActivity.class);

                    startActivity(intent);
                }


            });
            SharedPreferences preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String username = preferences.getString("username", "");
            String email = preferences.getString("email", "");


            Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);

        TextView headerUsername = headerView.findViewById(R.id.header_username);
        TextView headerEmail = headerView.findViewById(R.id.header_email);
        headerUsername.setText(username);
        headerEmail.setText(email);


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
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Successfully logged out!", Toast.LENGTH_SHORT).show();
                return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);

        return false;
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