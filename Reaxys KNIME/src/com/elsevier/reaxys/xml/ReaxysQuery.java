package com.elsevier.reaxys.xml;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import com.elsevier.reaxys.ReaxysDataTypes;
import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysDocument;

/**
 * this class provides an example of an external entry point into the Reaxys query system.
 * 
 * @author CLARKM
 *
 */
public class ReaxysQuery {
	
	ReaxysAPI api;
	String SEP = "\t";  // value separator
	String EOL = "\n";  // end of line indicator
	String URL = "https://www.reaxys.com/reaxys/api";
	String apikey = "apitest_clark";
	String username = "";
	String password = "";
	// replaces the EOL that is in the data itself; that is if the data has newlines
	String replacement_eol = "|";
	// request random sampling
	boolean sampling = false;
	// request that structures are added to results.
	boolean add_structures = false;
	String limit = "10";
	String startRecord = "1";
	String sortString = null;
	boolean always_auth = false;
	String reaxys_fact = "";
	boolean debug = false;
	
	ReaxysDocument ra;
	
	File tempFile = null;
	long lastSessionTime = -1;
	final long sessionLength = 5 * 60 * 1000;  // in milliseconds.
	String sessionID = "ReaxysSession";
	
	/*
	 * environment variables used for query
	 */
	static final String REAXYS_APIKEY = "reaxys_apikey";
	static final String REAXYS_USERNAME = "reaxys_username";
	static final String REAXYS_PASSWORD = "reaxys_password";
	static final String REAXYS_URL = "reaxys_url";
	static final String REAXYS_EOL = "reaxys_eol";
	static final String REAXYS_SAMPLING = "reaxys_sampling";
	static final String REAXYS_SEPARATOR = "reaxys_separator";
    static final String REAXYS_STARTRECORD = "reaxys_startrecord";
	static final String REAXYS_LIMIT = "reaxys_limit";
	static final String REAXYS_SORT =  "reaxys_sort";
	static final String REAXYS_SESSION_ID =  "reaxys_session_id";
	static final String REAXYS_AUTH = "reaxys_always_auth";
	static final String REAXYS_FACT = "reaxys_fact";
	static final String REAXYS_DEBUG = "debug";
	
	// replaces the EOL that is in the data itself; that is if the data has newlines
	static final String REAXYS_REPLACEMENT_EOL = "reaxys_replacement_eol";
	static final String REAXYS_ADD_STRUCTURES = "reaxys_add_structures";
	
	
	ReaxysQuery() {
		
		try {

			username = getProperty(REAXYS_USERNAME, username);
			password = getProperty(REAXYS_PASSWORD, password);
			apikey =   getProperty(REAXYS_APIKEY, apikey);
			URL = getProperty(REAXYS_URL, URL);
			SEP = getProperty(REAXYS_SEPARATOR, SEP);
			EOL = getProperty(REAXYS_EOL, EOL);
			sampling = Boolean.valueOf(getProperty(REAXYS_SAMPLING, String.valueOf(sampling)));
			add_structures = Boolean.valueOf(getProperty(REAXYS_ADD_STRUCTURES, String.valueOf(add_structures)));
			replacement_eol = getProperty(REAXYS_REPLACEMENT_EOL, replacement_eol);
			sortString = getProperty(REAXYS_SORT, sortString);
			sessionID = getProperty(REAXYS_SESSION_ID, sessionID);
			tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + sessionID + ".reaxysjar");
			startRecord = getProperty(REAXYS_STARTRECORD, startRecord);
			limit = getProperty(REAXYS_LIMIT, limit);
			always_auth = Boolean.valueOf(getProperty(REAXYS_AUTH, String.valueOf(always_auth)));
			reaxys_fact = getProperty(REAXYS_FACT, reaxys_fact);
			debug = Boolean.valueOf(getProperty(REAXYS_DEBUG, String.valueOf(debug)));
			
			try {

				ra = (ReaxysDocument) read();
				ra.setDebug(debug);
				ra.getIO().setDebug(debug);
				ra.getIO().init();
				
				
				boolean timedout = System.currentTimeMillis() - lastSessionTime > sessionLength;
				if (timedout) {
					throw new Exception("timed out");
				}
				
			} catch (Exception e) {
				
				ra = new ReaxysDocument(URL, apikey, username, password);
				ra.setDebug(debug);
				
				// proxy authentication, if required
				proxyAuth();

				if (!ra.login()) {
					System.err.println("error logging in to Reaxys server");
					// it didn't work; do it again with debugging on to give more information.
					ra.setDebug(true);
					ra.login();
					System.exit(2);
				}	
			}
			
			// rewrite file with latest time
			serialize(ra);
			api = new ReaxysAPI(null, ra, debug);
			

		} catch (Exception e) {
			System.err.println("ReaxysQuery: " + e);
		}
	}
	
	
	/**
	 * return the default if property not found as an environment variable. Java provides
	 * this function for system properties but not environment variables.
	 * 
	 * @param property  environment variable to find
	 * @param defaultProperty default if not found
	 * @return value
	 */
	String getProperty(String property, String defaultProperty) {
		
		// if defined as a java property e.g. java -D reaxys_limit=2  then use that 
		// in preference.
		if (System.getProperty(property) != null) {
			
			String result =  System.getProperty(property);
			if (debug) System.out.println("set " + property + " = " + result);
			return result;
		}
		
		String result = System.getenv(property) == null ? defaultProperty : System.getenv(property);
		
		if (debug) System.out.println("set " + property + " = " + result);
		return result;
	}
	
	
