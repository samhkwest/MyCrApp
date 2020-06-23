package com.example.mycrapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.gcpstorage.CloudStorage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnCapture;
    private Button btnDetect;
    private ImageView imgView;
    private TextView txtView;
    private Bitmap imageBitmap;
    private String currentPhotoPath;
    private Uri photoURI;
    private File photoFile;
    private CloudStorage cloudStorage = new CloudStorage();
    final static int REQUEST_IMAGE_CAPTURE = 1;
    final static int REQUEST_TAKE_PHOTO = 1;
    //final static String PHOTO_FILE_PREFIX = "ocr_";
    //final static String BUCKET_URL = "gs://captured-image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCapture = findViewById(R.id.btnCapture);
        btnDetect = findViewById(R.id.btnDetect);
        imgView = findViewById(R.id.imgView);
        txtView = findViewById(R.id.txtDetectedText);

        btnCapture.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        dispatchTakePictureIntent();
                        txtView.setText("");
                    }
                }
        );
        btnDetect.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        detectTextFromImage();
                    }
                }
        );
    }

    private void detectTextFromImage() {
        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();
        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("Error: ", e.getMessage());
            }
        });
    }

    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();
        if (blockList.size() == 0){
            Toast.makeText(this, "No Text Found in picture.", Toast.LENGTH_SHORT).show();
        }else{
            for (FirebaseVisionText.Block block : firebaseVisionText.getBlocks())
            {
                String text = block.getText();
                txtView.setText(text);
            }
        }
    }

    private void dispatchTakePictureIntent() {
        System.out.println("dispatchTakePictureIntent...");
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check if there is a camera respond to this activity
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                photoFile = cloudStorage.createImageFile(this);
                currentPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                System.out.println("Exception: "+ ex.getMessage());
            }
            System.out.println("photoFile: "+ photoFile.toString());

            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,"com.example.mycrapp.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                takePictureIntent.putExtra("return-data", true);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult...");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoURI));
            } catch (FileNotFoundException e) {
                System.out.println("Exception: "+e.getMessage());
            }
            imgView.setImageBitmap(imageBitmap);

            cloudStorage.uploadFile(photoFile);
        }
    }

/*    private File createImageFile(String filePrex) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = filePrex + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  *//* prefix *//*
                ".jpg",  *//* suffix *//*
                storageDir     *//* directory *//*
        );

        //currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void uploadFile(File photoFile)  {
        FirebaseStorage storage = FirebaseStorage.getInstance(BUCKET_URL);
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child(photoFile.getName());

        UploadTask uploadTask = ref.putFile(Uri.fromFile(photoFile));
        uploadTask.addOnSuccessListener(new
            OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    System.out.println("Upload photo Success");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println("Upload photo failed");
                }
            });
    }

    private void configContext(Intent intent, Uri uri){
        System.out.println("configContext...");
        Context context = getApplicationContext();
        List<ResolveInfo> resInfoList = context.getPackageManager()
                .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resInfoList) {
            String packageName = resolveInfo.activityInfo.packageName;
            context.grantUriPermission(packageName, uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
*/
}