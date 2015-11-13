package com.intelematics.interview.util;

import java.io.IOException;
import java.util.ArrayList;

import com.intelematics.interview.models.Song;

import android.util.JsonReader;
import android.util.JsonToken;

/**
 *
 */
public class JsonParser {
	public static final String JSON_KEY_RESULTCOUNT = "resultCount";
	public static final String JSON_KEY_RESULTS = "results";
	public static final String JSON_KEY_TRACKID = "trackId";
	public static final String JSON_KEY_ARTISTNAME = "artistName";
	public static final String JSON_KEY_TRACKNAME = "trackName";
	public static final String JSON_KEY_TRACKPRICE = "trackPrice";
	public static final String JSON_KEY_TRACKCOVER = "artworkUrl100";
	
	
	public JsonParser(){
	}
	
	public ArrayList<Song> parseSongList(JsonReader jsonReader){
		ArrayList<Song> songList = new ArrayList<Song>();
		
		try {
			jsonReader.beginObject();
		    while (jsonReader.hasNext()) {
		    	String name = jsonReader.nextName();
		    	if (name.equals(JSON_KEY_RESULTS) && jsonReader.peek() != JsonToken.NULL) {
		        	 
		    		jsonReader.beginArray();
		    		while (jsonReader.hasNext()) {
		    			Song song = parseSong(jsonReader);
		    			if(song != null){
		    				songList.add(song);
		    			}
		    		}
		    		jsonReader.endArray();
			   	     
		    	} else {
		    		jsonReader.skipValue();
		    	}
		    }
		    jsonReader.endObject();
		     
		} catch (IOException e) {
		}
		
		return songList;
	}
	
	public Song parseSong(JsonReader jsonReader){
		Song song = new Song();
		
		try {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				String name = jsonReader.nextName();
				if (name.equals(JSON_KEY_TRACKID)) {
					song.setId(jsonReader.nextLong());
				} else if (name.equals(JSON_KEY_ARTISTNAME)) {
					song.setArtist(jsonReader.nextString());
				} else if (name.equals(JSON_KEY_TRACKNAME)) {
					song.setTitle(jsonReader.nextString());
				} else if (name.equals(JSON_KEY_TRACKPRICE)) {
					song.setPrice(jsonReader.nextDouble());
				} else if (name.equals(JSON_KEY_TRACKCOVER)) {
					song.setCoverURL(jsonReader.nextString());
				} else {
					jsonReader.skipValue();
				}
			}
			jsonReader.endObject();
			
		} catch (IOException e) {
		}
		
		return song;
	}
	
}
