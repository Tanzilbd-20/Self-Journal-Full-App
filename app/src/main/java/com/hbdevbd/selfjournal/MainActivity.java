package com.hbdevbd.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.hbdevbd.selfjournal.controller.JournalApi;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private Button getStart_button;
    public static final String TAG = "MainActivity";

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("User Data");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null){
                    currentUser = firebaseAuth.getCurrentUser();
                    String userUid = currentUser.getUid();

                    collectionReference.whereEqualTo("userId",userUid)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {

                                    if(error != null){
                                        Log.d(TAG, "onEvent: "+error.getMessage());
                                    }

                                    if(!queryDocumentSnapshots.isEmpty()){
                                        for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                                            JournalApi journalApi = JournalApi.getINSTANCE();
                                            journalApi.setUserId(snapshot.getString("userId"));
                                            journalApi.setUserName(snapshot.getString("userName"));

                                            startActivity(new Intent(MainActivity.this,PostListActivity.class));

                                            finish();
                                        }
                                    }
                                }
                            });
                }
            }
        };

        getStart_button = findViewById(R.id.getStarted_button);

        getStart_button.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}