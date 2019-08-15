package com.mehdi.firstindellpc.PROFILE;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mehdi.firstindellpc.HOME.HomeActivity;
import com.mehdi.firstindellpc.HOME.MainActivity;
import com.mehdi.firstindellpc.MESSAGING.CHATROOM.RoomChatActivity;
import com.mehdi.firstindellpc.R;
import com.mehdi.firstindellpc.databinding.ProfileBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;



public class ProfileActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference referenceP;
    private FirebaseAuth auth;

    private String uid;
    private ValueEventListener valueEventListener;

    ProfileBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.profile);

        binding.backProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            }
        });


        database = FirebaseDatabase.getInstance();
        reference = database.getReference();
        auth = FirebaseAuth.getInstance();

        final SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this);


        String uidIntent = getIntent().getStringExtra("uid");

        if (uidIntent.equals("me")) {
            uid = preference.getString("uid", null);

            binding.editProfil.setVisibility(View.VISIBLE);
            binding.editProfil.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(ProfileActivity.this, EditProfile.class));
                }
            });

        } else {
            binding.log.setVisibility(View.GONE);
            uid = uidIntent;
        }

        if (uid != null) {

            referenceP = reference.child("PROFILES/" + uid);
            valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    profilData data = dataSnapshot.getValue(profilData.class);
                    updateUI(data);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            referenceP.addValueEventListener(valueEventListener);
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (valueEventListener != null){
            referenceP.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }

    public void message(View view) {
        if (uid == null) return;
        Intent i = new Intent(this, RoomChatActivity.class);
        i.putExtra("uid", uid);
        startActivity(i);
    }

    public void out(View v) {
        SharedPreferences.Editor preference = PreferenceManager.getDefaultSharedPreferences(ProfileActivity.this).edit();
        preference.putString("uid", null);
        preference.apply();
        auth.signOut();
        startActivity(new Intent(ProfileActivity.this, MainActivity.class));
    }

    public void updateUI(profilData data) {
        if (data == null) return;
        if (data.getName().length() > 0){
            binding.nam.setText(data.getName());
        }

        if (data.getEmail().length() > 0){
            binding.emailee.setText(data.getEmail());
        }

        if (data.getNumber() != null){
            binding.num.setText(data.getNumber());
        }

        if (data.getLocation()  != null){
            binding.lo.setText(data.getLocation());
        }
        if (data.getBloodLastTime() != null){
            binding.bloodTi.setText(data.getBloodLastTime());
        }
        if (data.getBloodType() != null){
            switch (data.getBloodType()){
                case "A" :
                    binding.bloodTy.setImageResource(R.drawable.ic_a);
                    break;
                case "B" :
                    binding.bloodTy.setImageResource(R.drawable.ic_b);
                    break;
                case "O" :
                    binding.bloodTy.setImageResource(R.drawable.ic_o);
                    break;
                case "AB" :
                    binding.bloodTy.setImageResource(R.drawable.ic_ab);
                    break;
            }
        }
        if (data.getDiseases() != null){
            binding.dis.setText(data.getDiseases());
        }


        if (data.getPhoto() != null){
            Glide.with(this).load(data.getPhoto()).into(binding.tof);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            onBackPressed();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, HomeActivity.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadImage(data.getData());
        }
    }

    StorageReference storageReference;


    private void uploadImage(Uri filePath) {

        if (filePath == null) return;

        ProgressDialog PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);
        PD.show();

        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            Toast.makeText(ProfileActivity.this, "Failed Try again", Toast.LENGTH_SHORT).show();
            PD.dismiss();
            return;
        }
        ;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                PD.dismiss();
                Toast.makeText(ProfileActivity.this, "Failed update photo profile", Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        PD.dismiss();
                        String url = uri.toString();
                        reference.child("PROFILES/" + uid).child("photo").setValue(url);
                        startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    }
                });

            }
        });

    }


}
