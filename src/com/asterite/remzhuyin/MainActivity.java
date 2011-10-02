package com.asterite.remzhuyin;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity {
	
	public final static String SHARED_PREFS_NAME = "com.asterite.remzhuyin.settings";
	
	public final static String PREF_DAY = "day";
	public final static String PREF_MODE = "mode";
	public final static String PREF_FIRST_FRAME = "firstFrame";
	public final static String PREF_LAST_FRAME = "lastFrame";
	public final static String PREF_TOTAL_FRAMES = "totalFrames";
	
	private final static int STATE_FIRST = 0;
	private final static int STATE_SECOND = 1;
	
	private final static int MODE_FIRST_CHARACTER = 0;
	private final static int MODE_FIRST_KEYWORD = 1;
	
	private final static int MENU_SWITCH_MODE = 0;
	private final static int MENU_SWITCH_DAY_NIGHT = 1;
	private final static int MENU_SHUFFLE = 2;
	
	private int state = STATE_FIRST;
	private int mode = MODE_FIRST_KEYWORD;
	private boolean day = true;
	private RandomCharacters randomCharacters;
	private ViewFlipper flipper;
	private View[] characterViews;
	private int currentCharacterViewIndex;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        this.flipper = (ViewFlipper) findViewById(R.id.main);
        LayoutInflater inflater = getLayoutInflater();

        this.characterViews = new View[2];        
        for(int i = 0; i < 2; i++) {
        	this.flipper.addView(this.characterViews[i] = inflater.inflate(R.layout.character, null));
        }
        
        loadState();
        prepareDayNight();
        
        // Start search when the user types
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        
        // Check if this is a search request
        Intent intent = getIntent();
        handleIntent(intent);
        showCurrent();
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
    	if (handleIntent(intent)) {
    		showCurrent();
    	}
    }
    
	private boolean handleIntent(Intent intent) {
		if (intent == null) return false;
		
		Log.e("action", intent.getAction());
		Log.e("data", String.valueOf(intent.getData()));
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			int index = randomCharacters.search(query);
			if (index == -1) {
				Toast.makeText(this, getResources().getString(
						R.string.no_results_found, query), Toast.LENGTH_SHORT).show();
				return false;
			} else {
				return true;
			}
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			String query = intent.getData().getLastPathSegment();			
			randomCharacters.search(query);
			return true;
		} else {
			return false;
		}
	}

	private void saveState() {
    	Editor editor = writePrefs();
		editor.putBoolean(PREF_DAY, day);
		editor.putInt(PREF_MODE, mode);
		editor.putInt(PREF_TOTAL_FRAMES, Characters.size());
		editor.commit();
	}

	private void loadState() {
		SharedPreferences prefs = readPrefs();
		Editor editor = prefs.edit();
		
		int totalCharacters = Characters.size();
		
		day = prefs.getBoolean(PREF_DAY, true);
		mode = prefs.getInt(PREF_MODE, MODE_FIRST_KEYWORD);
		int lastLesson = prefs.getInt(PREF_LAST_FRAME, totalCharacters);
		int totalLessons = prefs.getInt(PREF_TOTAL_FRAMES, -1);
		if (totalLessons != -1 && lastLesson == totalLessons) {
			editor.putInt(PREF_LAST_FRAME, totalCharacters);
			lastLesson = totalCharacters;
		}
		editor.putInt(PREF_TOTAL_FRAMES, totalCharacters);
		
		randomCharacters = RandomCharacters.load(prefs);
		randomCharacters.save(editor);
		
		editor.commit();
	}
	
	private SharedPreferences readPrefs() {
		return getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
	}
	
	private Editor writePrefs() {
		return readPrefs().edit();
	}
	
	private float oldTouchValue;
	private boolean preparedNextView;
	private boolean preparedPreviousView;
	
	@Override
	public boolean onTouchEvent(MotionEvent touchEvent) {
		final int FLING_THRESHOLD = 50;
		float currentX = touchEvent.getX();
		
		switch (touchEvent.getAction()) {
	    case MotionEvent.ACTION_DOWN:
	      oldTouchValue = touchEvent.getX();
	      preparedNextView = false;
	      preparedPreviousView = false;
	      break;
	    case MotionEvent.ACTION_UP:
			if (preparedPreviousView) {
				showPrevious();
			} else if (preparedNextView) {
				showNext();
			} else {
				final View currentView = flipper.getCurrentView();
				currentView.layout(0,
						currentView.getTop(), currentView.getRight(), currentView
								.getBottom());
				click();
			}
	      break;
	    case MotionEvent.ACTION_MOVE:
	    	boolean isPrevious = oldTouchValue < currentX - FLING_THRESHOLD;
			boolean isNext = oldTouchValue > currentX + FLING_THRESHOLD;
	    	if ((isPrevious || isNext) || preparedPreviousView || preparedNextView) {
	    		if (isPrevious && !preparedPreviousView) {
	    			preparedPreviousView = true;
	    			preparedNextView = false;
	    		} else if (isNext && !preparedNextView) {
	    			preparedPreviousView = false;
	    			preparedNextView = true;
	    		}
				final View currentView = flipper.getCurrentView();
				currentView.layout(
					(int) (touchEvent.getX() - oldTouchValue), currentView.getTop(), 
					currentView.getRight(), currentView.getBottom());
	    	}
	      break;
	  }
	  return false;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_SWITCH_MODE, 0, mode == MODE_FIRST_CHARACTER ? R.string.keyword_mode : R.string.character_mode);
		menu.add(0, MENU_SWITCH_DAY_NIGHT, 0, R.string.day_night);
		menu.add(0, MENU_SHUFFLE, 0, R.string.shuffle);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case MENU_SWITCH_MODE: switchMode(); break;
		case MENU_SWITCH_DAY_NIGHT: switchDayNight(); break;
		case MENU_SHUFFLE: shuffle(); break;
		}
		return true;
	}
	
	private void switchMode() {
		mode = mode == MODE_FIRST_CHARACTER ? MODE_FIRST_KEYWORD : MODE_FIRST_CHARACTER;
		saveState();
		showCurrent();
	}
	
	private void switchDayNight() {
		day = !day;
		saveState();
		prepareDayNight();
	}
	
	private void prepareDayNight() {
		for(int i = 0; i < 2; i++) {
			View root = this.characterViews[i];
			if (day) {
				findViewById(R.id.main).setBackgroundColor(Color.WHITE);
				root.findViewById(R.id.main).setBackgroundColor(Color.WHITE);
				((TextView) root.findViewById(R.id.sequence)).setTextColor(Color.BLACK);
				((TextView) root.findViewById(R.id.character)).setTextColor(Color.BLACK);
				((TextView) root.findViewById(R.id.keyword)).setTextColor(Color.BLACK);
			} else {
				findViewById(R.id.main).setBackgroundColor(Color.BLACK);
				root.findViewById(R.id.main).setBackgroundColor(Color.BLACK);
				((TextView) root.findViewById(R.id.sequence)).setTextColor(Color.WHITE);
				((TextView) root.findViewById(R.id.character)).setTextColor(Color.WHITE);
				((TextView) root.findViewById(R.id.keyword)).setTextColor(Color.WHITE);
			}
		}
	}
	
	private void shuffle() {
		randomCharacters.shuffle();
		randomCharacters.save(writePrefs());
		showCurrent();
	}
	
	private void showCurrent() {
		show(randomCharacters.current(), currentCharacterViewIndex);
	}
	
	private void showNext() {
		int nextIndex = 1 - currentCharacterViewIndex;
		show(randomCharacters.next(), nextIndex);
		currentCharacterViewIndex = nextIndex;
		
		flipper.setInAnimation(AnimationHelper.inFromRightAnimation());
		flipper.setOutAnimation(AnimationHelper.outToLeftAnimation());
		flipper.showNext();
		
		randomCharacters.save(writePrefs());
	}
	
	private void showPrevious() {
		int nextIndex = 1 - currentCharacterViewIndex;
		show(randomCharacters.previous(), nextIndex);
		currentCharacterViewIndex = nextIndex;
		
		flipper.setInAnimation(AnimationHelper.inFromLeftAnimation());
		flipper.setOutAnimation(AnimationHelper.outToRightAnimation());
		flipper.showNext();
		
		randomCharacters.save(writePrefs());
	}
	
	private void show(int index, int viewIndex) {
		View root = this.characterViews[viewIndex];
		TextView sequenceView = (TextView)root.findViewById(R.id.sequence);
		TextView characterView = (TextView)root.findViewById(R.id.character);
		TextView keywordView = (TextView)root.findViewById(R.id.keyword);
		
		int idx = randomCharacters.index() + 1;
		int total = randomCharacters.total();
		String character = randomCharacters.getCharacter();
		String keyword = randomCharacters.getKeyword();
		
		sequenceView.setText(idx + "/" + total);
        characterView.setText(character);
        keywordView.setText(keyword);
        
        if (mode == MODE_FIRST_CHARACTER) {
        	keywordView.setVisibility(View.INVISIBLE);
        	characterView.setVisibility(View.VISIBLE);
        } else {
        	keywordView.setVisibility(View.VISIBLE);
        	characterView.setVisibility(View.INVISIBLE);
        }
        state = STATE_FIRST;
	}
	
	private void click() {
		View root = this.characterViews[this.currentCharacterViewIndex];
		switch(state) {
		case STATE_FIRST:
			if (mode == MODE_FIRST_CHARACTER) {
				root.findViewById(R.id.keyword).setVisibility(View.VISIBLE);
			} else {
				root.findViewById(R.id.character).setVisibility(View.VISIBLE);
			}
			state = STATE_SECOND;
			break;
		case STATE_SECOND:
			showNext();
			state = STATE_FIRST;
			break;
		}
	}
    
}