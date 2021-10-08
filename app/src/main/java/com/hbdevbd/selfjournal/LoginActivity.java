package com.hbdevbd.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbdevbd.selfjournal.controller.JournalApi;
import com.hbdevbd.selfjournal.model.Journal;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {


    //Widgets
    private AutoCompleteTextView email_edit_text;
    private EditText password_edit_text;
    private Button login_button,create_new_account_button;
    private ProgressBar progressBar;


    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference userColRef = db.collection("User Data");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        email_edit_text = findViewById(R.id.login_email_edit_text);
        password_edit_text = findViewById(R.id.login_password_edit_text);
        login_button = findViewById(R.id.login_button);
        create_new_account_button = findViewById(R.id.create_new_account_button);
        progressBar = findViewById(R.id.login_progressBar);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };



        login_button.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = email_edit_text.getText().toString().trim();
            String password = password_edit_text.getText().toString().trim();
            if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                LoginWithEmailAndPassword(email,password);
            }else{
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(LoginActivity.this, "Enter email and password !", Toast.LENGTH_SHORT).show();
            }

        });



        create_new_account_button.setOnClickListener(view -> {

            startActivity(new Intent(LoginActivity.this,CreateAccountActivity.class));
        });
    }

    private void LoginWithEmailAndPassword(String email, String password) {

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){

            firebaseAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){

                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                assert user != null;
                                String currentUserId = user.getUid();
                                Log.d("TAG", "onComplete: "+currentUserId);

                                userColRef.whereEqualTo("userId",currentUserId)
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                            @Override
                                            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                                if(error != null){
                                                    Log.d("TAG", "Login Error: "+error.getMessage());
                                                }

                                                assert value != null;
                                                if(!value.isEmpty()){
                                                    for (QueryDocumentSnapshot snapshot : value){
                                                        JournalApi journalApi = JournalApi.getINSTANCE();
                                                        journalApi.setUserId(snapshot.getString("userId"));
                                                        journalApi.setUserName(snapshot.getString("userName"));

                                                        Log.d("TAG", "onEvent: "+snapshot.getId());

                                                        if(snapshot.contains("isAdmin")){
                                                            Log.d("TAG", "onEvent: true");
                                                            startActivity(new Intent(LoginActivity.this,CreatePostJournal.class));
                                                        }else{
                                                            Log.d("TAG", "onEvent: false");
                                                            startActivity(new Intent(LoginActivity.this,PostListActivity.class));
                                                        }

                                                        //Go to PostList Activity
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        startActivity(new Intent(LoginActivity.this,PostListActivity.class));
                                                        finish();
                                                    }
                                                }


                                            }
                                        });


                            }else{
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(LoginActivity.this, "Incorrect Email or Password", Toast.LENGTH_SHORT).show();
                            }


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}