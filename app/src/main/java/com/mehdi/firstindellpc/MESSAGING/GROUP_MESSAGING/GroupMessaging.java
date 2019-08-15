package com.mehdi.firstindellpc.MESSAGING.GROUP_MESSAGING;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mehdi.firstindellpc.HOME.HomeActivity;
import com.mehdi.firstindellpc.MESSAGING.AdapMessage;
import com.mehdi.firstindellpc.MESSAGING.Message;
import com.mehdi.firstindellpc.Notification;
import com.mehdi.firstindellpc.PROFILE.Search;
import com.mehdi.firstindellpc.PROFILE.profilData;
import com.mehdi.firstindellpc.R;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class GroupMessaging extends AppCompatActivity implements AdapMessage.ClickMsgs {


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
    }

    String grPhoto;

    private DatabaseReference referenceMsg;
    private ChildEventListener listenerForMesg;

    private ImageView searchToAdd;

    private String lastMesg = "Sey hello";

    private AdapMessage adapMessage;
    private ArrayList<Message> messages;

    private ImageView PhotoView;
    private TextView NameView;
    private EditText message;

    private String myName, YourName;
    private String myPhotoUri;

    private LocationManager mLocationManager;

    private String USERS;


    FirebaseStorage storage;
    StorageReference storageReference;
    DatabaseReference reference;

    private String MyUid = "me";
    private String YouUid = "you";
    private String GroupId = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat);

        findViewById(R.id.back_chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GroupMessaging.this, HomeActivity.class));
            }
        });



        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        PhotoView = findViewById(R.id.ph);
        ImageView pickImage = findViewById(R.id.pick_img);
        ImageView sendMsg = findViewById(R.id.send);
        NameView = findViewById(R.id.namee);
        message = findViewById(R.id.edittext);
        RecyclerView rec = findViewById(R.id.rec_msg);


        adapMessage = new AdapMessage(this, this);
        rec.setHasFixedSize(true);
        rec.setLayoutManager(new LinearLayoutManager(this));
        rec.setAdapter(adapMessage);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(GroupMessaging.this);

        YouUid = getIntent().getStringExtra("uid");
        MyUid = preferences.getString("uid", "");

        if (YouUid == null) return;


        reference.child("PROFILES").child(MyUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profilData userInfo = dataSnapshot.getValue(profilData.class);
                if (userInfo == null) return;
                myName = userInfo.getName();
                myPhotoUri = userInfo.getPhoto();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.child("PROFILES").child(YouUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profilData userInfo = dataSnapshot.getValue(profilData.class);
                if (userInfo == null) return;
                YourName = userInfo.getName();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        grPhoto = "https://firebasestorage.googleapis.com/v0/b/blood-fff9b.appspot.com/o/group.png?alt=media&token=aa3c3825-0efd-401b-b936-db849a9847a2";

        if (getIntent().getStringExtra("t").equals("create")) {
            USERS = MyUid + "," + YouUid;
            NameView.setText("Group");
            GroupId = "Admine--"+ YouUid + String.valueOf(new Random().nextInt(100000));
        Glide.with(this).load(grPhoto).into(PhotoView);
            reference.child("PROFILES").child(MyUid).child("GROUP_MESSAGE").child(GroupId).setValue(
                    new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));
            reference.child("PROFILES").child(YouUid).child("GROUP_MESSAGE").child(GroupId).setValue(
                    new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));


        } else if (getIntent().getStringExtra("t").equals("simple")) {
            GroupId = YouUid;
            reference.child("PROFILES").child(MyUid).child("GROUP_MESSAGE").child(GroupId).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                            if (message == null) return;
                           // GroupId = message.getId();
                            NameView.setText(message.getNamegroup());
                            Glide.with(GroupMessaging.this).load(message.getPhoto()).into(PhotoView);

                            USERS = message.getUsers();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
        }

        messages = new ArrayList<>();
        Notification notification = new Notification(this, HomeActivity.class);
        referenceMsg = reference.child("MESSAGS_GROUP").child(GroupId);
        listenerForMesg = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Message message = dataSnapshot.getValue(Message.class);
                if (message == null) return;
                messages.add(message);
                adapMessage.swapAdapter(messages);

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        referenceMsg.addChildEventListener(listenerForMesg);


        sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = message.getText().toString();
                if (content.length() > 0) {
                    Message message = new Message(myName, myPhotoUri, content, null, (int) System.currentTimeMillis(), MyUid, null);
                    referenceMsg.push().setValue(message);
                    lastMesg = content;

                    for (String user : USERS.split(",")) {
                        reference.child("PROFILES").child(user).child("GROUP_MESSAGE").child(GroupId).setValue(
                                new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));
                    }

                }
                message.setText("");
            }
        });


        findViewById(R.id.pick_location).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(GroupMessaging.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                    return;
                }
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                        0, mLocationListener);

            }
        });

        pickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

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
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messages.clear();
        referenceMsg.removeEventListener(listenerForMesg);
    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void openLocation(String uri) {
        showMap(Uri.parse(uri));
    }

    private void uploadImage(Uri filePath) {

        if (filePath == null) return;

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();

        final StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());

        Bitmap bitmap = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bitmap == null) {
            progressDialog.dismiss();
            Toast.makeText(GroupMessaging.this, "Failed Try again", Toast.LENGTH_SHORT).show();
            return;
        }
        ;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressDialog.dismiss();
                Toast.makeText(GroupMessaging.this, "Failed " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri r) {
                        progressDialog.dismiss();
                        Toast.makeText(GroupMessaging.this, "Uploaded", Toast.LENGTH_SHORT).show();

                        Message message = new Message(myName, myPhotoUri, null,
                                r.toString(), (int) System.currentTimeMillis(), MyUid, null);
                        referenceMsg.push().setValue(message);
                        lastMesg = myName + " Sent photo";

                        for (String user : USERS.split(",")) {
                            reference.child("PROFILES").child(user).child("GROUP_MESSAGE").child(GroupId).setValue(
                                    new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));
                        }

                    }
                });

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                        .getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int) progress + "%");
            }
        });


    }

    public void menu(View view) {
        findViewById(R.id.menu).setVisibility(View.VISIBLE);
        findViewById(R.id.grp).setVisibility(View.GONE);
        findViewById(R.id.del).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child("PROFILES").child(MyUid).child("GROUP_MESSAGE").child(GroupId).removeValue();
                startActivity(new Intent(GroupMessaging.this, HomeActivity.class));
            }
        });
        findViewById(R.id.addone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(GroupMessaging.this, Search.class);
                i.putExtra("type", 1);
                i.putExtra("gId", GroupId);
                startActivity(i);
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.menu).setVisibility(View.GONE);
            }
        });
    }


    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            String loca = "geo:" + String.valueOf(location.getLatitude()) + "," + String.valueOf(location.getLongitude());
            Message message = new Message(myName, myPhotoUri, null,
                    null, (int) System.currentTimeMillis(), MyUid, loca);
            referenceMsg.push().setValue(message);
            lastMesg = loca;
            for (String user : USERS.split(",")) {
                reference.child("PROFILES").child(user).child("GROUP_MESSAGE").child(GroupId).setValue(
                        new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));
            }
            mLocationManager.removeUpdates(mLocationListener);
        }
        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }
        @Override
        public void onProviderEnabled(String s) {

        }
        @Override
        public void onProviderDisabled(String s) {

        }
    };


    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                            0, mLocationListener);
                }
            }
        }


    }



    /*

    else if (getIntent().getStringExtra("t").equals("addone")) {
            GroupId = getIntent().getStringExtra("gId2");
            reference.child("PROFILES").child(MyUid).child("GROUP_MESSAGE").child(GroupId).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                            if (message == null) return;
                            NameView.setText(message.getNamegroup());
                            Glide.with(GroupMessaging.this).load(message.getPhoto()).into(PhotoView);
                            USERS = message.getUsers();

                            if (USERS == null) return;


                            reference.child("PROFILES").child(YouUid).child("GROUP_REQUEST").child(GroupId).setValue(
                                    new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto)
                            );

                            Message a = new Message(myName, myPhotoUri, myName + " sent requst to " +
                                    YourName, null, (int) System.currentTimeMillis(), MyUid, null);

                            reference.child("MESSAGS_GROUP").child(GroupId).push().setValue(a);
                            lastMesg = myName + " sent requst to " + YourName;

                            for (String user : USERS.split(",")) {
                                reference.child("PROFILES").child(user).child("GROUP_MESSAGE").child(GroupId).setValue(
                                        new GroupMessage(GroupId, USERS, myName + ", " + YourName, lastMesg, grPhoto));
                            }



                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });






            reference.child("PROFILES").child(MyUid).child("GROUP_MESSAGE").child(GroupId).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            GroupMessage message = dataSnapshot.getValue(GroupMessage.class);
                            if (message == null) return;
                            GroupId = message.getId();
                            NameView.setText(message.getNamegroup());
                            Glide.with(GroupMessaging.this).load(message.getPhoto()).into(PhotoView);
                            String[] s = message.getUsers().split(",");
                            ArrayList<String> list = new ArrayList<>(Arrays.asList(s));
                            if (list.contains(YouUid)) {
                                USERS = message.getUsers();
                                return;
                            }
                            USERS = message.getUsers() + "," + YouUid;
                            for (String user : USERS.split(",")) {
                                reference.child("PROFILES").child(user).child("GROUP_MESSAGE").child(GroupId).setValue(
                                        new GroupMessage(GroupId, USERS, message.getNamegroup(), myName + " add " + YourName,
                                                grPhoto));
                            }

                        }


                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });



        }

     */


}