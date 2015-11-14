package com.intelematics.interview.net;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.JsonReader;
import android.util.Log;


/**
 *
 */
public class ConnectionManager {
	private static final String TAG = "ConnectionManager";
	private HttpURLConnection httpConnection = null;
	private URL url = null;
	private InputStream is = null;
	private JsonReader jsonReader = null;
	
	private Context context;
	private ConnectionRequestListener mConnectionRequestListener = null;

	public ConnectionManager(Context context, String requestURL){
		this.context = context;
		mConnectionRequestListener = (ConnectionRequestListener) context;
		try {
			url = new URL(requestURL);
			
		} catch (MalformedURLException e) {
			mConnectionRequestListener.onExceptionOccurred(e);
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return (networkInfo != null && networkInfo.isConnected());
	}

	
	public JsonReader requestJson(){
		if (!isNetworkAvailable()) {
			mConnectionRequestListener.onNetworkDown("No Network Connection");
			return null;
		}

		try {
			jsonReader = new JsonReader(new InputStreamReader(request(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			mConnectionRequestListener.onExceptionOccurred(e);
		}
		
		return jsonReader;
	}
	
	public InputStream request(){
		if (!isNetworkAvailable()) {
			mConnectionRequestListener.onNetworkDown("No Network Connection");
			return null;
		}
	    try {
	        httpConnection = (HttpURLConnection) url.openConnection();

	        int responseCode = httpConnection.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	            is = httpConnection.getInputStream();
	            
	        } else {
				mConnectionRequestListener.onResponseErrorReceived("Server Response", String.valueOf(responseCode));
			}
	        
	    } catch (Exception ex) {
			mConnectionRequestListener.onExceptionOccurred(ex);
		}
	    
	    return is;
	}
	
	public void closeConnection(){
	    try{
	    	if(is != null){
	    		is.close();
	    	}
	    	if(httpConnection != null){
	    		httpConnection.disconnect();
	    	}
		} catch(Exception e){
			mConnectionRequestListener.onExceptionOccurred(e);
		}
	}
	
	
	public ByteArrayBuffer requestImage(){
		HttpURLConnection httpConnection = null;
		ByteArrayBuffer baf = new ByteArrayBuffer(1024);
		BufferedInputStream bis = null;

		if(!isNetworkAvailable()){
			mConnectionRequestListener.onNetworkDown("No Network Connection");
			return null;
		}
		
	    try {
	        httpConnection = (HttpURLConnection) url.openConnection();
	        
	        int responseCode = httpConnection.getResponseCode();
	        if (responseCode == HttpURLConnection.HTTP_OK) {
	        	bis = new BufferedInputStream(httpConnection.getInputStream(), 1024);

				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
	        } else {
				mConnectionRequestListener.onResponseErrorReceived("Server Response", String.valueOf(responseCode));
			}
	        
	    } catch (Exception ex) {
			mConnectionRequestListener.onExceptionOccurred(ex);

	    }
	    return baf;
	} 
	
}
