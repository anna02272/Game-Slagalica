package com.example.slagalica.games;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.example.slagalica.R;

public class KorakPoKorakActivity extends AppCompatActivity {

    private List<Button> buttons;
    private FirebaseDatabase firebaseDatabase;
    private List<String> stepKeys;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_activity_korak_po_korak);

        Button buttonNext = findViewById(R.id.button_next);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KorakPoKorakActivity.this, MojBrojActivity.class);
                startActivity(intent);
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

        firebaseDatabase = FirebaseDatabase.getInstance();
        random = new Random();

        retrieveStepKeys();
    }

    private void retrieveStepKeys() {
        firebaseDatabase.getReference("korak_po_korak").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    stepKeys = new ArrayList<>();
                    for (DataSnapshot stepSnapshot : dataSnapshot.getChildren()) {
                        if (stepSnapshot.getKey().startsWith("steps")) {
                            stepKeys.add(stepSnapshot.getKey());
                        }
                    }

                    setButtonListeners();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }


    private void setButtonListeners() {
        for (int i = 0; i < buttons.size(); i++) {
            final Button button = buttons.get(i);
            final int buttonIndex = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (stepKeys != null && !stepKeys.isEmpty()) {
                        String randomKey = stepKeys.get(random.nextInt(stepKeys.size()));
                        retrieveStepsForButton(buttonIndex, randomKey);
                    }
                }
            });
        }
    }


    private void retrieveStepsForButton(final int buttonIndex, String stepKey) {
        firebaseDatabase.getReference("korak_po_korak/" + stepKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, String> stepsMap = (Map<String, String>) dataSnapshot.getValue();
                if (stepsMap != null && !stepsMap.isEmpty()) {
                    String step = stepsMap.get("step" + (buttonIndex + 1));
                    if (step != null) {
                        buttons.get(buttonIndex).setText(step);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

}


