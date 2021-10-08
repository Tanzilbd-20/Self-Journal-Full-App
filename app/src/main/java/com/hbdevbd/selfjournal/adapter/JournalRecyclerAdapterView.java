package com.hbdevbd.selfjournal.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hbdevbd.selfjournal.PostListActivity;
import com.hbdevbd.selfjournal.R;
import com.hbdevbd.selfjournal.model.Journal;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class JournalRecyclerAdapterView extends RecyclerView.Adapter<JournalRecyclerAdapterView.ViewHolder> {

    private Context context;
    private final List<Journal> journalList;
    private String documentId;
    private Dialog infoDialog;
    public static final String TAG = "TAG Journal Recycler";

    private CardView delete_cardView;
    private ProgressBar delete_progressBar;
    private ImageView bigImage;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Journals");

    public JournalRecyclerAdapterView(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.journal_row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Journal journal = journalList.get(position);

        String imageUrl;
        holder.userName_textView.setText(journal.getUserName());
        holder.title_textView.setText(journal.getTitle());
        holder.thought_textView.setText(journal.getThought());
        imageUrl = journal.getImageUrl();
        Picasso.get().load(imageUrl).fit()
                .error(android.R.drawable.stat_notify_error)
                .placeholder(android.R.drawable.stat_sys_download)
                .fit().into(holder.journal_image);

        PrettyTime prettyTime = new PrettyTime(Locale.getDefault());
        String ago = prettyTime.format(new Date(journal.getAddedTime().getSeconds() * 1000));
        holder.time_added_text.setText(ago);

    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView userName_textView,title_textView,thought_textView,time_added_text;
        public ImageView journal_image;
        public ImageButton share_imageButton;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            userName_textView = itemView.findViewById(R.id.row_userName_textView);
            title_textView = itemView.findViewById(R.id.row_journal_title);
            thought_textView = itemView.findViewById(R.id.row_journal_thought);
            time_added_text = itemView.findViewById(R.id.row_time_added_text);
            journal_image = itemView.findViewById(R.id.row_journal_imageView);
            share_imageButton = itemView.findViewById(R.id.row_share_imageButton);



            share_imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {/*
                    int position = getAdapterPosition();
                    String imageUrl = journalList.get(position).getImageUrl();*/
                    Journal journal = journalList.get(getAdapterPosition());

                    Picasso.get().load(journal.getImageUrl()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_SUBJECT,"From Self Journal");
                            intent.putExtra(Intent.EXTRA_TEXT,journal.getTitle()+"\n"+journal.getThought());

                            intent.putExtra(Intent.EXTRA_STREAM,getLocalBitmapUri(bitmap));
                            context.startActivity(Intent.createChooser(intent,context.getResources().getText(R.string.share_text)));

                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });

                }
            });

            journal_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    infoDialog = new Dialog(context);
                    infoDialog.setContentView(R.layout.journal_image_in_big_display);
                    infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    infoDialog.show();

                    bigImage = infoDialog.findViewById(R.id.bigImage);

                    Journal journal = journalList.get(getAdapterPosition());
                    String imageLink = journal.getImageUrl();

                    Picasso.get().load(imageLink)
                            .placeholder(android.R.drawable.stat_sys_download)
                            .error(android.R.drawable.stat_notify_error)
                            .into(bigImage);



                }
            });

            journal_image.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    infoDialog = new Dialog(context);
                    infoDialog.setContentView(R.layout.delete_popup);
                    infoDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    infoDialog.show();

                    delete_cardView = infoDialog.findViewById(R.id.card_delete);
                    delete_progressBar = infoDialog.findViewById(R.id.deleteProgressBar);

                    delete_cardView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            delete_progressBar.setVisibility(View.VISIBLE);

                            int position = getAdapterPosition();
                            Journal journal = journalList.get(position);
                            String userId = journal.getUserId();
                            String imageUrl = journal.getImageUrl();
                            Timestamp timeAdded = journal.getAddedTime();

                            collectionReference.whereEqualTo("addedTime",timeAdded)
                                    .whereEqualTo("userId",userId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {

                                            if(error != null){
                                                Log.d(TAG, "onEvent: "+error.getMessage());
                                            }

                                            if(!queryDocumentSnapshots.isEmpty()){
                                                for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots){



                                                    snapshot.getReference().delete()
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void unused) {
                                                                    Toast.makeText(context, "Journal Deleted !", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(context,PostListActivity.class);
                                                                     context.startActivity(intent);
                                                                }
                                                            });
                                                }
                                            }
                                        }
                                    });
                            StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                            storageReference.delete();
                        }
                    });


                    return true;
                }
            });


        }
    }


     public Uri getLocalBitmapUri(Bitmap bmp){

        Uri bmpUri = null;

        try {

            Log.d(TAG, "External files: " + this.context.getExternalFilesDir(null).toString());

            File imagePath = new File(this.context.getExternalFilesDir(null), "tempImages");
            if (!imagePath.exists()) {
                boolean hasBeenCreated = imagePath.mkdirs();
                Log.d(TAG, "imagePath does exist. Was it created? " + hasBeenCreated);
            }

            File newFile = new File(imagePath,"share image"+System.currentTimeMillis()+".png");
            FileOutputStream out = new FileOutputStream(newFile);
            bmp.compress(Bitmap.CompressFormat.PNG,90,out);


            String authorities = context.getPackageName()+".fileprovider";
            bmpUri = FileProvider.getUriForFile(context, authorities, newFile);
            out.close();

        }catch (Exception e){
            e.printStackTrace();
            Log.d(TAG, "getLocalBitmapUri: "+e.getMessage());
        }

        return bmpUri;
    }
}
