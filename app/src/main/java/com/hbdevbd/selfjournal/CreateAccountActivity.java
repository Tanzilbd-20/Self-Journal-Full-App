package com.hbdevbd.selfjournal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hbdevbd.selfjournal.controller.JournalApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreateAccountActivity extends AppCompatActivity {

    //Widgets
    private AutoCompleteTextView email_edit_text;
    private EditText userName_edit_text,password_editText;
    private Button create_account_button;
    private ProgressBar progressBar;


    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("User Data");

    //Users info
    public static final String USER_ID = "userId";
    public static final String USER_NAME = "userName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        Objects.requireNonNull(getSupportActionBar()).setElevation(0);



        //Widgets
        email_edit_text = findViewById(R.id.create_email_edit_text);
        userName_edit_text = findViewById(R.id.create_userName_edit_text);
        password_editText = findViewById(R.id.create_password_edit_text);
        create_account_button = findViewById(R.id.create_account_button);
        progressBar = findViewById(R.id.create_account_progressBar);


        //Firebase
        firebaseAuth = FirebaseAuth.getInstance();





        //Functions starts here

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };


        create_account_button.setOnClickListener(view -> {
            String userId = userName_edit_text.getText().toString().trim();
            String email = email_edit_text.getText().toString().trim();
            String password = password_editText.getText().toString().trim();

            if(!TextUtils.isEmpty(userId) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

                CreateAccountWithEmail(userId,email,password);
            }else{
                Toast.makeText(this, "Please Provide Required Information !", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void CreateAccountWithEmail(String userName, String email, String password) {

        if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){
                        //we take users to JournalListActivity

                        user = firebaseAuth.getCurrentUser();
                        assert user != null;
                        String currentUserId = user.getUid();

        //Here we'll create a Map to create our user Data Collection in our FireStore.
                        Map<String ,String> userObj = new HashMap<>();
                        userObj.put(USER_ID,currentUserId);
                        userObj.put(USER_NAME,userName);

                        //Add the User Data into our FireStore.
                        collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {

                                documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                        if(task.getResult().exists()){

                                            String userName = task.getResult().getString("userName");
                                            progressBar.setVisibility(View.INVISIBLE);

                                            //This is our global Api for userId and userName
                                            JournalApi journalApi = JournalApi.getINSTANCE();
                                            journalApi.setUserId(currentUserId);
                                            journalApi.setUserName(userName);


                                            startActivity(new Intent(CreateAccountActivity.this,CreatePostJournal.class));


                                            finish();
                                        }else{
                                            progressBar.setVisibility(View.INVISIBLE);
                                        }


                                    }
                                });


                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Creating new account failed: "+e.getMessage());
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });



                    }else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(CreateAccountActivity.this, "This email already in use !", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG", "Failed to create account: "+e.getMessage());
                }
            });

        }else{
            Toast.makeText(CreateAccountActivity.this, "Fulfill Required Field", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}