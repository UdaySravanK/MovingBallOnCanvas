package com.usk.movingballoncanvas;

import java.util.ArrayList;

import com.crittercism.app.Crittercism;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

	private RelativeLayout baseLayout;
	private MySurfaceView surfaceView;
	private ArrayList<Point> path = new ArrayList<Point>();
	private ArrayList<ArrayList<Point>> paths=new ArrayList<ArrayList<Point>>();
	private Paint paint = new Paint();
	private Button gameSwitch;
	private boolean bool= false;
	private int pathTolerance = 20;
	private int pathEdgeTolerance = 20;
	private ArrayList<ArrayList<ArrayList<Point>>> tempPaths=new ArrayList<ArrayList<ArrayList<Point>>>();
	private ArrayList<ArrayList<Point>> tempPath ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Crittercism.init(getApplicationContext(), "518d21718b2e33782f000013");
		baseLayout = (RelativeLayout) findViewById(R.id.baseLayout);
		surfaceView = new MySurfaceView(this,BitmapFactory.decodeResource(getResources(), R.drawable.ball));
		baseLayout.addView(surfaceView);
		MyRelativeLayout topLayout = new MyRelativeLayout(this);
		baseLayout.addView(topLayout);
		paint.setColor(Color.RED);
		paint.setStrokeWidth(5);
		gameSwitch = (Button) findViewById(R.id.button1);
		gameSwitch.setOnClickListener(this);
		((RelativeLayout)(baseLayout.getParent().getParent())).setOnTouchListener(null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public class MyRelativeLayout extends RelativeLayout implements OnTouchListener
	{

		public MyRelativeLayout(Context context) {
			super(context);
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
			setOnTouchListener(this);
			setBackgroundColor(0x00000000);
		}
		
		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			for(int j=0;j<paths.size();j++)
			{
				for(int i=1;i<paths.get(j).size();i++)
				{	
					canvas.drawLine(paths.get(j).get(i-1).x, paths.get(j).get(i-1).y, paths.get(j).get(i).x, paths.get(j).get(i).y, paint);
				}
			}
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Point currPoint = new Point();
			Point prevPoint = new Point();
			switch (event.getAction()) 
			{
				case MotionEvent.ACTION_DOWN:
					path = new ArrayList<Point>();
					paths.add(path);
					currPoint= new Point((int)event.getX(), (int)event.getY());
					if(currPoint.x<pathEdgeTolerance)
					{
						currPoint= new Point(0, (int)event.getY());
					}
					else if(currPoint.x>baseLayout.getMeasuredWidth()-pathEdgeTolerance)
					{
						currPoint= new Point(baseLayout.getMeasuredWidth(), (int)event.getY());
					}
					if(currPoint.y<pathEdgeTolerance)
					{
						currPoint= new Point(currPoint.x, 0);
					}
					else if(currPoint.y>baseLayout.getMeasuredHeight()-pathEdgeTolerance)
					{
						currPoint= new Point(currPoint.x, baseLayout.getMeasuredHeight());
					}
					path.add(currPoint);
					invalidate();
					break;
				case MotionEvent.ACTION_MOVE:
					currPoint= new Point((int)event.getX(), (int)event.getY());
					prevPoint = path.get(path.size()-1);
					if(Math.abs(currPoint.x-prevPoint.x)<pathTolerance)
					{
						currPoint = new Point(prevPoint.x, currPoint.y);
					}
					
					if(Math.abs(currPoint.y-prevPoint.y)<pathTolerance)
					{
						currPoint = new Point(currPoint.x, prevPoint.y);
					}
					
					int xDiff = Math.abs((int)event.getX()-prevPoint.x);
					int yDiff = Math.abs((int)event.getY()-prevPoint.y);
					
					if(xDiff>pathTolerance && yDiff>pathTolerance)
					{
						if(xDiff>yDiff)
						{
							currPoint = new Point((int)event.getX(),prevPoint.y);
						}
						else
						{
							currPoint = new Point(prevPoint.x, (int)event.getY());
						}
//						Toast.makeText(getApplicationContext(), "Don't go cross", Toast.LENGTH_SHORT).show();
					}
					else if(!(prevPoint.x==currPoint.x && prevPoint.y== currPoint.y))
					{
						path.add(currPoint);
					}
					invalidate();
					break;
				case MotionEvent.ACTION_UP:
					Point lastPoint = path.get(path.size()-1);
					Point newPoint = null;
					if(lastPoint.x < pathEdgeTolerance)
					{
						newPoint= new Point(0, lastPoint.y);
					}
					else if(lastPoint.x > baseLayout.getMeasuredWidth()-pathEdgeTolerance)
					{
						newPoint= new Point(baseLayout.getMeasuredWidth(), lastPoint.y);
					}
					else if(lastPoint.y < pathEdgeTolerance)
					{
						newPoint= new Point(lastPoint.x, 0);
					}
					else if(lastPoint.y > baseLayout.getMeasuredHeight()-pathEdgeTolerance)
					{
						newPoint= new Point(lastPoint.x, baseLayout.getMeasuredHeight());
					}
					if(newPoint != null)
					{
						path.add(newPoint);
					}
					breakPathIntoLines(path);
					invalidate();
					break;
				
				default:
					break;
			}
			return true;
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) 
		{
			case R.id.button1:
				if(bool)
				{
					surfaceView.pause();
					gameSwitch = null;
					gameSwitch.setText("Start");
				}
				else
				{
					surfaceView.start();
					gameSwitch.setText("Pause");
				}
				bool = !bool;
				break;
	
			default:
				break;
		}
	}
	
	public void breakPathIntoLines(ArrayList<Point> path2) 
	{
		ArrayList<Point> temp= new ArrayList<Point>();
		tempPath = new ArrayList<ArrayList<Point>>();
		int z =-1;
		for(int k=1;k<path2.size();k++)
		{
			boolean bool = false;
			Point p1= path2.get(k-1);
			Point p2= path2.get(k);
			if(k==1)
			{
				temp.add(0,p1);
				if(p1.x==p2.x)
				{
					z = 0;
				}
				else if(p1.y== p2.y)
				{
					z = 1;
				}
			}
			else if(k==path2.size()-1)
			{
				temp.add(1,p2);
				tempPath.add(temp);
			}
			if(p1.x==p2.x)
			{
				if(z==1)
				{
					bool = true;
				}
				z =0;//x constant
			}
			else if(p1.y== p2.y)
			{
				if(z==0)
				{
					bool = true;
				}
				z =1;//y constant
			}
			if(bool)
			{
				temp.add(1,p1);
				tempPath.add(temp);
				temp= new ArrayList<Point>();
				temp.add(0,p1);
			}
		}
		tempPaths.add(tempPath);
	}

	public ArrayList<ArrayList<ArrayList<Point>>> getPathsArray()
	{
		return tempPaths;
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		surfaceView.destroyDrawingCache();
	}
}
