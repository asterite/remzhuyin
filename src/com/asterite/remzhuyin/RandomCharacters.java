package com.asterite.remzhuyin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RandomCharacters {
	
	private final static String PREF_SEED = "random:seed";
	private final static String PREF_INDEX = "random:index";
	
	private int first;
	private int last;
	private int index;
	private long seed;
	private Random random;
	private List<Integer> numbers = new ArrayList<Integer>(Characters.size());
	
	public RandomCharacters(int first, int last) {
		this(first, last, 0, new Random().nextLong());
	}
	
	private RandomCharacters(int first, int last, int index, long seed) {
		this.first = first;
		this.last = last;
		this.index = index;
		for (int i = first; i <= last; i++) {
			numbers.add(i);
		}
		this.seed = seed;
		internalShuffle();
	}
	
	public void save(Editor editor) {
		editor.putLong(PREF_SEED, seed);
		editor.putInt(PREF_INDEX, index);
		editor.commit();
	}
	
	public static RandomCharacters load(SharedPreferences prefs) {
		int first = prefs.getInt(MainActivity.PREF_FIRST_FRAME, 1) - 1;
		int last = prefs.getInt(MainActivity.PREF_LAST_FRAME, Characters.size()) - 1;
		int index = prefs.getInt(PREF_INDEX, 0);
		long seed = prefs.getLong(PREF_SEED, new Random().nextLong());
		return new RandomCharacters(first, last, index, seed);
	}
	
	public String getCharacter() {
		return Characters.getCharacter(current());
	}
	
	public String getKeyword() {
		return Characters.getKeyword(current());
	}
	
	public int index() {
		return index;
	}
	
	public int total() {
		return last - first + 1;
	}
	
	public int current() {
		return numbers.get(index);
	}
	
	public int previous() {
		if (index == 0) {
			this.seed = new Random().nextLong();
			internalShuffle();
			index = total();
		}
		index--;
		return current();
	}
	
	public int next() {
		if (index == total() - 1) {
			this.seed = new Random().nextLong();
			internalShuffle();
			index = -1;
		}
		index++;
		return current();
	}
	
	public int search(String query) {
		int searchNum = -1;
		try {
			searchNum = Integer.parseInt(query);
		} catch (NumberFormatException e) {			
		}
		
		for (int i = 0; i < numbers.size(); i++) {
			int num = numbers.get(i);
			String character = Characters.getCharacter(num);
			String keyword = Characters.getKeyword(num);
			if (query.equals(character) || query.equals(keyword) || searchNum == num || keyword.startsWith(query + " (")) {
				index = i;
				return index;
			}
		}
		
		return -1;
	}
	
	public void shuffle() {
		this.index = 0;
		this.seed = new Random().nextLong();
		internalShuffle();
	}
	
	private void internalShuffle() {
		this.random = new Random(seed);
		for (int i = first, j = 0; i <= last; i++, j++) {
			numbers.set(j, i);
		}
		Collections.shuffle(numbers, random);
	}

}
