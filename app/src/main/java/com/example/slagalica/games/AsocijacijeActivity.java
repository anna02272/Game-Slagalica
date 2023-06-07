package com.example.slagalica.games;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import androidx.core.content.ContextCompat;

import com.example.slagalica.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
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
    private Map<EditText, String> editTexts;
    private Random random;

    private CountDownTimer countDownTimer;
    private TextView timerTextView;
    private long timeLeftInMillis = 120000; // 120 seconds

    private int currentColumnIndex;
    private int currentScore;
    private int[] columnScores;
    private int currentEnabledButtonIndex = 0;
    private int numberField =0;
    private int fieldPoints ;
    private int counter =0;
    private EditText input;
    private EditText input1;
    private EditText input2;
    private EditText input3;
    private EditText input4;
    private String answer1;
    private String answer2;
    private String answer3;
    private String answer4;
    private String answer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_asocijacije);

        timerTextView = findViewById(R.id.time);
        startTimer();
        PlayersFragment playersFragment = PlayersFragment.newInstance(120);
        playersFragment.setGameType("Asocijacija");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();

        Button buttonNext = findViewById(R.id.button_next);
        input1=findViewById(R.id.input1);
        input2=findViewById(R.id.input2);
        input3=findViewById(R.id.input3);
        input4=findViewById(R.id.input4);
        input=findViewById(R.id.input);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(AsocijacijeActivity.this);
                builder.setTitle("Da li ste sigurni?")
                        .setMessage("Da li zelite da izadjete iz igre?")
                        .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(AsocijacijeActivity.this, MainActivity.class);
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
                    positiveButton.setTextColor(ContextCompat.getColor(AsocijacijeActivity.this, R.color.buttonTextColorDark));
                    negativeButton.setTextColor(ContextCompat.getColor(AsocijacijeActivity.this, R.color.buttonTextColorDark));
                } else {
                    positiveButton.setTextColor(ContextCompat.getColor(AsocijacijeActivity.this, R.color.buttonTextColorLight));
                    negativeButton.setTextColor(ContextCompat.getColor(AsocijacijeActivity.this, R.color.buttonTextColorLight));
                }
            }
        });

        Button confirmButton = findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numberField+=1;
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
                        answer1 = asociationsMap.get("A");
                        answer2 = asociationsMap.get("B");
                        answer3 = asociationsMap.get("C");
                        answer4 = asociationsMap.get("D");
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
        String userInput = input.getText().toString().trim();
        String userInput1 = input1.getText().toString().trim();
        String userInput2 = input2.getText().toString().trim();
        String userInput3 = input3.getText().toString().trim();
        String userInput4 = input4.getText().toString().trim();

        if(answer1 !=null || answer2 !=null || answer3 !=null || answer3 !=null ){
            if(answer1 != null && userInput1.equalsIgnoreCase(answer1)){
                Log.d("TAG", answer1);
//                fieldPoints=2+(4-numberField);
//                updateGuestPoints(fieldPoints);
                Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
                calculatePartScore();
            }
            else if (answer2 != null && userInput2.equalsIgnoreCase(answer2)) {
                Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
                calculatePartScore();
            } else if (answer3 != null && userInput3.equalsIgnoreCase(answer3)) {
                Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
                calculatePartScore();
            } else if (answer4 != null && userInput4.equalsIgnoreCase(answer4)) {
                Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
                calculatePartScore();
            } else{Toast.makeText(AsocijacijeActivity.this, "Netacan odgovor!", Toast.LENGTH_SHORT).show();
            }
        }
        if ( answer != null && userInput.equalsIgnoreCase(answer)) {
            Toast.makeText(AsocijacijeActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
            calculateFinalScore();
            for (Button button : buttons) {
                String step = buttonSteps.get(button);
                if (step != null) {
                    button.setText(step);
                    button.setEnabled(false);
                }
                input.setText(answer);
                input1.setText(answer1);
                input2.setText(answer2);
                input3.setText(answer3);
                input4.setText(answer4);
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            Toast.makeText(AsocijacijeActivity.this, "Sledi igra SKOCKO!", Toast.LENGTH_SHORT).show();

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
        Log.d("TAG", String.valueOf(unopenedFields));
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
    private int calculatePartScore() {

        int partScore = 0;
        for (int score : columnScores) {
            partScore += score;
        }
        partScore += 2 + (4-((16-(counter*4))-countUnopenedFields()));
        updateGuestPoints(partScore);
        counter=+1;
        return partScore;
    }
    private int countUnopenedField() {
        int unopenedColumns = 0;
        for (int i = 0; i < 4; i++) {
            if (columnScores[i] == 0 ) {
                unopenedColumns++;
                Log.d("TAG", String.valueOf(unopenedColumns));
            }
        }
        Log.d("TAG", String.valueOf(unopenedColumns));
        return unopenedColumns;
    }

    private int calculateFinalScore() {

        int finalScore = 0;
        for (int score : columnScores) {
            finalScore += score;
        }
        finalScore += 7 + 6 * countUnopenedColumns();
        updateGuestPoints(finalScore-1);
        return finalScore;
    }

    private int countUnopenedColumns() {
        int unopenedColumns = 0;
        for (int i = 0; i < 4; i++) {
            if (columnScores[i] == 0 ) {
                unopenedColumns++;
                Log.d("TAG", String.valueOf(unopenedColumns));
            }
        }
        Log.d("TAG", String.valueOf(unopenedColumns));
        return unopenedColumns;
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
//                updateTimerText();
            }

            @Override
            public void onFinish() {
//                input.setText(answer);
//                Toast.makeText(AsocijacijeActivity.this, "Vase vreme je isteklo, sledi igra SKOCKO!",
//                        Toast.LENGTH_SHORT).show(); ;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(AsocijacijeActivity.this, SkockoActivity.class);
//                        startActivity(intent);
//                    }
//                }, 5000);

            }

        }.start();
    }
//    private void updateTimerText() {
//        int minutes = (int) ( timeLeftInMillis/ 1000) / 60;
//        int seconds = (int) (timeLeftInMillis / 1000) % 60;
//        String timeFormatted = String.format("%02d:%02d", minutes, seconds);
//        timerTextView.setText(timeFormatted);
//    }

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
    private void updateGuestPoints(int fieldPoints) {
        DatabaseReference guestPointsRef = firebaseDatabase.getReference("points/guest_points");

        guestPointsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentPoints = dataSnapshot.getValue(Integer.class);
                    if(fieldPoints<30){
                        int updatedPoints = currentPoints + fieldPoints;
                        guestPointsRef.setValue(updatedPoints);}
                    else{int updatedPoints = 30;
                        guestPointsRef.setValue(updatedPoints);}
                } else {
                    guestPointsRef.setValue(fieldPoints);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}