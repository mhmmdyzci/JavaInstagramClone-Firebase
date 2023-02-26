package com.mhmmdyzci.instagramclonejava.view;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mhmmdyzci.instagramclonejava.databinding.ActivityUploadActivituBinding;

import java.util.HashMap;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    // galeriye gitme intentini başlatıp veriye geri aldığımız
    ActivityResultLauncher<Intent> activityResultLauncher;
    // izin istediğimiz
    ActivityResultLauncher<String> permissionLauncher;
    Uri imageData ;
    private ActivityUploadActivituBinding binding;
    private FirebaseAuth auth;
    private FirebaseStorage firebaseStorage;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadActivituBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLaumcher();
        firebaseStorage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();
    }
    public void uploadButtonClicked(View view){
        if (imageData != null){
            // unique id veren sınıf
            UUID uuid = UUID.randomUUID();
            String imageName = "images/" + uuid + ".png";



            // Referans: storage nereye ne kaydetmek istediğimizin yerini tutan obje
            // child klasör açıyor
            // arka planda çalıştır
            storageReference.child(imageName).putFile(imageData)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Dowload url
                            StorageReference newReference = firebaseStorage.getReference(imageName);
                            newReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String downloadUrl = uri.toString();
                                    String comment = binding.commentText.getText().toString();
                                    FirebaseUser user = auth.getCurrentUser();
                                    String email= user.getEmail();
                                    HashMap<String, Object> postData = new HashMap<>();
                                    postData.put("useremail", email);
                                    postData.put("dowloadUrl",downloadUrl);
                                    postData.put("comment", comment);
                                    postData.put("date", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Post").add(postData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            Intent intent = new Intent(UploadActivity.this, FeedActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                                        }
                                    });


                                }
                            });


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UploadActivity.this,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();

                        }
                    });
        }else{
            Toast.makeText(UploadActivity.this,"Please select a picture",Toast.LENGTH_LONG).show();
        }

    }
    public void selectImage(View view){
        // ilk izin var mı onu kontrol et
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            // Mantığını kullanıcıya göstermeli miyiz? Neden bu izni istediğimizi göster
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                //                                         Kullanıcı ne zaman okeye basarsa o zaman kapat
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // izni iste
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();

            } else {
                //izni iste
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

        } else {
            //izin verilmiş galeriye git[       git ve ordan bişey al
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }


    }
    private void registerLaumcher(){
        //                                                 ne tarz işlem yapcağımızı soyluyoruz                  bu dinleyici callback
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // resultun sonuç kodu OK mu
                if(result.getResultCode()== RESULT_OK){
                    //  resulttan veriyi alıp intente kaydettik
                    Intent intentFromResult =result.getData();
                    // veri boşmu değil mi
                    if(intentFromResult != null){
                        //        urİ döndürüyo nerde kayıtlı onu söylüyo
                        imageData = intentFromResult.getData();
                        binding.imageView.setImageURI(imageData);
                    }
                }

            }
        });
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }else {
                    Toast.makeText(UploadActivity.this, "Permission needed for gallery", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}