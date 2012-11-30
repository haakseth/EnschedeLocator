package com.haakseth.enschedelocator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

/**SQLiteOpenHelper class for querying the address database*/
public class DBHelper extends SQLiteOpenHelper {

	/**Static fields*/
	private static String DB_PATH = "/data/data/com.haakseth.enschedelocator/";
	private static String DB_NAME = "addresses.db";
	public static String DB_TABLE = "addresses";
	public static String ID = "_id";
	public static String STRAATNAAM = "straatnaam";
	public static String HUISNUMMER = "huisnummer";
	public static String LATITUDE = "latitude";
	public static String LONGITUDE = "longitude";
	
	private SQLiteDatabase database;
	private boolean dbIsOpen = false;
	private final Context myContext;
	
    /*** Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context*/
	public DBHelper(Context context) {
		super(context, DB_NAME, null, 1);
		this.myContext = context;
	}
	
	/**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exists
    		System.out.println("Database already exists.");
    	}else{
    		System.out.println("Creating database.");
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
 
    			copyDataBase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }
    
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database doesn't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	database = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	dbIsOpen = true;
    	System.out.println("Setting dbIsOpen to " + dbIsOpen);
    }
 
    @Override
	public synchronized void close() {
 
    	    if(database != null){
    	    	database.close();
    	    	dbIsOpen = false;
    	    	System.out.println("Setting dbIsOpen to " + dbIsOpen);
    	    }
 
    	    super.close();
 
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}
	
	public String getStreetFromID(int id){
		Cursor c = database.query(DB_TABLE, new String[]{STRAATNAAM}, ID + "="+ id, 
				null, null, null, null);
		c.moveToFirst();
		return c.getString(0);
		
	}
	
	public double getLatFromID(int id){
		Cursor c = database.query(DB_TABLE, new String[]{LATITUDE}, ID + "="+ id, 
				null, null, null, null);
		c.moveToFirst();
		return c.getDouble(0);
	}
	
	public double getLongFromID(int id){
		Cursor c = database.query(DB_TABLE, new String[]{LONGITUDE}, ID + "="+ id, 
				null, null, null, null);
		c.moveToFirst();
		return c.getDouble(0);
	}
	
	public double getLatFromAddress(String straat, int huisNummer){
		double lat = -1;
		//db contains two streets with ' in their names. Causes trouble, this
		//is a quick-fix, hardcoded.
		Cursor c;
		if(straat.contains("'t Hoff")){	
			c = database.query(DB_TABLE, new String[]{LATITUDE}, STRAATNAAM + " like '%t Hoff%'" + " and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);
		}
		if(straat.contains("'t Hofj")){	
			c = database.query(DB_TABLE, new String[]{LATITUDE}, STRAATNAAM + " like '%t Hofj%'" + " and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);
		}
		else{
			c = database.query(DB_TABLE, new String[]{LATITUDE}, STRAATNAAM + "='"+ straat + "' and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);			
		}
		if(c.moveToFirst()){
			c.moveToFirst();
			lat = c.getDouble(0); 
			return lat;			
		}
		else return -1;
		//Also used in EnschedeLocatorActivity to check if street have been found
		//so that user can be allowed to start next activity
	}
	
	public double getLongFromAddress(String straat, int huisNummer){
		//db contains two streets with ' in their names. Causes trouble, this
		//is a quick-fix, hardcoded.
		Cursor c;
		if(straat.contains("'t Hoff")){	
			c = database.query(DB_TABLE, new String[]{LONGITUDE}, STRAATNAAM + " like '%t Hoff%'" + " and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);
		}
		if(straat.contains("'t Hofj")){	
			c = database.query(DB_TABLE, new String[]{LONGITUDE}, STRAATNAAM + " like '%t Hofj%'" + " and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);
		}
		else{
			c = database.query(DB_TABLE, new String[]{LONGITUDE}, STRAATNAAM + "='"+ straat + "' and "+ HUISNUMMER + "=" +huisNummer, 
					null, null, null, null);
		}
		c.moveToFirst();
		return c.getDouble(0);
	}
	
	/**Get cursor that lists every address in the database once, for 
	 * the streetNamesTextField in EnschedeLocatorActivity*/
	public Cursor getDistinctStreetNames(){
		return database.query(DB_TABLE, new String[]{"distinct " + STRAATNAAM + " as _id"}, 
				null, null, null, null, null);
	}
	
	/**Get Cursor of given street's housenumbers*/
	public Cursor getHouseNumbersFromStreetName(String streetName){
		//db contains two streets with ' in their names. Causes trouble, this
		//is a quick-fix, hardcoded.
		if(streetName.contains("'t Hoff")){	
			return database.query(DB_TABLE, new String[]{"distinct "+HUISNUMMER}, 
					STRAATNAAM +" like "+ "'%t Hoff%'", null, null, null, HUISNUMMER);
		}
		if(streetName.contains("'t Hofj")){	
			return database.query(DB_TABLE, new String[]{"distinct "+HUISNUMMER}, 
					STRAATNAAM +" like "+ "'%t Hofj%'", null, null, null, HUISNUMMER);
		}
		else{
			return database.query(DB_TABLE, new String[]{"distinct "+HUISNUMMER}, 
					STRAATNAAM +" = "+ "'"+streetName+"'", null, null, null, HUISNUMMER);			
		}
//		return database.rawQuery("cast(select " + HUISNUMMER + " from addresses where "+
//				STRAATNAAM + " = '" + streetName + "' as integer)", null);
	}
	
	/**Makes a String[] of the first column of given Cursor, used for 
	 * streetNamesTextField and numberSpinner in EnschedeLocatorActivity*/
	public String[] getStringArrayFromCursor(Cursor c){
		
		if(c.getCount()>0){
			String[]names = new String[c.getCount()];
			int i = 0;
			while(c.moveToNext()){
				names[i] = c.getString(0);
				i++;
			}
			return names;
		}
		else return new String[]{};
	}
	
	/**Cast String[] to sorted int[]*/
	public String[]sortedNumberArray(String[] str){
		int[] intArray = new int[str.length];
		for(int i=0;i<intArray.length;i++){
			intArray[i]=Integer.parseInt(str[i]);
		}
		Arrays.sort(intArray);
		for(int i=0; i<intArray.length;i++){
			str[i] = Integer.toString(intArray[i]);
		}
		
		return str;
	}
	
	public Cursor getStreetNamesOnString(String str){
		return database.query(DB_TABLE, new String[]{"distinct " + STRAATNAAM + " as _id"}, 
				"_id" + " like '" + str + "%'", null, null, null, null);
	}

	public boolean isDbIsOpen() {
		return dbIsOpen;
	}

	public void setDbIsOpen(boolean dbIsOpen) {
		this.dbIsOpen = dbIsOpen;
	}

}
