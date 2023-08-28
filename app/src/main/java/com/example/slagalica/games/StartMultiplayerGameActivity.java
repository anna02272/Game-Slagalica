package com.example.slagalica.games;


import static com.example.slagalica.MainActivity.socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.slagalica.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter;

public class StartMultiplayerGameActivity  extends AppCompatActivity {

    private String username;
    private Context context;
    private List<String> connectedUsernames;
    private  String connectedUserUsername;
    public static JSONArray usernamesArray;

    public StartMultiplayerGameActivity(Context context) {
        this.context = context;
        connectedUsernames = new ArrayList<>();
    }

    public void showPopupWindow(final View view) {
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.game_activity_start, null);

        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        boolean focusable = true;

        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        SharedPreferences preferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = preferences.getString("username", "");

        try {
            socket.emit("userConnected", new JSONObject().put("username", username));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        socket.on("updateConnectedUsers", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                usernamesArray = (JSONArray) args[0];
                connectedUsernames.clear();

                for (int i = 0; i < usernamesArray.length(); i++) {
                    try {
                         connectedUserUsername = usernamesArray.getString(i);
                        if (!connectedUserUsername.equals(username)) {
                            connectedUsernames.add(connectedUserUsername);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView opponentTextView = popupView.findViewById(R.id.opponent);
                        opponentTextView.setText(TextUtils.join(", ", connectedUsernames));


                    }
                });


            }
        });

       Button buttonStart = popupView.findViewById(R.id.startButton);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.emit("startGame");
                buttonStart.setEnabled(false);
                TextView titleText = popupView.findViewById(R.id.titleText);
                titleText.setText("Čeka se protivnik..");

            }
        });

                socket.on("gameStarting", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                TextView titleText = popupView.findViewById(R.id.titleText);
                                titleText.setText("Igra počinje za:");

                                final TextView timerTextView = popupView.findViewById(R.id.timer);
                                int countdown = 4;

                                new CountDownTimer(countdown * 1000, 1000) {
                                    public void onTick(long millisUntilFinished) {
                                        timerTextView.setText("" + (millisUntilFinished / 1000));
                                    }

                                    public void onFinish() {
                                        timerTextView.setText(" 0");
                                        popupWindow.dismiss();
                                    }

                                }.start();

                            }

                        });

                    }
                });

                popupView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        popupWindow.dismiss();
                        try {
                            socket.emit("userDisconnected", new JSONObject().put("username", username));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        return true;

                    }

        });
    }
}