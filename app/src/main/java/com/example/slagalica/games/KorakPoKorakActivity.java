package com.example.slagalica.games;

import static com.example.slagalica.MainActivity.socket;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.game_helpers.DisableTouchActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.slagalica.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class KorakPoKorakActivity extends AppCompatActivity {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private CountDownTimer countDownTimer;
    private int currentCount;
    private String answer;
    private EditText input;
    private Handler buttonHandler;
    private Runnable buttonRunnable;
    private int currentEnabledButtonIndex = 0;
    private int currentButtonIndex = 1;
    private int currentStep ;
    private Button confirmButton;
    private PlayersFragment playersFragment;
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
    private int currentGame1;
    private final int TOTAL_ROUNDS = 2;
    private int buttonId;
    private boolean isContinued;
    private DatabaseReference usersRef;
    private String userId;
    @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_korak_po_korak);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        disableTouchActivity = new DisableTouchActivity(KorakPoKorakActivity.this);

//        socket.emit("timerStart1", currentNotPlayingUserSocketId);
//        JSONObject timerData = new JSONObject();
//        try {
//            timerData.put("duration", 70);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        socket.emit("startTimer", timerData);

        playersFragment = PlayersFragment.newInstance(71);
        playersFragment.setGameType("KorakPoKorak");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();
        firebaseDatabase = FirebaseDatabase.getInstance();
        if (currentUser != null) {
            userId = currentUser.getUid();
            usersRef = firebaseDatabase.getReference("users");
            usersRef.child(userId).child("korakPoKorak").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        int korakPoKorak = dataSnapshot.getValue(Integer.class);
                        int newKorakPoKorak= korakPoKorak + 1;

                        usersRef.child(userId).child("korakPoKorak").setValue(newKorakPoKorak)
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
        Button buttonNext = findViewById(R.id.button_next);
        input = findViewById(R.id.input);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playersFragment.showExitConfirmationDialog();
            }
        });

        confirmButton = findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    checkAnswer();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
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

        random = new Random();

        buttonSteps = new HashMap<>();

        retrieveSteps();

        for (int i = 1; i <= 6; i++) {
            Button button = buttons.get(i);
            button.setEnabled(false);
        }

        socket.on("updatePlayingUsers2", new Emitter.Listener() {
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
        socket.on("touchDisabled1", new Emitter.Listener() {
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
        socket.on("touchEnabled1", new Emitter.Listener() {
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
        socket.on("timerStarted1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("Here", "call");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("Here", "run");
                        currentCount = 7;
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        countDownTimer = new CountDownTimer(71000, 10000) {
                            private Context context = KorakPoKorakActivity.this.getApplicationContext();

                            @Override
                            public void onTick(long millisUntilFinished) {
                                int time = (int) (millisUntilFinished / 1000);
                                int buttonIndex = currentCount;
                                if (buttonIndex >= 7 && buttonIndex < buttons.size()) {
                                    Button button = buttons.get(buttonIndex);
                                    if (currentUser == null) {
                                        button.setText(String.valueOf(time));
                                    } else {
                                        socket.emit("timerText", buttonIndex, String.valueOf(time));

                                    }
                                }
                                currentCount++;

                            }

                            @Override
                            public void onFinish() {
                                if (currentGame1 > 0){

                                } else {
                                    socket.emit("continueGame1");
                                }
                            }
                        };

                        countDownTimer.start();
                    }
                });
            }
        });
        socket.on("1timerStarted10", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (countDownTimer != null) {
                            countDownTimer.cancel();
                        }
                        countDownTimer = new CountDownTimer(16000, 10000) {
                            private Context context = KorakPoKorakActivity.this.getApplicationContext();
                            @Override
                            public void onTick(long millisUntilFinished) {
                            }
                            @Override
                            public void onFinish() {
                                if (currentGame1 > 0){

                                } else {
                                    if (answerIndex == 1) {
                                        socket.emit("decrementAnswerIndex1");
                                        endGame();
//                                    socket.emit("endGame1");
                                        socket.emit("continuedFalse1");
                                    } else {
                                        socket.emit("incrementAnswerIndex1");
                                        socket.emit("incrementRoundIndex1");
                                        socket.emit("startNextGame1");
                                        socket.emit("continuedFalse1");

                                    }
                                }
                            }
                        };

                        countDownTimer.start();
                    }
                });
            }
        });
        socket.on("stepChange", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                String step = (String) args[1];
                String stepAnswer = (String) args[2];
                answer = stepAnswer;
                  Button stepButton = findViewById(buttonId);
                runOnUiThread(() -> {
                    buttonSteps.put(stepButton, step);
                });
            }
        });
        socket.on("timerText", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonIndex = (int) args[0];
                String text = (String) args[1];

                Button button = buttons.get(buttonIndex);
                runOnUiThread(() -> {
                    button.setText(text);
                });
            }

        });
        socket.on("startActivity1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    Intent intent = new Intent(KorakPoKorakActivity.this, MojBrojActivity.class);
                    startActivity(intent);
                    finish();
                });
            }
        });
        socket.on("buttonText", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                boolean enabled = (boolean) args[1];
                String step = (String) args[2];

                runOnUiThread(() -> {
                    Button button = findViewById(buttonId);
                    if (button != null) {
                        button.setText(step);
                        button.setEnabled(enabled);
                    }
                });

            }
        });
        socket.on("buttonClickable", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int buttonId = (int) args[0];
                boolean clickable = (boolean) args[1];
                String step = (String) args[2];

                runOnUiThread(() -> {
                    Button button = findViewById(buttonId);
                    if (button != null) {
                        button.setText(step);
                        button.setClickable(clickable);
                    }
                });

            }
        });
        socket.on("answer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                String answer = (String) args[0];
                runOnUiThread(() -> {
                    input.setText(answer);
                });
            }

        });
        socket.on("endGame1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    endGame();
                });
            }
        });
        socket.on("continueGame1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    try {
                        continueGame();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        socket.on("startNextGame1", new Emitter.Listener() {
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
        socket.on("updateRoundIndex1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                roundIndex = (int) args[0];
            }
        });
        socket.on("updateAnswerIndex1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                answerIndex = (int) args[0];
            }
        });
        socket.on("updateContinued1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                isContinued = (boolean) args[0];
            }
        });
        socket.on("updateCurrentGame1", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                currentGame1 = (int) args[0];
            }
        });
        buttonHandler = new Handler();
        buttonRunnable = new Runnable() {
            @Override
            public void run() {
                Button button = buttons.get(currentButtonIndex);
                button.setEnabled(true);
                currentButtonIndex++;
                Button button1 = buttons.get(currentEnabledButtonIndex);
                String step = buttonSteps.get(button1);
                if (step != null) {
                    button1.setText(step);
                    button1.setEnabled(false);
                    currentEnabledButtonIndex++;
                }

                if (currentButtonIndex <= 7) {
                    buttonHandler.postDelayed(this, 10000);
                }
            }
        };

    }
    
    private void retrieveConnectedUsers() throws JSONException {
        if (playingUsernamesArray.length() >= 2) {
            currentPlayingUserIndex = (currentPlayingUserIndex) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
        }
        if (playingSocketsArray.length() >= 2) {
            currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
            currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
//            socket.emit("disableTouch1", currentNotPlayingUserSocketId);
//            Log.d("Touch disabled", "disable not playing retrieve: " + currentNotPlayingUserSocketId);
            socket.emit("timerStart1", currentNotPlayingUserSocketId);
            JSONObject timerData = new JSONObject();
            try {
                timerData.put("duration", 70);
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
        currentEnabledButtonIndex = 0;
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
                                buttonId = button.getId();
                                buttonSteps.put(button, step);
                                setButtonListener(button);
                            }
                        }
                        answer = stepsMap.get("answer");
                        if (currentUser != null) {
                            socket.emit("stepChange", buttonId, step, answer);
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
    private int getCurrentStep() {
         currentStep = currentButtonIndex ;
        //int currentStep = currentButtonIndex ;
        return currentStep < 0 ? 0 : currentStep;
    }

    private void checkAnswer() throws JSONException {
            String userInput = input.getText().toString().trim();
            socket.emit("answer", userInput);

        if (answer != null && userInput.equalsIgnoreCase(answer)) {
            if (currentUser != null) {
                showToastAndEmit("Tacan odgovor!");
            } else {
                confirmButton.setEnabled(false);
                Toast.makeText(KorakPoKorakActivity.this, "Tacan odgovor!", Toast.LENGTH_SHORT).show();
            }
            for (Button button : buttons) {
                String step = buttonSteps.get(button);
                if (step != null) {
                    if (currentUser != null) {
                        int buttonId = button.getId();
                        socket.emit("buttonText", buttonId, false, step);
                    }
                    button.setText(step);
                    button.setEnabled(false);
                }
            }

            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);

            if (countDownTimer != null) {
                countDownTimer.cancel();
            }

            int currentStep = getCurrentStep();
            int pointsToAdd = 20 - (2 * (currentStep - 1));
            if (currentUser != null) {
                updatePoints(currentPlayingUserIndex + 1, pointsToAdd);
                updatePointsCount(currentPlayingUserIndex, pointsToAdd);
                socket.emit("answer", "");

                if (isContinued == true) {
                    if (answerIndex == 1) {
//                        socket.emit("endGame1");
                        endGame();
                        socket.emit("decrementAnswerIndex1");
                         updatePoints(currentPlayingUserIndex + 1, 5);
                        updatePointsCount(currentPlayingUserIndex, 5);
                    } else {
                        socket.emit("startNextGame1");
                        socket.emit("incrementAnswerIndex1");
                        socket.emit("incrementRoundIndex1");
                        socket.emit("continuedTrue1");
                        updatePoints(currentPlayingUserIndex + 1, 5);
                        updatePointsCount(currentPlayingUserIndex, 5);
                    }
                } else {
                    if (roundIndex == 1) {
                        socket.emit("decrementRoundIndex1");
                        endGame();
//                        socket.emit("endGame1");
                    } else {
                        socket.emit("incrementAnswerIndex1");
                        socket.emit("incrementRoundIndex1");
                        socket.emit("startNextGame1");
                        //socket.emit("continuedTrue1");
                    }
                }
            } else {
                updatePoints(pointsToAdd);
                endGame();
            }

        } else {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
            if (currentUser != null){
                socket.emit("answer", "");
                showToastAndEmit("Netacan odgovor!");

                if (isContinued == true) {
                    if (answerIndex == 1) {
                        socket.emit("decrementAnswerIndex1");
                        endGame();
//                        socket.emit("endGame1");
                        socket.emit("continuedFalse1");
                    } else {
                        socket.emit("incrementAnswerIndex1");
                        socket.emit("startNextGame1");
                        socket.emit("continuedFalse1");
                    }
                }
            }
            input.setText("");
            Toast.makeText(KorakPoKorakActivity.this, "Netacan odgovor!", Toast.LENGTH_SHORT).show();
        }
    }
    private void endGame(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
             if (countDownTimer != null) {
                countDownTimer.cancel();
             }
                if (currentUser != null) {
                    showToastAndEmit("Igra je gotova! Sledi igra MOJ BROJ!");
                    socket.emit("incrementCurrentGame1");
                } else {
                    Toast.makeText(KorakPoKorakActivity.this, "Sledi igra MOJ BROJ!", Toast.LENGTH_SHORT).show();
                }
                if (currentUser != null) {
                    socket.emit("enableTouch1", currentNotPlayingUserSocketId);
                    socket.emit("startActivity1");
                } else {
                     Intent intent = new Intent(KorakPoKorakActivity.this, MojBrojActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);
    }
    private void startNextGame() throws JSONException {
        if (isContinued == true) {
            if (playingUsernamesArray.length() > 0) {
                currentPlayingUserIndex = (currentPlayingUserIndex ) % playingUsernamesArray.length();
                currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
                showToastAndEmit("Playing User: " + currentPlayingUser);
            }
            if (playingSocketsArray.length() > 0) {
                currentNotPlayingUserIndex = (currentNotPlayingUserIndex) % playingSocketsArray.length();
                currentPlayingUserIndex = (currentPlayingUserIndex) % playingSocketsArray.length();
                currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex);
                currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
//                socket.emit("enableTouch1", currentPlayingUserSocketId);
//                socket.emit("disableTouch1", currentNotPlayingUserSocketId);
                Log.d("Touch disabled", "enable  playing start next: " + currentPlayingUserSocketId);
                Log.d("Touch disabled", "disable not playing start next: " + currentNotPlayingUserSocketId);
            }
            socket.emit("continuedFalse1");
        } else {
            if (playingUsernamesArray.length() > 0) {
            currentPlayingUserIndex = (currentPlayingUserIndex + 1) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
            showToastAndEmit("Playing User: " + currentPlayingUser);
            }
            if (playingSocketsArray.length() > 0) {
                currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
                currentPlayingUserIndex = (currentPlayingUserIndex) % playingSocketsArray.length();
                currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex);
                currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
//                socket.emit("enableTouch1", currentPlayingUserSocketId);
//                socket.emit("disableTouch1", currentNotPlayingUserSocketId);
                Log.d("Touch disabled", "enable  playing start next 2: " + currentPlayingUserSocketId);
                Log.d("Touch disabled", "disable not playing start next 2: " + currentNotPlayingUserSocketId);
            }
         }
                for (int i = 0; i < 1; i++) {
                    Button button = buttons.get(i);
                    int buttonId = button.getId();
                    socket.emit("buttonText", buttonId, true,  "Korak");
                }
                for (int i = 1; i <  7; i++) {
                    Button button = buttons.get(i);
                    int buttonId = button.getId();
                    socket.emit("buttonText", buttonId, false, "Korak");
                }
                for (int i = 7; i <  buttons.size(); i++) {
                    Button button = buttons.get(i);
                    int buttonId = button.getId();
                    socket.emit("buttonClickable", buttonId, false, "");
                }

                switchPlayersTurn();
                currentButtonIndex = 1;
                currentEnabledButtonIndex = 0;
                currentStep = currentButtonIndex;
                JSONObject timerData = new JSONObject();
                try {
                    timerData.put("duration", 70);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("startTimer", timerData);
                socket.emit("timerStart1", currentPlayingUserSocketId);
                showToastAndEmit("Runda 1 je gotova! Pocinje nova runda.");
                retrieveSteps();
                buttonHandler.postDelayed(buttonRunnable, 10000);
    }
    private void continueGame() throws JSONException {
        socket.emit("continuedTrue1");
        if (playingUsernamesArray.length() > 0) {
            currentPlayingUserIndex = (currentPlayingUserIndex + 1) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
            showToastAndEmit("Playing User: " + currentPlayingUser);
        }
        if (playingSocketsArray.length() > 0) {
            currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
            currentPlayingUserIndex = (currentPlayingUserIndex) % playingSocketsArray.length();
            currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
            currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
//            socket.emit("enableTouch1", currentPlayingUserSocketId);
//            socket.emit("disableTouch1", currentNotPlayingUserSocketId);
            Log.d("Touch disabled", "enable  playing continue : " + currentPlayingUserSocketId);
            Log.d("Touch disabled", "disable not playing continue : " + currentNotPlayingUserSocketId);
        }
        socket.emit("1timerStart10", currentPlayingUserSocketId);
        JSONObject timerData = new JSONObject();
        try {
            timerData.put("duration", 15);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("startTimer", timerData);
        showToastAndEmit("Drugi igrač dobija šansu da pogodi pojam!");
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
    private void switchPlayersTurn() {
        roundsPlayed++;
    }
    private void setButtonListener(final Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String step = buttonSteps.get(button);
                if (step != null) {
                    if (currentUser != null) {
                        int buttonId = button.getId();
                        socket.emit("buttonText", buttonId, false, step);
                    } else {
                        button.setText(step);
                        button.setEnabled(false);
                    }
                }
            }
        });
    }

    private void startTimer() {
        currentCount = 7;
        if (currentUser == null) {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            countDownTimer = new CountDownTimer(71000, 10000) {
                private Context context = KorakPoKorakActivity.this.getApplicationContext();

                @Override
                public void onTick(long millisUntilFinished) {
                    int time = (int) (millisUntilFinished / 1000);
                    int buttonIndex = currentCount;
                    if (buttonIndex >= 7 && buttonIndex < buttons.size()) {
                        Button button = buttons.get(buttonIndex);
                        button.setText(String.valueOf(time));
                    }
                    currentCount++;
                }

                @Override
                public void onFinish() {
                    input.setText(answer);
                    endGame();

                }
            };

            countDownTimer.start();
        }
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

    private void showToastAndEmit(String message) {
        Toast.makeText(KorakPoKorakActivity.this, message, Toast.LENGTH_SHORT).show();
        socket.emit("showToast1", message);
    }
    private void updatePointsCount(int playerNumber, int points) throws JSONException {
        currentPlayingUser = playingUsernamesArray.getString(playerNumber);

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        Query query = usersRef.orderByChild("username").equalTo(currentPlayingUser);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();

                    DatabaseReference ref = usersRef.child(userId);
                    ref.child("korakPoKorakPoints").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int korakPoKorakPoints = dataSnapshot.getValue(Integer.class);
                                int newPoints = (int) (korakPoKorakPoints + points);
                                ref.child("korakPoKorakPoints").setValue(newPoints);
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