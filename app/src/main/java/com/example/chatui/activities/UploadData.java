package com.example.chatui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chatui.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class UploadData extends AppCompatActivity implements View.OnClickListener {

    private ImageView imgView;
    private Button btnUpload;
    private Button btnTakePic;
    private Button btnFromGallery;
    private EditText edtDesc;
    private StorageReference reference, childRef;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private byte[] store;
    private Uri url;

    private static final int GALLERY_PICK = 2;
    private static final int TAKE_PICTURE = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_data);

        widgetDeclaration();

        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseStorage.getInstance().getReference();

        databaseReference = FirebaseDatabase.getInstance().getReference();

        btnTakePic.setOnClickListener(this);
        btnFromGallery.setOnClickListener(this);
        btnUpload.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_gallery:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    takeFromGallery();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, GALLERY_PICK);
                    }
                }
                break;
            case R.id.btn_take_picture:

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    takeFromCam();

                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, TAKE_PICTURE);
                    }
                }
                break;
            case R.id.btn_upload:
                uploadToStorage();
                break;
            default:
                Toast.makeText(this, "There is no such button.", Toast.LENGTH_SHORT).show();
        }
    }

    private void widgetDeclaration() {
        imgView = findViewById(R.id.imgPic);
        btnUpload = findViewById(R.id.btn_upload);
        btnFromGallery = findViewById(R.id.btn_gallery);
        btnTakePic = findViewById(R.id.btn_take_picture);
        edtDesc = findViewById(R.id.edt_desc);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == TAKE_PICTURE && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takeFromCam();
        } else {
            takeFromGallery();
        }
    }


    private void takeFromCam() {
        Intent fromCam = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(fromCam, TAKE_PICTURE);
    }

    private void takeFromGallery() {
        Intent fromGallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(fromGallery, GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GALLERY_PICK:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    Uri pic = data.getData();
                    imgView.setImageURI(pic);

                    imgView.buildDrawingCache();
                    Bitmap bitmap = imgView.getDrawingCache();
                    sendToStorage(bitmap);
                }
                break;
            case TAKE_PICTURE:
                if (resultCode == RESULT_OK) {
                    assert data != null;
                    Bundle getData = data.getExtras();
                    assert getData != null;
                    Bitmap insertImage = (Bitmap) getData.get("data");
                    imgView.setImageBitmap(insertImage);

                    imgView.buildDrawingCache();
                    Bitmap bit = imgView.getDrawingCache();
                    sendToStorage(bit);
                }
                break;
            default:
                Toast.makeText(this, "No Such Code.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendToStorage(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        store = stream.toByteArray();
    }


    private void uploadToStorage() {
        final String desc = String.valueOf(edtDesc.getText());
        childRef = reference.child(desc);
        UploadTask uploadTask = childRef.putBytes(store);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e.getMessage()!=null)
                    Log.i("TAG", e.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i("TAG", taskSnapshot.toString());
                childRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                       url=uri;
                        Log.i("TAG", "Url :" + uri);
                        databaseReference.child(user.getUid()).child("ImageUrl").push().setValue(String.valueOf(url));
                        databaseReference.child(user.getUid()).child("desc").push().setValue(desc);
                    }
                });

            }
        });

    }
}
