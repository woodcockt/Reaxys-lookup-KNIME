package com.elsevier.reaxys;

import java.util.LinkedHashMap;
import java.util.Map;

	public class Cache<K, V> extends LinkedHashMap<K, V> {
	    /**
		 * 
		 */
		private static final long serialVersionUID = -7904753309605084671L;
		private final int maxSize;
		private static final int DEFAULT_SIZE = 9000; // can't go over 9000

	    public Cache(int maxSize) {
	        this.maxSize = maxSize;
	    }
	    
	    public Cache() {
	    	this(DEFAULT_SIZE);
	    }

	    @Override
	    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
	        return size() > maxSize;
	    }
	    
	    
	    /**
	     * use this to make a cache of canonical values
	     * @param key key/value for canonicalization
	     * @return a canonicalized value
	     */
	    @SuppressWarnings("unchecked")
		public V canon(K key) {
	    	V result = get(key);
	    	if (result == null) {
	    		put(key, (V) key);
	    		return (V) key;
	    	} else {
	    		return result;
	    	}
	    }
	}

