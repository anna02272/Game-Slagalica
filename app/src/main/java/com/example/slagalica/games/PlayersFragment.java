package com.example.slagalica.games;

import static com.example.slagalica.MainActivity.socket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.slagalica.MainActivity;
import com.example.slagalica.R;
import com.example.slagalica.config.SocketHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.socket.emitter.Emitter;

public class PlayersFragment extends Fragment {
    private static final String ARG_TIMER_DURATION = "timer_duration";
    private static final String ARG_GAME_TYPE = "game_type";

    private CountDownTimer countDownTimer;
    private FirebaseDatabase firebaseDatabase;
    TextView player1PointsTextView;
    TextView player2PointsTextView;
    private int mTimerDuration;
    private String mGameType;
    private DatabaseReference guestPointsRef;
    private DatabaseReference player1PointsRef;
    private DatabaseReference player2PointsRef;
    private SharedPreferences preferences;
    private View rootView;
    public static JSONArray playingUsernamesArray;
    private   String username;
    private String player1Username;
    private String player2Username;
    private FirebaseUser currentUser;
    TextView player1UsernameTextView;
    TextView player2UsernameTextView;
    ImageView player1ImageView;
    ImageView player2ImageView;
    private  int timerDuration;
    private  TextView timeTextView;
    public PlayersFragment() {
    }

