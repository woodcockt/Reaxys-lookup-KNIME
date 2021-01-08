package com.elsevier.reaxys.xml.ReaxysDocument;

import java.lang.reflect.Method;
import java.util.Random;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysAuthentication;
import com.elsevier.reaxys.xml.utils.IO;

/**
 * document encapsulates a Reaxys query XML document, creating structure
 * specific to Reaxys queries.
 * 
 * @author CLARKM
 * 
 */
public class ReaxysDocument extends ReaxysAuthentication {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 523482527194555066L;

	/**
	 * constructor using full information
	 * 
	 * @param url  Reaxys url to contact
	 * @param apikey apikey required for login
	 * @param username optional username
	 * @param password optional password
	 */
	public ReaxysDocument(String url, String apikey, String username,
			String password) {
		super(url, apikey, username, password);
	}

	/**
	 * constructor for anonymous (IP address based) login
	 * 
	 * @param url  Reaxys url to contact
	 * @param apikey apikey required for login
	 */
	public ReaxysDocument(String url, String apikey) {
		super(url, apikey);
	}

	
	/**
	 * copy constructor
	 * @param ra ReaxysAuthentication object to copy
	 */
	public ReaxysDocument(ReaxysAuthentication ra) {
		super(ra);
	}

	/*
	 * enumerate document types that can be automatically created by name
	 */
	public enum DocTypes {

		login("login", "createLoginDocument"), logout("logout",
				"createLogoutDocument"), search("search",
				"createSearchDocument"), retrieve("retrieve",
				"createRetrieveDocument");

		String type;
		String method;

		DocTypes(String t, String m) {
			type = t;
			method = m;
		}

		/**
		 * get an XML document corresponding to the type
		 * 
		 * @param rdf
		 * 
		 * @param type
		 * @return Document
		 */
		static String getMethodName(final String type) {

			for (final DocTypes t : values()) {
				if (t.type.equals(type)) {
					return t.method;
				}
			}
			return null;
		}

	} // end of enumeration

