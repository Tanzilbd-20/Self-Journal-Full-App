package com.hbdevbd.selfjournal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hbdevbd.selfjournal.controller.JournalApi;
import com.hbdevbd.selfjournal.model.Journal;

import java.util.Date;
import java.util.Objects;

public class CreatePostJournal extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    //Widgets
    private ImageView background_image;
    private ImageButton upload_image_button;
    private EditText title_edit_text,thought_edit_text;
    private TextView userName;
    private ProgressBar progressBar;
    private Button post_journal_button;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journals");
    private StorageReference storageReference;

    //Variables
    private String currentUserId;
    private String currentUserName;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post_journal);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        background_image = findViewById(R.id.background_image);
        upload_image_button = findViewById(R.id.upload_image_button);
        title_edit_text = findViewById(R.id.title_edit_text);
        thought_edit_text = findViewById(R.id.thought_edit_text);
        progressBar = findViewById(R.id.post_journal_progressBar);
        userName = findViewById(R.id.post_userName_textView);
        post_journal_button = findViewById(R.id.post_journal_button);



        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        if(JournalApi.getINSTANCE() != null){
            currentUserName = JournalApi.getINSTANCE().getUserName();
            currentUserId = JournalApi.getINSTANCE().getUserId();
            userName.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null){

                }
            }
        };

        upload_image_button.setOnClickListener(view -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent,REQUEST_CODE);
        });

        post_journal_button.setOnClickListener(view -> {

            //Save the journal
            SaveJournal();
        });








    }

    private void SaveJournal() {
        progressBar.setVisibility(View.VISIBLE);
        String title = title_edit_text.getText().toString().trim();
        String thought = thought_edit_text.getText().toString().trim();

        if(!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thought) && imageUri != null){


           final StorageReference filePath = storageReference
                    .child("Journal_Images")
                    .child(title+"_"+ Timestamp.now().getSeconds());//It will give each Image an unique name.

            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {



                            Journal journal = new Journal();
                            journal.setUserId(currentUserId);
                            journal.setUserName(currentUserName);
                            journal.setTitle(title);
                            journal.setThought(thought);
                            journal.setImageUrl(uri.toString());
                            journal.setAddedTime(new Timestamp(new Date()));

                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    progressBar.setVisibility(View.INVISIBLE);

                                    startActivity(new Intent(CreatePostJournal.this,PostListActivity.class));

                                    finish();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("TAG", "Failed to Create Journal: "+e.getMessage());
                                }
                            });

                        }
                    });


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG", "Image Upload Failure : "+e.getMessage());
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });



        }else{
            progressBar.setVisibility(View.INVISIBLE);
            if(title.isEmpty() && thought.isEmpty() && imageUri == null){
                Toast.makeText(CreatePostJournal.this, "Empty Field Not Allowed", Toast.LENGTH_SHORT).show();
            }else if (title.isEmpty() && thought.isEmpty()){

                Toast.makeText(CreatePostJournal.this, "Enter Title and Thought.!", Toast.LENGTH_SHORT).show();
            }else if (title.isEmpty() && imageUri == null){

                Toast.makeText(CreatePostJournal.this, "Select an Image and enter your title!", Toast.LENGTH_SHORT).show();
            }else if (imageUri == null && thought.isEmpty()){


                Toast.makeText(CreatePostJournal.this, "Select an Image and enter your thought!", Toast.LENGTH_SHORT).show();
            }else if (title.isEmpty()){
                Toast.makeText(CreatePostJournal.this, "Your Title is empty !", Toast.LENGTH_SHORT).show();
            }else if (thought.isEmpty()){
                Toast.makeText(CreatePostJournal.this, "Your Thought is empty !", Toast.LENGTH_SHORT).show();
            }else if (imageUri == null){
                Toast.makeText(CreatePostJournal.this, "Select your Image !", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK){
            if(data != null){
                imageUri = data.getData();//We we'll have actual path of the Image.

                background_image.setImageURI(imageUri);//Show the image
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
        if(imageUri != null){
            upload_image_button.setAlpha(0.3f);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}