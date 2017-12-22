package oboard.ch;

/*
 * Android多点触控技术练习
 * @Author：Robin
 * @Date：2013年12月29日
 * @边界处理暂时不知道怎么写啊
 * 目前的问题有：
 * 手势识别不是很顺畅，经常出现该放缩时放缩不了的情况
 * 由于没有边界判断，程序可能会出现崩溃
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;  

public class MultiTouchView extends ImageView {
	//本地图像资源
	private int mDrawable;  
	//图像位图
	private Bitmap mBitmap;
	//原始图像矩阵
	private Matrix mMatrix = new Matrix();
	//过程图像矩阵
	private Matrix mSavedMatrix = new Matrix();
	//结果图像矩阵
	private Matrix mResultMatrix = new Matrix();
	//定义三种模式：None、Drag、Zoom  
	public static final int Mode_None = 0;  
	public static final int Mode_Drag = 1;  
	public static final int Mode_Zoom = 2;  
	//当前操作模式  
	public int mMode = Mode_None;  
	//当前坐标  
	private float mDownX, mDownY;  
	//存储两点间的距离  
	private float mDistance = 0f;  
	//存储旋转角  
	@SuppressWarnings("unused")  
	private float mAngle = 0f;  
	//存储中点  
	private PointF mPoint;  
	//最大缩放比例  
	//private float MaxScale=3f;  
	//最小缩放比例  
	//private float MinScale=0.5f;  

	public MultiTouchView(Activity mActivity, int Drawable) {  
		super(mActivity);  
		//设置当前图片资源  
		this.mDrawable = Drawable;
		//获取Bitmap  
		mBitmap = BitmapFactory.decodeResource(getResources(), mDrawable);
		mMatrix = new Matrix();  
	}  

	@SuppressLint("DrawAllocation")  
	@Override  
	protected void onDraw(Canvas canvas) {  
		//消除图像锯齿  
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));  
		canvas.save();  
		//绘制图像  
		canvas.drawBitmap(mBitmap, mMatrix, null);  
		canvas.restore();  
	}

	@Override
	public void setImageResource(int resId) {
		super.setImageResource(resId);
		mBitmap = BitmapFactory.decodeResource(getResources(), resId);
	}

	@Override  
	public boolean onTouchEvent(MotionEvent event) { 
	try {
		switch (event.getActionMasked()) {  
				//单点触控处理
			case MotionEvent.ACTION_DOWN:  
				//设置当前操作模式为Drag  
				mMode = Mode_Drag;  
				//获取当前坐标  
				mDownX = event.getX();  
				mDownY = event.getY();  
				mSavedMatrix.set(mMatrix);  
				break;
				//多点触控处理  
			case MotionEvent.ACTION_POINTER_DOWN:  
				mMode = Mode_Zoom;  
				//获取两点间距离  
				mDistance = getDistance(event);  
				//获取旋转角  
				mAngle = getAngle(event);  
				//获取中点  
				mPoint = getMidPoint(event);  
				mSavedMatrix.set(mMatrix);  
				break;
			case MotionEvent.ACTION_MOVE:
				//缩放处理
				if (mMode == Mode_Zoom && event.getPointerCount() >= 1) {
					mResultMatrix.set(mSavedMatrix);  
					//获取缩放比率  
					float mScale = getDistance(event) / mDistance;  
					//获取旋转角
					float Angle = getAngle(event) - mAngle;
					//以中点为中心，进行缩放
					mResultMatrix.postScale(mScale, mScale, mPoint.x, mPoint.y);  
					//以中点为中心，进行旋转，
					mResultMatrix.postRotate(Angle * -180, mPoint.x, mPoint.y);  
					mMatrix.set(mResultMatrix);
					invalidate();
				} else {
					mResultMatrix.set(mSavedMatrix);  
					//计算平移量  
					float DeltalX = event.getX() - mDownX;  
					float DeltalY = event.getY() - mDownY;  
					//平移  
					mResultMatrix.postTranslate(DeltalX, DeltalY);  
					mMatrix.set(mResultMatrix);  
					invalidate();  
				}  
				break;  

			case MotionEvent.ACTION_UP:  
				//这里要不要处理呢,如果需要,怎么办  
			case MotionEvent.ACTION_POINTER_UP:  
				//mMode = Mode_None;  
				break;  
		}
		} catch (Exception e) {
			Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return true;
	}  

	//返回两点间的距离  
	public float getDistance(MotionEvent event) {  
		//计算X的变化量
		double DeltalX = event.getX(0) - event.getX(1);  
		//计算Y的变化量
		double DeltalY = event.getY(0) - event.getY(1);  
		//计算距离
		return (float)Math.sqrt(DeltalX * DeltalX + DeltalY * DeltalY);
	}  

	//返回两点的中点
	public PointF getMidPoint(MotionEvent event) {  
		float X = event.getX(0) + event.getX(1);  
		float Y = event.getY(0) + event.getY(1);  
		return new PointF(X / 2, Y / 2);  
	}  

	//获得旋转角  
	public float getAngle(MotionEvent event) {  
		double DeltalX = event.getX(0) - event.getX(1);  
		double DeltalY = event.getY(0) - event.getY(1);  
		return (float)Math.atan2(DeltalX, DeltalY);  
	}  

	//边界处理,暂时没找到比较好的方法  
	public boolean CheckBounary() {  
		return false;  
	}  


}
