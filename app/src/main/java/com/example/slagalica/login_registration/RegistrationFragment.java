package com.example.slagalica.login_registration;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.slagalica.MainActivity;
import com.example.slagalica.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;


public class RegistrationFragment extends Fragment {

    public RegistrationFragment() {
        // Required empty public constructor
    }
    private FirebaseAuth auth;
    private EditText signupEmail, signupUsername,  signupPassword, signupConfirmPass;
    private Button signupButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment_registration, container, false);

        Button buttonCancelLogin = view.findViewById(R.id.cancel_signup_button);

        buttonCancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
            }
        });


        auth = FirebaseAuth.getInstance();
        signupEmail = view.findViewById(R.id.signup_email);
        signupUsername = view.findViewById(R.id.signup_username);
        signupPassword = view.findViewById(R.id.signup_password);
        signupConfirmPass = view.findViewById(R.id.signup_confirm);
        signupButton = view.findViewById(R.id.signup_button);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = signupEmail.getText().toString().trim();
                String username = signupUsername.getText().toString().trim();
                String pass = signupPassword.getText().toString().trim();
                String confirmPass = signupConfirmPass.getText().toString().trim();
                if (email.isEmpty()) {
                    signupEmail.setError("Email mora biti popunjen ");
                } else if (!isValidEmail(email)) {
                    signupEmail.setError("Pogrešan format email-a");
                }
                if (username.isEmpty()) {
                    signupUsername.setError("Korisničko ime mora biti popunjeno");
                }
                if (pass.isEmpty()) {
                    signupPassword.setError("Lozinka mora biti popunjena");
                } else if (pass.length() < 8) {
                    signupPassword.setError("Lozinka mora imati bar 8 karaktera");
                }
                if (confirmPass.isEmpty() || !confirmPass.equals(pass)){
                    signupConfirmPass.setError("Ponovljena lozinka nije ista");
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String userId = firebaseUser.getUid();
                                int initialTokens = 5;
                                int initialStars = 0;
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String initialDate = dateFormat.format(new Date());
                                int initialPlayedGames = 0;
                                int initialWonGames = 0;
                                int initialLostGames = 0;
                                int initialKoZnaZna = 0;
                                int initialSpojnice = 0;
                                int initialAsocijacije= 0;
                                int initialSkocko = 0;
                                int initialKorakPoKorak = 0;
                                int initialMojBroj = 0;
                                int initialSpojnicePoints = 0;
                                int initialKorakPoKorakPoints = 0;
                                int initialMojBrojPoints = 0;


                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                User user = new User(username, initialTokens, initialStars, initialDate,  initialPlayedGames, initialWonGames, initialLostGames,
                                        initialKoZnaZna, initialSpojnice, initialAsocijacije, initialSkocko, initialKorakPoKorak, initialMojBroj,
                                        initialSpojnicePoints, initialKorakPoKorakPoints, initialMojBrojPoints);
                                usersRef.child(userId).setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "Registracija uspešna ", Toast.LENGTH_SHORT).show();
                                                   startActivity(new Intent(getActivity(), RegistrationLoginActivity.class));
                                                } else {
                                                    Toast.makeText(getActivity(), "Registracija neuspešna" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
//                                SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = preferences.edit();
//                                editor.putInt("tokens", initialTokens);
//                                editor.putInt("stars", initialStars);
//                                editor.apply();
                                Toast.makeText(getActivity(), "Registracija uspešna", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), RegistrationLoginActivity.class));
                            } else {
                                Toast.makeText(getActivity(), "Registracija neuspešna" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
        return view;
    }
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

}
