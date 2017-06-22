package io.intelehealth.client.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseFile;
import com.parse.ParseObject;

import java.io.ByteArrayOutputStream;
import java.io.File;

import io.intelehealth.client.utilities.HelperMethods;
import io.intelehealth.client.database.DelayedJobQueueProvider;
import io.intelehealth.client.objects.WebResponse;

/**
 * Created by Dexter Barretto on 6/9/17.
 * Github : @dbarretto
 */

public class PersonPhotoUploadService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    private String imageName;
    private Bitmap bitmap;

    public PersonPhotoUploadService(String name) {
        super(name);
    }

    private static final String LOG_TAG = PersonPhotoUploadService.class.getSimpleName();

    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    public int mId = 2;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        String patientId = intent.getStringExtra("patientID");
        String person = intent.getStringExtra("person");

        String base64EncodedImage = null;

        String baseDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        String filePath = baseDir + File.separator + "Profile_Images" + File.separator + "patient_photo" + File.separator +
                patientId + ".jpg";

        File profile_image = new File(filePath);
        imageName = profile_image.getName();
        imageName = imageName.replace('%', '_');

        if (profile_image != null) {
            bitmap = BitmapFactory.decodeFile(filePath);
            byte[] byteArray = bitmapToByteArray(bitmap);
            base64EncodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        if (base64EncodedImage != null) {
            String photoString =
                    String.format("{\"person\":\"%s\"," +
                                    "\"base64EncodedImage\":\"%s\"}",
                            person,
                            base64EncodedImage);

            WebResponse responsePersonImage;
            responsePersonImage = HelperMethods.postCommand("personimage", photoString, getApplicationContext());

            if (responsePersonImage != null && responsePersonImage.getResponseCode() != 200) {
                String newText = "Person Image posting unsuccessful";
                mBuilder.setContentText(newText);
                mNotifyManager.notify(mId, mBuilder.build());
                addJobToQueue(intent);
                Log.d(LOG_TAG, "Person Image Posting Unsuccessful");

            } else if (responsePersonImage == null) {
                addJobToQueue(intent);
                Log.d(LOG_TAG, "Person Image Posting unsuccessful");

            } else {
                String newText = "Person Image Posted successfully.";
                uploadImage();
                mBuilder.setContentText(newText);
                mNotifyManager.notify(mId, mBuilder.build());
                if (intent.hasExtra("queueId")) {
                    int queueId = intent.getIntExtra("queueId", -1);
                    removeJobFromQueue(queueId);
                }

            }

        }
    }

    public void uploadImage() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // Compress image to lower quality scale 1 - 100
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] image = stream.toByteArray();
        // Create the ParseFile
        ParseFile file = new ParseFile(imageName + ".png", image);
        // Upload the image into Parse Cloud
        //file.saveInBackground();
        // Create a New Class called "ImageUpload" in Parse
        ParseObject imgupload = new ParseObject("ImageUpload");
        // Create a column named "ImageName" and set the string
        imgupload.put("Patient_ID", "test upload");
        // Create a column named "ImageFile" and insert the image
        imgupload.put("ImageFile", file);
        // Create the class and the columns
        imgupload.saveInBackground();
        Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
    }

    private byte[] bitmapToByteArray(Bitmap image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }


    private void addJobToQueue(Intent intent) {
        if (!intent.hasExtra("queueId")) {
            Log.d(LOG_TAG, "Adding to Queue");
            // Add a new Delayed Job record
            ContentValues values = new ContentValues();
            values.put(DelayedJobQueueProvider.JOB_TYPE, "photoUpload");
            values.put(DelayedJobQueueProvider.PATIENT_NAME, intent.getStringExtra("name"));
            values.put(DelayedJobQueueProvider.JOB_PRIORITY, 1);
            values.put(DelayedJobQueueProvider.JOB_REQUEST_CODE, 0);
            values.put(DelayedJobQueueProvider.PATIENT_ID, intent.getStringExtra("patientID"));
            values.put(DelayedJobQueueProvider.DATA_RESPONSE, intent.getStringExtra("person"));
        }
    }

    private void removeJobFromQueue(int queueId) {
        Log.d(LOG_TAG, "Removing from Queue");
        if (queueId > -1) {
            String url = DelayedJobQueueProvider.URL + "/" + queueId;
            Uri uri = Uri.parse(url);
            int result = getContentResolver().delete(uri, null, null);
            if (result > 0) {
                Log.i(LOG_TAG, result + " row deleted");
            } else {
                Log.e(LOG_TAG, "Database error while deleting row!");
            }
        }

    }
}
