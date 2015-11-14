package com.intelematics.interview.tasks;

import java.util.ArrayList;

import com.intelematics.interview.R;
import com.intelematics.interview.SongListActivity;
import com.intelematics.interview.db.DBManager;
import com.intelematics.interview.db.SongManager;
import com.intelematics.interview.models.Song;
import com.intelematics.interview.net.ConnectionManager;
import com.intelematics.interview.util.JsonParser;
import android.os.AsyncTask;
import android.util.JsonReader;

/**
 *
 */
public class DownloadSongListTask extends AsyncTask<Void, Void, Boolean> {
	private DBManager dbManager;
	private SongListActivity activity;
	private ArrayList<Song> songList;
	
	private ConnectionManager connectionManager;
	
	public DownloadSongListTask(SongListActivity activity, DBManager dbManager) {
		this.activity = activity;
		this.dbManager = dbManager;
		songList = new ArrayList<Song>();
	}
	
	@Override
	protected Boolean doInBackground(Void... params) {
		JsonParser parser = new JsonParser();

        // Rock version of the app
        connectionManager = new ConnectionManager(activity, "https://itunes.apple.com/search?term=rock&amp;media=music&amp;entity=song&amp;limit=50");

        // Pop version of the app
        //connectionManager = new ConnectionManager(activity, https://itunes.apple.com/search?term=popk&amp;media=music&amp;entity=song&amp;limit=50);

        // Classic version of the app
        //connectionManager = new ConnectionManager(activity, https://itunes.apple.com/search?term=classick&amp;media=music&amp;entity=song&amp;limit=50);

		JsonReader jsonReader = connectionManager.requestJson();
		if (jsonReader != null) {
			songList = parser.parseSongList(jsonReader);
			SongManager songManager = new SongManager(activity, dbManager);
			songManager.saveSongsList(songList);
		}
		connectionManager.closeConnection();
		return (jsonReader != null);
	}

    protected void onPostExecute(Boolean result) {
		if (result) {
			activity.updateSongList(songList);
		}
    }
}
