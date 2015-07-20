package com.example.dailyselfie;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by jb-edu on 15-07-14.
 */
public class DailySelfieActivity extends ListActivity {

    private static final String TAG = "DailySelfieActivity";
    private static final String PHOTOPATH_KEY = "mCurrentPhotoPath";
    private static final String SELFIESLOADED_KEY = "mSelfiesAlreadyLoaded";
    private static final String DAILYSELFIE_DIR = "/DailySelfies";
    private static final String IMAGE_PREFIX = "JPEG";
    private static final String FILENAME_SEPARATOR = "_";
    private static final String IMAGE_FILE_EXTENTION = ".jpg";
    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final String INTENT_IMG_TYPE = "image/*";
    private static final int REQUEST_TAKE_PHOTO = 1;

    private String mCurrentPhotoPath;
    private DailySelfieAlarmReceiver mAlarm;
    private boolean mSelfiesAlreadyLoaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_daily_selfie);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SELFIESLOADED_KEY)) {
                mSelfiesAlreadyLoaded = savedInstanceState.getBoolean(SELFIESLOADED_KEY);
            }
        }

        DailySelfieAdapter adapter = (DailySelfieAdapter)getListAdapter();

        if (adapter == null) {
            setListAdapter(new DailySelfieAdapter(getApplicationContext()));
            loadSelfiesFromExternalStorage();
        }
        else if (!mSelfiesAlreadyLoaded) {
            loadSelfiesFromExternalStorage();
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PHOTOPATH_KEY)) {
                mCurrentPhotoPath = savedInstanceState.getString(PHOTOPATH_KEY);
            }
        }

        // RQT #5: start the alarm by default on start-up. The user may also cancel the alarm and
        // reset it manually from the menu options if they wish.
        if (mAlarm == null)
            mAlarm = new DailySelfieAlarmReceiver();
        mAlarm.setAlarm(this.getApplicationContext());

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(PHOTOPATH_KEY, mCurrentPhotoPath);
        outState.putBoolean(SELFIESLOADED_KEY, mSelfiesAlreadyLoaded);
        super.onSaveInstanceState(outState);
    }

    // When a user clicks on a thumbnail in the ListView, load a larger version of the image
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(Intent.ACTION_VIEW);

        DailySelfie selfie = (DailySelfie) getListAdapter().getItem(position);

        intent.setDataAndType(Uri.fromFile(new File(selfie.getImagePath())), INTENT_IMG_TYPE);

        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_daily_selfie, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            // When the user clicks START ALARM, set the alarm.
            case R.id.action_camera:
                dispatchTakePictureIntent();
                return true;
            case R.id.action_delete_selected:
                deleteSelfies(false);
                return true;
            case R.id.action_delete_all:
                deleteSelfies(true);
                return true;
            case R.id.action_start_alarm:
                if (mAlarm == null)
                    mAlarm = new DailySelfieAlarmReceiver();
                mAlarm.setAlarm(this.getApplicationContext());
                return true;
            case R.id.action_cancel_alarm:
                if (mAlarm == null)
                    mAlarm = new DailySelfieAlarmReceiver();
                mAlarm.cancelAlarm(this.getApplicationContext());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Handle the result from the camera activity, either add a DailySelfie or delete the unused
    // file.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File file = new File(mCurrentPhotoPath);

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && file.length() > 0) {
            // The file was not empty, so add it, if it wasn't already added by a refresh.

            DailySelfieAdapter adapter = (DailySelfieAdapter)getListAdapter();
            int count = adapter.getCount();
            if (count > 0) {

                DailySelfie selfie = null;
                boolean selfieInAdapter = false;

                // Check if the selfie already exists in the adapter data.
                for (int i=0; i < count; i++) {
                    selfie = (DailySelfie) adapter.getItem(i);
                    if (selfie.getImagePath().equals(mCurrentPhotoPath)) {
                        selfieInAdapter = true;
                        break;
                    }
                }

                // Exclude duplicates that can result from data refreshes while new selfies
                // are being taken or if the Activity was destroyed.
                if (!selfieInAdapter) {
                    adapter.add(createSelfie(mCurrentPhotoPath));
                }

            }
            else {
                adapter.add(createSelfie(mCurrentPhotoPath));
            }

            adapter.sort(new DailySelfieComparator());
            adapter.notifyDataSetChanged();

        }
        else {
            // The user did not accept the picture, so remove the empty file.
            file.delete();
        }

    }

    // As recommended by the course instructor, Dr. Porter, this method has been adapted from the
    // sample source provided at: http://developer.android.com/training/camera/photobasics.html
    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        String imageFileName = IMAGE_PREFIX + FILENAME_SEPARATOR + timeStamp + FILENAME_SEPARATOR;
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + DAILYSELFIE_DIR);

        if (!storageDir.exists()) {
            storageDir.mkdir();
        }

        File imageFile = File.createTempFile(
                imageFileName,          /* prefix */
                IMAGE_FILE_EXTENTION,   /* suffix */
                storageDir              /* directory */
        );

        // Save a file path to be used by camera intent
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    // As recommended by the course instructor (Dr. Porter) this method has been adapted from the
    // sample source provided at: http://developer.android.com/training/camera/photobasics.html
    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            // Create the File where the photo should go
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Load File information from storage and use it to populate DailySelfie objects
    private void loadSelfiesFromExternalStorage () {

        File imageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + DAILYSELFIE_DIR);
        String photoPath;

        if (imageDir.length() > 0) {
            for (File f : imageDir.listFiles()) {
                if (f != null) {
                    photoPath = f.getPath();
                    ((DailySelfieAdapter)getListAdapter()).add(createSelfie(photoPath));
                }
            }
        }

        DailySelfieAdapter adapter = (DailySelfieAdapter)getListAdapter();
        adapter.sort(new DailySelfieComparator());
        adapter.notifyDataSetChanged();
        mSelfiesAlreadyLoaded = true;

    }

    /**
     * Creates and returns a DailySelfie object from the supplied photo path.
     */
    private DailySelfie createSelfie(String currentPhotoPath) {

        Date date;

        // Make sure that mTimeStamp has the proper format
        // Get the year month day
        String yyyyMMdd = currentPhotoPath.split("_")[1];

        // Get the hours minutes seconds
        String HHmmss = currentPhotoPath.split("_")[2];

        try {
            date = (new SimpleDateFormat(DATE_FORMAT)).parse(yyyyMMdd + HHmmss);
        }
        catch(ParseException pe) {
            // The date could not be parsed from the filename. We'll use the last modified date
            // instead as a default.
            Log.e(TAG, getString(R.string.date_parse_error));
            date = null;
        }

        // The date could not be parsed from the filename, therefore, use the last modified date as
        // the default date to display to the user.
        if (date == null) {
            File file = new File(currentPhotoPath);
            date = new Date(file.lastModified());
        }

        DailySelfie selfie = new DailySelfie(currentPhotoPath, date);

        return selfie;
    }

    // **** OPTIONAL FUNCTIONALITY ****
    // Delete all selfies that have a checkbox selected in the ListView
    private void deleteSelfies(boolean deleteAll) {

        DailySelfieAdapter adapter = (DailySelfieAdapter) getListAdapter();
        DailySelfie selfie;
        ArrayList selfieList = new ArrayList();
        int adapterCount = adapter.getCount();

        if (!deleteAll) {
            for (int i = 0; i < adapterCount; i++) {
                selfie = (DailySelfie) adapter.getItem(i);
                if (selfie.isChecked()) {
                    selfieList.add(selfie);
                }
            }

            Iterator iter = selfieList.iterator();

            while (iter.hasNext()) {
                selfie = (DailySelfie) iter.next();
                deleteSelfieFile(selfie.getImagePath());
                adapter.remove(selfie);
            }
        }
        else {
            for (int i = 0; i < adapterCount; i++) {
                selfie = (DailySelfie) adapter.getItem(i);
                deleteSelfieFile(selfie.getImagePath());
            }
            adapter.clear();
        }

        adapter.notifyDataSetChanged();
    }

    // **** OPTIONAL FUNCTIONALITY ****
    // Delete an individual file from storage
    private void deleteSelfieFile(String fileName) {
        File file = new File(fileName);
        if (!file.delete()) {
            Log.e(TAG, getString(R.string.delete_selfie_error) + file.getAbsolutePath());
        }
    }

}
