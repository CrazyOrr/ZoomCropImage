package com.github.crazyorr.zoomcropimage.sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.github.crazyorr.zoomcropimage.CropShape;
import com.github.crazyorr.zoomcropimage.ZoomCropImageActivity;

import java.io.File;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SELECT_PICTURE = 0;
    private static final int REQUEST_CODE_CROP_PICTURE = 1;

    private static final int PICTURE_WIDTH = 600;
    private static final int PICTURE_HEIGHT = 800;

    private Uri mPictureUri;

    public static File createPictureFile(String fileName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return null;
        }

        File pictureStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), BuildConfig.APPLICATION_ID);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!pictureStorageDir.exists()) {
            if (!pictureStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File pictureFile = new File(pictureStorageDir.getPath() + File.separator + fileName);
        return pictureFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.id_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
                pickIntent.setType("image/*");

                Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                mPictureUri = Uri.fromFile(createPictureFile("picture.png"));

                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPictureUri);

                Intent chooserIntent = Intent.createChooser(pickIntent,
                        getString(R.string.take_or_select_a_picture));
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                        new Intent[]{takePhotoIntent});

                startActivityForResult(chooserIntent, REQUEST_CODE_SELECT_PICTURE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_PICTURE:
                switch (resultCode) {
                    case Activity.RESULT_OK:

                        Uri selectedImageUri = null;
                        if (data != null) {
                            selectedImageUri = data.getData();
                        }
                        if (selectedImageUri == null) {
                            selectedImageUri = mPictureUri;
                        }

                        Intent intent = new Intent(this, ZoomCropImageActivity.class);
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_URI, selectedImageUri);
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_OUTPUT_WIDTH, PICTURE_WIDTH);
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_OUTPUT_HEIGHT, PICTURE_HEIGHT);
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_CROP_SHAPE, CropShape.SHAPE_OVAL);   //optional
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_SAVE_DIR,
                                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName());   //optional
                        intent.putExtra(ZoomCropImageActivity.INTENT_EXTRA_FILE_NAME, "cropped.png");   //optional
                        startActivityForResult(intent, REQUEST_CODE_CROP_PICTURE);
                        break;
                }
                break;
            case REQUEST_CODE_CROP_PICTURE:
                switch (resultCode) {
                    case ZoomCropImageActivity.CROP_SUCCEEDED:
                        if (data != null) {
                            Uri croppedPictureUri = data
                                    .getParcelableExtra(ZoomCropImageActivity.INTENT_EXTRA_URI);
                            ImageView iv = (ImageView) findViewById(R.id.id_iv);
                            // workaround for ImageView to refresh cache
                            iv.setImageURI(null);
                            iv.setImageURI(croppedPictureUri);
                        }
                        break;
                    case ZoomCropImageActivity.CROP_CANCELLED:
                    case ZoomCropImageActivity.CROP_FAILED:
                        break;
                }
                break;
        }

    }
}
