/*
 * Copyright (C) 2011 Steven Luo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.droidplanner.android.ssh;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Properties;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

/**
 * A terminal session, consisting of a TerminalEmulator, a TranscriptScreen, the
 * PID of the process attached to the session, and the I/O streams used to talk
 * to the process.
 */
public class TermSession {
	private JSch jsch;	
	private Session mSession;

	private TermSettings mSettings;
	private UpdateCallback mNotify;

	private OutputStream mTermOut;
	private InputStream mTermIn;

	private TranscriptScreen mTranscriptScreen;
	private TerminalEmulator mEmulator;

	private Thread mPollingThread;
	private ByteQueue mByteQueue;
	private byte[] mReceiveBuffer;

	private CharBuffer mWriteCharBuffer;
	private ByteBuffer mWriteByteBuffer;
	private CharsetEncoder mUTF8Encoder;


	// Number of rows in the transcript
	private static final int TRANSCRIPT_ROWS = 10000;

	private static final int NEW_INPUT = 1;

	private boolean mIsRunning = false;

	@SuppressLint("HandlerLeak")
	private Handler mMsgHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (!mIsRunning) {
				return;
			}
			if (msg.what == NEW_INPUT) {
				readFromProcess();
			} 
		}
	};

	private ChannelShell channelssh;

	public class SShConnect extends AsyncTask<Void, Void, Boolean>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			if(sshConnected()){
				
				return true;
				//Toast.makeText(getApplicationContext(), "You are successfully connected", Toast.LENGTH_LONG).show();
			}else{
				return false;
				//Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
			}
		}
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
	
	private String user;
	private String host;  
	private int port = 22;
	private String pass;
	private PrintWriter toChannel;
	public boolean sshConnected() {
		jsch = new JSch();

		try {
			mSession = jsch.getSession(user, host, port);
			mSession.setPassword(pass);

			Properties prop = new Properties();
			prop.put("StrictHostKeyChecking", "no");
			mSession.setConfig(prop);
			
			mSession.connect();
			channelssh = (ChannelShell) mSession.openChannel("shell");
			mTermIn = channelssh.getInputStream();
			mTermOut = channelssh.getOutputStream();
	        toChannel = new PrintWriter(new OutputStreamWriter(mTermOut), true);
			// Execute command
			channelssh.connect();
			return true;
		} catch (JSchException e) {
			String message = e.getMessage();
	        if(message.contains("UnknownHostException"))
	            System.out.println(">>>>> Unknow Host. Please verify hostname.");
	        else if(message.contains("socket is not established"))
	        	System.out.println(">>>>> Can't connect to the server for the moment.");
	        else if(message.contains("Auth fail"))
	        	System.out.println(">>>>> Please verify login and password");
	        else if(message.contains("Connection refused"))
	        	System.out.println(">>>>> The server refused the connection");
	        else
	            System.out.println("*******Unknown ERROR********");
	        
			return false;
		}catch (Exception e) {
			/*
			 * UserAuthException,TransportException,ConnectionException,IOException
			 */
			Log.w(TermSession.class.getName(), "exec command error", e);
			this.cleanup();
			return false;
		}
	}
	
	public void sendCommand(final String command)
	{
	    if(mSession != null && mSession.isConnected())
	    {
	        try {
	            toChannel.println(command);
	        } catch(Exception e){
	            e.printStackTrace();
	        }
	    }
	}
	
	public TermSession(TermSettings settings, String ip, int port, String user, String password, Context context) throws Exception 
	{
		mSettings = settings;
		mTermOut = null;
		mTermIn = null;
		try{
			this.host = ip;
			this.port = port;
			this.user = user;
			this.pass = password;
			
			boolean flag = new SShConnect().execute().get();
			
			if(flag){
				Toast.makeText(context.getApplicationContext(), "You are successfully connected", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(context.getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
			}
			
		}catch (Exception e){
			/*
			 * UserAuthException,TransportException,ConnectionException,IOException
			 */
			Log.w(TermSession.class.getName(), "exec command error", e);
			this.cleanup();
			throw e;
		}

		mWriteCharBuffer = CharBuffer.allocate(2);
		mWriteByteBuffer = ByteBuffer.allocate(4);
		mUTF8Encoder = Charset.forName("UTF-8").newEncoder();
		mUTF8Encoder.onMalformedInput(CodingErrorAction.REPLACE);
		mUTF8Encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

		mReceiveBuffer = new byte[4 * 1024];
		mByteQueue = new ByteQueue(4 * 1024);

		mPollingThread = new Thread() {
			private byte[] mBuffer = new byte[4096];

			@Override
			public void run(){
				try{
					while (!isInterrupted()){
						int read = mTermIn.read(mBuffer);
						
						if (read == -1){
							// EOF -- process exited
							return;
						}
						mByteQueue.write(mBuffer, 0, read);
						mMsgHandler.sendMessage(mMsgHandler.obtainMessage(NEW_INPUT));
					}
				}catch (Exception e){
					Log.w(TermSession.class.getName(), "polling thread error", e);
				} 
			}
		};

		mPollingThread.setName("Input reader");
	}

	private void initializeEmulator(int columns, int rows) {
		TermSettings settings = mSettings;
		int[] colorScheme = settings.getColorScheme();
		mTranscriptScreen = new TranscriptScreen(columns, TRANSCRIPT_ROWS,
				rows, colorScheme[0], colorScheme[2]);
		mEmulator = new TerminalEmulator(settings, mTranscriptScreen, columns,
				rows, mTermOut);

		mIsRunning = true;
		mPollingThread.start();
	}
	
	public void write(String data) {
		try {
			mTermOut.write(data.getBytes("UTF-8"));
			mTermOut.flush();
		} catch (IOException e) {
			// Ignore exception
			// We don't really care if the receiver isn't listening.
			// We just make a best effort to answer the query.
		}
	}

	public void write(int codePoint) {
		CharBuffer charBuf = mWriteCharBuffer;
		ByteBuffer byteBuf = mWriteByteBuffer;
		CharsetEncoder encoder = mUTF8Encoder;
		try {
			charBuf.clear();
			byteBuf.clear();
			Character.toChars(codePoint, charBuf.array(), 0);
			encoder.reset();
			encoder.encode(charBuf, byteBuf, true);
			encoder.flush(byteBuf);
			mTermOut.write(byteBuf.array(), 0, byteBuf.position() - 1);
			mTermOut.flush();
		} catch (IOException e) {
			// Ignore exception
		}
	}

	public OutputStream getTermOut() {
		return mTermOut;
	}

	public TranscriptScreen getTranscriptScreen() {
		return mTranscriptScreen;
	}

	public TerminalEmulator getEmulator() {
		return mEmulator;
	}

	public void setUpdateCallback(UpdateCallback notify) {
		mNotify = notify;
	}

	public void updateSize(int columns, int rows) {
		// Inform the attached pty of our new size:
		// Exec.setPtyWindowSize(mTermFd, rows, columns, 0, 0);

		if (mEmulator == null) {
			initializeEmulator(columns, rows);
		} else {
			mEmulator.updateSize(columns, rows);
		}
	}

	public String getTranscriptText() {
		return mTranscriptScreen.getTranscriptText();
	}

	/**
	 * Look for new input from the ptty, send it to the terminal emulator.
	 */
	private void readFromProcess() {
		int bytesAvailable = mByteQueue.getBytesAvailable();
		int bytesToRead = Math.min(bytesAvailable, mReceiveBuffer.length);
		try {
			int bytesRead = mByteQueue.read(mReceiveBuffer, 0, bytesToRead);
			mEmulator.append(mReceiveBuffer, 0, bytesRead);
		} catch (InterruptedException e) {
		}

		if (mNotify != null) {
			mNotify.onUpdate();
		}
	}

	public void updatePrefs(TermSettings settings) {
		mSettings = settings;
		if (mEmulator == null) {
			// Not initialized yet, we'll pick up the settings then
			return;
		}

		mEmulator.updatePrefs(settings);

		int[] colorScheme = settings.getColorScheme();
		mTranscriptScreen.setDefaultColors(colorScheme[0], colorScheme[2]);
	}

	public void reset() {
		mEmulator.reset();
		if (mNotify != null) {
			mNotify.onUpdate();
		}
	}


	public void finish() {
		mIsRunning = false;
		mTranscriptScreen.finish();
		cleanup();
	}

	protected void cleanup() 
	{
		if (mPollingThread != null) {
			try {
				mPollingThread.interrupt();
				mPollingThread = null;
			} catch (Exception e) {
				Log.w(TermSession.class.getName(),
						"polling thread interrupt error", e);
			}
		}

		if (jsch != null) {
			try {
				channelssh.disconnect();
				channelssh = null;
				mTermIn = null;
				mTermOut = null;
			} catch (Exception e) {
				Log.w(TermSession.class.getName(), "shell.close error", e);
			}
		}

		if (mSession != null) {
			try {
				mSession.disconnect();
				mSession = null;
			} catch (Exception e) {
				Log.w(TermSession.class.getName(), "session.close error", e);
			}
		}

		if (channelssh != null) {
			try {
				channelssh.disconnect();
				channelssh = null;
			} catch (Exception e) {
				Log.w(TermSession.class.getName(), "ssh.disconnect error", e);
			}
		}
	}
}
