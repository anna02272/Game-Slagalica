package com.example.slagalica.games;

import static com.example.slagalica.MainActivity.socket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.slagalica.MainActivity;

import com.example.slagalica.R;
import com.example.slagalica.game_helpers.DisableTouchActivity;
import com.example.slagalica.game_helpers.ShakeDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.socket.emitter.Emitter;

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
    private Button buttonAnswer2;
    private Button confirmButton;
    private CountDownTimer countDownTimer;
    private EditText input;
    private ShakeDetector shakeDetector;
    private PlayersFragment playersFragment;
    private SharedPreferences preferences;
    private FirebaseUser currentUser;
    private DisableTouchActivity disableTouchActivity;
    private  JSONArray playingUsernamesArray;
    private  JSONArray playingSocketsArray;
    private  String currentPlayingUser;
    private int currentPlayingUserIndex;
    private int currentNotPlayingUserIndex;
    private String currentPlayingUserSocketId;
    private String currentNotPlayingUserSocketId;
    private int roundsPlayed = 1;
    private int roundIndex;
    private int answerIndex;
    private final int TOTAL_ROUNDS = 2;
    private int buttonId;
    private boolean isContinued;
    private String number;

    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_moj_broj);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        disableTouchActivity = new DisableTouchActivity(MojBrojActivity.this);

        playersFragment = PlayersFragment.newInstance(61);
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
        buttonAnswer2 = findViewById(R.id.button_answer2);
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
                       if (currentUser != null){
                            socket.emit("inputText", "");
                        } else{
                            input.setText("");
                        }
                        enableAllButtons();
                        clickableButtons();
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
                     String currentInput = input.getText().toString();

                    if (buttonIndex != 6) {
                        String buttonText = buttonSteps.get(button);
                        currentInput = input.getText().toString();
                        if (currentUser != null){
                            socket.emit("inputText", currentInput + buttonText);
                        }
                            input.setText(currentInput + buttonText);
                    }

                    if (buttonIndex < 6 && button.isEnabled()) {
                        if (currentUser != null) {
                            int buttonId = button.getId();
                            socket.emit("buttonEnabled", buttonId, false);
                        } else {
                            button.setEnabled(false);
                        }
                        disableNumberButtons();
                    } else if (buttonIndex >= 6) {
                        currentInput = input.getText().toString();
                        if (hasOperationInInput(currentInput)) {
                            enablePreviouslyClickableButtons();
                        } else {
                            disableNumberButtons();
                        }
                    }
                }
            });
        }

        socket.on("updatePlayingUsers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length >= 2) {
                    playingUsernamesArray = (JSONArray) args[0];
                    playingSocketsArray = (JSONArray) args[1];
                    try {
                        retrieveConnectedUsers();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        socket.on("touchDisabled", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disableTouchActivity.disableTouch();
                    }
                });
            }
        });
        socket.on("touchEnabled", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disableTouchActivity.enableTouch();
                    }
                });
            }
        });
        socket.on("startActivity", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
        socket.on("numberChange", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                String number = (String) args[1];
                Button numButton = findViewById(buttonId);
                runOnUiThread(() -> {
                    buttonSteps.put(numButton, number);
                });
            }
        });
        socket.on("answerChange", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String receivedAnswer = (String) args[0];
                String receivedAnswerFinal = (String) args[1];
                answer = receivedAnswer;
                finalAnswer = receivedAnswerFinal;
            }
        });
        socket.on("setButtonText", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                String buttonText = (String) args[1];

                runOnUiThread(() -> {
                    Button button = findViewById(buttonId);
                    if (button != null) {
                        button.setText(buttonText);
                    }
                });

            }
        });
        socket.on("buttonEnabled", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                boolean enabled = (boolean) args[1];

                runOnUiThread(() -> {
                    Button button = findViewById(buttonId);
                    if (button != null) {
                        button.setEnabled(enabled);
                    }
                });

            }
        });
        socket.on("buttonsClickable", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                boolean clickable = (boolean) args[1];

                runOnUiThread(() -> {
                    Button button = findViewById(buttonId);
                    if (button != null) {
                        button.setClickable(clickable);
                    }
                });

            }
        });
        socket.on("inputText", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String text = (String) args[0];
                runOnUiThread(() -> {
                    input.setText(text);
                });
            }
        });
        socket.on("timerStarted", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        countDownTimer = new CountDownTimer(61000, 10000) {
                            private Context context = MojBrojActivity.this.getApplicationContext();

                            @Override
                            public void onTick(long millisUntilFinished) {
                            }

                            @Override
                            public void onFinish() {
                                socket.emit("inputText", finalAnswer);
                                showToastAndEmit("Vase vreme je isteklo, kraj igre!");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        socket.emit("startActivity");
                                            try {
                                                preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                                String username = preferences.getString("username", "");
                                                socket.emit("playerDisconnected", new JSONObject().put("username", username));
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                }, 5000);
                            }
                        };
                        countDownTimer.start();
                    }
                });
            }
        });

    }

    private void retrieveConnectedUsers() throws JSONException {
        if (playingUsernamesArray.length() >= 2) {
            currentPlayingUserIndex = (currentPlayingUserIndex) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
        }
        if (playingSocketsArray.length() >= 2) {
            currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
            currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
            socket.emit("disableTouch", currentNotPlayingUserSocketId);
            socket.emit("timerStart", currentNotPlayingUserSocketId);
            JSONObject timerData = new JSONObject();
            try {
                timerData.put("duration", 60);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("startTimer", timerData);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentPlayingUser != null) {
                    showToastAndEmit("Playing User: " + currentPlayingUser);
                }
            }
        });
    }
    private void showToastAndEmit(String message) {
        Toast.makeText(MojBrojActivity.this, message, Toast.LENGTH_SHORT).show();
        socket.emit("showToast", message);
    }

    private void disableNumberButtons() {
        for (int i = 0; i < 6; i++) {
            Button button = buttons.get(i);
            if (button.isClickable()) {
                if (currentUser != null){
                    int buttonId =  button.getId();
                    socket.emit("buttonsClickable", buttonId, false);
                } else {
                    button.setClickable(false);
                }
            }
        }
    }

    private void enablePreviouslyClickableButtons() {
        for (int i = 0; i < 6; i++) {
            Button button = buttons.get(i);
            if (!button.isClickable()) {
                if (currentUser != null){
                    int buttonId =  button.getId();
                    socket.emit("buttonsClickable", buttonId, true);
                } else {
                    button.setClickable(true);
                }
            }
        }
    }

    private void clickableButtons() {
        for (Button button : buttons) {
            if (currentUser != null) {
                int buttonId = button.getId();
                socket.emit("buttonsClickable", buttonId, true);
            } else {
                button.setClickable(true);
            }
        }
    }

    private boolean hasOperationInInput(String input) {
        String[] operations = { "+", "-", "*", "/", "(", ")" };
        for (String operation : operations) {
            if (input.contains(operation)) {
                return true;
            }
        }
        return false;
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
                        number = stepsMap.get("number" + i);
                        if (number != null) {
                            int buttonIndex = i - 1;
                            if (buttonIndex < buttons.size()) {
                                Button button = buttons.get(buttonIndex);
                                buttonId = button.getId();
                                buttonSteps.put(button, number);
                                if (currentUser != null) {
                                    socket.emit("numberChange", buttonId, number);
                                }
                            }
                        }
                    }
                    answer = stepsMap.get("number7");
                    finalAnswer = stepsMap.get("number8");
                    if (currentUser != null) {
                        socket.emit("answerChange", answer, finalAnswer);
                    }
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
            if (currentUser != null) {
                int buttonId = button7.getId();
                socket.emit("setButtonText", buttonId, buttonText7);
            } else {
                button7.setText(buttonText7);
            }
        } else if (currentEnabledButtonIndex > 1 ) {
            Button button = buttons.get(currentEnabledButtonIndex-2);
            String buttonText = buttonSteps.get(button);
            if (currentUser != null) {
                int buttonId = button.getId();
                socket.emit("setButtonText", buttonId, buttonText);
            } else {
                button.setText(buttonText);
            }
            if (currentEnabledButtonIndex == 7) {
                if (currentUser != null) {
                    int buttonId = stopButton.getId();
                    socket.emit("buttonEnabled", buttonId, false);
                } else {
                    stopButton.setEnabled(false);
                }
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
        if (currentUser != null){
            int buttonId = buttonAnswer.getId();
            socket.emit("setButtonText", buttonId, finalResult);
        } else {
            buttonAnswer.setText(finalResult);
        }
        if (answer != null && finalResult.equals(answer)) {
            if (currentUser != null){
                updatePoints(currentPlayingUserIndex + 1, 20);
            }
            else {
                updatePoints(20);
            }
            if(!finalResult.equals("Err")) {
                if (currentUser != null) {
                    showToastAndEmit(finalResult + " :  Tacan odgovor!");
                } else {
                    Toast.makeText(MojBrojActivity.this, finalResult + " :  Tacan odgovor!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            if (!finalResult.equals("Err")) {
                if (currentUser != null) {
                    socket.emit("inputText", finalAnswer);
                } else {
                    input.setText(finalAnswer);
                }
                if (currentUser != null) {
                    showToastAndEmit(finalResult + " :  Netacan odgovor!");
                } else {
                    Toast.makeText(MojBrojActivity.this, finalResult + " :  Netacan odgovor!", Toast.LENGTH_SHORT).show();
                }
            }
        }
            if (currentUser != null) {
                showToastAndEmit("Kraj igre!");
                int buttonId = confirmButton.getId();
                socket.emit("buttonEnabled", buttonId, false);
            } else {
                Toast.makeText(MojBrojActivity.this, "Kraj igre!", Toast.LENGTH_SHORT).show();
                confirmButton.setEnabled(false);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (currentUser == null) {
                        Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        socket.emit("startActivity");
                    try {
                        preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                        String username = preferences.getString("username", "");
                                socket.emit("playerDisconnected", new JSONObject().put("username", username));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }

                }
            }, 5000);
        }

    private void updatePoints(int points) {
        playersFragment.updateGuestPoints(points);
    }
    private void updatePoints(int currentPlayerNumber, int points) {
        if (currentUser != null) {
            if (currentPlayerNumber > 0) {
                playersFragment.updatePlayerPoints(currentPlayerNumber, points);
            }
        }
    }
    private void enableAllButtons() {
        for (Button button : buttons) {
            if (currentUser != null) {
                int buttonId = button.getId();
                socket.emit("buttonEnabled", buttonId, true);
            } else {
                button.setEnabled(true);
            }
        }
    }
    private void startTimer() {
        if (currentUser == null) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(61000, 10000) {
                private Context context = MojBrojActivity.this.getApplicationContext();

                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    input.setText(finalAnswer);
                    Toast.makeText(MojBrojActivity.this, "Vase vreme je isteklo, kraj igre",
                            Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            if (currentUser != null) {
                                try {
                                    preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                                    String username = preferences.getString("username", "");
                                    socket.emit("playerDisconnected", new JSONObject().put("username", username));
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }, 5000);

                }
            };

            countDownTimer.start();
        }
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
            if (currentUser != null) {
                int buttonId = button.getId();
                socket.emit("setButtonText", buttonId, buttonText);
            } else {
                button.setText(buttonText);
            }
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



