package com.github.crazyorr.zoomcropimage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 * 
 * @author zhy
 *
 */
public class CropImageLayout extends RelativeLayout {

	protected static final String TAG = CropImageLayout.class.getSimpleName();
	
	/**
	 * 默认缩放最小值
	 */
	private static final float DEFAULT_SCALE_MIN = 0.5f;
	/**
	 * 默认缩放最大值
	 */
	private static final float DEFAULT_SCALE_MAX = 4.0f;
	
	private ZoomCropImageView mZoomCropImageView;
	private CropImageBorderView mCropImageBorderView;

	/**
	 * 输出图片宽度
	 */
	private int mOutputWidth;
	/**
	 * 输出图片高度
	 */
	private int mOutputHeight;
	
	private int mWidth;
	private int mHeight;

	public CropImageLayout(Context context){
		this(context, null);
	}
	
	public CropImageLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CropImageLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

		TypedArray tArray = context.obtainStyledAttributes(attrs,
                R.styleable.CropImage);
		float scaleMin = tArray.getFloat(R.styleable.CropImage_zci_scaleMin, DEFAULT_SCALE_MIN);
		float scaleMax = tArray.getFloat(R.styleable.CropImage_zci_scaleMax, DEFAULT_SCALE_MAX);
		float borderWidth = tArray.getDimension(R.styleable.CropImage_zci_borderWidth,
				getResources().getDimension(R.dimen.default_border_width));
		int borderColor = tArray.getColor(R.styleable.CropImage_zci_borderColor, Color.WHITE);
		int shaderColor = tArray.getColor(R.styleable.CropImage_zci_shaderColor,
				getResources().getColor(R.color.default_shader_color));
		int cropShape = tArray.getInt(R.styleable.CropImage_zci_cropShape,
				CropShape.SHAPE_RECTANGLE);
		tArray.recycle();
		
		mZoomCropImageView = new ZoomCropImageView(context);
		mZoomCropImageView.setScaleSize(scaleMin, scaleMax);
		mZoomCropImageView.setCropShape(cropShape);
		mCropImageBorderView = new CropImageBorderView(context);
		mCropImageBorderView.setBorderWidth(borderWidth);
		mCropImageBorderView.setBorderColor(borderColor);
		mCropImageBorderView.setShaderColor(shaderColor);
		mCropImageBorderView.setCropShape(cropShape);

		android.view.ViewGroup.LayoutParams lp = new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		this.addView(mZoomCropImageView, lp);
		this.addView(mCropImageBorderView, lp);
    }

	/**
	 * 读取图片属性：旋转的角度
	 *
	 * @param path 图片绝对路径
	 * @return degree旋转的角度
	 */
	public static int readImageRotationDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					degree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					degree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					degree = 270;
					break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * 旋转图片
	 *
	 * @param degrees
	 * @param bitmap
	 * @return Bitmap
	 */
	public static Bitmap rotaingBitmap(int degrees, Bitmap bitmap) {
		// 旋转图片 动作
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		return resizedBitmap;
	}

	/**
	 * 压缩图片尺寸
	 *
	 * @return
	 */
	public static Bitmap sampleBitmap(Context context, Uri uri,
									  int width, int height) {

		Bitmap bitmap = null;
		//先量尺寸，如果太大，要作sample，否则会报OutOfMemory错误
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		try {
			BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return bitmap;
		}
		Log.i(TAG, "Original Width = " + options.outWidth);
		Log.i(TAG, "Original Height = " + options.outHeight);

		int sampleWidth = 0;
		int sampleHeight = 0;
		if (options.outWidth < options.outHeight) {
			if (width < height) {
				sampleWidth = options.outWidth / width;
				sampleHeight = options.outHeight / height;
			} else {
				sampleWidth = options.outWidth / height;
				sampleHeight = options.outHeight / width;
			}
		} else {
			if (width < height) {
				sampleWidth = options.outHeight / width;
				sampleHeight = options.outWidth / height;
			} else {
				sampleWidth = options.outHeight / height;
				sampleHeight = options.outWidth / width;
			}
		}
		int sampleSize = Math.max(sampleWidth, sampleHeight);
		options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inSampleSize = sampleSize;
		try {
			bitmap = BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(uri), null, options);
			Log.i(TAG, "Scaled Width = " + bitmap.getWidth());
			Log.i(TAG, "Scaled Height = " + bitmap.getHeight());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mWidth = w;
		mHeight = h;
		initCropSize();
	}

	/**
	 * 裁切图片
	 *
	 * @return
	 */
	public Bitmap crop() {
		return mZoomCropImageView.crop(mOutputWidth, mOutputHeight);
	}

	/**
	 * 必须提供Uri，否则无法获取到旋转信息
	 *
	 * @param uri
	 */
	public void setImageURI(Uri uri) {
		String path = FileUtils.getPath(getContext(), uri);

		WindowManager windowManager = (WindowManager) getContext()
				.getSystemService(Context.WINDOW_SERVICE);
		Display windowDisplay = windowManager.getDefaultDisplay();
		Point size = new Point();
		windowDisplay.getSize(size);
		Log.i(TAG, "Screen Width = " + size.x);
		Log.i(TAG, "Screen Height = " + size.y);

		int degrees = readImageRotationDegree(path);
		Bitmap rotatedBitmap = rotaingBitmap(degrees,
				sampleBitmap(getContext(), uri, size.x, size.y));
		mZoomCropImageView.setImageBitmap(rotatedBitmap);
	}

	/**
	 * 设置输出图片尺寸
	 *
	 * @param outputWidth
	 * @param outputHeight
	 */
	public void setOutputSize(int outputWidth, int outputHeight) {
		mOutputWidth = outputWidth;
		mOutputHeight = outputHeight;
		initCropSize();
	}

	/**
	 * 设置切割形状
	 * @param cropShape
	 */
	public void setCropShape(int cropShape){
		mCropImageBorderView.setCropShape(cropShape);
		mZoomCropImageView.setCropShape(cropShape);
	}

	private void initCropSize(){
		if(mWidth != 0 && mHeight != 0
				&& mOutputWidth != 0 && mOutputHeight != 0){
			final double CROP_SIZE_RATIO = 0.9;
			double scaleWidth = mWidth * CROP_SIZE_RATIO / mOutputWidth;
			double scaleHeight = mHeight * CROP_SIZE_RATIO / mOutputHeight;
			//缩放倍数取较小值
			double scale = Math.min(scaleWidth, scaleHeight);

			int mCropWidth = (int)(mOutputWidth * scale);
			int mCropHeight = (int)(mOutputHeight * scale);

			mZoomCropImageView.setCropSize(mCropWidth, mCropHeight);
			mCropImageBorderView.setCropSize(mCropWidth, mCropHeight);
		}
	}
}
