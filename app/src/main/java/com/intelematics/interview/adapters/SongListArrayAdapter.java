package com.intelematics.interview.adapters;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import com.intelematics.interview.R;
import com.intelematics.interview.SongListActivity;
import com.intelematics.interview.db.DBManager;
import com.intelematics.interview.db.SongManager;
import com.intelematics.interview.models.Song;
import com.intelematics.interview.net.ConnectionManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 *
 */
public class SongListArrayAdapter extends ArrayAdapter<Song> implements Filterable{
	private DBManager dbManager;
	private final SongListActivity activity;
	private ArrayList<Song> filteredSongsList;
	private ArrayList<Song> songsList;

	public SongListArrayAdapter(SongListActivity activity, ArrayList<Song> songs, DBManager dbManager) {
		super(activity, R.layout.song_list_row, songs);
		this.activity = activity;
		this.songsList = songs;
		this.filteredSongsList = songs;
		this.dbManager = dbManager;
	}

	public void updateList(ArrayList<Song> songs) {
		this.songsList = songs;
		this.filteredSongsList.clear();
		this.filteredSongsList.addAll(songs);
		this.notifyDataSetChanged();
	}
	
	public void updateList(ArrayList<Song> songs, Editable sequence) {
		this.songsList = songs;
		this.filteredSongsList.clear();
		this.filteredSongsList.addAll(songs);
		this.getFilter().filter(sequence);
	}
	

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.song_list_row, parent, false);

            ImageView albumCover = (ImageView)convertView.findViewById(R.id.album_cover);
          TextView songName = (TextView)convertView.findViewById(R.id.song_title);
            TextView songArtist = (TextView)convertView.findViewById(R.id.song_artist);
            TextView songPrice = (TextView)convertView.findViewById(R.id.song_price);
        ProgressBar progressBar = (ProgressBar)convertView.findViewById(R.id.progress_bar);


		final Song song = filteredSongsList.get(position);
		
		songName.setText(song.getTitle());
		  songArtist.setText(song.getArtist());
		songPrice.setText("$" + String.valueOf(song.getPrice()));
		
		if(song.getCover() != null){
	     albumCover.setImageBitmap(song.getCover());
		} else {
		 albumCover.setImageResource(R.drawable.img_cover);
		 progressBar.setVisibility(View.VISIBLE);
		 getCover(song);
		}
		
		return convertView;
	}
	
	@Override
	public int getCount() {
		return filteredSongsList.size();
	}
	
	
	@Override
	public Filter getFilter() {

    Filter filter = new Filter() {

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
    filteredSongsList = (ArrayList<Song>) results.values;
    notifyDataSetChanged();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
    FilterResults results = new FilterResults();
    ArrayList<Song> filteredSongs = new ArrayList<Song>();

    constraint = constraint.toString().toLowerCase();
    for (int i = 0; i < songsList.size(); i++) {
    Song song = songsList.get(i);
    if (song.getArtist().toLowerCase().contains(constraint.toString()) ||
            song.getTitle().toLowerCase().contains(constraint.toString()))  {
    filteredSongs.add(song);
    }
    }

    results.count = filteredSongs.size();
    results.values = filteredSongs;

    return results;
    }
    };

    return filter;
	}
	

    private void getCover(Song song){
        if(song.getCover() == null){
            ConnectionManager connectionManager = new ConnectionManager(activity, song.getCoverURL());
            byte[] imageByteArray = connectionManager.requestImage().buffer();
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
            Bitmap cover = BitmapFactory.decodeStream(imageStream);
            song.setCover(cover);

            SongManager songManager = new SongManager(activity, dbManager);
            songManager.saveCover(song, imageByteArray);
        }
    }

	
}
