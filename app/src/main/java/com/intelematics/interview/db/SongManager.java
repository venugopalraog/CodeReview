package com.intelematics.interview.db;


import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import com.intelematics.interview.R;
import com.intelematics.interview.models.Song;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


/**
 *
 */
public class SongManager{
	private DBManager dbManager;
	private Context context;
	
	public SongManager(Context context, DBManager dbManager) {
		this.context = context;
		this.dbManager = dbManager;
	}

	
	public synchronized ArrayList<Song> getSongsList(){
		ArrayList<Song> list = new ArrayList<Song>();
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(context.getString(R.string.sql_table_song));
		
		SQLiteDatabase db = dbManager.getReadableDatabase();
		if(db == null){
			return list;
		}
		if(!db.isOpen()){
			dbManager.openDB(db);
			db = dbManager.getReadableDatabase();
		}

		String[] values = {};
		Cursor cursor = db.rawQuery(context.getString(R.string.sql_select_all_songs), values); 
		
	    if (cursor == null) {
	    	if (db != null && db.isOpen()){
	    		db.close();
	    	}
	        return list;
	    } else if (!cursor.moveToFirst()) {
	        cursor.close();
	    	if (db != null && db.isOpen()){
	    		db.close();
	    	}
	        return list;
	    }

	    int idSongIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_idsong));
	    int titleIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_title));
	    int artistIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_artist));
	    int priceIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_price));
	    int coverURLIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_coverURL));
	    int coverIndex = cursor.getColumnIndex(context.getString(R.string.sql_column_song_cover));
	    
	    do{
		    Song song = new Song();
		    song.setId(cursor.getLong(idSongIndex));
		    song.setTitle(cursor.getString(titleIndex));
		    song.setArtist(cursor.getString(artistIndex));
		    song.setPrice(cursor.getDouble(priceIndex));
		    song.setCoverURL(cursor.getString(coverURLIndex));
		    byte[] imageByteArray = cursor.getBlob(coverIndex);

		    Bitmap image = null;
		    if(imageByteArray != null){
			    ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
			    image = BitmapFactory.decodeStream(imageStream);
			    imageStream = null;
		    }
		    song.setCover(image);
		    
		    list.add(song);
	    } while(cursor.moveToNext());
	    
	    cursor.close();
    	if (db != null && db.isOpen()){
    		db.close();
    	}
		
		return list;
	}

	public synchronized void deleteSongsList(){
		SQLiteDatabase db = dbManager.getReadableDatabase();
		if(db == null){
			return;
		}
		if(!db.isOpen()){
			dbManager.openDB(db);
			db = dbManager.getReadableDatabase();
		}

		String[] values = {};
		db.delete(context.getString(R.string.sql_table_song), "", values); 
		
		if (db != null && db.isOpen()){
    		db.close();
    	}
	}
	
    
    public void saveSongsList(ArrayList<Song> list){
    	deleteSongsList();
    	for(Song song : list){
    		saveSong(song);
    	}
    }
    
	private synchronized void saveSong(Song song){
    SQLiteDatabase db = dbManager.getWritableDatabase();
    if(db == null){
        return;
    }
    ContentValues dataToInsert = new ContentValues();
    dataToInsert.put(context.getString(R.string.sql_column_song_idsong), song.getId());
    dataToInsert.put(context.getString(R.string.sql_column_song_title), song.getTitle());
    dataToInsert.put(context.getString(R.string.sql_column_song_artist), song.getArtist());
    dataToInsert.put(context.getString(R.string.sql_column_song_price), song.getPrice());
    dataToInsert.put(context.getString(R.string.sql_column_song_coverURL), song.getCoverURL());

    if(!db.isOpen()){
    dbManager.openDB(db);
    db = dbManager.getWritableDatabase();
    }
    if(db == null){
    return;
    }
    db.insertWithOnConflict(context.getString(R.string.sql_table_song), null, dataToInsert,
            SQLiteDatabase.CONFLICT_IGNORE);
    if (db != null && db.isOpen()){
    db.close();
    }
	}
	
	public synchronized void saveCover(Song song, byte[] imageByteArray){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		if(db == null){
			return;
		}
		ContentValues dataToInsert = new ContentValues();
		dataToInsert.put(context.getString(R.string.sql_column_song_cover), imageByteArray);
		String[] whereValues = {String.valueOf(song.getId())};

		if(!db.isOpen()){
			dbManager.openDB(db);
			db = dbManager.getWritableDatabase();
		}
		if(db == null){
			return;
		}
		db.update(context.getString(R.string.sql_table_song), dataToInsert, 
				context.getString(R.string.sql_update_song_cover_whereclause), whereValues);
    	if (db != null && db.isOpen()){
    		db.close();
    	}
	}
}
