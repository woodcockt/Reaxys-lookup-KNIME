package com.elsevier.reaxys.memoryBackedList;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * this class implements a memory-backed arraylist that can reduce memory by writing the
 * data to a memory-mapped file instead of storing the objects in normal memory.
 * 
 * @author clarkm
 *
 * @param <E>
 */
public class MemoryBackedList<E extends Serializable> implements List<E>, Cloneable {

	private DataStore storage = new DataStore();
	/**
	 * 
	 */
	//private static final long serialVersionUID = -1519085539375240223L;
	
	/**
	 * close the storage file and clear the array.
	 */
	public void clear() {
		try {
			storage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean contains(Object o) {
		for (E item: this) {
			if (item.equals(o)) return true;
		}
		return false;
	}

	
	public Object[] toArray() {
		Object[] list = new Object[size()];
		for (int i = 0; i < size(); i++) {
			list[i] = get(i);
		}
		return list;
	}
	
	
	public boolean remove(Object o) {
		int index = this.indexOf(o);
		if (index != -1) {
			remove(index);
			return true;
		}
		
		return false;
	}
	
	
	public boolean isEmpty() {
		return storage.size() == 0;
	}
	
	
	public int size() {
		return storage.size();
	}
	
	public boolean add(E object) {

		ByteArrayOutputStream bos = null;
		ObjectOutput out = null;
		
		try {
			bos = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.flush();
			storage.write(bos.toByteArray());

		} catch (Exception e) {
			System.err.println("error adding object to MemoryBackedArrayList: " + e);
			return false;
		} finally {
			try {
				out.close();
				bos.close();
			} catch(Exception e2) {}
		}
		return true;
	}
	
	
	public boolean addAll(Collection<? extends E> coll) {
		
		if (coll.size() == 0) return false;
		for (E item : coll) {
			add(item);
		}
		return true;
	}
	
	
	@SuppressWarnings("unchecked")
	public E get(int index) {
		
		E result = null;
		ObjectInput in = null;
		
		try {
			final byte[] objbytes = storage.read(index);
			final ByteArrayInputStream bis = new ByteArrayInputStream(objbytes);
			in = new ObjectInputStream(bis);
			result = (E) in.readObject();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				in.close();
			} catch (Exception e2) {}

		}
		
		return result;
	}
	
	
	public Iterator<E> iterator() {
        return new mbaIterator<E>(this);
    }
	
	
	public class mbaIterator <T extends Serializable> implements Iterator<T> {

		private int counter = 0;
		private MemoryBackedList<T>list = null;
		
	    @SuppressWarnings("unchecked")
		public mbaIterator(MemoryBackedList<T> memoryBackedList) {
			list = (MemoryBackedList<T>) memoryBackedList.clone();
		}

		public boolean hasNext() {
	        return counter < list.size();
	    }

		public T next() {
	    	return list.get(counter++);
	    }

	}


	public void close() {
		try {
			storage.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	
	public boolean containsAll(Collection<?> c) {
		
		for (Object o : c) {
			if (!contains(o)) return false;
		}
		return true;
	}

	public Object clone() {
		return this;
	}
	
	
	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		
		boolean flag = false;
		for (Object o : c) {
			if (contains(o)) {
				remove(o);
				flag = true;
			}
		}
		return flag;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		E value = get(index);
		storage.delete(index);
		return value;
	}

	@Override
	public int indexOf(Object o) {
		
		for (int i = 0; i < size(); i++) {
			if (get(i).equals(o)) return i;
		}
		
		return -1;
	}

	@Override
	public int lastIndexOf(Object o) {
		
		for (int i = size() - 1 ; i > -1; i--) {
			if (get(i).equals(o)) return i;
		}
		return -1;
	}

	
	@Override
	public ListIterator<E> listIterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		
		List<E> result = new MemoryBackedList<E>();
		
		for (int i = fromIndex; i < toIndex && i < size(); i++) {
			result.add(get(i));
		}
		
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T[] toArray(T[] array) {
		
		if (array.length < size()) {
			array = (T[])new Object[size()];
		}
		
		for (int i = 0; i < array.length; i++) array[i] = null;
		
		for (int i = 0; i < size(); i++) {
			array[i] = (T) get(i);
		}
		
		return array;
	}


}
