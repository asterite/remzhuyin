package com.asterite.remzhuyin;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.AbstractCursor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class CharacterSuggestionProvider extends ContentProvider {
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sortOrder) {
		SharedPreferences pref = getContext().getSharedPreferences(MainActivity.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		int first = pref.getInt(MainActivity.PREF_FIRST_FRAME, 1) - 1;
		int last = pref.getInt(MainActivity.PREF_LAST_FRAME, Characters.size()) - 1;
		
		String query = uri.getLastPathSegment().toLowerCase();
		int queryNum = -1;
		try {
			queryNum = Integer.parseInt(query) - 1;
		} catch (NumberFormatException e) {
			
		}
		
		List<Integer> indices = new ArrayList<Integer>();
		List<String> results = new ArrayList<String>();
		for (int i = first; i <= last; i++) {
			String character = Characters.getCharacter(i);
			String keyword = Characters.getKeyword(i);
			if (character.equals(query) || keyword.startsWith(query) || i == queryNum) {
				indices.add(i);
				results.add(character + ": " + keyword);
			}
		}
		return new SuggestionsCursor(indices, results);
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return true;
	}	

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		return 0;
	}
	
	private class SuggestionsCursor extends AbstractCursor {
		
		private final List<Integer> indices;
		private final List<String> results;

		public SuggestionsCursor(List<Integer> indices, List<String> results) {
			this.indices = indices;
			this.results = results;
		}

		@Override
		public String[] getColumnNames() {
			return new String[] { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_INTENT_DATA };
		}

		@Override
		public int getCount() {
			return indices.size();
		}
		
		@Override
		public int getInt(int column) {
			return 0;
		}
		
		@Override
		public String getString(int column) {
			switch(column) {
			case 0: return String.valueOf(indices.get(getPosition()));
			case 1: return results.get(getPosition());
			case 2: return "content://com.asterite.remzhuyin.CharacterSuggestion/" + indices.get(getPosition());
			default: throw new IllegalArgumentException(); 
			}
		}

		@Override
		public double getDouble(int arg0) {
			return 0;
		}

		@Override
		public float getFloat(int arg0) {
			return 0;
		}		

		@Override
		public long getLong(int arg0) {
			return 0;
		}

		@Override
		public short getShort(int arg0) {
			return 0;
		}		

		@Override
		public boolean isNull(int arg0) {
			return false;
		}
		
	}

}
