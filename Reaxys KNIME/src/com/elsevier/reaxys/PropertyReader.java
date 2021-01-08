package com.elsevier.reaxys;


import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Properties;
import java.util.Map.Entry;

/**
 * class to read parameters that are stored in files either in the Jar file with this
 * class, or in other files outside the jar. This wraps the Java standard Properties object
 * with a constructor that takes a file name.
 * 
 * @author CLARKM
 * @version 1.0
 *
 */
public class PropertyReader  {

	/*
	 * filename to read
	 */
	final static String path = "/com/elsevier/reaxys/";
	final static String configFile = "proxysettings.conf";
	final static String encoding = "UTF-8";
	final static String proxyUserString = "http.proxyUser";
	final static String proxyPasswordString = "http.proxyPassword";

	/**
	 * instantiate the Reader.  Since all of the 'work' is saved in System properties
	 * the object is no longer needed.
	 * 
	 * @throws Exception on error reading config file.
	 */
	public PropertyReader() throws Exception {
		
		final String myPath = Thread.currentThread().getStackTrace()[1].getClassName();
		// pretty sure this works everywhere, windows and linux and mac due to java conventions
		final String pathSep = "/";
		/*
		 * needs leading and trailing slashes. 
		 */
		final String PATH = 
				pathSep 
				+ myPath.substring(0, myPath.lastIndexOf(".")).replaceAll("[.]", pathSep) 
				+ pathSep;
		

		final InputStream fr = ReaxysDataTypes.class.getResourceAsStream(PATH + configFile);
		
		readProperties(fr);
		fr.close();
	}
	
	
	/**
	 * Read configuration file and set system properties.  In addition, create an
	 * Authentication object from the username and password in the properties.
	 * @throws IOException 
	 * 
	 * @throws Exception
	 */
	void readProperties(final InputStream is) throws IOException {
		/*
		 * read properties using standard PropertyReader
		 */
		final Properties settings = new Properties();
		settings.load(is);

		/*
		 * use setProperty to blindly set the system properties from the file, whatever they are
		 */
		for (Entry<Object, Object> entry : settings.entrySet()) {
			System.setProperty((String)entry.getKey(), (String)entry.getValue());
		}

		/*
		 * if these were set, get the values. Otherwise these will be null if 
		 * not set.
		 */
		final String userName = System.getProperty(proxyUserString);
		final String password = System.getProperty(proxyPasswordString);

		/*
		 * set up proxy authentication using the system Authenticator object
		 */
		if (userName != null && password != null) {
			Authenticator.setDefault(
					new Authenticator() {
						public PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(
									userName, password.toCharArray());
						}
					}
					);
		}
	}

}