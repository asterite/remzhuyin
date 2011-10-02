package com.asterite.remzhuyin;

import java.util.ArrayList;
import java.util.List;

public class Characters {
	
	private final static List<String> characters = new ArrayList<String>();
	private final static List<String> keywords = new ArrayList<String>();
	
	public static int size() {
		return characters.size();
	}
	
	public static String getCharacter(int frame) {
		return characters.get(frame);
	}
	
	public static String getKeyword(int frame) {
		return keywords.get(frame);
	}
	
	static {
		add("ㄅ", "B");
		add("ㄆ", "P");
		add("ㄇ", "M");
		add("ㄈ", "F");
		add("ㄉ", "D");
		add("ㄊ", "T");
		add("ㄋ", "N");
		add("ㄌ", "L");
		add("ㄍ", "G");
		add("ㄎ", "K");
		add("ㄏ", "H");
		add("ㄐ", "J");
		add("ㄑ", "Q");
		add("ㄒ", "X");
		add("ㄓ", "ZH");
		add("ㄔ", "CH");
		add("ㄕ", "SH");
		add("ㄖ", "R");
		add("ㄗ", "Z");
		add("ㄘ", "C");
		add("ㄙ", "S");
		add("ㄧ", "I, Y");
		add("ㄨ", "U, W");
		add("ㄩ", "Ü, YU");
		add("ㄚ", "A");
		add("ㄛ", "O");
		add("ㄜ", "E");
		add("ㄝ", "Ê");
		add("ㄞ", "AI");
		add("ㄟ", "EI");
		add("ㄠ", "AO");
		add("ㄡ", "OU");
		add("ㄢ", "AN");
		add("ㄣ", "EN");
		add("ㄤ", "ANG");
		add("ㄥ", "ENG");
		add("ㄦ", "ER");
		add("ㄭ", "-I");
	}
	
	private static void add(String character, String keyword) {
		characters.add(character);
		keywords.add(keyword);
	}

}
