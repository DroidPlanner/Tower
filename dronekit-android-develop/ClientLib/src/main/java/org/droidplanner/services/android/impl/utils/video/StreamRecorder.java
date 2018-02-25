package org.droidplanner.services.android.impl.utils.video;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

import com.o3dr.android.client.utils.video.MediaCodecManager;
import com.o3dr.android.client.utils.video.NaluChunk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import timber.log.Timber;

/**
 * Created by Fredia Huya-Kouadio on 11/22/15.
 */
class StreamRecorder implements MediaCodecManager.NaluChunkListener{

    private final AtomicReference<String> recordingFilename = new AtomicReference<>();
    private final AtomicBoolean areParametersSet = new AtomicBoolean(false);

    private final File mediaRootDir;
    private final Context context;

    private final MediaScannerConnection.OnScanCompletedListener scanCompletedListener = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted(String path, Uri uri) {
            Timber.i("Media file %s was scanned successfully: %s", path, uri);
        }
    };

    private ExecutorService asyncExecutor;

    private BufferedOutputStream h264Writer;

    StreamRecorder(Context context) {
        this.context = context;
        this.mediaRootDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "stream");
        if (!this.mediaRootDir.exists()) {
            this.mediaRootDir.mkdirs();
        }
    }

    String getRecordingFilename(){
        return recordingFilename.get();
    }

    void startConverterThread() {
        if (asyncExecutor == null || asyncExecutor.isShutdown()) {
            asyncExecutor = Executors.newSingleThreadExecutor();
        }
    }

    void stopConverterThread() {
        if (asyncExecutor != null)
            asyncExecutor.shutdown();
    }

    boolean isRecordingEnabled() {
        return !TextUtils.isEmpty(recordingFilename.get());
    }

    boolean enableRecording(String mediaFilename) {
        if (!isRecordingEnabled()) {
            areParametersSet.set(false);
            recordingFilename.set(mediaFilename);

            Timber.i("Enabling local recording to %s", mediaFilename);
            File h264File = new File(mediaRootDir, mediaFilename);
            if (h264File.exists())
                h264File.delete();

            try {
                h264Writer = new BufferedOutputStream(new FileOutputStream(h264File));
                return true;
            } catch (FileNotFoundException e) {
                Timber.e(e, e.getMessage());
                recordingFilename.set(null);
                return false;
            }
        } else {
            Timber.w("Video stream recording is already enabled");
            return false;
        }
    }

    boolean disableRecording() {
        if (isRecordingEnabled()) {
            Timber.i("Disabling local recording");

            //Close the Buffered output stream
            if (h264Writer != null) {
                try {
                    h264Writer.close();
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                } finally {
                    h264Writer = null;

                    //Kickstart conversion of the h264 file to mp4.
                    convertToMp4(recordingFilename.get());

                    recordingFilename.set(null);
                }
            }
        }

        areParametersSet.set(false);

        return true;
    }

    //TODO: Maybe put this on a background thread to avoid blocking on the write to file.
    @Override
    public void onNaluChunkUpdated(NaluChunk parametersSet, NaluChunk dataChunk) {
        if (isRecordingEnabled() && h264Writer != null) {
            if(areParametersSet.get()) {
                try {
                    writeNaluChunk(h264Writer, dataChunk);
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
            }
            else{
                try {
                    areParametersSet.set(writeNaluChunk(h264Writer, parametersSet));
                } catch (IOException e) {
                    Timber.e(e, e.getMessage());
                }
            }
        }
    }

    private boolean writeNaluChunk(BufferedOutputStream bos, NaluChunk naluChunk) throws IOException {
        if(naluChunk == null)
            return false;

        int payloadCount = naluChunk.payloads.length;
        for (int i = 0; i < payloadCount; i++) {
            ByteBuffer payload = naluChunk.payloads[i];

            if (payload.capacity() == 0)
                continue;

            final int dataLength = payload.position();
            byte[] payloadData = payload.array();
            bos.write(payloadData, 0, dataLength);
        }

        return true;
    }

    void convertToMp4(final String filename) {
        if (TextUtils.isEmpty(filename)) {
            Timber.w("Invalid media filename.");
            return;
        }

        final File rawMedia = new File(mediaRootDir, filename);
        if (!rawMedia.exists()) {
            Timber.w("Media file doesn't exists.");
            return;
        }

        if(rawMedia.length() == 0){
            Timber.w("Media file is empty.");
            return;
        }

        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Timber.i("Starting h264 conversion process for media file %s.", filename);

                try {
                    H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(rawMedia));
                    Movie movie = new Movie();
                    movie.addTrack(h264Track);
                    Container mp4File = new DefaultMp4Builder().build(movie);

                    File dstDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                    File mp4Media = new File(dstDir, filename + ".mp4");
                    Timber.i("Generating the mp4 file @ %s", mp4Media.getAbsolutePath());
                    FileChannel fc = new FileOutputStream(mp4Media).getChannel();
                    mp4File.writeContainer(fc);
                    fc.close();

                    //Delete the h264 file.
                    Timber.i("Deleting raw h264 media file.");
                    rawMedia.delete();

                    //Add the generated file to the mediastore
                    Timber.i("Adding the generated mp4 file to the media store.");
                    MediaScannerConnection.scanFile(context,
                            new String[]{mp4Media.getAbsolutePath()}, null, scanCompletedListener);

                } catch (IOException | NullPointerException e) {
                    Timber.e(e, e.getMessage());
                }
            }
        });
    }
}
