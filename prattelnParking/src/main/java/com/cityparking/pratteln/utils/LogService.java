package com.cityparking.pratteln.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogService {

	/**
	 * Primary log tag for games output.
	 */
	private static final String LOG_TAG = "WE";

	public static final int LOG_LEVEL_FULL = 0;

	public static final int LOG_LEVEL_LITE = 1;

	private static boolean write_to_file = false;

	/**
	 * Whether the logs are enabled in release builds or not.
	 */
	private static final boolean ENABLE_LOG = true;

	public static boolean canLog(int level) {
		return (ENABLE_LOG) && Log.isLoggable(LOG_TAG, level);
	}

	public static void log(String tag, String msg) {
		if (canLog(Log.INFO)) {
			if (write_to_file) {
				appendLog(tag + msg);
			} else {
				Log.i(tag, msg);
			}
		}
	}

	public static void err(String tag, String msg, Throwable e) {
		if (canLog(Log.ERROR)) {
			if (write_to_file) {
				appendToFile(e);
			} else {
				Log.e(tag, msg, e);
			}
		}
	}

	public static void errline(String tag, String msg) {
		if (canLog(Log.ERROR)) {
			Log.e(tag, msg);
		}
	}

	public static void memoryCheck(String tag) {
		if (canLog(Log.ERROR)) {
			Log.i(tag, "==============================");
			Log.i(tag, ">> Free memory: " + (Runtime.getRuntime().freeMemory() / 1024) + " Kb");
			Log.i(tag, ">> Max memory: " + (Runtime.getRuntime().maxMemory() / 1024) + " Kb");
			Log.i(tag, ">> Total memory: " + (Runtime.getRuntime().totalMemory() / 1024) + " Kb");
			Log.i(tag, "==============================");
		}
	}

	public static void appendLog(String text) {
		File logFile = new File(Environment.getExternalStorageDirectory() + "/welog.txt");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				LogService.log("LogService", e.getMessage());
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
			buf.append(text);
			buf.newLine();
			buf.flush();
			buf.close();
		} catch (IOException e) {
			LogService.log("LogService", e.getMessage());
		}
	}

	public static void appendToFile(Throwable e) {
		try {
			File logFile = new File(Environment.getExternalStorageDirectory() + "/welog.txt");
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			FileWriter fstream = new FileWriter(logFile);
			BufferedWriter out = new BufferedWriter(fstream);
			PrintWriter pWriter = new PrintWriter(out, true);
			e.printStackTrace(pWriter);
		} catch (Exception ie) {
			throw new RuntimeException("Could not write Exception to file", ie);
		}
	}

}
