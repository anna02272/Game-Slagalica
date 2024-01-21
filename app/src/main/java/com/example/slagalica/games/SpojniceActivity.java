package com.example.slagalica.games;


import static com.example.slagalica.MainActivity.socket;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.R;
import com.example.slagalica.game_helpers.DisableTouchActivity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.socket.emitter.Emitter;

public class SpojniceActivity extends AppCompatActivity {
    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private Map<Button, String> buttonSteps;
    private Random random;
    private Handler buttonHandler;
    private Runnable buttonRunnable;
    private int currentEnabledButtonIndex = 0;
    private Map<String, String> stepsMap;
    private PlayersFragment playersFragment;
    private Button firstClickedButton = null;
    private Button secondClickedButton = null;
    private FirebaseUser currentUser;
    private boolean isPlayer1Turn = true;
    private int roundsPlayed = 1;
    private final int TOTAL_ROUNDS = 2;
    private Button stepButton;
    private Button answerButton;
    private boolean allButtonsClickable;
    List<Integer> stepIndices = new ArrayList<>();
    List<Integer> answerIndices = new ArrayList<>();
    private int currentPlayingUserIndex;
    private int currentNotPlayingUserIndex;
    private String currentPlayingUserSocketId;
    private String currentNotPlayingUserSocketId;
    private  JSONArray playingUsernamesArray;
    private  JSONArray playingSocketsArray;
    private  String currentPlayingUser;
    private int roundIndex;
    private int currentGame;
    private CountDownTimer countDownTimer;
    private DisableTouchActivity disableTouchActivity;
    private String username;
    private SharedPreferences preferences;
    private int spojnice ;
    private DatabaseReference usersRef;
    private String userId;
   @Override
    public void onBackPressed() {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_spojnice);

        preferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = preferences.getString("username", "");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        disableTouchActivity = new DisableTouchActivity(SpojniceActivity.this);

//        socket.emit("timerStart", currentNotPlayingUserSocketId);
//        JSONObject timerData = new JSONObject();
//        try {
//            timerData.put("duration", 30);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        socket.emit("startTimer", timerData);
        playersFragment = PlayersFragment.newInstance(31);
        playersFragment.setGameType("Spojnice");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, playersFragment)
                .commit();

        firebaseDatabase = FirebaseDatabase.getInstance();

            if (currentUser != null) {
                userId = currentUser.getUid();
                usersRef = firebaseDatabase.getReference("users");
                usersRef.child(userId).child("spojnice").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            int spojnice = dataSnapshot.getValue(Integer.class);
                            int newSpojnice = spojnice + 1;

                            usersRef.child(userId).child("spojnice").setValue(newSpojnice)
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

        random = new Random();
        buttonSteps = new HashMap<>();

        retrieveSteps();

        socket.on("updatePlayingUsers1", new Emitter.Listener() {
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
//                if (playingUsernamesArray.length() < 1) {
//                    //User left the game
//                    currentPlayingUserIndex = (currentPlayingUserIndex) % playingUsernamesArray.length();
//                    try {
//                        currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
//                    try {
//                        currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
//                    } catch (JSONException e) {
//                        throw new RuntimeException(e);
//                    }
//                    socket.emit("enableTouch", currentNotPlayingUserSocketId);
//                currentUser = null;
//                }
            }
        });
        socket.on("stepChanged", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int stepIndex = (int) args[0];
                String step = (String) args[1];

                Button stepButton = buttons.get(stepIndex - 1);
                runOnUiThread(() -> {
                    stepButton.setText(step);
                    stepButton.setTag(stepIndex);
                });
            }
        });
        socket.on("answerChanged", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                int shuffledIndex = (int) args[0];
                int answerIndex = (int) args[1];
                String answer = (String) args[2];

                Button answerButton = buttons.get(shuffledIndex + 5);
                runOnUiThread(() -> {
                    answerButton.setText(answer);
                    answerButton.setTag(answerIndex);
                });
            }
        });
        socket.on("colorChange", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject eventData = (JSONObject) args[0];
                    try {
                        int buttonTag = eventData.getInt("buttonTag");
                        String color = eventData.getString("color");
                        updateButtonColor(buttonTag, color);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        socket.on("reset_received", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject reset = (JSONObject) args[0];
                    try {
                        handleResetButtonsSocket(reset);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        socket.on("buttonStateChanged", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                boolean enableState = (boolean) args[0];

                runOnUiThread(() -> {
                    if (enableState) {
                        for (int i = 5; i < 10; i++) {
                            buttons.get(i).setEnabled(true);
                        }
                        for (int i = 0; i < 5; i++) {
                            buttons.get(i).setEnabled(false);
                        }
                    } else {
                        for (int i = 5; i < 10; i++) {
                            buttons.get(i).setEnabled(false);
                        }
                        for (int i = 0; i < 5; i++) {
                            buttons.get(i).setEnabled(true);
                        }
                    }
                });
            }
        });
        socket.on("startNextGame", new Emitter.Listener() {
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
        socket.on("continueGame", new Emitter.Listener() {
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
        socket.on("endGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    try {
                        endGame();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
//        socket.on("startActivity", new Emitter.Listener() {
//            @Override
//            public void call(Object... args) {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(SpojniceActivity.this, KorakPoKorakActivity.class);
//                        startActivity(intent);
//                        finish();
//
//                    }
//                });
//            }
//        });
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
        socket.on("message_received", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (args.length > 0) {
                    JSONObject message = (JSONObject) args[0];
                    try {
                        handleEnableMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        socket.on("updateRoundIndex", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                roundIndex = (int) args[0];
            }
        });
        socket.on("updateCurrentGame", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                currentGame = (int) args[0];
            }
        });

        socket.on("retrieveSteps", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                runOnUiThread(() -> {
                    retrieveSteps();
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
                            countDownTimer = null;
                        }
                            countDownTimer = new CountDownTimer(31000, 10000) {
                                private Context context = SpojniceActivity.this.getApplicationContext();
                                @Override
                                public void onTick(long millisUntilFinished) {
                                }
                                @Override
                                public void onFinish() {
                                    if (currentGame > 0){

                                    } else {
                                        if (currentUser != null) {
                                            if (roundIndex == 1) {
                                                socket.emit("startNextGame");
                                                socket.emit("decrementRoundIndex");
                                            } else {
                                                socket.emit("continueGame");
                                                socket.emit("incrementRoundIndex");
                                            }

                                        } else {
                                            try {
                                                endGame();
                                            } catch (JSONException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                }
                            };

                            countDownTimer.start();
                    }
                });
            }

        });
        socket.on("buttonClickableSpojnice", new Emitter.Listener() {
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

        buttonHandler = new Handler();
        buttonRunnable = new Runnable() {
            @Override
            public void run() {
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
             socket.emit("disableTouch", currentNotPlayingUserSocketId);
            socket.emit("timerStart", currentNotPlayingUserSocketId);
            JSONObject timerData = new JSONObject();
            try {
                timerData.put("duration", 30);
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
                            stepButton = buttons.get(i);
                            if (currentUser == null) {
                                stepButton.setText(step);
                                stepButton.setTag(stepIndex);
                            }
                            buttonSteps.put(stepButton, step);
                            setButtonListener(stepButton);
                            if (currentUser != null) {
                                socket.emit("stepChanged", stepIndex, step);
                            }

                        }
                    }
                    for (int i = 0; i < 5; i++) {
                        int answerIndex = answerIndices.get(i);
                        String answer = stepsMap.get("answer" + answerIndex);
                        if (answer != null) {
                            answerButton = buttons.get(i + 5);
                            if (currentUser == null) {
                                answerButton.setText(answer);
                                answerButton.setTag(answerIndex);
                            }
                            buttonSteps.put(answerButton, answer);
                            setButtonListener(answerButton);
                            if (currentUser != null) {
                                socket.emit("answerChanged", i, answerIndex, answer);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setButtonListener(Button button) {
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (firstClickedButton == null) {
                    firstClickedButton = (Button) v;

                    if (currentUser != null) {
                        emitColorChangeEvent((Integer) firstClickedButton.getTag(), "#FFFF00");
                        emitButtonState(true);
                    }
                    if (currentUser == null) {
                        firstClickedButton.setTextColor(Color.parseColor("#FFFF00"));
                        for (int i = 5; i < 10; i++) {
                            buttons.get(i).setEnabled(true);
                        }
                        for (int i = 0; i < 5; i++) {
                            buttons.get(i).setEnabled(false);
                        }
                    }
                } else if (secondClickedButton == null) {
                    secondClickedButton = (Button) v;
                    try {
                        checkAnswer();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    if (currentUser != null) {
                        emitButtonState(false);
                    }
                    if (currentUser == null) {
                        for (int i = 5; i < 10; i++) {
                            buttons.get(i).setEnabled(false);
                        }
                        for (int i = 0; i < 5; i++) {
                            buttons.get(i).setEnabled(true);
                        }
                    }
                }
            }

        });

    }

    private void checkAnswer() throws JSONException {
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

            if (currentUser != null) {
                resetButtonsSocket(firstClickedButton, "#00FF00");
                resetButtonsSocket(secondClickedButton, "#00FF00");
                updatePoints(currentPlayingUserIndex + 1, 2);
                updatePointsCount(currentPlayingUserIndex, 2);
              } else {
//                if (currentPlayingUserIndex == 0){
//                    Log.d("currentPlayingUserIndex", "currentPlayingUserIndex: " + currentPlayingUserIndex);
//                    updatePoints(currentPlayingUserIndex + 1, 2);
//                } else {
                    updatePoints(2);
//                }
            }
            checkIfGameIsFinished();

        } else {
            firstClickedButton.setTextColor(Color.parseColor("#FF0000"));
            secondClickedButton.setTextColor(Color.parseColor("#FF0000"));

            firstClickedButton.setClickable(false);

            if (currentUser != null) {
                resetButtonsSocket(firstClickedButton, "#FF0000");
                resetButtonsSocket(secondClickedButton, "#FF0000");
            }
            checkIfGameIsFinished();
        }

        firstClickedButton = null;
        secondClickedButton = null;
    }

    private void updateButtonColor(final int buttonTag, final String color) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 5; i++) {
                    Button button = buttons.get(i);
                    int tag = (Integer) button.getTag();
                    if (tag == buttonTag) {
                        button.setTextColor(Color.parseColor(color));
                        break;
                    }
                }
            }
        });
    }

    private void updatePoints(int currentPlayerNumber, int points) throws JSONException {
        if (currentUser != null) {
          if (currentPlayerNumber > 0) {
                playersFragment.updatePlayerPoints(currentPlayerNumber, points);
            }
        }
//        if (currentPlayerNumber >= 0) {
//            Log.d("currentPlayingUserIndex", "currentPlayerNumber: " + currentPlayerNumber +" points: " + points);
//            playersFragment.updatePlayerPoints(currentPlayerNumber, points);
//        }
    }
    private void updatePoints(int points) {
        playersFragment.updateGuestPoints(points);
    }

    private void switchPlayersTurn() {
        isPlayer1Turn = !isPlayer1Turn;
        roundsPlayed++;
    }

    private void checkIfGameIsFinished() throws JSONException {
        allButtonsClickable = true;
        boolean allButtonsAreCorrect = true;

        for (int i = 0; i < 5; i++) {
            if (buttons.get(i).isClickable()) {
                allButtonsClickable = false;
                allButtonsAreCorrect = false;
                break;
            }
            if (buttons.get(i).getCurrentTextColor() != Color.parseColor("#00FF00")) {
                allButtonsAreCorrect = false;
            }
        }
        if (allButtonsClickable) {
            if (currentUser == null) {
                endGame();
            } else {
                if (allButtonsAreCorrect) {
                    // Green: all answers are correct

                        socket.emit("startNextGame");
                } else {
                    if (roundIndex == 1) {
                        socket.emit("decrementRoundIndex");
                        socket.emit("startNextGame");
                    } else {
                        socket.emit("incrementRoundIndex");
                        socket.emit("continueGame");
                    }
                }
            }
        }
    }
    private void startNextGame() throws JSONException {
        if (currentUser != null) {
            if (roundsPlayed == TOTAL_ROUNDS) {
//                socket.emit("endGame");
                endGame();

            } else {
                if (playingUsernamesArray.length() > 0) {
                    currentPlayingUserIndex = (currentPlayingUserIndex ) % playingUsernamesArray.length();
                    currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
                    showToastAndEmit("Playing User: " + currentPlayingUser);
                }
                if (playingSocketsArray.length() > 0) {
                    currentNotPlayingUserIndex = (currentNotPlayingUserIndex) % playingSocketsArray.length();
                    currentPlayingUserIndex = (currentPlayingUserIndex) % playingSocketsArray.length();
                    currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
                    currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
                    socket.emit("enableTouch", currentPlayingUserSocketId);
                    socket.emit("disableTouch", currentNotPlayingUserSocketId);
                }
                socket.emit("retrieveSteps", currentNotPlayingUserSocketId);
                resetButtons();
                switchPlayersTurn();
                socket.emit("timerStart", currentPlayingUserSocketId);
                JSONObject timerData = new JSONObject();
                try {
                    timerData.put("duration", 30);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                socket.emit("startTimer", timerData);
                showToastAndEmit("Runda 1 je gotova! Pocinje nova runda.");
            }
        }
        else {
            endGame();

        }
    }
    private void continueGame() throws JSONException {
        if (playingUsernamesArray.length() > 0) {
            currentPlayingUserIndex = (currentPlayingUserIndex + 1) % playingUsernamesArray.length();
            currentPlayingUser = playingUsernamesArray.getString(currentPlayingUserIndex);
            showToastAndEmit("Playing User: " + currentPlayingUser);
        }
        if (playingSocketsArray.length() > 0) {
            currentNotPlayingUserIndex = (currentNotPlayingUserIndex + 1) % playingSocketsArray.length();
            currentPlayingUserIndex = (currentPlayingUserIndex ) % playingSocketsArray.length();
            currentNotPlayingUserSocketId = playingSocketsArray.getString(currentNotPlayingUserIndex );
            currentPlayingUserSocketId = playingSocketsArray.getString(currentPlayingUserIndex);
            socket.emit("enableTouch", currentPlayingUserSocketId);
            socket.emit("disableTouch", currentNotPlayingUserSocketId);
        }
            Button button;
            for (int i = 0; i < 10; i++) {
                if (buttons.get(i).getCurrentTextColor() != Color.parseColor("#00FF00")) {
                    button = buttons.get(i);
                    button.setTextColor(Color.parseColor("#FFFFFF"));
                    button.setClickable(true);
                    resetButtonsSocket(button, "#FFFFFF");
                } else {
                    if (buttons.get(i).getCurrentTextColor() != Color.parseColor("#FF0000")) {
                        button = buttons.get(i);
                        button.setEnabled(false);
                        enableMessage(button, false, false);
                    }
                }
            }

        socket.emit("timerStart", currentPlayingUserSocketId);
            JSONObject timerData = new JSONObject();
            try {
                timerData.put("duration", 30);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("startTimer", timerData);
            showToastAndEmit("Drugi igrač dobija šansu da poveže nepovezane pojmove!");
    }

    private void endGame() throws JSONException {
        if (currentUser != null) {
            showToastAndEmit("Igra je gotova! Sledi igra KORAK PO KORAK!");
            socket.emit("incrementCurrentGame");
        } else {
            Toast.makeText(SpojniceActivity.this, "Igra je gotova! Sledi igra KORAK PO KORAK!", Toast.LENGTH_SHORT).show();
        }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (countDownTimer != null) {
                        countDownTimer.cancel();
                        countDownTimer = null;
                    }
//                    if (currentUser != null) {
//                        socket.emit("enableTouch", currentNotPlayingUserSocketId);
//                        socket.emit("startActivity");
//                    }
//                    else {
                        Intent intent = new Intent(SpojniceActivity.this, KorakPoKorakActivity.class);
                        startActivity(intent);
                        finish();
//                    }
                }
            }, 3000);
    }

    private void startTimer() {
       if (currentUser == null) {
           if (countDownTimer != null) {
       }
           countDownTimer = new CountDownTimer(31000, 10000) {
               private Context context = SpojniceActivity.this.getApplicationContext();

               @Override
               public void onTick(long millisUntilFinished) {
               }

               @Override
               public void onFinish() {
                   try {
                       endGame();
                   } catch (JSONException e) {
                       throw new RuntimeException(e);
                   }
               }
           };

           countDownTimer.start();
       }
    }
    private void resetButtons() throws JSONException {
        for (int i = 0; i < buttons.size(); i++) {
            Button button = buttons.get(i);
            int buttonId = button.getId();
            button.setTextColor(Color.parseColor("#FFFFFF"));
            resetButtonsSocket(button, "#FFFFFF");
            socket.emit("buttonClickableSpojnice", buttonId, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentUser == null) {
           startTimer();
        } else {
            socket.emit("timerStart", currentPlayingUserSocketId);
        }

        buttonHandler.postDelayed(buttonRunnable, 10000);
    }
    @Override
    protected void onPause() {
        super.onPause();
        buttonHandler.removeCallbacks(buttonRunnable);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
    private void showToastAndEmit(String message) {
        Toast.makeText(SpojniceActivity.this, message, Toast.LENGTH_SHORT).show();
        socket.emit("showToast", message);
    }
    private void emitButtonState(boolean enableState) {
        JSONObject data = new JSONObject();
        try {
            data.put("enableState", enableState);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("buttonStateChanged", data);
    }
    private void emitColorChangeEvent(int buttonTag, String color) {
        JSONObject eventData = new JSONObject();
        try {
            eventData.put("buttonTag", buttonTag);
            eventData.put("color", color);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("colorChange", eventData);
    }
    void resetButtonsSocket(Button button, String color) throws JSONException {
        JSONObject reset = new JSONObject();
        reset.put("buttonId", button.getId());
        reset.put("color", color);
        socket.emit("reset_received", reset);
    }
    void handleResetButtonsSocket(JSONObject reset) throws JSONException {
        int buttonId = reset.getInt("buttonId");
        String color = reset.getString("color");

        Button button = findViewById(buttonId);

        runOnUiThread(() -> {
            button.setTextColor(Color.parseColor(color));
        });
    }
    void enableMessage(Button button,  boolean enabled, boolean clickable) throws JSONException {
        JSONObject message = new JSONObject();
        message.put("buttonId", button.getId());
        message.put("enabled", enabled);
        message.put("clickable", clickable);
        socket.emit("message_received", message);
    }

    void handleEnableMessage(JSONObject message) throws JSONException {
        int buttonId = message.getInt("buttonId");
        boolean enabled = message.getBoolean("enabled");
        boolean clickable = message.getBoolean("clickable");
        Button button = findViewById(buttonId);

        runOnUiThread(() -> {
            button.setEnabled(false);
            button.setClickable(false);
        });

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
                    ref.child("spojnicePoints").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int spojnicePoints = dataSnapshot.getValue(Integer.class);
                                int newPoints = (int) (spojnicePoints + points);
                                ref.child("spojnicePoints").setValue(newPoints);
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

