package com.mhmmdyzci.instagramclonejava.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.mhmmdyzci.instagramclonejava.R;
import com.mhmmdyzci.instagramclonejava.adapter.PostAdapter;
import com.mhmmdyzci.instagramclonejava.databinding.ActivityFeedBinding;
import com.mhmmdyzci.instagramclonejava.model.Post;

import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    private ActivityFeedBinding binding;
    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        postArrayList = new ArrayList<>();
        getData();
        binding.recylerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postArrayList);
        binding.recylerView.setAdapter(postAdapter);


    }
    private  void getData(){

        firebaseFirestore.collection("Post").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error !=null){
                    Toast.makeText(FeedActivity.this,error.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
                if(value != null){
                    for (DocumentSnapshot snapshot : value.getDocuments()){
                        Map<String, Object> data = snapshot.getData();
                        String userEmail = (String) data.get("useremail");
                        String comment = (String) data.get("comment");
                        String dowladUrl = (String) data.get("dowloadUrl");
                        Post post = new Post(userEmail,comment,dowladUrl);
                        postArrayList.add(post);




                    }
                }
                //Adaptöre haber ver yeni veri geldi
                postAdapter.notifyDataSetChanged();

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // menüdeki iki seçenekten biri seçilirse ne olcak
        if (item.getItemId() == R.id.addPost) {
            // Upload aktivitiye gidecek
            Intent intent = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intent);

        } else if (item.getItemId() == R.id.signout){
            //Çıkış yapacak (Main activitye gönder )
            auth.signOut();
            Intent intent = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Bağlama işlemi
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}