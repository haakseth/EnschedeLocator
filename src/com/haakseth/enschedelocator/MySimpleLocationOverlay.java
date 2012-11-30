package com.haakseth.enschedelocator;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**Overridden class in order to be able to use different icons for
 * location and destination than default OSMDroid ones.*/
public class MySimpleLocationOverlay extends SimpleLocationOverlay {

    private final Point screenCoords = new Point();
    Resources res;
    private Drawable balloonDrawable; 
    private Bitmap balloon;
	
	public MySimpleLocationOverlay(Context ctx) {
		super(ctx);
		res = ctx.getResources();
		balloonDrawable = res.getDrawable(R.drawable.balloon);
		balloon = ((BitmapDrawable)balloonDrawable).getBitmap();
	}

	@Override
	public void draw(Canvas c, MapView osmv, boolean shadow) {
		if (!shadow && this.mLocation != null) {
            final Projection pj = osmv.getProjection();
            pj.toMapPixels(this.mLocation, screenCoords);

            c.drawBitmap(balloon, screenCoords.x - PERSON_HOTSPOT.x, screenCoords.y
                            - PERSON_HOTSPOT.y, this.mPaint);
    }
	}
	
	
	

}
