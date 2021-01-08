package com.elsevier.reaxys.memoryBackedList;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 * class implements a data storage mechanism for objects written to a memory mapped file for high
 * performance reading/writing.
 * 
 * @author clarkm
 *
 */
public class DataStore {
	
	private ArrayList<indexEntry> index = new ArrayList<indexEntry>();
	private RandomAccessFile randomAccessFile = null;
	private File backingFile = null;
	
	void init() {
		try {
			backingFile = File.createTempFile("DataStore", "ser");
			randomAccessFile = new RandomAccessFile(backingFile, "rw");
		} catch (Exception e) {
			System.err.println("error creating data store: " + e);
		}
	}
	
	
	/**
	 * close the file, and try to delete after a delay for the gc to 
	 * take place.
	 * 
	 * @throws IOException
	 */
	void close() throws IOException {
		
		if (randomAccessFile != null) randomAccessFile.close();
		if (index != null) index.clear();
		if (backingFile != null && backingFile.exists()) backingFile.delete();
	}
	
	
	/**
	 * return number of entries in this file
	 * 
	 * @return number of entries.
	 */
	int size() {
		return index.size();
	}
	
	
	void delete(final int entry) {
		index.remove(entry);
	}
	
	/**
	 * read an entry from the file and return the bytes
	 * @param entry serial number of object to get
	 * @return the object, serialized in bytes
	 * 
	 * @throws IOException
	 */
	final byte[] read(int entry) throws IOException {
		
		final long position = index.get(entry).position;
		final long size = index.get(entry).size;
		final byte[] objbytes = new byte[(int) size];

		randomAccessFile.seek(position);
		randomAccessFile.readFully(objbytes);
        
		return objbytes;
	}
	
	/**
	 * write a serialized object to the memory mapped file as an array of bytes
	 * @param bytes bytes to write
	 * @throws IOException
	 */
	final void write(final byte[] bytes) throws IOException {
		
		// lazy initialize
		if (randomAccessFile == null) {
			init();
		}
		
		long position = 0;
		long size = bytes.length;
			
		if (!index.isEmpty()) {
			final indexEntry lastWritten = index.get(index.size() - 1);
			position = lastWritten.position + lastWritten.size + 1;
		}
		
		// check file space left.
		if ( size > backingFile.getUsableSpace()) {
			throw new IOException("file system out of space for backing file");
		}
		
		randomAccessFile.seek(position);
		randomAccessFile.write(bytes);
		
        index.add(new indexEntry(position, size));
	}
	
	/**
	 * index for the memory mapped file with starting positions and sizes of the objects in the file
	 * @author clarkm
	 *
	 */
	private class indexEntry {
		long position;
		long size;
		
		indexEntry(long position, long size) {
			this.position = position;
			this.size = size;
		}
	}
}