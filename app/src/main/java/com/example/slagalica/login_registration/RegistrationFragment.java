package com.example.slagalica.login_registration;

import android.content.Intent;
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
                    signupEmail.setError("Email cannot be empty");
                } else if (!isValidEmail(email)) {
                    signupEmail.setError("Invalid email format");
                }
                if (username.isEmpty()) {
                    signupUsername.setError("Username cannot be empty");
                }
                if (pass.isEmpty()) {
                    signupPassword.setError("Password cannot be empty");
                } else if (pass.length() < 8) {
                    signupPassword.setError("Password must be at least 8 characters long");
                }
                if (confirmPass.isEmpty() || !confirmPass.equals(pass)){
                    signupConfirmPass.setError("Confirm password must be same as password");
                } else {
                    auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                String userId = firebaseUser.getUid();


                                DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
                                User user = new User(username);
                                usersRef.child(userId).setValue(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getActivity(), "SignUp Successful", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(getActivity(), RegistrationLoginActivity.class));
                                                } else {
                                                    Toast.makeText(getActivity(), "SignUp Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                Toast.makeText(getActivity(), "SignUp Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), RegistrationLoginActivity.class));
                            } else {
                                Toast.makeText(getActivity(), "SignUp Failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
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
