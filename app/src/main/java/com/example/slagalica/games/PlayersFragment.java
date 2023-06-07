package com.example.slagalica.games;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.slagalica.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlayersFragment extends Fragment {

    private static final String ARG_TIMER_DURATION = "timer_duration";
    private static final String ARG_GAME_TYPE = "game_type";

    private CountDownTimer countDownTimer;

    private TextView player1PointsTextView;

    private int mTimerDuration;
    private String mGameType;

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
    private void setDescription(TextView descriptionTextView) {
        String description;
        switch (mGameType) {
            case "KorakPoKorak":
                description = "Korak po korak";
                break;
            case "MojBroj":
                description = "Moj broj";
                break;
            case "Asocijacije":
                description = "Asocijacije";
                break;
            case "Slagalica":
                description = "Slagalica";
                break;
            case "Spojnice":
                description = "Spojnice";
                break;
            case "KoZnaZna":
                description = "Ko zna zna";
                break;
            default:
                description = "";
                break;
        }
        descriptionTextView.setText(description);
    }


}