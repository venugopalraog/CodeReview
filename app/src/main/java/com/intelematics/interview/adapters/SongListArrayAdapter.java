package com.intelematics.interview.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import com.intelematics.interview.R;
import com.intelematics.interview.SongListActivity;
import com.intelematics.interview.db.DBManager;
import com.intelematics.interview.db.SongManager;
import com.intelematics.interview.models.Song;
import com.intelematics.interview.net.ConnectionManager;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.Editable;
import android.util.Log;
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

	private ImageLoader mImageLoader = null;

	//View Holder class for storing Row Item Views
	static class ViewHolderListItem {
		ImageView albumCover;
		TextView songName;
		TextView songArtist;
		TextView songPrice;
		ProgressBar progressBar;
		int position;
	}

	public SongListArrayAdapter(SongListActivity activity, ArrayList<Song> songs, DBManager dbManager) {
		super(activity, R.layout.song_list_row, songs);
		this.activity = activity;
		this.songsList = songs;
		this.filteredSongsList = songs;
		this.dbManager = dbManager;


		//Initialize the ImageLoader used for loading Images from URL in ListView
		mImageLoader = ImageLoader.getInstance();
		DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
				.cacheInMemory(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity)
				.defaultDisplayImageOptions(defaultOptions)
				.memoryCache(new WeakMemoryCache()).build();

		mImageLoader.init(config);
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
		final ViewHolderListItem viewHolderListItem;
		if (convertView == null) {
			LayoutInflater inflater = activity.getLayoutInflater();
			convertView = inflater.inflate(R.layout.song_list_row, parent, false);

			viewHolderListItem = new ViewHolderListItem();
			viewHolderListItem.albumCover = (ImageView) convertView.findViewById(R.id.album_cover);
			viewHolderListItem.songName = (TextView) convertView.findViewById(R.id.song_title);
			viewHolderListItem.songArtist = (TextView) convertView.findViewById(R.id.song_artist);
			viewHolderListItem.songPrice = (TextView) convertView.findViewById(R.id.song_price);
			viewHolderListItem.progressBar = (ProgressBar) convertView.findViewById(R.id.progress_bar);

			convertView.setTag(viewHolderListItem);
		} else {
			viewHolderListItem = (ViewHolderListItem) convertView.getTag();
		}
		final Song song = filteredSongsList.get(position);
		viewHolderListItem.position = position;
		viewHolderListItem.songName.setText(song.getTitle());
		viewHolderListItem.songArtist.setText(song.getArtist());
		viewHolderListItem.songPrice.setText("$" + String.valueOf(song.getPrice()));

		if(song.getCover() != null){
			Log.d("venu", "Retriew Image Byte array from Database ");
			viewHolderListItem.albumCover.setImageBitmap(song.getCover());
		} else {
			viewHolderListItem.albumCover.setImageResource(R.drawable.img_cover);
//			viewHolderListItem.progressBar.setVisibility(View.VISIBLE);
//		 	getCover(song);

			/*Get the Image using Universal Image Loader*/
			mImageLoader.displayImage(song.getCoverURL(), viewHolderListItem.albumCover,
				new ImageLoadingListener() {
					@Override
					public void onLoadingStarted(String s, View view) {
						viewHolderListItem.progressBar.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String s, View view, FailReason failReason) {
						viewHolderListItem.progressBar.setVisibility(View.INVISIBLE);
					}

					@Override
					public void onLoadingComplete(String s, View view, Bitmap bitmap) {
						viewHolderListItem.progressBar.setVisibility(View.INVISIBLE);
						song.setCover(bitmap);
						ByteArrayOutputStream stream = new ByteArrayOutputStream();
						bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
						byte[] imageByteArray = stream.toByteArray();
						SongManager songManager = new SongManager(activity, dbManager);
						songManager.saveCover(song, imageByteArray);
					}

					@Override
					public void onLoadingCancelled(String s, View view) {
						viewHolderListItem.progressBar.setVisibility(View.INVISIBLE);
					}
				});
			/*Get the Image using AsyncTask but this method has some issues while loading the images*/
	/*		viewHolderListItem.albumCover.setTag(song.getCoverURL());
			new ImageDownloaderTask(viewHolderListItem).execute(song);*/

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

    private Bitmap getCover(Song song){
        if(song.getCover() == null){
            ConnectionManager connectionManager = new ConnectionManager(activity, song.getCoverURL());
            byte[] imageByteArray = connectionManager.requestImage().buffer();
            ByteArrayInputStream imageStream = new ByteArrayInputStream(imageByteArray);
            Bitmap cover = BitmapFactory.decodeStream(imageStream);
            song.setCover(cover);

            SongManager songManager = new SongManager(activity, dbManager);
            songManager.saveCover(song, imageByteArray);
			return cover;
        }
		return null;
    }

	public class ImageDownloaderTask extends AsyncTask<Song, Void, Bitmap> {
		private ViewHolderListItem mViewHolderListItem = null;
		private int mPosition;

		ImageDownloaderTask (ViewHolderListItem viewHolderListItem) {
			mViewHolderListItem = viewHolderListItem;
			mPosition = mViewHolderListItem.position;
		}

		@Override
		protected Bitmap doInBackground(Song... song) {
			return getCover(song[0]);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mViewHolderListItem.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mViewHolderListItem.progressBar.setVisibility(View.INVISIBLE);
			if (result != null && mPosition == mViewHolderListItem.position) {
				mViewHolderListItem.albumCover.setImageBitmap(result);
			}
		}
	}
}
