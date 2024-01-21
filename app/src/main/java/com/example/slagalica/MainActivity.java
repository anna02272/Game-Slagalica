package com.example.slagalica;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.example.slagalica.games.KorakPoKorakActivity;
import com.example.slagalica.games.MojBrojActivity;
import com.example.slagalica.games.SpojniceActivity;
import com.example.slagalica.games.StartMultiplayerGameActivity;
import com.example.slagalica.login_registration.RegistrationLoginActivity;
import com.example.slagalica.login_registration.User;
import com.example.slagalica.menu.FriendsFragment;
import com.example.slagalica.menu.HomeFragment;
import com.example.slagalica.menu.NotificationFragment;
import com.example.slagalica.menu.ProfileFragment;
import com.example.slagalica.menu.RangFragment;
import com.example.slagalica.menu.SettingsFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    public static Socket socket;
    private String username;
    private String email;
    private int tokens;
    private String date;
    private int stars;
    private int playedGames;
    private FirebaseUser currentUser;
    private   SharedPreferences preferences;
    private String userId;
    private DatabaseReference usersRef;
    private  TextView tokenText;
    private  Button buttonStartGame;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

            SocketHandler.setSocket();

            socket = SocketHandler.getSocket();
            socket.connect();

            Button buttonRegister = findViewById(R.id.register);
            Button buttonStartGameGuest = findViewById(R.id.startgameguest);
             buttonStartGame = findViewById(R.id.startgame);
            View navView = findViewById(R.id.nav_view);

            ImageView tokenImage = findViewById(R.id.tokens_image);
            ImageView starImage = findViewById(R.id.stars_image);
             tokenText = findViewById(R.id.tokens_text);
            TextView starText = findViewById(R.id.stars_text);

            FirebaseAuth auth = FirebaseAuth.getInstance();
             currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                buttonRegister.setVisibility(View.GONE);
                buttonStartGameGuest.setVisibility(View.GONE);
                buttonStartGame.setVisibility(View.VISIBLE);
                navView.setVisibility(View.VISIBLE);
                tokenImage.setVisibility(View.VISIBLE);
                starImage.setVisibility(View.VISIBLE);
                tokenText.setVisibility(View.VISIBLE);
                starText.setVisibility(View.VISIBLE);
            } else {
                buttonRegister.setVisibility(View.VISIBLE);
                buttonStartGameGuest.setVisibility(View.VISIBLE);
                buttonStartGame.setVisibility(View.GONE);
                navView.setVisibility(View.GONE);
                tokenImage.setVisibility(View.GONE);
                starImage.setVisibility(View.GONE);
                tokenText.setVisibility(View.GONE);
                starText.setVisibility(View.GONE);
            }
            if (currentUser != null) {
                userId = currentUser.getUid();

             usersRef = firebaseDatabase.getReference("users");
            usersRef.child(userId).child("tokens").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        tokens = dataSnapshot.getValue(Integer.class);
                        String tokensText = String.valueOf(tokens);
                        tokenText.setText(tokensText);
                        if (tokens == 0) {
                            buttonStartGame.setEnabled(false);
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
                usersRef.child(userId).child("date").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            date = dataSnapshot.getValue(String.class);
                            getDate();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            usersRef.child(userId).child("stars").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        stars = dataSnapshot.getValue(Integer.class);
                        int tokensEarned = stars / 50;


                        if (tokensEarned != 0) {
                            usersRef.child(userId).child("tokens").setValue(tokens + tokensEarned);
                            usersRef.child(userId).child("stars").setValue(stars % 50);

                            String tokensText = String.valueOf(tokens + tokensEarned);
                            tokenText.setText(tokensText);

                            String starsText = String.valueOf(stars % 50);
                            starText.setText(starsText);
                        } else {
                            String starsText = String.valueOf(stars);
                            starText.setText(starsText);
                        }
                    } else {
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
            usersRef.child(userId).child("playedGames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        playedGames = dataSnapshot.getValue(Integer.class);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
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
                    if (currentUser == null) {
                        firebaseDatabase.getReference("points/guest_points").setValue(0);
                    }
                    Intent intent = new Intent(MainActivity.this, SpojniceActivity.class);
                    startActivity(intent);
                }
            });

            buttonStartGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUser != null) {
                        firebaseDatabase.getReference("points/player1_points").setValue(0);
                        firebaseDatabase.getReference("points/player2_points").setValue(0);
                    }
                    StartMultiplayerGameActivity start = new StartMultiplayerGameActivity(MainActivity.this);
                    start.showPopupWindow(v);
                }
            });
                socket.on("startActualGame", new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        try {
                            socket.emit("userDisconnected", new JSONObject().put("username", username));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        Intent intent = new Intent(MainActivity.this, SpojniceActivity.class);
                        startActivity(intent);


                        if (tokens > 0) {
                            int newTokens = tokens - 1;
                            int newPlayedGames = playedGames + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usersRef.child(userId).child("tokens").setValue(newTokens)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                    } else {
                                                    }
                                                }
                                            });
                                    tokens = newTokens;
                                    tokenText.setText(String.valueOf(tokens));

                                    usersRef.child(userId).child("playedGames").setValue(newPlayedGames)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                    } else {
                                                    }
                                                }
                                            });
                                }
                            });

                        }
                    }
                });

            preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            username = preferences.getString("username", "");
            email = preferences.getString("email", "");


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
                Toast.makeText(this, "Uspešno ste odjavljeni!", Toast.LENGTH_SHORT).show();
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

    private void getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());

        try {
            Date storedDate = dateFormat.parse(date);
            Date today = dateFormat.parse(currentDate);

            int daysDifference = daysBetween(storedDate, today);

            int newTokens = tokens + (5 * daysDifference);

            updateTokensAndDate(userId, newTokens, currentDate);

            String tokensText = String.valueOf(newTokens);
            tokenText.setText(tokensText);
            if (newTokens == 0) {
                buttonStartGame.setEnabled(false);
            } else{
                buttonStartGame.setEnabled(true);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
        private int daysBetween(Date startDate, Date endDate) {
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(startDate);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(endDate);

        long milliseconds = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        return (int) (milliseconds / (24 * 60 * 60 * 1000));
    }

    private void updateTokensAndDate(String userId, int newTokens, String currentDate) {
        DatabaseReference userRef = firebaseDatabase.getReference("users").child(userId);
        userRef.child("tokens").setValue(newTokens);
        userRef.child("date").setValue(currentDate);
        Log.d("USerID" , "userID: " + userId);
        Log.d("USerID" , "tokens: " + newTokens);
        Log.d("USerID" , "date: " + currentDate);
    }
}