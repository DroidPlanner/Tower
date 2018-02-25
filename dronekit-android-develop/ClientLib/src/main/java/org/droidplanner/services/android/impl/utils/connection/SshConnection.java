package org.droidplanner.services.android.impl.utils.connection;

import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SocketFactory;

import org.droidplanner.services.android.impl.communication.model.DataLink;
import org.droidplanner.services.android.impl.core.MAVLink.connection.MavLinkConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 2/24/15.
 */
public class SshConnection {

    private static final String TAG = SshConnection.class.getSimpleName();

    public interface UploadListener {
        void onUploaded(File uploadFile, long uploadBytesCount, long totalBytesCount);

        boolean shouldContinueUpload();
    }

    public interface DownloadListener {

        void onFileSizeCalculated(long fileSize);

        void onDownloaded(String downloadFile, long downloadBytesCount);
    }

    private static final int CONNECTION_TIMEOUT = 15000; //ms

    private static final String EXEC_CHANNEL_TYPE = "exec";

    private final JSch jsch;
    private final String host;
    private final String username;
    private final String password;
    private final DataLink.DataLinkProvider linkProvider;

    public SshConnection(String host, String username, String password, DataLink.DataLinkProvider linkProvider) {
        this.jsch = new JSch();
        this.host = host;
        this.username = username;
        this.password = password;
        this.linkProvider = linkProvider;
    }

    private Session getSession() throws JSchException {
        Session session = jsch.getSession(username, host);

        Bundle extras = linkProvider.getConnectionExtras();
        if (extras != null && !extras.isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network network = extras.getParcelable(MavLinkConnection.EXTRA_NETWORK);
                if (network != null) {
                    session.setSocketFactory(new SshSocketFactory(network.getSocketFactory()));
                }
            }
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.setPassword(this.password);
        session.connect(CONNECTION_TIMEOUT);

        return session;
    }

    public boolean ping() {
        Session session = null;
        try {
            session = getSession();
            return true;
        } catch (JSchException e) {
            return false;
        } finally {
            if (session != null && session.isConnected())
                session.disconnect();
        }
    }

    public String execute(String command) throws IOException {
        if (TextUtils.isEmpty(command))
            return null;

        Session session = null;
        Channel execChannel = null;
        try {
            session = getSession();

            execChannel = session.openChannel(EXEC_CHANNEL_TYPE);
            ((ChannelExec) execChannel).setCommand(command);
            execChannel.setInputStream(null);

            final InputStream in = execChannel.getInputStream();
            execChannel.connect(CONNECTION_TIMEOUT);

            final int bufferSize = 1024;
            final StringBuilder response = new StringBuilder();
            final byte[] buffer = new byte[bufferSize];
            while (true) {
                while (in.available() > 0) {
                    int dataSize = in.read(buffer, 0, bufferSize);
                    if (dataSize < 0)
                        break;

                    response.append(new String(buffer, 0, dataSize));
                }

                if (execChannel.isClosed()) {
                    if (in.available() > 0) continue;
                    Timber.d("SSH command exit status: " + execChannel.getExitStatus());
                    break;
                }
            }

            return response.toString();

        } catch (JSchException e) {
            throw new IOException(e);
        } finally {
            if (execChannel != null && execChannel.isConnected())
                execChannel.disconnect();

            if (session != null && session.isConnected())
                session.disconnect();
        }
    }

    public boolean downloadFile(String localFile, String remoteFilePath) throws IOException {
        return downloadFile(localFile, remoteFilePath, null);
    }


