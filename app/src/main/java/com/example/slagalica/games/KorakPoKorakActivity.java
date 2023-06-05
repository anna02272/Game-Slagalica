package com.example.slagalica.games;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.slagalica.R;

public class KorakPoKorakActivity extends AppCompatActivity {

    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private CountDownTimer countDownTimer;
    private int currentCount = 7;
    private String currentStepAnswer;
    private EditText input;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_korak_po_korak);

        PlayersFragment playersFragment = PlayersFragment.newInstance(71);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();


        Button buttonNext = findViewById(R.id.button_next);
        input = findViewById(R.id.input);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KorakPoKorakActivity.this, MojBrojActivity.class);
                startActivity(intent);
            }
        });
        Button confirmButton = findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer();
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
        buttons.add(findViewById(R.id.button_11));
        buttons.add(findViewById(R.id.button_12));
        buttons.add(findViewById(R.id.button_13));
        buttons.add(findViewById(R.id.button_14));

        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();

        buttonSteps = new HashMap<>();

        retrieveSteps();
    }

    private void retrieveSteps() {
        firebaseDatabase.getReference("korak_po_korak").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    List<DataSnapshot> stepSnapshots = new ArrayList<>();
                    for (DataSnapshot stepSnapshot : dataSnapshot.getChildren()) {
                        if (stepSnapshot.getKey().startsWith("steps")) {
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
        firebaseDatabase.getReference("korak_po_korak/" + stepKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> stepsMap = (Map<String, String>) dataSnapshot.getValue();
                if (stepsMap != null && !stepsMap.isEmpty()) {
                    for (int i = 1; i <= 7; i++) {
                        String step = stepsMap.get("step" + i);
                        if (step != null) {
                            int buttonIndex = i - 1;
                            if (buttonIndex < buttons.size()) {
                                Button button = buttons.get(buttonIndex);
                                buttonSteps.put(button, step);
                                setButtonListener(button);
                                if (stepKey.equals("steps" + (currentCount ))) {
                                    currentStepAnswer = stepsMap.get("answer");
                                }
                            }
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


    private void checkAnswer() {
        String userInput = input.getText().toString().trim();

        if (userInput.equalsIgnoreCase(currentStepAnswer)) {
            Toast.makeText(KorakPoKorakActivity.this, "Correct answer!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(KorakPoKorakActivity.this, "Incorrect answer!", Toast.LENGTH_SHORT).show();
        }
    }


    private void setButtonListener(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String step = buttonSteps.get(button);
                if (step != null) {
                    button.setText(step);
                    button.setEnabled(false);
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(70000, 10000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int time = (int) (millisUntilFinished / 1000);
                int buttonIndex = currentCount ;
                if (buttonIndex >= 7 && buttonIndex < buttons.size()) {
                    Button button = buttons.get(buttonIndex);
                    button.setText(String.valueOf(time));
                }
                currentCount++;
            }

            @Override
            public void onFinish() {

            }
        };

        countDownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}