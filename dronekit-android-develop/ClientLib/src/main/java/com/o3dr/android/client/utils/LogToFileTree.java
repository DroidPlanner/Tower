package com.o3dr.android.client.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.o3dr.android.client.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import timber.log.Timber;

/**
 * Timber Tree to log specific log levels to a file
 */
public class LogToFileTree extends Timber.DebugTree {
    private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("yyyy_MM_dd_HH_mm", Locale.US);

    private final LinkedBlockingQueue<String> logQueue = new LinkedBlockingQueue<>();
    private PrintStream logOutputFile;
    private Thread dequeueThread;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final Date date = new Date();

    @Override
    protected void log(int priority, String tag, String message, Throwable t) {
        super.log(priority, tag, message, t);

        if (isLoggableToFile(priority)) {
            String logOutput = getLogMessage(priority, tag, message);
            logQueue.add(logOutput);
        }
    }

    private boolean isLoggableToFile(int priority) {
        return priority >= BuildConfig.LOG_FILE_LEVEL;
    }

    private String getLogMessage(int priority, String tag, String message) {
        String priorityShort = getPriorityString(priority);
        date.setTime(System.currentTimeMillis());
        return String.format("%s %s/%s : %s", LOG_DATE_FORMAT.format(date), priorityShort, tag, message);
    }

    private String getPriorityString(int priority) {
        String priorityString = null;
        switch (priority) {
            case Log.ASSERT:
                priorityString = "ASSERT";
                break;
            case Log.ERROR:
                priorityString = "E";
                break;
            case Log.WARN:
                priorityString = "W";
                break;
            case Log.INFO:
                priorityString = "I";
                break;
            case Log.DEBUG:
                priorityString = "D";
                break;
            case Log.VERBOSE:
                priorityString = "V";
                break;
            default:
                priorityString = "";
                break;
        }
        return priorityString;
    }

    public void createFileStartLogging(final Context context) {
        if (dequeueThread != null && dequeueThread.isAlive()) {
            stopLoggingThread();
        }

        dequeueThread = new Thread(new Runnable() {
            public void run() {
                PackageInfo pInfo;
                String version = "";
                try {
                    pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.w("Failed to get package info");
                }

                File rootDir = context.getExternalFilesDir(null);
                File dir = new File(rootDir, "/log_cat/");
                dir.mkdirs();

                String fileName = String.format("%s_%s.log", version, FILE_DATE_FORMAT.format(new Date()));
                File logFile = new File(dir, fileName);
                try {
                    logOutputFile = new PrintStream(new FileOutputStream(logFile, true));

                    while (isRunning.get()) {
                        try {
                            String message = logQueue.take();
                            logOutputFile.println(message);
                        } catch (InterruptedException e) {
                            Timber.w("Failed to receive message from logQueue");
                        }
                    }

                } catch (IOException e) {
                    Timber.w("Failed to open file");
                } finally {
                    isRunning.set(false);
                    if (logOutputFile != null) {
                        logOutputFile.close();
                    }
                }
            }
        });

        isRunning.set(true);
        dequeueThread.start();
    }

    public void stopLoggingThread() {
        if (dequeueThread != null) {
            isRunning.set(false);
            dequeueThread.interrupt();
            dequeueThread = null;
        }
    }

}
