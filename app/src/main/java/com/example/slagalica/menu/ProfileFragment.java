package com.example.slagalica.menu;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.slagalica.MainActivity;
import com.example.slagalica.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.UUID;


public class ProfileFragment extends Fragment {
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView playedGamesTextView;
    private TextView wonGamesTextView ;
    private TextView lostGamesTextView ;
    private TextView korakPoKorakTextView ;
    private TextView spojniceTextView ;
    private TextView asocijacijeTextView;
    private TextView skockoTextView ;
    private TextView koZnaZnaTextView ;
    private TextView mojBrojTextView;
    private ImageView profileImageView;
    private String username;
    private String email;
    private int playedGames;
    private int wonGames ;
    private int lostGames ;
    private int koZnaZna ;
    private int spojnice ;
    private int asocijacije;
    private int skocko;
    private int korakPoKorak ;
    private int mojBroj ;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    StorageReference storageReference;
    private DatabaseReference usersRef;
    private String userId;
    private FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment_profile, container, false);

        usernameTextView = view.findViewById(R.id.profile_username);
        emailTextView = view.findViewById(R.id.profile_email);
        profileImageView = view.findViewById(R.id.uploadImage);


        playedGamesTextView = view.findViewById(R.id.playedGames);
        wonGamesTextView = view.findViewById(R.id.wonGames);
        lostGamesTextView = view.findViewById(R.id.lostGames);
        korakPoKorakTextView = view.findViewById(R.id.korakPoKorak);
        spojniceTextView = view.findViewById(R.id.spojnice);
        asocijacijeTextView = view.findViewById(R.id.asocijacije);
        skockoTextView = view.findViewById(R.id.skocko);
        koZnaZnaTextView = view.findViewById(R.id.koZnaZna);
        mojBrojTextView = view.findViewById(R.id.mojBroj);

        SharedPreferences preferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        username = preferences.getString("username", "");
        email = preferences.getString("email", "");


        usernameTextView.setText(username);
        emailTextView.setText(email);

        Button buttonLogout = view.findViewById(R.id.logout);
        Button saveButton = view.findViewById(R.id.saveImage);
        Button profileChangeButton = view.findViewById(R.id.changeImage);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        usersRef = firebaseDatabase.getReference("users");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
                Toast.makeText(getActivity(), "Succesfully logged out!", Toast.LENGTH_SHORT).show();
            }
        });
        profileChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        if (currentUser != null) {
            userId = currentUser.getUid();
            usersRef.child(userId).child("playedGames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        playedGames = dataSnapshot.getValue(Integer.class);
                        String playedGamesText = String.valueOf(playedGames);
                        playedGamesTextView.setText(playedGamesText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("wonGames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        wonGames = dataSnapshot.getValue(Integer.class);
                        String wonGamesText = String.valueOf(wonGames);
                        wonGamesTextView.setText(wonGamesText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("lostGames").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        lostGames = dataSnapshot.getValue(Integer.class);
                        String lostGamesText = String.valueOf(lostGames);
                        lostGamesTextView.setText(lostGamesText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("korakPoKorak").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        korakPoKorak = dataSnapshot.getValue(Integer.class);
                        String korakPoKorakText = String.valueOf(korakPoKorak);
                        korakPoKorakTextView.setText(korakPoKorakText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("spojnice").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        spojnice = dataSnapshot.getValue(Integer.class);
                        String spojniceText = String.valueOf(spojnice);
                        spojniceTextView.setText(spojniceText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("asocijacije").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        asocijacije = dataSnapshot.getValue(Integer.class);
                        String asocijacijeText = String.valueOf(asocijacije);
                        asocijacijeTextView.setText(asocijacijeText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });

            usersRef.child(userId).child("skocko").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        skocko = dataSnapshot.getValue(Integer.class);
                        String skockoText = String.valueOf(skocko);
                        skockoTextView.setText(skockoText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });

            usersRef.child(userId).child("koZnaZna").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        koZnaZna = dataSnapshot.getValue(Integer.class);
                        String koZnaZnaText = String.valueOf(koZnaZna);
                        koZnaZnaTextView.setText(koZnaZnaText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });

            usersRef.child(userId).child("mojBroj").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        mojBroj = dataSnapshot.getValue(Integer.class);
                        String mojBrojText = String.valueOf(mojBroj);
                        mojBrojTextView.setText(mojBrojText);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
            usersRef.child(userId).child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String imageUrl = dataSnapshot.getValue(String.class);

                        // Load the image into the ImageView using Picasso
                        Picasso.get().load(imageUrl).into(profileImageView);
                    } else {
                        // Handle the case where data doesn't exist
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
        return view;
    }
    private void selectImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContext().getContentResolver(),
                                filePath);
                 profileImageView.setImageBitmap(bitmap);
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
//    private void uploadImage() {
//        if (filePath != null) {
//
//            // Code for showing progressDialog while uploading
//            ProgressDialog progressDialog
//                    = new ProgressDialog(getContext());
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();
//
//            // Defining the child of storageReference
//            StorageReference ref
//                    = storageReference
//                    .child(
//                            "images/"
//                                    + UUID.randomUUID().toString());
//
//            // adding listeners on upload
//            // or failure of image
//            ref.putFile(filePath)
//                    .addOnSuccessListener(
//                            new OnSuccessListener<UploadTask.TaskSnapshot>() {
//
//                                @Override
//                                public void onSuccess(
//                                        UploadTask.TaskSnapshot taskSnapshot)
//                                {
//
//                                    // Image uploaded successfully
//                                    // Dismiss dialog
//                                    progressDialog.dismiss();
//                                    Toast
//                                            .makeText(getContext(),
//                                                    "Image Uploaded!!",
//                                                    Toast.LENGTH_SHORT)
//                                            .show();
//                                }
//                            })
//
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e)
//                        {
//
//                            // Error, Image not uploaded
//                            progressDialog.dismiss();
//                            Toast
//                                    .makeText(getContext(),
//                                            "Failed " + e.getMessage(),
//                                            Toast.LENGTH_SHORT)
//                                    .show();
//                        }
//                    })
//                    .addOnProgressListener(
//                            new OnProgressListener<UploadTask.TaskSnapshot>() {
//
//                                // Progress Listener for loading
//                                // percentage on the dialog box
//                                @Override
//                                public void onProgress(
//                                        UploadTask.TaskSnapshot taskSnapshot)
//                                {
//                                    double progress
//                                            = (100.0
//                                            * taskSnapshot.getBytesTransferred()
//                                            / taskSnapshot.getTotalByteCount());
//                                    progressDialog.setMessage(
//                                            "Uploaded "
//                                                    + (int)progress + "%");
//                                }
//                            });
//        }
//    }

    private void uploadImage() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Define the child of storageReference
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

            // Add listeners on upload or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image uploaded successfully
                            progressDialog.dismiss();

                            // Get the download URL of the uploaded image
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Save the download URL in the user's database entry
                                    saveImageUrlToDatabase(uri.toString());
                                }
                            });

                            Toast.makeText(getContext(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    private void saveImageUrlToDatabase(String imageUrl) {
        if (currentUser != null) {
            userId = currentUser.getUid();

            // Save the image URL in the user's database entry
            usersRef.child(userId).child("imageUrl").setValue(imageUrl)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Handle the success of saving the image URL
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle the failure of saving the image URL
                        }
                    });
        }
    }

}