    public static PlayersFragment newInstance(int timerDuration) {
        PlayersFragment fragment = new PlayersFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TIMER_DURATION, timerDuration);
        fragment.setArguments(args);
        return fragment;
    }

    public void setGameType(String gameType) {
        mGameType = gameType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTimerDuration = getArguments().getInt(ARG_TIMER_DURATION);
            firebaseDatabase = FirebaseDatabase.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();
            currentUser = auth.getCurrentUser();
            preferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            username = preferences.getString("username", "");

            if (currentUser == null) {
                guestPointsRef = firebaseDatabase.getReference("points/guest_points");
            }
            if (currentUser != null) {
                player1PointsRef = firebaseDatabase.getReference("points/player1_points");
                player2PointsRef = firebaseDatabase.getReference("points/player2_points");

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.game_fragment_players, container, false);

         timerDuration = getArguments().getInt(ARG_TIMER_DURATION);
         timeTextView = rootView.findViewById(R.id.time);
        TextView descriptionTextView = rootView.findViewById(R.id.gameDescription);
        player1PointsTextView = rootView.findViewById(R.id.player1Points);
        player2PointsTextView = rootView.findViewById(R.id.player2Points);

        startTimer(timeTextView, timerDuration);
        setDescription(descriptionTextView);
        setupFirebaseListener();

         player1UsernameTextView = rootView.findViewById(R.id.player1Username);
         player2UsernameTextView = rootView.findViewById(R.id.player2Username);
        player1ImageView = rootView.findViewById(R.id.player1Image);
        player2ImageView = rootView.findViewById(R.id.player2Image);
        if (currentUser != null) {
            try {
                socket.emit("userPlaying", new JSONObject().put("username", username));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            socket.on("updatePlayingUsers", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    playingUsernamesArray = (JSONArray) args[0];
                    retrieveConnectedUsers();
                    requireActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Activity activity = getActivity();
                            if(activity != null){
                                if (player1Username != null && player2Username != null) {
                                    player1UsernameTextView.setText(player1Username);
                                    player2UsernameTextView.setText(player2Username);
                                    loadProfileImages(player1Username, player2Username);
                                }
                            }
                        }
                    });
                }
            });
        }

        return rootView;
    }
    private void startTimer(TextView timeTextView, int timerDuration) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (currentUser != null) {
            socket.on("syncTimer", onSyncTimer);
        }
        countDownTimer = new CountDownTimer(timerDuration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished / 1000);
                Activity activity = getActivity();
                if(activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeTextView.setText(String.valueOf(time));
                        }
                    });
                }
            }

            @Override
            public void onFinish() {
                Activity activity = getActivity();
                if(activity != null){
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timeTextView.setText("0");
                        }
                    });
                }
            }
        };

        countDownTimer.start();
    }
    private Emitter.Listener onSyncTimer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject timerData = (JSONObject) args[0];
            int syncedDuration = 0;
            try {
                syncedDuration = timerData.getInt("duration");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            updateTimerDuration(syncedDuration);

        }
    };
    public void updateTimerDuration(int newDuration) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerDuration = newDuration;
        Activity activity = getActivity();
        if(activity != null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startTimer(timeTextView, timerDuration);
                }
            });
        }
        }
    private void retrieveConnectedUsers() {

        if (playingUsernamesArray.length() >= 2) {
            player1Username = null;
            try {
                player1Username = playingUsernamesArray.getString(0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
             player2Username = null;
            try {
                player2Username = playingUsernamesArray.getString(1);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }



        }
    }
    private void setupFirebaseListener() {
        if (currentUser == null) {
            FirebaseDatabase.getInstance().getReference("points/guest_points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long guestPoints = dataSnapshot.getValue(Long.class);
                            player1PointsTextView.setText(String.valueOf(guestPoints));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference("points/player1_points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long player1Points = dataSnapshot.getValue(Long.class);
                        player1PointsTextView.setText(String.valueOf(player1Points));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
            FirebaseDatabase.getInstance().getReference("points/player2_points").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Long player2Points = dataSnapshot.getValue(Long.class);
                            player2PointsTextView.setText(String.valueOf(player2Points));
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }

    }
    void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Da li ste sigurni?")
                .setMessage("Da li želite da izađete iz igre?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
                        if (currentUser != null) {
                            updateLostGamesCount(username);
                            try {
                                socket.emit("playerDisconnected", new JSONObject().put("username", username));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                    }
                    }
                })
                .setNegativeButton("Odustani", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        AlertDialog dialog = builder.show();

        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);

        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextColorDark));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextColorDark));
        } else {
            positiveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextColorLight));
            negativeButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.buttonTextColorLight));
        }
    }
    private void setDescription(TextView descriptionTextView) {
        String description;
        switch (mGameType) {
            case "KorakPoKorak":
                description = "Korak po korak: Pronadji resenje na osnovu koraka";
                break;
            case "MojBroj":
                description = "Moj broj: Pronadji tacno resenje";
                break;
            case "Asocijacije":
                description = "Asocijacije: Pronadji resenje asocijacije";
                break;
            case "Skocko":
                description = "Skocko: Pronadji resenje skocka";
                break;
            case "Spojnice":
                description = "Spojnice: Povezi pojmove";
                break;
            case "KoZnaZna":
                description = "Ko zna zna: Pronadji odgovor na pitanje";
                break;
            default:
                description = "";
                break;
        }
        descriptionTextView.setText(description);
    }
    void updateGuestPoints(int pointsToAdd) {
        DatabaseReference guestPointsRef = firebaseDatabase.getReference("points/guest_points");

        guestPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentPoints = dataSnapshot.getValue(Integer.class);
                    int updatedPoints = currentPoints + pointsToAdd;
                    guestPointsRef.setValue(updatedPoints);
                } else {
                    guestPointsRef.setValue(pointsToAdd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    void updatePlayerPoints(int playerNumber, int pointsToAdd) {
        DatabaseReference playerPointsRef = firebaseDatabase.getReference("points/player" + playerNumber + "_points");

        playerPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentPoints = dataSnapshot.getValue(Integer.class);
                    int updatedPoints = currentPoints + pointsToAdd;
                    playerPointsRef.setValue(updatedPoints);
                } else {
                    playerPointsRef.setValue(pointsToAdd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
    private void updateLostGamesCount(String username) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = usersRef.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    DatabaseReference ref = usersRef.child(userId);
                    ref.child("lostGames").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int lostGames = dataSnapshot.getValue(Integer.class);
                                ref.child("lostGames").setValue(lostGames + 1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private void loadProfileImages(String player1, String player2) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query player1Query = usersRef.orderByChild("username").equalTo(player1);
        Query player2Query = usersRef.orderByChild("username").equalTo(player2);
        player1Query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String player1UserId = userSnapshot.getKey();

                    DatabaseReference player1Ref = usersRef.child(player1UserId);
                    player1Ref.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String imageUrl = dataSnapshot.getValue(String.class);
                                if (imageUrl != null) {
                                    Picasso.get().load(imageUrl).into(player1ImageView);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

        player2Query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String player2UserId = userSnapshot.getKey();

                    DatabaseReference player2Ref = usersRef.child(player2UserId);
                    player2Ref.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String imageUrl = dataSnapshot.getValue(String.class);
                                if (imageUrl != null) {
                                    Picasso.get().load(imageUrl).into(player2ImageView);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });

    }

    }
