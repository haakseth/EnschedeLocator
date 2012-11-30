package com.haakseth.enschedelocator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

/**View that draws and rotates the compass pointing
 * to user's destination.*/
public class CompassView extends View {

	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	private float[] mValues;
	private float[] compassValues = new float[]{0,0,0,0,0};
	private float compassAverage = 0;
	private float bearing;
	
	/**For obtaining screen rotation, important for rotating compass*/
	Display display = ((WindowManager)this.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	
	
	/**Default constructor for CompassView*/
	public CompassView(Context context) {
		super(context);
		// construct a wedge-shaped path
		mPath.moveTo(0, -50);
		mPath.lineTo(-20, 60);
		mPath.lineTo(0, 50);
		mPath.lineTo(20, 60);
		mPath.close();
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		Paint paint = mPaint;
		
				
		paint.setAntiAlias(true);
		paint.setColor(Color.GRAY);
		paint.setStyle(Style.FILL);
		
		/**Place the view on the screen's upper right corner*/
		int w = canvas.getWidth();
		int h = canvas.getHeight();
		int cx = 3*w/4;
		int cy = h/3;
		canvas.translate(cx, cy-100);

	    int rotation = display.getRotation();
		if(compassAverage!=0){
			if(rotation == Surface.ROTATION_90){
				//Screen is rotated to the left (counter-clockwise)
				canvas.rotate(-compassAverage+bearing-90);
			}
			else if(rotation == Surface.ROTATION_270){
				//Screen is rotated to the right (clockwise)
				canvas.rotate(-compassAverage+bearing+90);
			}
			else if(rotation==Surface.ROTATION_180){
				//Screen is upside down (possible on some devices)
				canvas.rotate(-compassAverage+bearing+180);
			}
			else{	
				//Screen is not rotated
				canvas.rotate(-compassAverage+bearing);
			}
		}
		canvas.drawPath(mPath, mPaint);
	}
	
	/**Public getter for mValues*/
	public float[] getMValues(){
		return mValues;
	}
	
	/**Setting compass value, averaging over five measurements for smoother rotation
	 * @param mValues Orientation values received form the SensorEvent*/
	public void setmValues(float[] mValues) {
		this.mValues = mValues;
		compassValues[0]= compassValues[1];
		compassValues[1]= compassValues[2];
		compassValues[2]= compassValues[3];
		compassValues[3]= compassValues[4];
		compassValues[4]=mValues[0];
		compassAverage = (compassValues[0] +compassValues[1] +compassValues[2] +compassValues[3] +compassValues[4])/5;
	
	}
	
	/**Public getter for bearing value*/
	public float getBearing() {
		return bearing;
	}

	/**Public setter for bearing value, used from the updateWithNewLocation()
	 * method in EnschedeMapActivity*/
	public void setBearing(float bearing) {
		this.bearing = bearing;
	}
}