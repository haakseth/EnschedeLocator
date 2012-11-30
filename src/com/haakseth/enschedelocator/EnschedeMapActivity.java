package com.haakseth.enschedelocator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.SimpleLocationOverlay;
//import org.osmdroid.views.overlay.MyLocationOverlay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
//import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**TODO=========================================================
 * Move location listener to EnschedeLocatorActivity and require
 * fix before allowing to start this Activity?
 * ============================================================*/

/**Activity launched after user have chosen destination address. 
 * Displays OSMDroid MapView, a TextView with some info, 
 * retrieves user location, calculates bearing between user and destination
 * and displays a continuously updated CompassView, which is a compass needle
 * pointing towards destination.*/
public class EnschedeMapActivity extends Activity {
	
	//instance variables
	private MapView mapView;
	private double addressLat;
	private double addressLng;
	/**updated with getTime() when loc is updated*/
	private String timeOfLocation = "Using last known location"; 
	private TextView myLocationText;
	private CompassView compassView;
	/**Access sensors, like the magnetometer (compass)*/
	private SensorManager mSensorManager;
	private LocationManager locationManager;
	private Sensor compasSensor;
	private SimpleLocationOverlay locationOverlay;
	private GeoPoint locationGP;
	private List<OverlayItem> destinationList = new ArrayList<OverlayItem>();
	
			
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent mapIntent = getIntent();
		addressLat = mapIntent.getDoubleExtra("lat", 52.0);
		addressLng = mapIntent.getDoubleExtra("long", 6.8);
		GeoPoint desinationGP = new GeoPoint(addressLat, addressLng);
		
		OverlayItem destinationOverlayItem = new OverlayItem("1", "Address here", "This is your destination", desinationGP);
		destinationList.add(destinationOverlayItem);
		
		/**Getting the map tiles stored in /mnt/sdcard/osmdroid/*/
		mapView = new MapView(this,256);
		mapView.setClickable(false);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(false);
		mapView.getController().setZoom(16);
		mapView.getController().setCenter(new GeoPoint(52.221, 6.893));
		mapView.setUseDataConnection(false);
		
		locationOverlay = new MySimpleLocationOverlay(this);

		
		mapView.getOverlays().add(locationOverlay);
		mapView.getOverlays().add(new ItemizedIconOverlay<OverlayItem>(this,
				destinationList, null));
		
		
		        
        mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 	
        compassView = new CompassView(this);
        
        //TextView displaying various info
        myLocationText = new TextView(this);
        myLocationText.setTextColor(Color.BLACK);
        myLocationText.setTextSize(17);
        
        //Layout -> textview, mapview og compassview i rekkefølge
        final RelativeLayout relativeLayout = new RelativeLayout(this);
        
        //LaoutParams for layout
        final RelativeLayout.LayoutParams myLocTextLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        myLocTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        final RelativeLayout.LayoutParams mapViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.FILL_PARENT,RelativeLayout.LayoutParams.FILL_PARENT);
        final RelativeLayout.LayoutParams compassViewLayoutParams = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        
        //add Views to layout
        relativeLayout.addView(mapView, mapViewLayoutParams);
        relativeLayout.addView(myLocationText, myLocTextLayoutParams); //Denne må være etter add mapview for å synes
        relativeLayout.addView(compassView, compassViewLayoutParams);
        
        setContentView(relativeLayout); //show layout
        
        
        

        
    }
    
	//Lytter som kj¿rer updateWithNewLocation nŒr ny posisjon blir oppdaget
	private final LocationListener locationListener = new LocationListener() {
		
		public void onStatusChanged(String provider, int status, Bundle extras) {}
		
		public void onProviderEnabled(String provider) {}
		
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
			
		}
		
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
			locationOverlay.setLocation(new GeoPoint(location));
			timeOfLocation = getDateTime();
			
		}
	};
    private void updateWithNewLocation(Location loc) {
		String latLongString;
		
		if(loc!=null){	
			double lat = loc.getLatitude();
			double lng = loc.getLongitude();

			
			
			//Distance between two sets of coordinates
			double dLng = (lng-addressLng);
			double destDistance = Math.sin(deg2rad(lat)) * Math.sin(deg2rad(addressLat)) + Math.cos(deg2rad(lat)) * Math.cos(deg2rad(addressLat)) * Math.cos(deg2rad(dLng));
			destDistance = Math.acos(destDistance);
			destDistance = (destDistance*180/Math.PI);
			destDistance = destDistance*60*1.1515;
			destDistance = (destDistance*1.609344)*1000;
			
			//bearing between two sets of decimal coordinats
			double y = Math.sin(dLng)*Math.cos(addressLat);
			double x = Math.cos(lat) * Math.sin(addressLat) - Math.sin(lat) * Math.cos(addressLat) * Math.cos(dLng);
			double destBearing = Math.atan2(y, x) * 180 / Math.PI;
			float dBearing = (float)destBearing;
			
			compassView.setBearing(dBearing);
//			mapView.getController().setCenter(new GeoPoint(lat, lng));//make button for this in stead?
//			latLongString=lat+", "+ lng + "\nDistance to destination: " + Math.round(destDistance) + " meter\nBearing to destination: " + (int)destBearing;
			latLongString="Bearing to destination: " + (int)destBearing +"\nDistance: " + Math.round(destDistance) + " meters";
		}
		else{
			latLongString = "Cannot determine location.\nMake sure your GPS receiver is turned on or you have a working network connection.";
		}
		
		myLocationText.setText(latLongString + "\n"+timeOfLocation);
		
	}
    
	private double deg2rad(double deg) {
		  return (deg * Math.PI / 180.0);
		}
    

    @Override
    public void onResume(){
    	super.onResume();
    	
        /**Retrieve location*/
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//        Location loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);        
        updateWithNewLocation(loc);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1200000, 5, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 120000, 5, locationListener);
    	
        /**Retrieve orientation data*/
        compasSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    	mSensorManager.registerListener(mListener, compasSensor, SensorManager.SENSOR_DELAY_GAME);
    	System.out.println("onResume()");
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	//moved code to onPause()
    }
    
    
    
	@Override
	protected void onPause() {
		super.onPause();
    	mSensorManager.unregisterListener(mListener);
    	locationManager.removeUpdates(locationListener);
		System.out.println("onPause()");
	}

	private final SensorEventListener mListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			compassView.setmValues(event.values);
//			System.out.println(event.values[0]);
			if(compassView!=null){
				compassView.invalidate();
				
			}
		}
		


	};
	
	/**Return current date and time as String*/
	public String getDateTime(){
		//method to create a String of the current date, used in the filenaming of the printed textfile
		  final String DATE_FORMAT_NOW = "HH:mm";
		  Calendar cal = Calendar.getInstance();
		  SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		  return "Time of location: "+sdf.format(cal.getTime());
	}
}
