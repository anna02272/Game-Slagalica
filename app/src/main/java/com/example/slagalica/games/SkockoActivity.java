package com.example.slagalica.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.slagalica.R;

public class SkockoActivity extends AppCompatActivity {

    private CountDownTimer countDownTimer;
    private TextView timerTextView;
    private long timeLeftInMillis = 30000; // 30 seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_skocko);

        Button buttonNext = findViewById(R.id.button_next);
        timerTextView = findViewById(R.id.time);
        startTimer();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SkockoActivity.this, KorakPoKorakActivity.class);

                startActivity(intent);
            }
        });
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

    public void onIconClick(View view) {
        ImageView clickedIcon = (ImageView) view;
        // Ovdje implementirajte funkcionalnost unosa ikonice u prazno polje ili drugu logiku
    }



}