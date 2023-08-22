package com.example.slagalica.games;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.slagalica.MainActivity;
import com.example.slagalica.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlayersFragment extends Fragment {

    private static final String ARG_TIMER_DURATION = "timer_duration";
    private static final String ARG_GAME_TYPE = "game_type";

    private CountDownTimer countDownTimer;
    private FirebaseDatabase firebaseDatabase;
    private TextView player1PointsTextView;

    private int mTimerDuration;
    private String mGameType;
    private DatabaseReference guestPointsRef;
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

            guestPointsRef = firebaseDatabase.getReference("points/guest_points");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.game_fragment_players, container, false);

        int timerDuration = getArguments().getInt(ARG_TIMER_DURATION);
        TextView timeTextView = rootView.findViewById(R.id.time);
        TextView descriptionTextView = rootView.findViewById(R.id.gameDescription);
        player1PointsTextView = rootView.findViewById(R.id.player1Points);
        startTimer(timeTextView, timerDuration);
        setDescription(descriptionTextView);
        setupFirebaseListener();

        return rootView;
    }
    private void setupFirebaseListener() {
        FirebaseDatabase.getInstance().getReference("points/guest_points").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Long points = dataSnapshot.getValue(Long.class);
                    player1PointsTextView.setText(String.valueOf(points));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void startTimer(TextView timeTextView, int timerDuration) {
        CountDownTimer countDownTimer = new CountDownTimer(timerDuration * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished / 1000);
                timeTextView.setText(String.valueOf(time));
            }

            @Override
            public void onFinish() {
                timeTextView.setText("0");
            }
        };

        countDownTimer.start();
    }
    void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Da li ste sigurni?")
                .setMessage("Da li želite da izađete iz igre?")
                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        startActivity(intent);
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
}