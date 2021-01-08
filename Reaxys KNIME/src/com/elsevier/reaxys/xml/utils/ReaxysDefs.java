/**
 * 
 */
package com.elsevier.reaxys.xml.utils;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Namespace containing global definitions like version numbers
 * 
 * @author BROEKF
 *
 */
public final class ReaxysDefs {
	
	private static final String UNKNOWN = "18 August 2020 14:08EDT";


	/**
	 * Prevent instantiation
	 */
	private ReaxysDefs() {
		
	}
	
	/**
	 * Gets the jar file which contains the given compiled class
	 * @param cls The Class to find the jar for
	 * @return The jar File
	 */
	public static File getJarFile(Class<?> cls) {
		CodeSource source = cls.getProtectionDomain().getCodeSource();
		if (source == null) { 
			return null;
		}
		URL url = source.getLocation();
		if (url == null) { 
			return null;
		}
		String path = url.getFile();
		File res = new File(path);
		if (res.isDirectory()) { 
			return null;
		}
		return res.getAbsoluteFile();
	}
	
	/**
	 * Gets the Manifest from the given jar File
	 * 
	 * @param jarFile
	 *            The jar File to get the Manifest from
	 * @return the Manifest or null if not found
	 */
	private static Manifest getManifest(File jarFile) {
		try {
			JarFile jar = new JarFile(jarFile);
			try {
				Manifest mf = jar.getManifest();
				if (mf == null) {
					return null;
				}
				return mf;
			} finally {
				jar.close();
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Gets the svn version string from the jar Manifest
	 * 
	 * @return the version String
	 */
	public static String getVersion() {
		
		File jarFile = getJarFile(ReaxysDefs.class);
		if (jarFile == null) {
			return UNKNOWN;
		}
		Manifest manifest = getManifest(jarFile);
		if (manifest == null) {
			return UNKNOWN;
		}
		String version = manifest.getMainAttributes().getValue("Sources-Version");
		if (version == null) {
			version = UNKNOWN;
		} else { 
			// Only get the major number from the version string
			version = version.split("[MS:]")[0];
		}
		return version;
	}
	
	/**
	 * Gets the datetime string of the build date of the jar from the Manifest
	 * 
	 * @return the datetime string in YYYYMMDD.HHmm format
	 */
	public static String getBuildDateTime() {
		
		String dateString = UNKNOWN;
		File jarFile = getJarFile(ReaxysDefs.class);
		if (jarFile == null) {
			return UNKNOWN;
		}
		Manifest manifest = getManifest(jarFile);
		if (manifest == null) {
			return UNKNOWN;
		}
		String buildTimeString = manifest.getMainAttributes().getValue("Build-Time");
		if (buildTimeString == null) {
			buildTimeString = new Date().toString();
		}
		
		// Date format as defined in build.xml for Manifest
		SimpleDateFormat mfDateFormat = new SimpleDateFormat("dd MMMMM yyyy, HH:mm Z", new Locale("en","UK"));
		
		// Date format for version string (and use UTC for the time zone)
		SimpleDateFormat versionDateFormat = new SimpleDateFormat("yyyyMMdd.HHmm", new Locale("en","UK"));
		versionDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		try {
			Date buildDate = mfDateFormat.parse(buildTimeString);
			dateString = versionDateFormat.format(buildDate);
		} catch (ParseException e) {
			return UNKNOWN;
		}
		
		return dateString;
	}
	
	/**
	 * Gets a string composed of svn revision and datetime of build from manifest
	 * 
	 * @return String in format ##### (YYYYMMdd.HHmm)
	 */
	public static String getDateVersionString() {
		return getVersion() + " (" + getBuildDateTime() +")";
	}
}
