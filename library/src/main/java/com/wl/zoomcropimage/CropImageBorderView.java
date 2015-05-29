package com.wl.zoomcropimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author zhy http://blog.csdn.net/lmj623565791/article/details/39761281
 */
public class CropImageBorderView extends View {
	protected static final String TAG = CropImageBorderView.class.getSimpleName();
	
	/**
	 * 切割矩形的宽度
	 */
	private int mCropWidth;
	/**
	 * 切割矩形的高度
	 */
	private int mCropHeight;
	/**
	 * 蒙版的颜色
	 */
	private int mShaderColor;
	/**
	 * 边框的颜色
	 */
	private int mBorderColor;
	/**
	 * 边框的宽度 单位px
	 */
	private float mBorderWidth;
	
	/**
	 * 切割形状
	 */
	private int mCropShape;

	/**
	 * 中介Bitmap
	 */
	private Bitmap mIntermediateBitmap;

	public CropImageBorderView(Context context) {
		this(context, null);
	}

	public CropImageBorderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CropImageBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		// 计算距离屏幕水平边界 的边距
		int horizontalPadding = (w - mCropWidth) / 2;
		// 计算距离屏幕垂直边界 的边距
		int verticalPadding = (h - mCropHeight) / 2;
		
		mIntermediateBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas intermediateCanvas = new Canvas(mIntermediateBitmap);
		Paint muskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		muskPaint.setColor(mShaderColor);
		muskPaint.setStyle(Style.FILL);
		intermediateCanvas.drawRect(0, 0, intermediateCanvas.getWidth(),
				intermediateCanvas.getHeight(), muskPaint);

		RectF rect = new RectF(horizontalPadding, verticalPadding, getWidth()
				- horizontalPadding, getHeight() - verticalPadding);
		Paint transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		transparentPaint.setColor(Color.TRANSPARENT);
		transparentPaint.setXfermode(new PorterDuffXfermode(
				PorterDuff.Mode.CLEAR));
		switch(mCropShape){
		case CropShape.SHAPE_RECTANGLE:
			intermediateCanvas.drawRect(rect, transparentPaint);
			break;
		case CropShape.SHAPE_OVAL:
			intermediateCanvas.drawOval(rect, transparentPaint);
			break;
		}

		Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		borderPaint.setColor(mBorderColor);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(mBorderWidth);
		switch(mCropShape){
		case CropShape.SHAPE_RECTANGLE:
			intermediateCanvas.drawRect(rect, borderPaint);
			break;
		case CropShape.SHAPE_OVAL:
			intermediateCanvas.drawOval(rect, borderPaint);
			break;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mIntermediateBitmap, 0, 0, null);
	}

	public void setCropSize(int width, int height) {
		mCropWidth = width;
		mCropHeight = height;
	}
	
	public void setBorderColor(int color){
		mBorderColor = color;
	}
	
	public void setBorderWidth(float width){
		mBorderWidth = width;
	}
	
	public void setShaderColor(int color){
		mShaderColor = color;
	}
	
	public void setCropShape(int cropShape){
		mCropShape = cropShape;
	}
}