	/**
	 * return a document of the specified type.
	 * 
	 * @param type
	 *            - method name from the type enumeration
	 * @return an XML document framework of that type.
	 */
	public Document createDocument(final String type) {

		final String method = DocTypes.getMethodName(type);

		try {
			final Method m = this.getClass().getMethod(method);
			return (Document) m.invoke(this, (Object[]) null);

		} catch (final Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * create template Reaxys document
	 * 
	 * @return template reaxys document
	 * @throws ParserConfigurationException
	 *  
	 *  Added mode=async   TW
	 */
	Document createReaxysDoc(final String commandString) {

		try {
			final Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			
			final Element rootElement = doc.createElement("xf");
			doc.appendChild(rootElement);

			createElement(doc, "xf", "request");
			setAttribute(doc, "request", "caller", getApikey());
			createElement(doc, "request", "statement");
			setAttribute(doc, "statement", "command", commandString);
			setAttribute(doc, "statement", "mode", "async");

			return doc;

		} catch (final ParserConfigurationException e) {
			System.err.println("createReaxysDoc: error creating new document");
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * get a value from a document
	 * 
	 * @param tagName
	 *            tag
	 * @param element
	 *            document
	 * @return value value of the first occurence of the tag in the argument document
	 */
	public String getString(final String tagName, final Document element) {

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
	 * set the text value of a clause
	 * 
	 * @param doc
	 *            document
	 * @param clause
	 *            node to set text to
	 * @param text
	 *            value to set to
	 * @return document
	 */
	public  Document setTextNode(final Document doc, final String clause,
			final String text) {

		final NodeList list = doc.getElementsByTagName(clause);
		final Node where = list.item(list.getLength() - 1);
		where.appendChild(doc.createTextNode(text));
		return doc;
	}

	/**
	 * set an attribute of an XML element, e.g. attrib=value.
	 * 
	 * @param doc
	 *            document
	 * @param parent
	 *            parent node, the one to get the attribute
	 * @param attrib
	 *            attribute
	 * @param value
	 *            value
	 * @return the changed document
	 */
	public  Document setAttribute(final Document doc, final String parent,
			final String attrib, final String value) {

		final Element where = (Element) doc.getElementsByTagName(parent)
				.item(0);
		final Attr key = doc.createAttribute(attrib);

		key.setValue(value);
		where.setAttributeNode(key);

		return doc;
	}
	
	/**
	 * remove an XML element
	 * 
	 * @param doc
	 *            document
	 * @param node
	 *            node to remove
	 * @return the changed document
	 */
	public  Document removeNode(final Document doc, final String node) {

		Element where = (Element) doc.getElementsByTagName("request").item(0);
		Node child = where.getElementsByTagName(node).item(0);
		where.removeChild(child);
		
		return doc;
	}

	/**
	 * create a new XML element in a document.
	 * 
	 * @param doc
	 *            document
	 * @param parent
	 *            parent node where the new element will be placed
	 * @param name
	 *            name of new node
	 * @return changed document
	 */
	public  Document createElement(Document doc, String parent,
			String name) {

		final NodeList list = doc.getElementsByTagName(parent);
		final Node pnode = list.item(list.getLength() - 1);
		final Element where = doc.createElement(name);
		pnode.appendChild(where);

		return doc;
	}

	/**
	 * Create a login document. This uses the 'anonymous user' and
	 * uname/password can be added.
	 * 
	 * @return Document used for connecting a session
	 */
	public Document createLoginDocument() {

		final Document doc = createReaxysDoc("connect");

		doc.normalizeDocument();

		return doc;
	}

	/**
	 * Create a document to disconnect the session.
	 * 
	 * @return XML Document that will disconnect the session
	 */
	public Document createLogoutDocument() {

		final Document doc = createReaxysDoc("disconnect");
		return doc;
	}

	/**
	 * Create a search document. The criteria for the request must be added to
	 * this document.
	 * 
	 * Added order_by_clause - MC
	 * 
	 * @return XML document with basic fields for search
	 */
	public Document createSearchDocument() {

		final Document doc = createReaxysDoc("select");
		createElement(doc, "request", "from_clause");
		createElement(doc, "request", "where_clause");
		createElement(doc, "request", "order_by_clause");
		createElement(doc, "request", "options");

		doc.normalizeDocument();

		return doc;
	}

	/**
	 * Create a results retrieval request document. The criteria for the request
	 * must be added to this document.
	 * 
	 * @return XML document with fields for retrieval.
	 */
	public Document createRetrieveDocument() {

		final Document doc = createReaxysDoc("select");

		createElement(doc, "request", "select_list");
		createElement(doc, "request", "from_clause");
		createElement(doc, "request", "where_clause");
		createElement(doc, "request", "options");

		doc.normalizeDocument();
		return doc;

	}
	
	
	/**
	 * logout
	 * 
	 * @return true
	 */
	public boolean logout() {
 
		if (!getIO().hasSession()) return false;
		
		final Document doc = createDocument("logout");
		request(doc);
		System.out.println(getIO().report());
		getIO().reset();

		return true;
	}
	
	
	public static final String STATUS_FLAG = "status";
	public static final String OK = "ok";

	/**
	 * login using a Reaxys document.
	 * 
	 * @return true if the login was successful
	 */
	public boolean login() {

		int tries = 0;
		final int maxTries = 4;
		Random rand = new Random();
		/* make sure the session is ended otherwise this will fail */
		if (getIO().hasSession()) {
			logout();
		}

		final Document doc = createDocument("login");
		Document loginResult = null;

		if (getUsername() != null && getPassword() != null) {
			setAttribute(doc, "statement", "username",
					getUsername());
			setAttribute(doc, "statement", "password",
					getPassword());
		}

		// try more than once; occassionally there are issues logging in
		while (tries < maxTries) {
			
			loginResult = request(doc);
			
			// check status, and return if it looks ok.
			if (loginResult != null
					&& IO.getString("status", loginResult).toLowerCase()
					.equals("ok")) {

				return true;
			}
			final long sleepTimeMs = 1000 + rand.nextInt(2000); // random interval between 1 and 3 seconds.
			try {Thread.sleep(sleepTimeMs); } catch (InterruptedException e) {}
			tries++;
			getIO().clearCookies();
			if (getIO().getDebug()) return false;
		}

		return false;
	}	
	/**
	 * Send an XML query to the server, and return the result as an XML document
	 * 
	 * @param doc XML query or command
	 * @return response as an XML document
	 */
	public Document request(Document doc) {
		return getIO().request(doc, getUrl());
	}
	
	/**
	 * set the debug flag for IO operations
	 * 
	 * @param debug if true enables more verbose output of the XML conversation between this 
	 * process and the server.
	 */
	public void setDebug(final Boolean debug) {
		getIO().setDebug(debug);
	}

}
