package com.github.crazyorr.zoomcropimage.sample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.github.crazyorr.zoomcropimage.CropShape;
import com.github.crazyorr.zoomcropimage.ZoomCropImageActivity;

import java.io.File;


public class MainActivity extends Activity {

    private static final int REQUEST_CODE_SELECT_PICTURE = 0;
    private static final int REQUEST_CODE_CROP_PICTURE = 1;

    private static final int PICTURE_WIDTH = 600;
    private static final int PICTURE_HEIGHT = 800;

    private Uri mPictureUri;

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
                mPictureUri = Uri.fromFile(createFile(
                        Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getPackageName(),
                        "picture.png"));

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

                        final boolean isCamera;
                        if (data == null) {
                            isCamera = true;
                        } else {
                            final String action = data.getAction();
                            if (action == null) {
                                isCamera = false;
                            } else {
                                isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                            }
                        }

                        Uri selectedImageUri;
                        if (isCamera) {
                            selectedImageUri = mPictureUri;
                        } else {
                            selectedImageUri = data == null ? null : data.getData();
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
                switch(resultCode){
                    case ZoomCropImageActivity.CROP_SUCCEEDED:
                        if (data != null) {
                            Uri croppedPictureUri = data
                                    .getParcelableExtra(ZoomCropImageActivity.INTENT_EXTRA_URI);
                            ImageView iv = (ImageView)findViewById(R.id.id_iv);
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

    private File createFile(String dir, String name){
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dirFile, name);
        return file;
    }

}
