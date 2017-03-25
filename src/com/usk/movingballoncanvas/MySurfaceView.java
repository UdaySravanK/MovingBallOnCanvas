package com.usk.movingballoncanvas;

import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback{

	private Bitmap bitmap;
	private int ballWidth, ballHeight, surfaceWidth, surfaceHeight;
	private int speed = 25;// speed = 1 i.e move 1px for iteration
	private boolean run = false;
	private int x=0, y=0;
	private int xMin ,xMax ,yMin ,yMax;
	private int xDirection = 1, yDirection = 1;
	private int diversion=2;
	private MyThread thread;
	private MainActivity _mainActivity;
	
	public MySurfaceView(Context context, Bitmap bitmap) 
	{
		super(context);
		this.bitmap = bitmap ;
		getHolder().addCallback(this);
		_mainActivity = (MainActivity) context;
	}
	
	public MySurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MySurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		ballWidth = bitmap.getWidth();
		ballHeight = bitmap.getHeight();
		surfaceWidth = getMeasuredWidth();
		surfaceHeight = getMeasuredHeight();
		xMin = 0;
		xMax = surfaceWidth-ballWidth;
		yMin = 0;
		yMax = surfaceHeight-ballHeight;
		x = getRandom(xMin, xMax);
		y = getRandom(yMin, yMax);
		render();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		run = false;
		while (retry) {
			try {
				if(thread != null)
				{
					thread.interrupt();
					thread.join();
					retry = false;
				}
				else
				{
					retry = false;
				}
			} catch (InterruptedException e) {
			}
		}
	}
	public void start()
	{
		run = true;
		thread = new MyThread();
		thread.start();
	}
	public void pause()
	{
		run = false;
	}
	
	private class MyThread extends Thread 
	{
		
		@Override
		public void run() 
		{
			while(run)
			{
				render();
			}
		}
	}
	
	private void render() {
		// TODO Auto-generated method stub
		SurfaceHolder holder = getHolder();
		Canvas canvas = null;
        try {
            canvas = holder.lockCanvas();
            synchronized(holder) 
            {
            	drawObject(canvas);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (canvas != null) {
                holder.unlockCanvasAndPost(canvas);
            }
        }
//        try {
//			Thread.sleep(0);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	private void drawObject(Canvas canvas) 
	{
		canvas.drawColor(0xFFaabbff);
		ballMotionLogic(canvas);
    	canvas.drawBitmap(bitmap, x, y, null);
	}

	private void ballMotionLogic(Canvas can) 
	{
		x += (speed*xDirection);
		y += (speed*yDirection);
		if(x<xMin || x>xMax)
		{
			//left edge so change x direction
			y = (yDirection<0)?(y+=diversion):(y-=diversion);
			xDirection*=(-1);
			x = (x<xMin)?(xMin):(xMax);
		}
		else if(y<yMin || y>(yMax-speed))
		{
			//top edge or bottom edge so change y direction
			x = (xDirection<0)?(x+=diversion):(x-=diversion);
			yDirection*=(-1);
			y = (y<yMin)?(yMin):(yMax);
		}
		boolean collision = false;
		for(int i=0;i<_mainActivity.getPathsArray().size();i++)
		{
			for(int j=0;j<_mainActivity.getPathsArray().get(i).size();j++)
			{
				Point p1 = _mainActivity.getPathsArray().get(i).get(j).get(0);
				Point p2 = _mainActivity.getPathsArray().get(i).get(j).get(1);
				Point ballLeftTopCorner = new Point(x,y);
				Point ballTopMiddlePoint = new Point(x+ballWidth/2,y);
				Point ballTopRightCorner = new Point(x+ballWidth,y);
				Point ballRightMiddlePoint = new Point(x+ballWidth,y+ballHeight/2);
				Point ballRightBottomCorner = new Point(x+ballWidth,y+ballHeight);
				Point ballBottomMiddlePoint = new Point(x+ballWidth/2,y+ballHeight);
				Point ballBottomLeftCorner = new Point(x,y+ballHeight);
				Point ballLeftMiddlePoint = new Point(x,y+ballHeight/2);
				int left,top,right,bottom;
				if(p1.x<p2.x)
				{
					left = p1.x-speed;
					right = p2.x+speed;
				}
				else
				{
					left = p2.x-speed;
					right = p1.x+speed;
				}
				
				if(p1.y<p2.y)
				{
					top = p1.y-speed;
					bottom = p2.y+speed;
				}
				else
				{
					top = p2.y-speed;
					bottom = p1.y+speed;
				}
				RectF  rect = new RectF(left, top, right, bottom);
				if(p1.x == p2.x)
				{
					if(rect.contains(ballLeftMiddlePoint.x, ballLeftMiddlePoint.y))
					{
						xDirection*=(-1);
						collision = true;
					}
					else if(rect.contains(ballRightMiddlePoint.x, ballRightMiddlePoint.y))
					{
						xDirection*=(-1);
						collision = true;
					}
				}
				else if(p1.y == p2.y)
				{
					if(rect.contains(ballTopMiddlePoint.x, ballTopMiddlePoint.y))
					{
						yDirection*=(-1);
						collision = true;
					}
					else if(rect.contains(ballBottomMiddlePoint.x, ballBottomMiddlePoint.y))
					{
						yDirection*=(-1);
						collision = true;
					}
				}
				if(collision)
				{
					break;
				}
			}
			if(collision)
				break;
		}
	}
	
	private int getRandom(int min,int max)
	{
		int result = 0;
		Random rand= new Random();
		result = rand.nextInt(max);
		if(result<min)
		{
			result = result+min;
		}
		return result;
	}
	
//	private boolean checkIsPointIntersectingWithLine(Point p3,Point p1,Point p2)
//	{
//		boolean isBwXPoints = (p3.x>=p1.x && p3.x<=p2.x) || (p3.x>=p2.x && p3.x<=p1.x);
//		boolean isBwYPoints = (p3.y>=p1.y && p3.y<=p2.y) || (p3.y>=p2.y && p3.y<=p1.y);
//		boolean intersected = false;
//		if(isBwXPoints && isBwYPoints)
//		{
//			int left,right;
//			left = (p3.y-p1.y)*(p2.x-p1.x);
//			right =(p2.y-p1.y)*(p3.x-p1.x);
//			if(Math.abs(left-right)<=10)
//			{
//				intersected=  true;
//			}
//		}
//		return intersected;
//	}
}
