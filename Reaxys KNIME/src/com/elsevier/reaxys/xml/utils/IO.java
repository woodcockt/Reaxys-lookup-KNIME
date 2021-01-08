package com.elsevier.reaxys.xml.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.knime.core.node.NodeLogger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * this class handles the IO to and from the webserver, including cookie
 * management.
 * 
 * @author clarkm
 * 
 */
public class IO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3984819334373039648L;
	private  transient Transformer transformer = null;
	private  transient DocumentBuilder docBuilder = null;  // not serializable
	private  long bytesRead = 0;
	private  long bytesWritten = 0;
	private  long requests = 0;
	private  long elapsedTime = 0;
	
	/*
	 * map for cookies. We handle cookies here instead of the system cookie handler so that multiple instances
	 * of this node in the same JVM can have their own session cookies.  Otherwise the nodes interfere with each other
	 * by over-writing the session id's
	 */
	private Map<String, List<String>> headers;


	/* text encoding specified here */
	public static final String TEXT_ENCODING = "utf-8";
	
	/* timeout for reading data from Reaxys, in milliseconds */
	public static final int READ_TIMEOUT_MS = 300 * 1000;

	/*
	 * backstop for reading 'endless' stream; maximum size of document to read
	 * 100M
	 */
	public static final int MAX_DOCUMENT_SIZE = 1024 * 1024 * 100;

	// the logger instance
	private static NodeLogger logger = null;


	public IO() {
		headers = null;
		init();
		try {
		  logger = NodeLogger.getLogger(IO.class);
		} catch (Exception logger) {
			// We are not running in KNIME
		} catch(NoClassDefFoundError e) {
			  //ignore this too
		}
	}
	
	/**
	 * clear the cookies for this session in preparation for a new session
	 */
	public void clearCookies() {
		headers = null;
	}
	
	/**
	 * return true if there appears to be an active session
	 * 
	 * @return true if headers suggest a session is active
	 */
	public boolean hasSession() {
		return (headers != null);
	}
	/**
	 * get the count of bytes read for this IO object
	 * @return long count of bytes read from the URL
	 */
	long getBytesRead() {
		return bytesRead;
	}

	/**
	 * get count of bytes written by the IO object
	 * 
	 * @return long count of bytes written to the URL
	 */
	long getBytesWritten() {
		return bytesWritten;
	}

	/**
	 * get count of requests (searches or logins)
	 * 
	 * @return long count of requests
	 */
	long getRequests() {
		return requests;
	}
	
	/**
	 * get total elapsed time between request and responses
	 * 
	 * @return long total elapsed time in milliseconds
	 */
	long getElapsed() {
		return elapsedTime;
	}

	/**
	 * reset the collected IO statistics
	 */
	public  void reset() {
		bytesRead = 0;
		bytesWritten = 0;
		requests = 0;
		elapsedTime = 0;
		clearCookies();
	}

	/**
	 * report IO statistics; XML request/responses, and bytes read/written.
	 * 
	 * @return string with a 1-line report.
	 */
	public String report() {

		final String rep = String
				.format("---- Session IO Summary: %4d XML requests %5dk written %5dk read %5.2f seconds",
						getRequests(), getBytesWritten() / 1024,
						getBytesRead() / 1024, getElapsed() / 1000.0);

		if (logger != null) {
			logger.info(rep);
		}

		if (debug) {
			return rep;
		} else {
			return "";
		}
	}

	// debug flag
	boolean debug = false;

	/**
	 * set the debug flag for this class
	 * 
	 * @param value
	 *            true/false
	 */
	public void setDebug(boolean value) {
		debug = value;
	}

	/**
	 * initialize XML and other static stuffs.
	 */
	public void init() {
		
		try {
			
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, TEXT_ENCODING);

			// make xml pretty for debugging
			if (debug) {
				/*
				 * make it look pretty for debug, otherwise don't add more
				 * characters for formatting
				 */
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				// transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
				// "https://www.reaxys.com/xfserv/rx.dtd");
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "2");
			} else {
				
				transformer.setOutputProperty(OutputKeys.INDENT, "no");
				transformer.setOutputProperty(
						"{http://xml.apache.org/xslt}strip-space", "*");
			}

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			dbf.setValidating(false);
			dbf.setIgnoringElementContentWhitespace(true);
			docBuilder = dbf.newDocumentBuilder();

			

		} catch (final Exception e) {
			System.err.println("IO error: " + e);
			if (logger != null) logger.error("IO error:" + e);
		}

	}

	/**
	 * return string representation of XML document
	 * 
	 * @param doc XML document object
	 * @return string with document
	 */
	public final String docToString(final Document doc) {
		
		try {
			doc.normalize();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(new DOMSource(doc), new StreamResult(baos));

			return baos.toString(TEXT_ENCODING);

		} catch (final Exception e) {
			if (logger != null) logger.error("docToString:" + e);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * send the XML document to the URL and return the response as a Document.  If debug is
	 * true, write out the XML conversation to the log, obscuring the api key.
	 * 
	 * @param XML
	 *            object with query
	 * @return XML object with response to query
	 */
	public  Document request(final Document doc, String url) {

		String resp = null;
		
		try {

			final String text = docToString(doc);

			if (debug) {
				final String fixedText = text; //.replaceFirst("caller=\"[^\"]*[\"]", "caller=XXXXXXX");
				System.out.println("\n-------- request -----------");
				System.out.println(fixedText);
				if (logger != null) logger.info("\n-------- request -----------\n" + fixedText);
			}

			resp = request(text, url);

			if (debug) {
				final String fixedText = resp; //.replaceFirst("caller=\"[^\"]*[\"]", "caller=XXXXXXX");

				System.out.println("\n-------- response -----------");
				System.out.println(fixedText);
				if (logger != null) logger.info("\n-------- response -----------\n" + fixedText);
			}

			if (resp != null) {
				return stringToDoc(resp);
			}

		} catch (final IOException e) {
			if (logger != null) logger.error("IO.Request : " + e);
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			if (logger != null) logger.error("IO.Request : " + e);
			e.printStackTrace();
		} catch (final SAXException e) {
			if (logger != null) logger.error("IO.Request : " + e);
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * parse the string and return an XML document
	 * 
	 * @param xmlString
	 *            string to turn into XML document
	 * @return XML document
	 * 
	 * @throws ParserConfigurationException
	 *             if string is not an XML document.
	 * @throws SAXException
	 *             if string is not an XML document.
	 * @throws IOException
	 *             if string is not an XML document.
	 */
	Document stringToDoc(final String xmlString)
			throws ParserConfigurationException, SAXException, IOException {

		final InputSource is = new InputSource(new StringReader(xmlString));
		
		Document result = null;
		
		try {
			result = docBuilder.parse(is);
		} catch (Exception e) {
			if (logger != null) logger.error("docToString: " + e);
		}

		return result;
	}

	/**
	 * create an https connection to the given URL
	 * 
	 * @param reaxys string with URL
	 * @return open HTTPS connection 
	 * @throws IOException on error
	 */
	HttpsURLConnection makeConnection(String reaxys) throws IOException {
		
		final String METHOD = "POST";
		final URL url = new URL(reaxys);
		final HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
		
        // use the session headers to set the authentication cookies for this session
        if (headers != null) {
        	String cookieString = "";
        	for (final String cookie : headers.get("Set-Cookie")) {
        		cookieString += cookie.substring(0, cookie.indexOf(";")) + ";";
        	}
        	con.setRequestProperty("Cookie", cookieString);
        }
        
		con.setRequestMethod(METHOD);
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setUseCaches(true);
		con.setReadTimeout(READ_TIMEOUT_MS);
		// encourage use of desired encoding
        con.setRequestProperty("content-type", "text/plain; charset=" + TEXT_ENCODING);
        con.setRequestProperty("accept-charset", TEXT_ENCODING);	
        con.connect();
        return con;
	}
	
	/**
	 * send the text string to the url and return the response string
	 * 
	 * @param text
	 *            XML query
	 * @return text returned after posting the argument text to the url.
	 */
	public String request(final String text, final String reaxys) {

		requests++;
		final cpuTimer timer = new cpuTimer();
		DataOutputStream os = null;
		BufferedInputStream inputStream = null;
		HttpsURLConnection con = null;

		try {
			
			con = makeConnection(reaxys);
			os = new DataOutputStream(con.getOutputStream());
			final byte[] data = text.getBytes();
			bytesWritten += data.length;
			os.write(data);
			os.close();
			if (logger != null) logger.info("*send request " + String.format("%7d", data.length) + " bytes: " + timer.elapsedSeconds());

			timer.reset();

			if (headers == null) {
				headers = con.getHeaderFields();
			}

			String response = "";
			
			try {
				
				inputStream = new BufferedInputStream(con.getInputStream());
				response = readStream(inputStream);
				
			} catch (final IOException e) {
				if (logger != null) logger.error("\nIO.request: " + e 
						+ "\n ------- request:\n"
						+ text
						+ "\n--------\n"
						+ " after " + timer.elapsedSeconds() + " s\n"
						);
				return "";
			}

			if (logger != null) logger.info("*get response " + String.format("%7d", response.length()) + " bytes: " + timer.elapsedSeconds());

			return response;

		} catch (final IOException e) {
			if (logger != null) logger.error("IO.request error making request: " + e
					+ "\n ----- request:\n"
					+ text
					+ "\n--------\n"
					+ " after " + timer.elapsedSeconds() + " s\n"
					);
			e.printStackTrace();
			
		} finally {
			
			elapsedTime += timer.getElapsedTime() * 1000; // nanos to millis
			
			// these may be closed already, unless exit via error
			try { inputStream.close(); } catch (Exception i) {}
			if (con != null) {
				try { con.disconnect(); } catch(Exception d) {
					if (logger != null) logger.error("failed to diconnect session: " + con);
				}
			}
			
		}

		return null;
	}




	/**
	 * read a stream and return a string with its contents. If the stream blocks
	 * this will wait until it is not blocked. Close at end of operation because we have
	 * read all of the available bytes from the stream.
	 * 
	 * A maximum size is set to avoid reading very large streams.
	 * 
	 * @param inputStream
	 *            stream to read
	 * @return string generated from bytes read from the stream, using UTF-8 encoding
	 * @throws UnsupportedEncodingException 
	 */
	String readStream(final InputStream inputStream)  {

		if (inputStream == null) {
			return "";
		}

		/* buffer size for reading data */
		final int bufsize = 8192;
		final byte[] buffer = new byte[bufsize];
		final int EOF = -1;
		
		int localBytesRead = 0;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(bufsize);

		try {

			int len;
			
			while ((len = inputStream.read(buffer)) != EOF
					&& localBytesRead < MAX_DOCUMENT_SIZE) {
				
				baos.write(buffer, 0, len);
				localBytesRead += len;
			}

			// total bytes read by this class
			bytesRead += localBytesRead;

			if (localBytesRead >= MAX_DOCUMENT_SIZE) {
				if (logger != null) logger.error("readStream: reached max doc size "
						+ MAX_DOCUMENT_SIZE);
			}
			

			/*
			 * the unicode micro symbol in RMC is preceeded by some other character which needs to be removed.
			 * this is a silly, time consuming, and memory intensive fix.
			 */
			final String moji = new String(new byte[] {(byte) 0xC3, (byte) 0x82, (byte) 0xC2, (byte) 0xB5}, TEXT_ENCODING);
			final String fixedMoji = new String(new byte[] {(byte) 0xC2, (byte) 0xB5}, TEXT_ENCODING);
			final String result = baos.toString(TEXT_ENCODING).replaceAll(moji, fixedMoji);

			return result;

		} catch (final IOException e) {
			if (logger != null) logger.error("IO.readStream :" + e);
				
		} finally {
			
			baos.reset();
			
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;

	}
	
	
	/**
	 * write to a stream aIf the stream blocks
	 * this will wait until it is not blocked. Leave stream open at end of operation in case
	 * other calls want to write more data.
	 * 
	 * @param outputStream
	 *            stream to write to
	 * @return string of bytes read from the stream.
	 */
	public void writeStream(final OutputStream outputStream, final String data) {

		if (outputStream == null || data == null || data.equals("")) {
			return;
		}

		try {
			final byte[] byteArray = data.getBytes(TEXT_ENCODING);
			bytesWritten += byteArray.length;
			outputStream.write(byteArray);
			outputStream.close();

		} catch (final IOException e) {
			if (logger != null) logger.error("IO.writeStream: " + e);
		}
	}

	/**
	 * get a value from a document by traversing the structure
	 * 
	 * @param tagName
	 *            tag tag to get
	 * @param element
	 *            document
	 * @return value text value of tag
	 */
	public static String getString(final String tagName, final Document element) {

		final NodeList list = element.getElementsByTagName(tagName);

		if (list != null && list.getLength() > 0) {

			final NodeList subList = list.item(0).getChildNodes();

			if (subList != null && subList.getLength() > 0) {
				return subList.item(0).getNodeValue();
			}
		}

		return null;
	}

	/**
	 * return value of debug flag
	 * @return true if debug output is enabled
	 */
	public boolean getDebug() {
		return debug;
	}
}
