package com.example.slagalica.games;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.slagalica.R;

public class SpojniceActivity extends AppCompatActivity {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private CountDownTimer countDownTimer;
    private EditText input;
    private Handler buttonHandler;
    private Runnable buttonRunnable;
    private int currentEnabledButtonIndex = 0;
    private int currentButtonIndex = 1;
    private Map<String, String> stepsMap;
    private PlayersFragment playersFragment;

    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_spojnice);

         playersFragment = PlayersFragment.newInstance(31);
        playersFragment.setGameType("Spojnice");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();


        Button buttonNext = findViewById(R.id.button_next);
        input = findViewById(R.id.input);
        buttonNext.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playersFragment.showExitConfirmationDialog();
                }
            });

        buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.button_1));
        buttons.add(findViewById(R.id.button_2));
        buttons.add(findViewById(R.id.button_3));
        buttons.add(findViewById(R.id.button_4));
        buttons.add(findViewById(R.id.button_5));
        buttons.add(findViewById(R.id.button_6));
        buttons.add(findViewById(R.id.button_7));
        buttons.add(findViewById(R.id.button_8));
        buttons.add(findViewById(R.id.button_9));
        buttons.add(findViewById(R.id.button_10));

        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();
        buttonSteps = new HashMap<>();

        retrieveSteps();

        buttonHandler = new Handler();
        buttonRunnable = new Runnable() {
            @Override
            public void run() {
            }
            };

    }

    private void retrieveSteps() {
        firebaseDatabase.getReference("spojnice").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    List<DataSnapshot> stepSnapshots = new ArrayList<>();
                    for (DataSnapshot stepSnapshot : dataSnapshot.getChildren()) {
                        if (stepSnapshot.getKey().startsWith("spojnica")) {
                            stepSnapshots.add(stepSnapshot);
                        }
                    }

                    if (!stepSnapshots.isEmpty()) {
                        int randomIndex = random.nextInt(stepSnapshots.size());
                        DataSnapshot randomStepSnapshot = stepSnapshots.get(randomIndex);
                        retrieveStep(randomStepSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void retrieveStep(final DataSnapshot stepSnapshot) {
        final String stepKey = stepSnapshot.getKey();
        currentEnabledButtonIndex = 0;

        firebaseDatabase.getReference("spojnice/" + stepKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> stepsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {});
                if (stepsMap != null && !stepsMap.isEmpty()) {
                    SpojniceActivity.this.stepsMap = stepsMap;
                    List<Integer> stepIndices = new ArrayList<>();
                    List<Integer> answerIndices = new ArrayList<>();

                    for (int i = 1; i <= 5; i++) {
                        stepIndices.add(i);
                        answerIndices.add(i);
                    }

                    Collections.shuffle(stepIndices);
                    Collections.shuffle(answerIndices);

                    for (int i = 0; i < 5; i++) {
                        int stepIndex = stepIndices.get(i);
                        String step = stepsMap.get("step" + stepIndex);
                        if (step != null) {
                            Button button = buttons.get(i);
                            button.setText(step);
                            buttonSteps.put(button, step);
                            setButtonListener(button);
//                            button.setEnabled(false);

                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        int answerIndex = answerIndices.get(i);
                        String answer = stepsMap.get("answer" + answerIndex);
                        if (answer != null) {
                            Button button = buttons.get(i + 5);
                            button.setText(answer);
                            buttonSteps.put(button, answer);
                            setButtonListener(button);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void setButtonListener(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(button);
            }
        });
    }

    private void checkAnswer(Button button) {

    }


    private void updatePoints(int points) {
        playersFragment.updateGuestPoints(points);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(91000, 10000) {
            private Context context = SpojniceActivity.this.getApplicationContext();
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                Toast.makeText(SpojniceActivity.this, "Vase vreme je isteklo, sledi igra Asocijacije!",
                        Toast.LENGTH_SHORT).show(); ;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(SpojniceActivity.this, AsocijacijeActivity.class);
                        startActivity(intent);
                    }
                }, 5000);

            }
        };

        countDownTimer.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        buttonHandler.postDelayed(buttonRunnable, 10000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        buttonHandler.removeCallbacks(buttonRunnable);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}