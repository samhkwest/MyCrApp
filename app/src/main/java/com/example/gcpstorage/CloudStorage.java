package com.example.gcpstorage;

import android.app.Activity;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CloudStorage {
    final static String PHOTO_FILE_PREFIX = "ocr_";
    final static String BUCKET_URL = "gs://captured-image";

/*    public static void uploadFile(String filePath) throws FileNotFoundException {
        InputStream stream = new FileInputStream(new File(filePath));

        StorageReference mountainsRef = getStorageRef(filePath);
        UploadTask uploadTask = mountainsRef.putStream(stream);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                System.out.println("uploadFile.onFailure");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                System.out.println("uploadFile.onSuccess");
            }
        });
    }*/

    public void uploadFile(File photoFile)  {
        FirebaseStorage storage = FirebaseStorage.getInstance(BUCKET_URL);
        StorageReference storageRef = storage.getReference();
        StorageReference ref = storageRef.child(photoFile.getName());

        UploadTask uploadTask = ref.putFile(Uri.fromFile(photoFile));
        uploadTask.addOnSuccessListener(new
                OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("Upload photo succeeded.");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Upload photo failed.");
                    }
                });
    }

    public File createImageFile(Activity act) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = PHOTO_FILE_PREFIX + timeStamp;
        File storageDir = act.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",  /* suffix */
                storageDir     /* directory */
        );

        //currentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
