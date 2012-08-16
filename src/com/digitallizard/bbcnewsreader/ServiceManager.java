package com.digitallizard.bbcnewsreader;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.ListIterator;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ServiceManager {
	
	private Context context;
	private Messenger resourceMessenger;
	private Messenger messenger;
	private boolean resourceServiceBound;
	private ArrayList<Message> messageQueue;
	
	public interface MessageReceiver {
		public void handleMessage(Message msg);
	}
	
	static class IncomingHandler extends Handler {
		private final WeakReference<MessageReceiver> receiver;		
		
		IncomingHandler(MessageReceiver receiver) {
			this.receiver = new WeakReference<MessageReceiver>(receiver);
		}
		
		@Override
		public void handleMessage(Message msg) {
			// pass the message on
			receiver.get().handleMessage(msg);
		}
	}
		
	private ServiceConnection resourceServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			resourceServiceBound = true; // flag the service as bound
			resourceMessenger = new Messenger(service); // allows us to talk to the service
			// try and tell the service that we have connected so it will keep talking to us
			sendMessageToService(ResourceService.MSG_REGISTER_CLIENT);
			
			// send any queued messages
			ListIterator<Message> iterator = messageQueue.listIterator();
			while (iterator.hasNext()) {
				sendMessageToService(iterator.next());
			}
			messageQueue.clear();
		}
		
		public void onServiceDisconnected(ComponentName className) {
			// this runs if the service randomly disconnects if this happens there are more problems than a missing service
			resourceMessenger = null; // as the service no longer exists, destroy its pointer
		}
	};
	
	public void doBindService() {
		// bind the service
		context.bindService(new Intent(context, ResourceService.class), resourceServiceConnection, Context.BIND_AUTO_CREATE);
		resourceServiceBound = true;
	}
	
	public void doUnbindService() {
		// disconnect the resource service
		// check if the service is bound, if so, disconnect it
		if (resourceServiceBound) {
			// politely tell the service that we are disconnected
			sendMessageToService(ResourceService.MSG_UNREGISTER_CLIENT);
			// remove local references to the service
			context.unbindService(resourceServiceConnection);
			resourceServiceBound = false;
		}
	}
	
	public void sendMessageToService(Message msg) {
		// check the service is bound before trying to send a message
		if (resourceServiceBound && resourceMessenger != null) {
			try {
				resourceMessenger.send(msg); // send the message
			} catch (RemoteException e) {
				// we are probably shutting down, but report it anyway
				Log.e("ERROR", "Unable to send message to service: " + e.getMessage());
			}
		}
		else {
			// things haven't initialized yet, queue the message up for sending in a bit
			messageQueue.add(msg);
		}
	}
	
	public void sendMessageToService(int what, Bundle bundle) {
		// create a message according to parameters
		Message msg = Message.obtain(null, what);
		// add the bundle if needed
		if (bundle != null) {
			msg.setData(bundle);
		}
		msg.replyTo = messenger; // tell the service to reply to us, if needed
		
		// check the service is bound before trying to send a message
		if (resourceServiceBound && resourceMessenger != null) {
			try {
				resourceMessenger.send(msg); // send the message
			} catch (RemoteException e) {
				// we are probably shutting down, but report it anyway
				Log.e("ERROR", "Unable to send message to service: " + e.getMessage());
			}
		}
		else {
			// things haven't initialized yet, queue the message up for sending in a bit
			messageQueue.add(msg);
		}
	}
	
	public void sendMessageToService(int what) {
		sendMessageToService(what, null);
	}
	
	public ServiceManager(Context context, MessageReceiver receiver) {
		this.context = context;
		
		messenger = new Messenger(new IncomingHandler(receiver));
		messageQueue = new ArrayList<Message>();
	}
}
