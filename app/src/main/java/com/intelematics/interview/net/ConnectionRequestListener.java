package com.intelematics.interview.net;

/**
 * Created by Vgubbala on 11/13/15.
 */
public interface ConnectionRequestListener {
		void onNetworkDown(String error);
		void onResponseErrorReceived(String error_content, String error_id);
		void onExceptionOccurred(Exception error);
}
