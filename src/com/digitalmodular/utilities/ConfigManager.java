// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)
// Source File Name: ConfigManager.java

package com.digitalmodular.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ConfigManager {
	private static final String				filename	= "config.ini";
	private static TreeMap<String, String>	data		= new TreeMap<String, String>();

	static {
		revert();
	}

	public static void revert() {
		data.clear();

		try {
			BufferedReader in = new BufferedReader(new FileReader(ConfigManager.filename));
			String s;
			while ((s = in.readLine()) != null) {
				int split = s.indexOf(' ');
				String key = s.substring(0, split++);
				String value = s.substring(split);
				ConfigManager.data.put(key, value);
			}
			in.close();
		}
		catch (FileNotFoundException filenotfoundexception) {}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(ConfigManager.filename));
			Set<String> keys = ConfigManager.data.keySet();
			for (String key : keys) {
				out.write(new StringBuilder(key).append(" ").append(ConfigManager.data.get(key)).append("\n").toString());
			}
			out.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setValue(String key, String value) {
		ConfigManager.data.put(key, value);
	}

	public static void setIntValue(String key, int value) {
		ConfigManager.data.put(key, Integer.toString(value));
	}

	public static void setLongValue(String key, long value) {
		ConfigManager.data.put(key, Long.toString(value));
	}

	public static void setFloatValue(String key, float value) {
		ConfigManager.data.put(key, Float.toString(value));
	}

	public static void setDoubleValue(String key, double value) {
		ConfigManager.data.put(key, Double.toString(value));
	}

	public static void setBoolValue(String key, boolean value) {
		ConfigManager.data.put(key, Boolean.toString(value));
	}

	public static String getValue(String key, String defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setValue(key, defaultValue);
			return defaultValue;
		}
		return value;
	}

	public static int getIntValue(String key, int defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setIntValue(key, defaultValue);
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static long getLongValue(String key, long defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setLongValue(key, defaultValue);
			return defaultValue;
		}
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static float getFloatValue(String key, float defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setFloatValue(key, defaultValue);
			return defaultValue;
		}
		try {
			return Float.parseFloat(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static double getDoubleValue(String key, double defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setDoubleValue(key, defaultValue);
			return defaultValue;
		}
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static boolean getBoolValue(String key, boolean defaultValue) {
		String value = ConfigManager.data.get(key);
		if (value == null) {
			ConfigManager.setBoolValue(key, defaultValue);
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}
        
        public static Map<String, String> getAllData() {
            Map<String, String> allData = new TreeMap<String, String>();
            allData.putAll(data);
            return allData;
        }
}
