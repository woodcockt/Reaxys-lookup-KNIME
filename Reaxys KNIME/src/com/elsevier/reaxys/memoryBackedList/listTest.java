package com.elsevier.reaxys.memoryBackedList;

import java.util.ArrayList;
import java.util.Random;

public class listTest {
	
	public static void main(String[] args) {
		
		MemoryBackedList<Double> test = new MemoryBackedList<Double>();
		
		for (int i = 0; i < 100; i++)  {
			test.add( (double)i);
		}
		
		ArrayList<Double> c = new ArrayList<Double>();
		c.add(1.0);
		c.add(2.0);
		
		test.removeAll(c);
		
		Random rand = new Random();
		
		
		for (int i = 0; i < 90; i++) {
			int r = rand.nextInt(test.size());
			System.out.println("removing entry " + r);
			test.remove(new Double(r));
		}
		
		for (int i = 0; i < 5; i++)  {
			test.add( (double)i);
		}
		
		
		for (int i = 0; i < test.size(); i++) {
			System.out.println("index  " + i + " value " + test.get(i));
		}
		
		for( Object o : test) {
			System.out.println(o);
		}
		
	}

}
