package com.github.crazyorr.zoomcropimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;

/**
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 *
 * @author zhy
 * @author wl
 */
public class ZoomCropImageActivity extends Activity implements OnClickListener {
    /**
     * intent extra name : uri
     */
    public static final String INTENT_EXTRA_URI = "INTENT_EXTRA_URI";

    /**
     * intent extra name : outputWidth
     */
    public static final String INTENT_EXTRA_OUTPUT_WIDTH = "INTENT_EXTRA_OUTPUT_WIDTH";
    /**
     * intent extra name : outputHeight
     */
    public static final String INTENT_EXTRA_OUTPUT_HEIGHT = "INTENT_EXTRA_OUTPUT_HEIGHT";
    /**
     * intent extra name : cropShape
     */
    public static final String INTENT_EXTRA_CROP_SHAPE = "INTENT_EXTRA_CROP_SHAPE";
    /**
     * intent extra name : mDir
     */
    public static final String INTENT_EXTRA_SAVE_DIR = "INTENT_EXTRA_SAVE_DIR";
    /**
     * intent extra name : mFileName
     */
    public static final String INTENT_EXTRA_FILE_NAME = "INTENT_EXTRA_FILE_NAME";

    /**
     * default cropped image name
     */
    private static final String DEFAULT_CROPPED_IMAGE_NAME = "cropped_picture.png";

    /**
     * default cropped image width
     */
    private static final int DEFAULT_OUTPUT_WIDTH = 75;
    /**
     * default cropped image height
     */
    private static final int DEFAULT_OUTPUT_HEIGHT = 100;

    //crop status
    public static final int CROP_SUCCEEDED = 0;
    public static final int CROP_CANCELLED = 1;
    public static final int CROP_FAILED = 2;

    private CropImageLayout mCropImageLayout;

    private String mDir;
    private String mFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);

        mCropImageLayout = (CropImageLayout) findViewById(R.id.id_crop_image_layout);

        // Image Uri
        Uri uri = getIntent().getParcelableExtra(INTENT_EXTRA_URI);
        if(uri == null){
            throw new NullPointerException("uri == null");
        }else{
            mCropImageLayout.setImageURI(uri);
        }

        // width
        int outputWidth = getIntent().getIntExtra(INTENT_EXTRA_OUTPUT_WIDTH,
                DEFAULT_OUTPUT_WIDTH);
        // height
        int outputHeight = getIntent().getIntExtra(INTENT_EXTRA_OUTPUT_HEIGHT,
                DEFAULT_OUTPUT_HEIGHT);
        mCropImageLayout.setOutputSize(outputWidth, outputHeight);

        // shape
        int notSpecified = -1;
        int cropShape = getIntent().getIntExtra(INTENT_EXTRA_CROP_SHAPE, notSpecified);
        if (cropShape != notSpecified) {
            mCropImageLayout.setCropShape(cropShape);
        }

        // directory
        mDir = getIntent().getStringExtra(INTENT_EXTRA_SAVE_DIR);
        if(mDir == null){
            mDir = getDefaultSaveDir(this);
        }

        // file name
        mFileName = getIntent().getStringExtra(INTENT_EXTRA_FILE_NAME);
        if(mFileName == null){
            mFileName = DEFAULT_CROPPED_IMAGE_NAME;
        }

        // TODO customize style of the buttons
        Button btnCancel = (Button) findViewById(R.id.id_btn_cancel);
        btnCancel.setOnClickListener(this);
        Button btnConfirm = (Button) findViewById(R.id.id_btn_confirm);
        btnConfirm.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.id_btn_cancel) {
            setResult(CROP_CANCELLED);
            finish();

        } else if (i == R.id.id_btn_confirm) {
            Bitmap bitmap = mCropImageLayout.crop();
            try {
                File file = createFile(mDir, mFileName);
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();

                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent();
                intent.putExtra(INTENT_EXTRA_URI, uri);
                setResult(CROP_SUCCEEDED, intent);
            } catch (Exception e) {
                e.printStackTrace();
                setResult(CROP_FAILED);
            }
            finish();

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(CROP_CANCELLED);
        }
        return super.onKeyDown(keyCode, event);
    }

    public static File createFile(String dir, String name) {
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dirFile, name);
        return file;
    }

    public static boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * get default directory to save cropped image
     * @param context
     * @return
     */
    public static String getDefaultSaveDir(Context context){
        String path;
        if(isSdCardMounted()){
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        }else{
            path = context.getFilesDir().getAbsolutePath();
        }
        return path + File.separator + context.getPackageName() + File.separator;
    }
}
