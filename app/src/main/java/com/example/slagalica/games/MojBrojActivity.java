package com.example.slagalica.games;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.slagalica.MainActivity;

import com.example.slagalica.R;

public class MojBrojActivity extends AppCompatActivity {

    @Override
    public void onBackPressed() {
        return;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_moj_broj);


        Button buttonEnd = findViewById(R.id.button_end);

        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MojBrojActivity.this, MainActivity.class);

                startActivity(intent);
            }
        });
    }
}