package com.example.slagalica.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.slagalica.R;

public class SkockoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_skocko);

        Button buttonNext = findViewById(R.id.button_next);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SkockoActivity.this, KorakPoKorakActivity.class);

                startActivity(intent);
            }
        });
    }
}