    public boolean downloadFile(String localFile, String remoteFilePath, DownloadListener listener) throws IOException {
        if (localFile == null || remoteFilePath == null)
            return false;

        Session session = null;
        Channel execChannel = null;
        FileOutputStream fos = null;
        OutputStream out = null;
        InputStream in = null;

        try {

            String prefix = null;
            if (new File(localFile).isDirectory()) {
                prefix = localFile + File.separator;
            }
            session = getSession();

            // exec 'scp -f remoteFilePath' remotely
            String command = "scp -f " + remoteFilePath;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            out = channel.getOutputStream();
            in = channel.getInputStream();

            channel.connect();
            byte[] buf = new byte[1024];

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            int c = checkAck(in);
            if (c != 'C') {
                return false;
            }

            // read '0644 '
            in.read(buf, 0, 5);

            long fileSize = 0L;
            while (true) {
                if (in.read(buf, 0, 1) < 0) {
                    // error
                    return false;
                }
                if (buf[0] == ' ') break;
                fileSize = fileSize * 10L + (long) (buf[0] - '0');
            }

            String file = null;
            for (int i = 0; ; i++) {
                in.read(buf, i, 1);
                if (buf[i] == (byte) 0x0a) {
                    file = new String(buf, 0, i);
                    break;
                }
            }

            if (listener != null)
                listener.onFileSizeCalculated(fileSize);
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            // read a content of localFile
            fos = new FileOutputStream(prefix == null ? localFile : prefix + file);
            int bytesToRead;
            long progress = 0;
            while (true) {
                if (buf.length < fileSize) bytesToRead = buf.length;
                else bytesToRead = (int) fileSize;
                bytesToRead = in.read(buf, 0, bytesToRead);
                if (bytesToRead < 0) {
                    // error
                    return false;
                }
                progress += bytesToRead;
                fos.write(buf, 0, bytesToRead);
                fileSize -= bytesToRead;
                if (fileSize == 0L) break;

                if (listener != null)
                    listener.onDownloaded(localFile, progress);


            }
            fos.close();
            fos = null;

            if (checkAck(in) != 0) {
                return false;
            }

            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();


            session.disconnect();

        } catch (JSchException e) {
            throw new IOException(e);
        } finally {
            if (fos != null) {
                fos.close();
            }

            if (out != null)
                out.close();

            if (in != null)
                in.close();

            if (execChannel != null && execChannel.isConnected())
                execChannel.disconnect();

            if (session != null && session.isConnected())
                session.disconnect();
        }

        return true;
    }

    public boolean uploadFile(File localFile, String remoteFilePath, UploadListener listener) throws IOException {
        if (localFile == null || !localFile.isFile() || (listener != null && !listener.shouldContinueUpload()))
            return false;

        Session session = null;
        Channel execChannel = null;
        FileInputStream fis = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            session = getSession();

            String command = "scp -t " + remoteFilePath;
            execChannel = session.openChannel(EXEC_CHANNEL_TYPE);
            ((ChannelExec) execChannel).setCommand(command);

            //Get I/O streams for remote scp
            out = execChannel.getOutputStream();
            in = execChannel.getInputStream();

            execChannel.connect(CONNECTION_TIMEOUT);

            if (checkAck(in) != 0)
                return false;

            if (listener != null && !listener.shouldContinueUpload())
                return false;

            //Send "C0644 fileSize filename"
            final long fileSize = localFile.length();
            command = "C0644 " + fileSize + " " + localFile.getName() + "\n";
            out.write(command.getBytes());
            out.flush();
            if (checkAck(in) != 0)
                return false;

            //Send local file content
            final int bufferSize = 8192;
            fis = new FileInputStream(localFile);
            final byte[] buffer = new byte[bufferSize];
            long uploadedBytesCount = 0;
            while (true) {
                int len = fis.read(buffer, 0, bufferSize);
                if (len <= 0)
                    break;

                out.write(buffer, 0, len);
                uploadedBytesCount += len;
                if (listener != null) {
                    listener.onUploaded(localFile, uploadedBytesCount, fileSize);

                    if (!listener.shouldContinueUpload())
                        return false;
                }
            }

            //Send '\0'
            out.write(0);
            out.flush();
            if (checkAck(in) != 0)
                return false;

            return true;

        } catch (JSchException e) {
            throw new IOException(e);
        } finally {
            if (fis != null) {
                fis.close();
            }

            if (out != null)
                out.close();

            if (in != null)
                in.close();

            if (execChannel != null && execChannel.isConnected())
                execChannel.disconnect();

            if (session != null && session.isConnected())
                session.disconnect();
        }
    }

    private static int checkAck(InputStream in) throws IOException {
        int result = in.read();
        // result may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //              -1

        if (result == 1 || result == 2) {
            //Log the error
            final StringBuilder errorMsg = new StringBuilder();
            int character;
            do {
                character = in.read();
                errorMsg.append((char) character);
            }
            while (character != '\n');

            if (errorMsg.length() > 0)
                Timber.e( errorMsg.toString());
        }

        return result;
    }

    private static class SshSocketFactory implements SocketFactory {

        private final javax.net.SocketFactory socketFactory;

        private SshSocketFactory(javax.net.SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            return socketFactory.createSocket(host, port);
        }

        @Override
        public InputStream getInputStream(Socket socket) throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream(Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }
}
