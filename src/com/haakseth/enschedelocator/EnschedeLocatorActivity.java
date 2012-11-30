package com.haakseth.enschedelocator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class EnschedeLocatorActivity extends Activity {
	
	String selectedStreet = null;
	int selectedHouseNumber = -1;
	String[] selectedStreetHouseNumbers = null;
	
	Button goButton;
	AutoCompleteTextView streetNameTextField;
	Spinner numberSpinner;
	
	DBHelper db;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        numberSpinner = (Spinner)findViewById(R.id.numberSpinner);

        db = new DBHelper(EnschedeLocatorActivity.this);
        //Create database of addresses on first run
        try {
			db.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        db.openDataBase();
        //TextView to insert street name. Autocompletes to select one of the existing streets.
        streetNameTextField = (AutoCompleteTextView)findViewById(R.id.streetNameTextField);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
        		android.R.layout.simple_dropdown_item_1line, db.getStringArrayFromCursor(db.getDistinctStreetNames()));
        streetNameTextField.setAdapter(adapter);
        
        db.close();
        streetNameTextField.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				db.openDataBase();
				selectedHouseNumber = -1;
				selectedStreet = adapter.getItem(pos);
				selectedStreetHouseNumbers = db.sortedNumberArray(db.getStringArrayFromCursor(db.getHouseNumbersFromStreetName(selectedStreet)));
				System.out.println(selectedStreet);
				System.out.println(selectedStreetHouseNumbers[0]);
				numberSpinner.setAdapter(new ArrayAdapter<String>(EnschedeLocatorActivity.this, android.R.layout.simple_dropdown_item_1line, 
						selectedStreetHouseNumbers));
				db.close();
			}
		});
        db.openDataBase();
        
        numberSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				selectedHouseNumber = Integer.parseInt(numberSpinner.getAdapter().getItem(arg2).toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        db.close();
        
        //Button to send user to next activity
        goButton = (Button)findViewById(R.id.goButton);
        goButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				db.openDataBase();
				
				if(selectedHouseNumber>0){
					double latitude = db.getLatFromAddress(selectedStreet, selectedHouseNumber);
					double longitude = db.getLongFromAddress(selectedStreet, selectedHouseNumber);
					Intent addressIntent = new Intent(EnschedeLocatorActivity.this, EnschedeMapActivity.class);
					addressIntent.putExtra("lat", latitude);
					addressIntent.putExtra("long", longitude);
					EnschedeLocatorActivity.this.startActivity(addressIntent);					
				}
				db.close();
			}
		});
        
        putMapOnSD();
    }

	@Override
	protected void onPause() {
		super.onPause();
		db.close();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
    
	/**Source: User Rohith on Stackoverflow
	 * http://stackoverflow.com/questions/4447477/android-how-to-copy-files-in-assets-to-sdcard*/
	private void putMapOnSD(){
		new File("/mnt/sdcard/osmdroid/").mkdir();
		AssetManager assetManager = getAssets();
		String[] files = null;
		try{
			files = assetManager.list("");
		}
		catch(IOException e){
			Log.e("tag", e.getMessage());
		}
		File enschedemap = new File("/mnt/sdcard/osmdroid/Enschede.zip");
		if(!enschedemap.exists()){
			for(String filename : files){
				if(filename.contains("Ensch")){
					InputStream in = null;
					OutputStream out = null;
					try{
						in = assetManager.open(filename);
						System.out.println("copying " + filename);
						out = new FileOutputStream("/sdcard/osmdroid/" + filename);
						copyFile(in, out);
						in.close();
						in = null;
						out.flush();
						out.close();
						out = null;
						Toast.makeText(getApplicationContext(), 
								"Map file added to "+ enschedemap, Toast.LENGTH_LONG).show();
					} 
					catch(Exception e) {
						Log.e("tag", e.getMessage());
					}    	
				}
			}
		}
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.menuAbout:
			Toast.makeText(getApplicationContext(), 
					"Created by John Wika Haakseth\nwww.haakseth.com", Toast.LENGTH_LONG).show();
			return true;

			
		}
		return super.onOptionsItemSelected(item);
		
	}

}