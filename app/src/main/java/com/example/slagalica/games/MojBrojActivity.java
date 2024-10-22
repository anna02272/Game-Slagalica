package com.example.slagalica.games;

import static com.example.slagalica.MainActivity.socket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.MainActivity;
import com.example.slagalica.R;
import com.example.slagalica.config.SocketHandler;
import com.example.slagalica.game_helpers.DisableTouchActivity;
import com.example.slagalica.game_helpers.ShakeDetector;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class MojBrojActivity extends AppCompatActivity {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private int currentEnabledButtonIndex = 0;
    private String answer;
    private String receivedAnswer;
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
    private JSONArray playingUsernamesArray;
    private JSONArray playingSocketsArray;
    private String currentPlayingUser;
    private String currentNotPlayingUser;
    private int currentPlayingUserIndex;
    private int currentNotPlayingUserIndex;
    private String currentPlayingUserSocketId;
    private String currentNotPlayingUserSocketId;
    private String socketIdFromPlayerThatClicked;
    private int roundIndex;
    private int confirmClicked;
    private int currentGame2;
    private int buttonId;
    private String number;
    private String SocketIdFromPlayerThatClicked;
    private String finalResult;
    private String finalResult2;
    private String userInput;
    private DatabaseReference usersRef;
    private String userId;

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
//        socket.emit("timerStart2", currentNotPlayingUserSocketId);
//        JSONObject timerData = new JSONObject();
//        try {
//            timerData.put("duration", 60);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        socket.emit("startTimer", timerData);
        playersFragment = PlayersFragment.newInstance(61);
        playersFragment.setGameType("MojBroj");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();

        firebaseDatabase = FirebaseDatabase.getInstance();
        if (currentUser != null) {
            userId = currentUser.getUid();
            usersRef = firebaseDatabase.getReference("users");
            usersRef.child(userId).child("mojBroj").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int mojBroj = dataSnapshot.getValue(Integer.class);
                        int newMojBroj = mojBroj + 1;

                        usersRef.child(userId).child("mojBroj").setValue(newMojBroj)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                        }
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
                if (currentUser != null) {
                    getPlayersSocketId(new OnSocketIdReceivedListener() {
                        @Override
                        public void onSocketIdReceived(String socketId) {
//                            socket.emit("checkTwoAnswers");
//                            socket.emit("incrementConfirmCount");
                            checkTwoAnswers();
                        }
                    });
                } else {
                    checkAnswer();
                }
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

        random = new Random();

        buttonSteps = new HashMap<>();

        retrieveNumbers();

        for (int i = 0; i < buttons.size(); i++) {
            final int buttonIndex = i;
            final Button button = buttons.get(i);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentUser != null) {
                        getPlayersSocketId(new OnSocketIdReceivedListener() {
                            @Override
                            public void onSocketIdReceived(String socketId) {
                                String currentInput = input.getText().toString();

                                if (buttonIndex != 6) {
                                    String buttonText = buttonSteps.get(button);
                                    currentInput = input.getText().toString();
                                    input.setText(currentInput + buttonText);
                                }
                                if (buttonIndex < 6 && button.isEnabled()) {
                                    button.setEnabled(false);
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
                    } else {
                        String currentInput = input.getText().toString();
                        if (buttonIndex != 6) {
                            String buttonText = buttonSteps.get(button);
                            currentInput = input.getText().toString();
                            input.setText(currentInput + buttonText);

                        }
                        if (buttonIndex < 6 && button.isEnabled()) {
                            button.setEnabled(false);
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
                }
            });
        }

        socket.on("updatePlayingUsers3", new Emitter.Listener() {
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
        socket.on("touchDisabled2", new Emitter.Listener() {
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
        socket.on("touchEnabled2", new Emitter.Listener() {
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

        socket.on("startActivity2", new Emitter.Listener() {
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
        socket.on("buttonClickable2", new Emitter.Listener() {
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
        socket.on("buttonAnswerText", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String text = (String) args[0];
                runOnUiThread(() -> {
                    buttonAnswer.setText(text);
                });
            }
        });
        socket.on("buttonAnswerText2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String text = (String) args[0];
                runOnUiThread(() -> {
                    buttonAnswer2.setText(text);
                });
            }
        });
        socket.on("setFinalAnswer", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        String receivedAnswer = (String) args[0];

                        runOnUiThread(() -> {
                            finalResult = receivedAnswer;
                        });

                    }
                });
        socket.on("setFinalAnswer2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String receivedAnswer = (String) args[0];

                runOnUiThread(() -> {
                    finalResult2 = receivedAnswer;
                });
            }
        });
        socket.on("endGame2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    endGame();
                });
            }
        });
        socket.on("startNextGame2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    try {
                        startNextGame();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        socket.on("checkTwoAnswers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    checkTwoAnswers();
                });
            }
        });
        socket.on("updateConfirmClicked", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                confirmClicked = (int) args[0];
            }
        });
        socket.on("updateRoundIndex2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                roundIndex = (int) args[0];
            }
        });
        socket.on("updateCurrentGame2", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                currentGame2 = (int) args[0];
            }
        });
        socket.on("timerStarted2", new Emitter.Listener() {
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
                                if (currentGame2 > 0) {

                                } else {
                                    if (confirmClicked == 1) {
                                        try {
                                            processAnswers();
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
//                                    if (!finalResult.equalsIgnoreCase("org.mozilla.javascript.Undefined@0")
//                                    && (!finalResult.equalsIgnoreCase("Err")  && (!finalResult.equals("null")))) {
//
//
//                                    }
//
//                                    if (!finalResult2.equalsIgnoreCase("org.mozilla.javascript.Undefined@0")
//                                            && (!finalResult2.equalsIgnoreCase("Err")  && (!finalResult.equals("null")))) {
//
//                                    }
//                                Log.d("Here", "nista");

                                    } else {
                                        socket.emit("incrementRoundIndex2");
                                        socket.emit("startNextGame2");
                                    }
                                }

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
            currentNotPlayingUserIndex = (currentPlayingUserIndex + 1) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
            currentNotPlayingUser = playingUsernamesArray.getString(currentNotPlayingUserIndex);
          }
        if (playingSocketsArray.length() >= 2) {
            currentPlayingUserIndex = (currentPlayingUserIndex) % playingSocketsArray.length();
            currentNotPlayingUserIndex = (currentPlayingUserIndex + 1) ;
            currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
            currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex);

            socket.emit("disableTouch2", currentNotPlayingUserSocketId);
            socket.emit("enableTouch2", currentPlayingUserSocketId);
            socket.emit("timerStart2", currentNotPlayingUserSocketId);
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
        socket.emit("showToast2", message);
    }

    private void disableNumberButtons() {
        for (int i = 0; i < 6; i++) {
            Button button = buttons.get(i);
            if (button.isClickable()) {
                button.setClickable(false);
            }
        }
    }

    private void enablePreviouslyClickableButtons() {
        for (int i = 0; i < 6; i++) {
            Button button = buttons.get(i);
            if (!button.isClickable()) {
                button.setClickable(true);
            }
        }
    }

    private void clickableButtons() {
        for (Button button : buttons) {
            button.setClickable(true);
        }
    }

    private boolean hasOperationInInput(String input) {
        String[] operations = {"+", "-", "*", "/", "(", ")"};
        for (String operation : operations) {
            if (input.contains(operation)) {
                return true;
            }
        }
        return false;
    }

    private void retrieveNumbers() {
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
                Map<String, String> stepsMap = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, String>>() {
                });
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
        } else if (currentEnabledButtonIndex > 1) {
            Button button = buttons.get(currentEnabledButtonIndex - 2);
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

    String getResult(String data) {
        try {
            org.mozilla.javascript.Context context = org.mozilla.javascript.Context.enter();
            context.setOptimizationLevel(-1);
            Scriptable scriptable = context.initStandardObjects();
            String finalResult = context.evaluateString(scriptable, data, "Javascript", 1, null).toString();
            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.replace(".0", "");
            }
            return finalResult;
        } catch (Exception e) {
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
            updatePoints(20);
            if (!finalResult.equals("Err")) {
                Toast.makeText(MojBrojActivity.this, finalResult + " :  Tacan odgovor!", Toast.LENGTH_SHORT).show();
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

    private void checkTwoAnswers() {
         userInput = input.getText().toString().trim();

        if (confirmClicked != 1) {
            // Wait for the other user to finish
            confirmButton.setEnabled(false);
            socket.emit("incrementConfirmCount");

            // Player 1
            socket.emit("setFinalAnswer", getResult(userInput), new Ack() {
                @Override
                public void call(Object... args) {
                    receivedAnswer = (String) args[0];
                    finalResult = receivedAnswer;

                    processPlayer1Result(finalResult);
                }
            });
        } else {
            confirmButton.setEnabled(false);

            // Player 2
            socket.emit("setFinalAnswer2", getResult(userInput), new Ack() {
                @Override
                public void call(Object... args) {
                    receivedAnswer = (String) args[0];
                    finalResult2 = receivedAnswer;
                    processPlayer2Result(finalResult2);

                    getPlayersSocketId(new OnSocketIdReceivedListener() {
                        @Override
                        public void onSocketIdReceived(String socketId) throws JSONException {
                            processAnswers();
                        }
                });
                }
            });

        }

    }

    private void processPlayer1Result(String result) {
        int buttonId1 = buttonAnswer.getId();
        socket.emit("setButtonText", buttonId1, result);
        if (userInput.isEmpty()) {
            socket.emit("setButtonText", buttonId1, "0");
        }
    }

    private void processPlayer2Result(String result) {
        int buttonId2 = buttonAnswer2.getId();
        socket.emit("setButtonText", buttonId2, result);
        if (userInput.isEmpty()) {
            socket.emit("setButtonText", buttonId2, "0");
        }
    }

    private void processAnswers() throws JSONException {
        socket.emit("decrementConfirmCount");
         if (finalResult == null || finalResult.equalsIgnoreCase("org.mozilla.javascript.Undefined@0")) {
            finalResult = "0";
        }

        if (finalResult2 == null || finalResult2.equalsIgnoreCase("org.mozilla.javascript.Undefined@0")) {
            finalResult2 = "0";
        }
        if ("Err".equals(finalResult) || "Err".equals(finalResult2)) {
            showToastAndEmit("Pogresan format odgovora.");
        } else if (finalResult.equals(finalResult2)) {
            if (finalResult.equals("0") && finalResult2.equals("0")) {
                showToastAndEmit("Niko ne dobija bodove!");
            } else {
                updatePoints(currentPlayingUserIndex + 1, 5);
                updatePointsCount(currentPlayingUser, 5);
                showToastAndEmit(currentPlayingUser + " - Brojevi su isti, poene dobija cija je runda!");
            }
        } else {
            if (roundIndex != 1) {
                round1Logic(answer);
            } else {
                round2Logic(answer);
            }
        }
        socket.emit("inputText", finalAnswer);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                socket.emit("incrementRoundIndex2");
                socket.emit("startNextGame2");
            }
        }, 5000);
    }

    private void round1Logic(String answer) throws JSONException {
          if (finalResult.equals(answer)) {
            updatePoints(currentPlayingUserIndex + 1, 20);
              updatePointsCount(currentPlayingUser, 20);
            showToastAndEmit(currentPlayingUser  + " - Tacan broj!");

          } else if (finalResult2.equals(answer)) {
            updatePoints(currentPlayingUserIndex + 2, 20);
              updatePointsCount(currentNotPlayingUser, 20);
            showToastAndEmit(currentNotPlayingUser + " - Tacan broj!");

        } else {
            double answerValue = Double.parseDouble(answer);
            double finalResultValue = Double.parseDouble(finalResult);
            double finalResult2Value = Double.parseDouble(finalResult2);

            double difference1 = Math.abs(finalResultValue - answerValue);
            double difference2 = Math.abs(finalResult2Value - answerValue);

            if (difference1 < difference2) {
                updatePoints(currentPlayingUserIndex + 1, 5);
                updatePointsCount(currentPlayingUser, 5);
                showToastAndEmit(currentPlayingUser + " : " + finalResult + " - Blizi broj!");
            } else {
                updatePoints(currentPlayingUserIndex + 2, 5);
                updatePointsCount(currentNotPlayingUser, 5);
                showToastAndEmit(currentNotPlayingUser + " : " + finalResult2 + " - Blizi broj!");
            }
        }
    }
    private void round2Logic(String answer) throws JSONException {
         if (finalResult.equals(answer)) {
            updatePoints(currentPlayingUserIndex, 20);
             updatePointsCount(currentNotPlayingUser, 20);
            showToastAndEmit(currentNotPlayingUser +  " - Tacan broj!");
            } else if (finalResult2.equals(answer)) {
            updatePoints(currentPlayingUserIndex + 1, 20);
             updatePointsCount(currentPlayingUser, 20);
            showToastAndEmit(currentPlayingUser +  " - Tacan broj!");
             } else {
            double answerValue = Double.parseDouble(answer);
            double finalResultValue = Double.parseDouble(finalResult);
            double finalResult2Value = Double.parseDouble(finalResult2);

            double difference1 = Math.abs(finalResultValue - answerValue);
            double difference2 = Math.abs(finalResult2Value - answerValue);

            if (difference1 < difference2) {
                updatePoints(currentPlayingUserIndex + 1, 5);
                updatePointsCount(currentNotPlayingUser, 5);
                showToastAndEmit(currentNotPlayingUser + " : " + finalResult + " - Blizi broj!");
            } else {
                updatePoints(currentPlayingUserIndex + 2, 5);
                updatePointsCount(currentPlayingUser, 5);
                showToastAndEmit(currentPlayingUser + " : " + finalResult2 + " - Blizi broj!");
            }
        }
    }
    private void endGame(){
        socket.emit("inputText", finalAnswer);
        showToastAndEmit("Kraj igre!");

        String player1PUsername = playersFragment.player1UsernameTextView.getText().toString();
        String player2Username = playersFragment.player2UsernameTextView.getText().toString();
        int player1Points = Integer.parseInt(playersFragment.player1PointsTextView.getText().toString());
        int player2Points = Integer.parseInt(playersFragment.player2PointsTextView.getText().toString());

        if (player1Points > player2Points) {
              updateGamesCount(player1PUsername, player2Username);
              updateStarsCount(player1PUsername, player2Username, player1Points, player2Points);
        } else if (player1Points < player2Points) {
              updateGamesCount(player2Username, player1PUsername);
              updateStarsCount(player2Username, player1PUsername, player2Points, player1Points);
        } else {
            updateTiedStarsCount(player1PUsername, player2Username, player1Points, player2Points);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                socket.emit("startActivity2");

                try {
                    preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    String username = preferences.getString("username", "");
                    socket.emit("playerDisconnected", new JSONObject().put("username", username));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 3000);
    }
    private void updateGamesCount(String winnerUsername, String loserUsername) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query winnerQuery = usersRef.orderByChild("username").equalTo(winnerUsername);
        Query loserQuery = usersRef.orderByChild("username").equalTo(loserUsername);
        winnerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String winnerUserId = userSnapshot.getKey();

                    DatabaseReference winnerRef = usersRef.child(winnerUserId);
                    winnerRef.child("wonGames").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int wonGames = dataSnapshot.getValue(Integer.class);
                                winnerRef.child("wonGames").setValue(wonGames + 1);
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

        loserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String loserUserId = userSnapshot.getKey();

                    DatabaseReference loserRef = usersRef.child(loserUserId);
                    loserRef.child("lostGames").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int lostGames = dataSnapshot.getValue(Integer.class);
                                loserRef.child("lostGames").setValue(lostGames + 1);
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
    private void updateStarsCount(String winnerUsername, String loserUsername, int winnerPoints, int loserPoints) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query winnerQuery = usersRef.orderByChild("username").equalTo(winnerUsername);
        Query loserQuery = usersRef.orderByChild("username").equalTo(loserUsername);

        int winnerAdditionalStars = winnerPoints / 40;
        int loserAdditionalStars = loserPoints / 40;
        winnerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String winnerUserId = userSnapshot.getKey();

                    DatabaseReference winnerRef = usersRef.child(winnerUserId);
                    winnerRef.child("stars").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int stars = dataSnapshot.getValue(Integer.class);
                                winnerRef.child("stars").setValue(stars + 10 + winnerAdditionalStars);
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
        loserQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String loserUserId = userSnapshot.getKey();

                    DatabaseReference loserRef = usersRef.child(loserUserId);
                    loserRef.child("stars").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int stars = dataSnapshot.getValue(Integer.class);
                                int newStars = Math.max(stars - 10 + loserAdditionalStars, 0);
                                loserRef.child("stars").setValue(newStars);
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
    private void updateTiedStarsCount(String player1, String player2, int player1Points, int player2Points) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query player1Query = usersRef.orderByChild("username").equalTo(player1);
        Query player2Query = usersRef.orderByChild("username").equalTo(player2);

        int player1AdditionalStars = player1Points / 40;
        int player2AdditionalStars = player2Points / 40;
        player1Query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String player1UserId = userSnapshot.getKey();

                    DatabaseReference player1Ref = usersRef.child(player1UserId);
                    player1Ref.child("stars").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int stars = dataSnapshot.getValue(Integer.class);
                                 player1Ref.child("stars").setValue(stars + player1AdditionalStars);
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
                    player2Ref.child("stars").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int stars = dataSnapshot.getValue(Integer.class);
                                player2Ref.child("stars").setValue(stars + player2AdditionalStars);
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

    private void startNextGame() throws JSONException {
        if (roundIndex == 1) {
            if (playingUsernamesArray.length() > 0) {
                currentPlayingUserIndex = (currentPlayingUserIndex + 1) % playingUsernamesArray.length();
                currentNotPlayingUserIndex = (currentPlayingUserIndex - 1) % playingUsernamesArray.length();
                currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
                showToastAndEmit("Playing User: " + currentPlayingUser);
                currentNotPlayingUser = playingUsernamesArray.getString(currentNotPlayingUserIndex);
                 }
            if (playingSocketsArray.length() > 0) {
                currentPlayingUserIndex = (currentPlayingUserIndex + 1) % playingSocketsArray.length();
                currentNotPlayingUserIndex = (currentPlayingUserIndex ) ;
                currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex + 1);
                currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex);

                socket.emit("disableTouch2", currentNotPlayingUserSocketId);
                socket.emit("enableTouch2", currentPlayingUserSocketId);
            }
            socket.emit("inputText", "");
            socket.emit("buttonAnswerText", "");
            socket.emit("buttonAnswerText2", "");
            enableAll();
            removeButtonTextForAllButtons();
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            JSONObject timerData = new JSONObject();
            try {
                timerData.put("duration", 60);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("startTimer", timerData);
            socket.emit("timerStart2", currentPlayingUserSocketId);
            showToastAndEmit("Runda 1 je gotova! Pocinje nova runda.");
            retrieveNumbers();
            onResume();
        } else {
            endGame();
//            socket.emit("endGame2");
        }

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
                button.setEnabled(true);
        }
    }
    private void enableAll() {
        for (Button button : buttons) {
            button.setEnabled(true);
            button.setClickable(true);
            int buttonId = button.getId();
            int buttonConfirmId = confirmButton.getId();
            socket.emit("buttonEnabled", buttonId, true);
            socket.emit("buttonClickable2", buttonId, true);
            socket.emit("buttonEnabled", buttonConfirmId, true);
            socket.emit("buttonClickable2", buttonConfirmId, true);
        }
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
    private void removeButtonTextForAllButtons() {
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
             int buttonId = button.getId();
             socket.emit("setButtonText", buttonId, "");
        }
    }
    private void getPlayersSocketId(final OnSocketIdReceivedListener listener) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                 String serverUrl = SocketHandler.getServerBaseUrl() + "/getSocketId?socketId=" + SocketHandler.getSocket().id();
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        return result.toString();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String socketId) {
                if (socketId != null) {
                    try {
                        listener.onSocketIdReceived(socketId);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    Log.d("Socket", "SocketIdFromPlayerThatClicked: " + socketId);
                } else {
                    Log.d("Socket", "Failed to retrieve socket ID");
                }
            }
        }.execute();
    }
    interface OnSocketIdReceivedListener {
        void onSocketIdReceived(String socketId) throws JSONException;
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
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                    }
                    input.setText(finalAnswer);
                    Toast.makeText(MojBrojActivity.this, "Vase vreme je isteklo, kraj igre",
                            Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
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
                if (currentUser != null) {
                    socket.emit("enableTouch2", currentNotPlayingUserSocketId);
                }
                setButtonTextForAllButtons();
            }
        }, 5000);
        startTimer();
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
    private void updatePointsCount(String playerUsername, int points) throws JSONException {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = usersRef.orderByChild("username").equalTo(playerUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    DatabaseReference ref = usersRef.child(userId);
                    ref.child("mojBrojPoints").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int mojBrojPoints = dataSnapshot.getValue(Integer.class);
                                int newPoints = (int) (mojBrojPoints + points);
                                ref.child("mojBrojPoints").setValue(newPoints);
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



