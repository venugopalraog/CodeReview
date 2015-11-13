package com.intelematics.interview.tasks;

import java.util.ArrayList;

import com.intelematics.interview.SongListActivity;
import com.intelematics.interview.db.DBManager;
import com.intelematics.interview.db.SongManager;
import com.intelematics.interview.models.Song;
import android.os.AsyncTask;

/**
 *
 */
public class ReadDBSongListTask extends AsyncTask<Void, Void, Void> {
	private DBManager dbManager;
	private SongListActivity activity;
	private ArrayList<Song> songList;
	
	
	public ReadDBSongListTask(SongListActivity activity, DBManager dbManager) {
		this.activity = activity;
		this.dbManager = dbManager;
		songList = new ArrayList<Song>();
	}

	
	@Override
	protected Void doInBackground(Void... params) {
		SongManager songManager = new SongManager(activity, dbManager);
		songList = songManager.getSongsList();
		
		return null;
	}

    protected void onPostExecute(Void result) {
    	activity.updateSongList(songList);
    }



}
