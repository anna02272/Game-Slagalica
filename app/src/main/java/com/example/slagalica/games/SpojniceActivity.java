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

import com.example.slagalica.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SpojniceActivity extends AppCompatActivity  {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private CountDownTimer countDownTimer;
    private Handler buttonHandler;
    private Runnable buttonRunnable;
    private int currentEnabledButtonIndex = 0;
    private int currentButtonIndex = 1;
    private Map<String, String> stepsMap;
    private PlayersFragment playersFragment;
    private Button firstClickedButton = null;
    private Button secondClickedButton = null;

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
        EditText input = findViewById(R.id.input);



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

        for (int i = 5; i < 10; i++) {
            buttons.get(i).setEnabled(false);
        }

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

    List<Integer> stepIndices = new ArrayList<>();
    List<Integer> answerIndices = new ArrayList<>();
    private void retrieveStep(final DataSnapshot stepSnapshot) {
        final String stepKey = stepSnapshot.getKey();
        currentEnabledButtonIndex = 0;
        firebaseDatabase.getReference("spojnice/" + stepKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> stepsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {
                });
                if (stepsMap != null && !stepsMap.isEmpty()) {
                    SpojniceActivity.this.stepsMap = stepsMap;


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
                            Button stepButton = buttons.get(i);
                            stepButton.setText(step);
                            stepButton.setTag(stepIndex);
                            buttonSteps.put(stepButton, step);
                            setButtonListener(stepButton, step);

                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        int answerIndex = answerIndices.get(i);
                        String answer = stepsMap.get("answer" + answerIndex);
                        if (answer != null) {
                            Button answerButton = buttons.get(i + 5);
                            answerButton.setText(answer);
                            answerButton.setTag(answerIndex);
                            buttonSteps.put(answerButton, answer);
                            setButtonListener(answerButton, answer);

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


    private void setButtonListener(Button button,  final String text) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (firstClickedButton == null) {
                    firstClickedButton = (Button) v;
                    firstClickedButton.setTextColor(Color.parseColor("#FFFF00"));
                    for (int i = 5; i < 10; i++) {
                        buttons.get(i).setEnabled(true);
                    }
                } else if (secondClickedButton == null) {
                    secondClickedButton = (Button) v;
                    checkAnswer();
                    for (int i = 5; i < 10; i++) {
                        buttons.get(i).setEnabled(false);
                    }
                }
            }
        });
    }


    private void checkAnswer() {
        if (firstClickedButton == null || secondClickedButton == null) {
            return;
        }
        int clickedStepIndex = (Integer) firstClickedButton.getTag();
        int clickedAnswerIndex = (Integer) secondClickedButton.getTag();

        if (clickedStepIndex == clickedAnswerIndex) {
            firstClickedButton.setTextColor(Color.parseColor("#00FF00"));
            secondClickedButton.setTextColor(Color.parseColor("#00FF00"));
            firstClickedButton.setClickable(false);
            secondClickedButton.setClickable(false);
            updatePoints(2);

        } else {
            firstClickedButton.setTextColor(Color.parseColor("#FF0000"));
            firstClickedButton.setClickable(false);
            secondClickedButton.setTextColor(Color.parseColor("#FF0000"));
            checkIfGameIsFinished();
        }

        firstClickedButton = null;
        secondClickedButton = null;
    }
    private void checkIfGameIsFinished() {
        boolean allButtonsClickable = true;
        for (int i = 0; i < 5; i++) {
            if (buttons.get(i).isClickable()) {
                allButtonsClickable = false;
                break;
            }
        }

        if (allButtonsClickable) {
            Toast.makeText(SpojniceActivity.this, "Igra je gotova! Sledi igra ASOCIJACIJE!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SpojniceActivity.this, AsocijacijeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 5000);
        }
    }


    private void updatePoints(int points) {
        playersFragment.updateGuestPoints(points);
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(31000, 10000) {
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