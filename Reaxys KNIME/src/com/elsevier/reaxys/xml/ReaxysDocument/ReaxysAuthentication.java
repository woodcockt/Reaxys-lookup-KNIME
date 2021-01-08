package com.elsevier.reaxys.xml.ReaxysDocument;

import java.io.Serializable;

import com.elsevier.reaxys.xml.ReaxysDocument.ReaxysAuthentication;
import com.elsevier.reaxys.xml.utils.IO;

/**
 * class implements a container for authentication information
 * 
 * @author CLARKM
 * 
 */
public class ReaxysAuthentication implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2674947485798667778L;
	private String url;
	private String apikey;
	private String username;
	private String password;
	private IO io;
	/**
	 * copy constructor
	 * 
	 * @param ra
	 *            ReaxysAuthentication document to copy
	 */
	public ReaxysAuthentication(final ReaxysAuthentication ra) {
		this.setUrl(ra.getUrl());
		this.setApikey(ra.getApikey());
		this.setUsername(ra.getUsername());
		this.setPassword(ra.getPassword());
		io = ra.io;
	}

	public IO getIO() {
		return io;
	}
	
	public void clearCookies() {
		if (io != null) {
			io.clearCookies();
		}
	}
	/**
	 * create authentication document
	 * 
	 * @param url
	 *            connection url
	 * @param apikey
	 *            apikey for connection
	 * @param username
	 *            optional username
	 * @param password
	 *            optional password
	 */
	public ReaxysAuthentication(String url, String apikey, String username,
			String password) {
		this.setUrl(url);
		this.setApikey(apikey);
		this.setUsername(username);
		this.setPassword(password);
		if (io == null) {
			io = new IO();
		}
	}

	/**
	 * anonymous login using only url and apikey
	 * 
	 * @param url
	 * @param apikey
	 */
	public ReaxysAuthentication(String url, String apikey) {
		this.setUrl(url);
		this.setApikey(apikey);
	}

	/**
	 * get the access url
	 * @return String url used for connection
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * set the access url for connection
	 * @param url String Url for the connection
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * get the apiKey used for authentication.
	 * 
	 * @return String apikey
	 */
	public String getApikey() {
		return apikey;
	}

	/**
	 * set the apikey used for authentication
	 * 
	 * @param apikey String api key
	 */
	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	/**
	 * get the (optional) username used to authenticate.
	 * 
	 * @return String username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * set the (optional) username used to authenticate
	 * @param username String username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * get the password used to authenticate.
	 * 
	 * @return String password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * set the password used to authenticate
	 * 
	 * @param password String password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

}
