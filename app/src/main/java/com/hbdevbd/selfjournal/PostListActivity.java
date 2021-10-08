package com.hbdevbd.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbdevbd.selfjournal.adapter.JournalRecyclerAdapterView;
import com.hbdevbd.selfjournal.controller.JournalApi;
import com.hbdevbd.selfjournal.model.Journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView noThought_text_view;
    private JournalRecyclerAdapterView recyclerAdapterView;

    public static final String TAG = "PostListActivity";
    private List<Journal> journalList;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;
    private CollectionReference collectionReference = db.collection("Journals");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        noThought_text_view = findViewById(R.id.noThought_text);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        journalList = new ArrayList<>();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();






    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {


        switch (item.getItemId()){
            case R.id.menu_add_new_journal:
                //go to Create Journal page
                if(currentUser != null && firebaseAuth != null){
                    startActivity(new Intent(PostListActivity.this,CreatePostJournal.class));

                }

                break;

            case R.id.menu_logout:
                //go to MainActivity
                if(currentUser != null && firebaseAuth != null){
                    firebaseAuth.signOut();
                    startActivity(new Intent(PostListActivity.this,MainActivity.class));
                    finish();

                }

                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();


       collectionReference.whereEqualTo("userId",JournalApi.getINSTANCE().getUserId())
               .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
           @Override
           public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

               if(!queryDocumentSnapshots.isEmpty()){
                   for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){
                       Journal journal = snapshot.toObject(Journal.class);

                       journalList.add(journal);
                   }

                   recyclerAdapterView = new JournalRecyclerAdapterView(PostListActivity.this,journalList);

                   recyclerView.setAdapter(recyclerAdapterView);
                   recyclerAdapterView.notifyDataSetChanged();


               }else{

                   noThought_text_view.setVisibility(View.VISIBLE);
               }

           }
       }).addOnFailureListener(new OnFailureListener() {
           @Override
           public void onFailure(@NonNull Exception e) {

           }
       });

    }




    @Override
    protected void onStop() {
        super.onStop();
        if(journalList != null){
            journalList.clear();
        }
    }
}