package com.github.crazyorr.zoomcropimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * http://blog.csdn.net/lmj623565791/article/details/39761281
 *
 * @author zhy
 *
 */
public class ZoomCropImageView extends ImageView implements
		OnScaleGestureListener, OnTouchListener,
		ViewTreeObserver.OnGlobalLayoutListener {

	protected static final String TAG = ZoomCropImageView.class.getSimpleName();
	/**
	 * 用于存放矩阵的9个值
	 */
	private final float[] matrixValues = new float[9];
	private final Matrix mScaleMatrix = new Matrix();
	/**
	 * 缩放最大值
	 */
	public float mScaleMax;
	/**
	 * 缩放最小值
	 */
	private float mScaleMin;
	/**
	 * 切割形状
	 */
	private int mCropShape;
	/**
	 * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
	 */
	private float initScale = 1.0f;
	private boolean once = true;
	/**
	 * 缩放的手势检测
	 */
	private ScaleGestureDetector mScaleGestureDetector = null;
	/**
	 * 用于双击检测
	 */
	private GestureDetector mGestureDetector;
	private boolean isAutoScale;

	/**
	 * 拖动的容忍度
	 */
	private int mTouchSlop;

	private float mLastX;
	private float mLastY;

	private boolean isCanDrag;
	private int lastPointerCount;
	/**
	 * 切割矩形的宽度
	 */
	private int mCropWidth;
	/**
	 * 切割矩形的高度
	 */
	private int mCropHeight;

	public ZoomCropImageView(Context context) {
		this(context, null);
	}

	public ZoomCropImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("ClickableViewAccessibility")
	public ZoomCropImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setScaleType(ScaleType.MATRIX);
		mGestureDetector = new GestureDetector(context,
				new SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (isAutoScale == true)
							return true;

						float x = e.getX();
						float y = e.getY();
						if (getScale() < initScale) {
							ZoomCropImageView.this.postDelayed(
									new AutoScaleRunnable(mScaleMax, x, y), 16);
							isAutoScale = true;
						} else {
							ZoomCropImageView.this.postDelayed(
									new AutoScaleRunnable(mScaleMin, x, y), 16);
							isAutoScale = true;
						}

						return true;
					}
				});
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();

		if (getDrawable() == null)
			return true;

		/**
		 * 缩放的范围控制
		 */
		if ((scale < mScaleMax && scaleFactor > 1.0f)
				|| (scale > mScaleMin && scaleFactor < 1.0f)) {
			/**
			 * 最大值最小值判断
			 */
			if (scaleFactor * scale < mScaleMin) {
				scaleFactor = mScaleMin / scale;
			}
			if (scaleFactor * scale > mScaleMax) {
				scaleFactor = mScaleMax / scale;
			}
			/**
			 * 设置缩放比例
			 */
			mScaleMatrix.postScale(scaleFactor, scaleFactor,
					detector.getFocusX(), detector.getFocusY());
			checkBorder();
			setImageMatrix(mScaleMatrix);
		}
		return true;

	}

	/**
	 * 根据当前图片的Matrix获得图片的范围
	 *
	 * @return
	 */
	private RectF getMatrixRectF() {
		Matrix matrix = mScaleMatrix;
		RectF rect = new RectF();
		Drawable d = getDrawable();
		if (null != d) {
			rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {

		if (mGestureDetector.onTouchEvent(event))
			return true;
		mScaleGestureDetector.onTouchEvent(event);

		float x = 0, y = 0;
		// 拿到触摸点的个数
		final int pointerCount = event.getPointerCount();
		// 得到多个触摸点的x与y均值
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x = x / pointerCount;
		y = y / pointerCount;

		/**
		 * 每当触摸点发生变化时，重置mLasX , mLastY
		 */
		if (pointerCount != lastPointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}

		lastPointerCount = pointerCount;
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = x - mLastX;
			float dy = y - mLastY;

			if (!isCanDrag) {
				isCanDrag = isCanDrag(dx, dy);
			}
			if (isCanDrag) {
				if (getDrawable() != null) {

					RectF rectF = getMatrixRectF();
					// 如果宽度小于屏幕宽度，则禁止左右移动
					if (rectF.width() <= mCropWidth) {
						dx = 0;
					}
					// 如果高度小于屏幕高度，则禁止上下移动
					if (rectF.height() <= mCropHeight) {
						dy = 0;
					}
					mScaleMatrix.postTranslate(dx, dy);
					checkBorder();
					setImageMatrix(mScaleMatrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			lastPointerCount = 0;
			break;
		}

		return true;
	}

	/**
	 * 获得当前的缩放比例
	 *
	 * @return
	 */
	public final float getScale() {
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

	@Override
	public void onGlobalLayout() {
		if (once) {
			Drawable d = getDrawable();
			if (d == null)
				return;

			int width = getWidth();
			int height = getHeight();
			// 拿到图片的宽和高
			int dw = d.getIntrinsicWidth();
			int dh = d.getIntrinsicHeight();
			float scale = 1.0f;
			if (dw < mCropWidth && dh > mCropHeight) {
				scale = mCropWidth * 1.0f / dw;
			}

			if (dh < mCropHeight && dw > mCropWidth) {
				scale = mCropHeight * 1.0f / dh;
			}

			if (dw < mCropWidth && dh < mCropHeight) {
				float scaleW = mCropWidth * 1.0f / dw;
				float scaleH = mCropHeight * 1.0f / dh;
				scale = Math.max(scaleW, scaleH);
			}

			initScale = scale;

			float scaleW = mCropWidth * 1.0f / dw;
			float scaleH = mCropHeight * 1.0f / dh;
			//不能小于切割矩形区域
			mScaleMin = Math.max(Math.max(scaleW, scaleH), mScaleMin);
			mScaleMax = Math.max(mScaleMax, mScaleMin);
			mScaleMatrix.postTranslate((width - dw) / 2, (height - dh) / 2);
			mScaleMatrix.postScale(scale, scale, getWidth() / 2,
					getHeight() / 2);
			// 图片移动至屏幕中心
			setImageMatrix(mScaleMatrix);
			once = false;
		}

	}

	/**
	 * 剪切图片，返回剪切后的bitmap对象
	 *
	 * @return
	 */
	public Bitmap crop(int outputWidth, int outputHeight) {
		int width = getWidth();
		int height = getHeight();
		int horizontalPadding = (width - mCropWidth) / 2;
		int verticalPadding = (height - mCropHeight) / 2;
		// TODO may cause OutOfMemory exception
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
// 		bitmap.setHasAlpha(true);
		Canvas canvas = new Canvas(bitmap);
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0,
				Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG));
		Path clipPath = new Path();
		RectF rect = new RectF(horizontalPadding, verticalPadding, width
				- horizontalPadding, height - verticalPadding);
		switch (mCropShape) {
			case CropShape.SHAPE_RECTANGLE:
				clipPath.addRect(rect, Direction.CW);
				break;
			case CropShape.SHAPE_OVAL:
				clipPath.addOval(rect, Direction.CW);
				break;
		}
		canvas.clipPath(clipPath);

		draw(canvas);

		Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, horizontalPadding,
				verticalPadding, mCropWidth, mCropHeight);
		return Bitmap.createScaledBitmap(croppedBitmap, outputWidth, outputHeight,
				false);
	}

	/**
	 * 边界检测
	 */
	private void checkBorder() {

		RectF rect = getMatrixRectF();
		float deltaX = 0;
		float deltaY = 0;

		int width = getWidth();
		int height = getHeight();

		int horizontalPadding = (width - mCropWidth) / 2;
		int verticalPadding = (height - mCropHeight) / 2;

		// 如果宽或高大于屏幕，则控制范围 ; 这里的0.001是因为精度丢失会产生问题，但是误差一般很小，所以我们直接加了一个0.01
		if (rect.width() + 0.01 >= mCropWidth) {
			if (rect.left > horizontalPadding) {
				deltaX = -rect.left + horizontalPadding;
			}
			if (rect.right < width - horizontalPadding) {
				deltaX = width - horizontalPadding - rect.right;
			}
		}
		if (rect.height() + 0.01 >= mCropHeight) {
			if (rect.top > verticalPadding) {
				deltaY = -rect.top + verticalPadding;
			}
			if (rect.bottom < height - verticalPadding) {
				deltaY = height - verticalPadding - rect.bottom;
			}
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);

	}

	/**
	 * 是否是拖动行为
	 *
	 * @param dx
	 * @param dy
	 * @return
	 */
	private boolean isCanDrag(float dx, float dy) {
		return Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
	}

	public void setCropSize(int cropWidth, int cropHeight) {
		mCropWidth = cropWidth;
		mCropHeight = cropHeight;
	}

	public void setCropShape(int cropShape){
		mCropShape = cropShape;
	}

	public void setScaleSize(float scaleMin, float scaleMax){
		// integrity check
		if(scaleMin > scaleMax){
			throw new IllegalArgumentException(
					"scaleMin must not be greater than scaleMax");
		}
		mScaleMin = Math.max(scaleMin, mScaleMin);
		mScaleMax = Math.max(scaleMax, mScaleMin);
	}

	/**
	 * 自动缩放的任务
	 *
	 * @author zhy
	 */
	private class AutoScaleRunnable implements Runnable {
		static final float BIGGER = 1.07f;
		static final float SMALLER = 0.93f;
		private float mTargetScale;
		private float tmpScale;

		/**
		 * 缩放的中心
		 */
		private float x;
		private float y;

		/**
		 * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
		 *
		 * @param targetScale
		 */
		public AutoScaleRunnable(float targetScale, float x, float y) {
			this.mTargetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < mTargetScale) {
				tmpScale = BIGGER;
			} else {
				tmpScale = SMALLER;
			}

		}

		@Override
		public void run() {
			// 进行缩放
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorder();
			setImageMatrix(mScaleMatrix);

			final float currentScale = getScale();
			// 如果值在合法范围内，继续缩放
			if (((tmpScale > 1f) && (currentScale < mTargetScale))
					|| ((tmpScale < 1f) && (mTargetScale < currentScale))) {
				ZoomCropImageView.this.postDelayed(this, 16);
			} else
			// 设置为目标的缩放比例
			{
				final float deltaScale = mTargetScale / currentScale;
				mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
				checkBorder();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;
			}

		}
	}
}