/**
 * main.  expects string like  bioactivity dat.tname='h3' and dat.paureus > 6  where the item before the space is the 
 * data type, and the string after the space is the query.
 * 
 * It will process a list of such queries and write all data in tabular form to standard output.
 * 
 * @param args  not used
 * @throws IOException on error
 * @throws CanceledExecutionException
 */
	public static void main(String[] args) throws Exception {
		
		final ReaxysQuery rq = new ReaxysQuery();
		
		String[] data = null;
		//args = new String[] {"PC", "IDE.PID > 0" };
		//args = new String[] {"PC", "IDE.XRN > 0" };
		if (args.length == 0 && data == null) {

			// read in all lines and execute queries.  However, each query will make a new output table that is sent to the
			// same output stream
			final String input = rq.readStream(System.in);
			data = input.split("\\s+", 2);
		} else {
			data = args;
		}
		
		final List<HashMap<String, String>> result = rq.makeQuery(data[0], data[1]);
		rq.writeResult(result);
		result.clear();

	}
	
	
	/**
	 * end session
	 * @return true if logged out, false if there was no session.
	 */
	public boolean logout() {
		return ra.logout();
	}
	
	
	/**
	 * perform a query and return the result as a list of hashmaps
	 * 
	 * @param query  Reaxys advanced query
	 * @param fact fact name to request
	 * @return list of hashmaps
	 * @throws Exception on error
	 */
	public List<HashMap<String, String>>makeQuery(final String fact, final String query) throws Exception {
				
		final ReaxysDataTypes rdt = ReaxysDataTypes.getByName(fact);
		
		if (rdt == null) {
			System.err.println("data type not found: " + fact);
			System.exit(1);
		}
		
		int max = Integer.valueOf(limit);
		int min = Integer.valueOf(startRecord);
		
		final List<HashMap<String, String>> result = api.getFact(
				"",  // id field for search
				"",  // value for id field
				query, 
				rdt,
				min,
				max,
				sampling,
				add_structures,
				false,
				sortString);
		
		return result;
		
	}

	/**
	 * write result data to a stream for output.
	 * 
	 * @param result - output of query.
	 * @throws IOException on io error
	 */
	void writeResult(final List<HashMap<String, String>> result) throws IOException {
		
		final Set<String> columns = ReaxysAPI.keySet(result);
		final BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
		int numCols = columns.size();

		int cols = 0;
		
		// write headers
		for (String header : columns) {
			bw.write(header);
			cols++;
			if (cols < numCols) {
				bw.write(SEP);
			}
		}
		
		bw.write(EOL);
		
		// write data
		for (final HashMap<String, String> map : result) {
			cols = 0;
			
			for (final String header : columns) {
				String col = map.get(header);
				// replace newlines in data with something.
				if (col != null && !EOL.equals(replacement_eol)) {
					bw.write(map.get(header).replaceAll(EOL, replacement_eol));
				}
				cols++;
				
				if (cols < numCols) {
					bw.write(SEP);
				}
			}
			
			bw.write(EOL);
		}
		
		bw.close();
		
	}
	
	
	/**
	 * read an input stream and return a string as contents. Specify UTF-8 charset
	 * 
	 * @param is
	 * @return string from input stream.
	 */
	final String readStream(InputStream is) {
		
	    final Scanner s = new Scanner(is, "UTF-8");
	    s.useDelimiter("\\A");
	    final String result = s.hasNext() ? s.next() : "";
	    s.close();
	    return result;
	}

	
	/**
	 * serialize the object to the temporary file
	 * @param o object to write
	 * @throws IOException
	 */
	public void serialize(Object o) throws IOException {
		

		final FileOutputStream f_out = new  FileOutputStream(tempFile);
		final ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
		obj_out.writeObject(System.currentTimeMillis());
		obj_out.writeObject ( o );
		
		obj_out.close();
		f_out.close();
	}
	
	
	
	/**
	 * read an object from the temporary file
	 * 
	 * @return the object read
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Object read() throws IOException, ClassNotFoundException {
		
		final FileInputStream f_in = new FileInputStream(tempFile);
		final ObjectInputStream obj_in = new ObjectInputStream(f_in);
		lastSessionTime = (Long) obj_in.readObject();
		final Object result =  obj_in.readObject();
		
		obj_in.close();
		f_in.close();
		
		return result;
	}
	
	
	/**
	 * set up proxy authentication
	 */
	public void proxyAuth() {
		
		// proxy authentication, if required
		final String proxy_user = System.getProperty("http.proxyUser");
		final String proxy_password = System.getProperty("http.proxyPassword");

		if (proxy_user != null && proxy_password != null) {
			Authenticator.setDefault(new ProxyAuthenticator(proxy_user, proxy_password));
		}
	}
	
	
	/**
	 * authenticator for proxy
	 * 
	 * @author clarkm
	 *
	 */
	public class ProxyAuthenticator extends Authenticator {

	    private String userName, password;

	    protected PasswordAuthentication getPasswordAuthentication() {
	        return new PasswordAuthentication(userName, password.toCharArray());
	    }

	    public ProxyAuthenticator(final String userName, final String password) {
	        this.userName = userName;
	        this.password = password;
	    }
	}
}
