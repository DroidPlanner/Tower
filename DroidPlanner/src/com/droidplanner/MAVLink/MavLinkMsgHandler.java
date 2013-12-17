package com.droidplanner.MAVLink;

import java.util.ArrayList;
import java.util.List;

import com.MAVLink.Messages.MAVLinkMessage;

public class MavLinkMsgHandler {

	private List<OnMavLinkMsgListener>msgListeners = new ArrayList<OnMavLinkMsgListener>();

	public interface OnMavLinkMsgListener {
		public void onMavLinkMsg(MAVLinkMessage msg);
	}
	
	public MavLinkMsgHandler() {
	}

	public void unregisterMsgListeners(OnMavLinkMsgListener listener) {
		if(listener!=null&&msgListeners.contains(listener)){
			msgListeners.remove(listener);
		}
	}

	public void registerMsgListeners(OnMavLinkMsgListener listener) {
		if(listener!=null&&!msgListeners.contains(listener)){
			msgListeners.add(listener);
		}
	}

	public void receiveData(MAVLinkMessage msg) {

		if(msgListeners.size()>0){
			for(OnMavLinkMsgListener listener: msgListeners){
				listener.onMavLinkMsg(msg);
			}
		}
	}
}
