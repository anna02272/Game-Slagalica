package com.example.slagalica.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.slagalica.MainActivity;

import com.example.slagalica.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MojBrojActivity extends AppCompatActivity {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private int currentEnabledButtonIndex = 0;
    private String answer;

    private String finalAnswer;
    private Button stopButton;

    private Button buttonAnswer;
    private Button confirmButton;
    private CountDownTimer countDownTimer;
    private EditText input;

    private ShakeDetector shakeDetector;

    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_moj_broj);

        PlayersFragment playersFragment = PlayersFragment.newInstance(61);
        playersFragment.setGameType("MojBroj");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();

        shakeDetector = new ShakeDetector();
        shakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                currentEnabledButtonIndex++;
                showButtons();
            }
        });

        buttonAnswer = findViewById(R.id.button_answer);
        Button buttonEnd = findViewById(R.id.button_end);

        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersFragment.showExitConfirmationDialog();
            }
        });

         confirmButton = findViewById(R.id.button_confirm);
        stopButton = findViewById(R.id.button_stop);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkAnswer();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentEnabledButtonIndex++;
                showButtons();
            }
        });


        input = findViewById(R.id.input1);
        Drawable clearDrawable = getResources().getDrawable(R.drawable.game_clear);

        clearDrawable.setBounds(0, 0, clearDrawable.getIntrinsicWidth(), clearDrawable.getIntrinsicHeight());
        input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, clearDrawable, null);

        input.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getX() >= (v.getWidth() - v.getPaddingRight() - clearDrawable.getIntrinsicWidth())) {
                        input.setText("");
                        enableAllButtons();
                        return true;
                    }
                }

                int inType = input.getInputType();
                input.setInputType(InputType.TYPE_NULL);
                input.onTouchEvent(event);
                input.setInputType(inType);
                return true;
            }
        });

        buttons = new ArrayList<>();
        buttons.add(findViewById(R.id.button_number1));
        buttons.add(findViewById(R.id.button_number2));
        buttons.add(findViewById(R.id.button_number3));
        buttons.add(findViewById(R.id.button_number4));
        buttons.add(findViewById(R.id.button_number5));
        buttons.add(findViewById(R.id.button_number6));
        buttons.add(findViewById(R.id.button_number7));
        buttons.add(findViewById(R.id.button_plus));
        buttons.add(findViewById(R.id.button_minus));
        buttons.add(findViewById(R.id.button_multiple));
        buttons.add(findViewById(R.id.button_divide));
        buttons.add(findViewById(R.id.button_open));
        buttons.add(findViewById(R.id.button_close));

        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();

        buttonSteps = new HashMap<>();

        retrieveNumbers();

        for (int i = 0; i < buttons.size(); i++) {
            final int buttonIndex = i;
            final Button button = buttons.get(i);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (buttonIndex != 6) {
                        String buttonText = buttonSteps.get(button);
                        String currentInput = input.getText().toString();
                        input.setText(currentInput + buttonText);
                    }
                    if (buttonIndex < 6) {
                        button.setEnabled(false);
                    }
                }
            });

        }

    }

        private void retrieveNumbers () {
            firebaseDatabase.getReference("moj_broj").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        DataSnapshot operationsSnapshot = dataSnapshot.child("operations");

                        for (DataSnapshot operationSnapshot : operationsSnapshot.getChildren()) {
                            String operationName = operationSnapshot.getKey();
                            String operationSymbol = operationSnapshot.getValue(String.class);
                            Button operationButton = getButtonByName(operationName);
                            if (operationButton != null) {
                                operationButton.setText(operationSymbol);
                                buttonSteps.put(operationButton, operationSymbol);
                            }
                        }

                        List<DataSnapshot> stepSnapshots = new ArrayList<>();
                        for (DataSnapshot stepSnapshot : dataSnapshot.getChildren()) {
                            if (stepSnapshot.getKey().startsWith("numbers")) {
                                stepSnapshots.add(stepSnapshot);
                            }
                        }

                        if (!stepSnapshots.isEmpty()) {
                            int randomIndex = random.nextInt(stepSnapshots.size());
                            DataSnapshot randomStepSnapshot = stepSnapshots.get(randomIndex);
                            retrieveNumber(randomStepSnapshot);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    private void retrieveNumber(final DataSnapshot stepSnapshot) {
        final String numKey = stepSnapshot.getKey();
        currentEnabledButtonIndex = 0;
        firebaseDatabase.getReference("moj_broj/" + numKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> stepsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {});
                if (stepsMap != null && !stepsMap.isEmpty()) {
                    for (int i = 1; i <= 7; i++) {
                        String number = stepsMap.get("number" + i);
                        if (number != null) {
                            int buttonIndex = i - 1;
                            if (buttonIndex < buttons.size()) {
                                Button button = buttons.get(buttonIndex);
                                buttonSteps.put(button, number);
                            }
                        }
                    }
                    answer = stepsMap.get("number7");
                    finalAnswer = stepsMap.get("number8");

                    if (currentEnabledButtonIndex > 0) {
                        showButtons();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }
    private void showButtons() {
        if (currentEnabledButtonIndex == 1) {
            Button button7 = buttons.get(6);
            String buttonText7 = buttonSteps.get(button7);
            button7.setText(buttonText7);
        } else if (currentEnabledButtonIndex > 1 ) {
            Button button = buttons.get(currentEnabledButtonIndex-2);
            String buttonText = buttonSteps.get(button);
            button.setText(buttonText);
            if (currentEnabledButtonIndex == 7) {
                stopButton.setEnabled(false);
            }
        }
    }



    private Button getButtonByName(String name) {
            for (Button button : buttons) {
                if (button.getResources().getResourceEntryName(button.getId()).equals("button_" + name)) {
                    return button;
                }
            }
            return null;
        }

    String getResult(String data){
        try{
            org.mozilla.javascript.Context context  = org.mozilla.javascript.Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalResult =  context.evaluateString(scriptable,data,"Javascript",1,null).toString();
            if(finalResult.endsWith(".0")){
                finalResult = finalResult.replace(".0","");
            }
            return finalResult;
        }catch (Exception e){
            return "Err";
        }
    }

    private void checkAnswer() {
        String userInput = input.getText().toString().trim();
        if (userInput.isEmpty()) {
            return;
        }
        String finalResult = getResult(userInput);
        buttonAnswer.setText(finalResult);
        if (answer != null && finalResult.equals(answer)) {
            updateGuestPoints(20);
            if(!finalResult.equals("Err")){
                Toast.makeText(MojBrojActivity.this,  finalResult + " :  Tacan odgovor!", Toast.LENGTH_SHORT).show();
            }

        } else {
            if (!finalResult.equals("Err")) {
                input.setText(finalAnswer);
                Toast.makeText(MojBrojActivity.this, finalResult + " :  Netacan odgovor!", Toast.LENGTH_SHORT).show();
            }
        }
            Toast.makeText(MojBrojActivity.this, "Kraj igre!", Toast.LENGTH_SHORT).show();
            confirmButton.setEnabled(false);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 5000);
        }

    private void updateGuestPoints(int pointsToAdd) {
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

    private void enableAllButtons() {
        for (Button button : buttons) {
            button.setEnabled(true);
        }
    }


    private void startTimer() {
        countDownTimer = new CountDownTimer(61000, 10000) {
            private Context context = MojBrojActivity.this.getApplicationContext();
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                input.setText(finalAnswer);
                Toast.makeText(MojBrojActivity.this, "Vase vreme je isteklo, kraj igre",
                        Toast.LENGTH_SHORT).show(); ;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
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
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(shakeDetector, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setButtonTextForAllButtons();
            }
        }, 5000);
        startTimer();
    }

    private void setButtonTextForAllButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            String buttonText = buttonSteps.get(button);
            button.setText(buttonText);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(shakeDetector);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}



