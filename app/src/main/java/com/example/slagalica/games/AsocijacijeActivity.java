package com.example.slagalica.games;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

public class AsocijacijeActivity extends AppCompatActivity {

    private List<Button> buttons;
    private List<EditText> editText;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;

    private CountDownTimer countDownTimer;
    private TextView timerTextView;
    private long timeLeftInMillis = 120000; // 120 seconds

    private int currentColumnIndex;
    private int currentScore;
    private int[] columnScores;

    private EditText input;
    private EditText input1;
    private EditText input2;
    private EditText input3;
    private EditText input4;
//    private String answer1;
//    private String answer2;
//    private String answer3;
//    private String answer4;
    private String answer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_asocijacije);

        timerTextView = findViewById(R.id.time);
        startTimer();

//        PlayersFragment playersFragment = PlayersFragment.newInstance(120);
//
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.fragment_container, playersFragment)
//                .commit();

        Button buttonNext = findViewById(R.id.button_next);
//        input1=findViewById(R.id.input1);
//        input2=findViewById(R.id.input2);
//        input3=findViewById(R.id.input3);
//        input4=findViewById(R.id.input4);
        input=findViewById(R.id.input);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AsocijacijeActivity.this, SkockoActivity.class);

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
        editText = new ArrayList<>();
        buttons.add(findViewById(R.id.button_1));
        buttons.add(findViewById(R.id.button_2));
        buttons.add(findViewById(R.id.button_3));
        buttons.add(findViewById(R.id.button_4));
        editText.add(findViewById(R.id.input1));
        buttons.add(findViewById(R.id.button_5));
        buttons.add(findViewById(R.id.button_6));
        buttons.add(findViewById(R.id.button_7));
        buttons.add(findViewById(R.id.button_8));
        editText.add(findViewById(R.id.input2));
        buttons.add(findViewById(R.id.button_9));
        buttons.add(findViewById(R.id.button_10));
        buttons.add(findViewById(R.id.button_11));
        buttons.add(findViewById(R.id.button_12));
        editText.add(findViewById(R.id.input3));
        buttons.add(findViewById(R.id.button_13));
        buttons.add(findViewById(R.id.button_14));
        buttons.add(findViewById(R.id.button_15));
        buttons.add(findViewById(R.id.button_16));
        editText.add(findViewById(R.id.input4));
        editText.add(findViewById(R.id.input));



        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();

        buttonSteps = new HashMap<>();
        columnScores = new int[4];
        currentColumnIndex = 0;
        currentScore = 0;

        retrieveSteps();
    }

    private void retrieveSteps() {
        firebaseDatabase.getReference("asocijacije").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    List<DataSnapshot> asociationSnapshots = new ArrayList<>();
                    for (DataSnapshot asociationSnapshot : dataSnapshot.getChildren()) {
                        if (asociationSnapshot.getKey().startsWith("asociations")) {
                            asociationSnapshots.add(asociationSnapshot);
                        }
                    }

                    if (!asociationSnapshots.isEmpty()) {
                        int randomIndex = random.nextInt(asociationSnapshots.size());
                        DataSnapshot randomAsociationSnapshot = asociationSnapshots.get(randomIndex);
                        retrieveStep(randomAsociationSnapshot);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void retrieveStep(final DataSnapshot asociationSnapshot) {
        final String asociationKey = asociationSnapshot.getKey();
        firebaseDatabase.getReference("asocijacije/" + asociationKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> asociationsMap = (Map<String, String>) dataSnapshot.getValue();
                if (asociationsMap != null && !asociationsMap.isEmpty()) {
                    for (int i = 1; i <= 16; i++) {
                        String asoc = asociationsMap.get("asociation_field" + i);
                        if (asoc != null) {
                            int buttonIndex = i - 1;
                            if (buttonIndex < buttons.size()) {
                                Button button = buttons.get(buttonIndex);
                                buttonSteps.put(button, asoc);
                                setButtonListener(button);
                            }
                        }

                        answer = asociationsMap.get("Finaly");
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
            public void onClick(View v) {
                String asociation = buttonSteps.get(button);
                if (asociation != null) {
                    button.setText(asociation);
                    button.setEnabled(false);
                }
            }
        });
    }

    private void checkAnswer() {
        Log.d("MyTAG: " , "ovde");
        String userInput = input.getText().toString().trim();
        Log.d("here", userInput);


        if (answer != null && userInput.equalsIgnoreCase(answer)) {
            Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
            for (Button button : buttons) {
                String step = buttonSteps.get(button);
                if (step != null) {
                    button.setText(step);
                    button.setEnabled(false);
                }
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            Toast.makeText(AsocijacijeActivity.this, "Sledi igra MOJ BROJ!", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(AsocijacijeActivity.this, SkockoActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 5000);
        } else{
            Toast.makeText(AsocijacijeActivity.this, "Netacan odgovor!", Toast.LENGTH_SHORT).show();
        }
    }


    private int countUnopenedFields() {
        int unopenedFields = 0;
        for (Button button : buttons) {
            if (button.isEnabled()) {
                unopenedFields++;
            }
        }
        return unopenedFields;
    }

    private void updateScore(int columnIndex, int score) {
        columnScores[columnIndex] = score;
    }

    private void resetButtonsAndInputs() {
        for (Button button : buttons) {
            button.setText("");
            button.setEnabled(true);
        }
        for (EditText input : editText) {
            input.setText("");
            input.setEnabled(false);
        }
    }

    private void showNextColumn() {
        // Increase the current column index
        currentColumnIndex++;

        // Check if all columns have been played
        if (currentColumnIndex >= 4) {
            // Game is finished, calculate final score
            int finalScore = calculateFinalScore();
            Toast.makeText(this, "Final Score: " + finalScore, Toast.LENGTH_SHORT).show();
            // Perform any necessary actions for finishing the game
            // ...

            // Reset the game
            resetGame();
        } else {
            // Reset the buttons and inputs for the next column
            resetButtonsAndInputs();

            // Enable the buttons for the next column
            for (int i = currentColumnIndex * 4; i < (currentColumnIndex + 1) * 4; i++) {
                buttons.get(i).setEnabled(true);
            }
        }
    }

    private int calculateFinalScore() {
        int finalScore = 0;
        for (int score : columnScores) {
            finalScore += score;
        }
        finalScore += 7 + 6 * countUnopenedColumns();
        return finalScore;
    }

    private int countUnopenedColumns() {
        int unopenedColumns = 0;
        for (int i = 0; i < 4; i++) {
            if (columnScores[i] == 0) {
                unopenedColumns++;
            }
        }
        return unopenedColumns;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerText();
            }

            @Override
            public void onFinish() {
                // Timer finished, perform any necessary actions
                // For example, you can show a message or end the game
            }
        }.start();
    }
    private void updateTimerText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
        timerTextView.setText(timeFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void resetGame() {
        resetButtonsAndInputs();
        currentColumnIndex = 0;
        currentScore = 0;
        columnScores = new int[4];
    }
}