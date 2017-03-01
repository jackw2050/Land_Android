package com.zlscorp.ultragrav.activity;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockFragmentActivity;
import com.zlscorp.ultragrav.R;

public abstract class AbstractBaseActivity extends RoboSherlockFragmentActivity implements IntentParams {

   private boolean active;
   protected boolean hasFragments;

   SharedPreferences pref;
   public static String fragmentName = null;
   public int kind = 0;

   public static int language;

   public abstract String getActivityName();

   public abstract String getHelpKey();

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      ActionBar actionBar = getSupportActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
      pref = getSharedPreferences("language", 0);
      language = pref.getInt("lan", 0);
      // Set the size of the activity title (larger than default)
      int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
      TextView titleTextView = (TextView) findViewById(titleId);
      titleTextView.setTextColor(getResources().getColor(R.color.black));
      titleTextView.setTextSize((float) 24.0);
//        changeLanguage(language);
      // and increase the left margin
      LinearLayout.LayoutParams layoutParams =
            (LinearLayout.LayoutParams) titleTextView.getLayoutParams();
      layoutParams.setMargins(10, 0, 0, 0);
      titleTextView.setLayoutParams(layoutParams);
   }

   @Override
   protected void onResume() {
      super.onResume();
      if (active == false) {
         pref = getSharedPreferences("language", 0);
         language = pref.getInt("lan", 0);
//		changeLanguage(language);
         active = true;
      }
   }

   @Override
   protected void onPause() {
      super.onPause();
      active = false;
   }

   @Override
   public boolean onMenuItemSelected(int featureId, MenuItem item) {
      if (item.getItemId() == android.R.id.home) {
         onBackPressed();
      }

      return super.onMenuItemSelected(featureId, item);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      if (getHelpKey() != null) {
         menu.add(Menu.NONE, Menu.NONE + 4, 100, R.string.help)           // replaced R.id.helpButton with 0
               .setOnMenuItemClickListener(new HelpOnMenuItemClicked())
               .setIcon(android.R.drawable.ic_menu_help)
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(Menu.NONE, Menu.NONE, 96, "汉语")
               .setOnMenuItemClickListener(new LanguageOnMenuItemClicked())
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(Menu.NONE, Menu.NONE + 1, 97, "Español")
               .setOnMenuItemClickListener(new LanguageOnMenuItemClicked())
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(Menu.NONE, Menu.NONE + 2, 98, "English")
               .setOnMenuItemClickListener(new LanguageOnMenuItemClicked())
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
         menu.add(Menu.NONE, Menu.NONE + 3, 99, R.string.about)
               .setOnMenuItemClickListener(new AboutOnMenuItemClicked())
               .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
//			menu.add(Menu.NONE, Menu.NONE + 2 , 99, "Language")
//			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//		int ii = pref.getInt("lan", 0);
//		if ()
//			if (language == 0)
//			{
//			menu.add(Menu.NONE, Menu.NONE , 99, "汉语")           // replaced R.id.helpButton with 0
//			.setOnMenuItemClickListener(new LanguageOnMenuItemClicked())
//			
//			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//			}
//			else
//			{
//				menu.add(Menu.NONE, Menu.NONE , 99, "English")           // replaced R.id.helpButton with 0
//				.setOnMenuItemClickListener(new LanguageOnMenuItemClicked())
//				
//				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//			}
      }
      return super.onCreateOptionsMenu(menu);
   }

//	public void handleFatalActivitySetup() {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(R.string.activity_fatal_setup_title);
//		builder.setMessage(R.string.activity_fatal_setup_message);
//		builder.setPositiveButton(R.string.ok, new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				finish();
//			}
//		});
//        builder.show();
//	}
//
//	public void handleFatalActivityPersist() {
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle(R.string.activity_fatal_persist_title);
//		builder.setMessage(R.string.activity_fatal_persist_message);
//		builder.setPositiveButton(R.string.ok, new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		});
//		builder.show();
//	}

   public void updateTextView(TextView textView, Object object) {
      if (object != null) {
         textView.setText(object.toString());
      } else {
         textView.setText(null);
      }
   }

   public boolean isActive() {
      return active;
   }

   protected void changeLanguage(int kin) {

      int ii = kin;
      active = true;
      if (ii == 0) {
//    		edit.putInt("lan", 0);
//    		edit.commit();
//    		language = 1;
//    		item.setTitle("汉语");
//			Configuration newConfig = new Configuration();
//            newConfig.locale = Locale.CHINESE;
//            onConfigurationChanged(newConfig);

         //	String languageToLoad  = "CN_cn";
         String languageToLoad = "cn";
         Locale locale = new Locale(languageToLoad);
         Locale.setDefault(locale);
         Configuration config = new Configuration();
         config.locale = locale;
         getApplicationContext().getResources().updateConfiguration(config,
               getApplicationContext().getResources().getDisplayMetrics());
      } else if (ii == 2) {
//	    		edit.putInt("lan", 2);
//	    		edit.commit();
//	    		language = 0;
//	    		Configuration newConfig = new Configuration();
//	            newConfig.locale = Locale.ENGLISH;
//	            onConfigurationChanged(newConfig);
//	    		item.setTitle("English");
         //	String languageToLoad  = "EN_en" + "";
         String languageToLoad = "en";
         Locale locale = new Locale(languageToLoad);
         Locale.setDefault(locale);
         Configuration config = new Configuration();
         config.locale = locale;
         getApplicationContext().getResources().updateConfiguration(config,
               getApplicationContext().getResources().getDisplayMetrics());
//	   	     Intent in = getIntent();
//	   	     finish();
//	   	     startActivity(in);
      } else if (ii == 1) {
//	    	edit.putInt("lan", 1);
//	    	edit.commit();
//			language = 0;
//			Configuration newConfig = new Configuration();
//	        newConfig.locale = Locale.ENGLISH;
//	        onConfigurationChanged(newConfig);
//			item.setTitle("español");
         //	String languageToLoad  = "es_ES" + "";
         String languageToLoad = "es";

         Locale locale = new Locale(languageToLoad);
         Locale.setDefault(locale);
         Configuration config = new Configuration();
         config.locale = locale;
         getApplicationContext().getResources().updateConfiguration(config,
               getApplicationContext().getResources().getDisplayMetrics());
//		     Intent in = getIntent();
//		     finish();
//		     startActivity(in);
      }
      if (kind != 0) {
         Intent in = new Intent(this, DashboardActivity.class);
         in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
         in.putExtra("change", 1);
         startActivity(in);
         finish();
      } else {
         Intent in = getIntent();
//			 in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
         finish();
         startActivity(in);
      }
   }

   private class HelpOnMenuItemClicked implements OnMenuItemClickListener {

      @Override
      public boolean onMenuItemClick(MenuItem item) {

         if (!hasFragments) {
            fragmentName = null;
         }

         Intent intent = HelpActivity.createIntent(AbstractBaseActivity.this,
               getActivityName(), fragmentName);
         startActivity(intent);
         return false;
      }
   }


   private class AboutOnMenuItemClicked implements OnMenuItemClickListener {

      @Override
      public boolean onMenuItemClick(MenuItem item) {

         if (!hasFragments) {
            fragmentName = null;
         }

         Intent intent = AboutActivity.createIntent(AbstractBaseActivity.this);
         startActivity(intent);
         return false;
      }
   }

   boolean language_flag = false;

   private class LanguageOnMenuItemClicked implements OnMenuItemClickListener {

      @Override
      public boolean onMenuItemClick(MenuItem item) {

         if (!hasFragments) {
            fragmentName = null;
         }
         language_flag = false;
         int ii = item.getItemId();
         Editor edit = pref.edit();
         edit.putInt("lan", ii);
         edit.commit();

//           if (ii == 0)
//           {
//        	   item.setTitle("汉语");
//           }
//           else if (ii == 1)
//           {
//        	   item.setTitle("español");
//           }
//           else if (ii == 2)
//           {
//        	   item.setTitle("English");
//           }
//            Toast.makeText(getApplicationContext(), "" + item.getItemId(), Toast.LENGTH_SHORT).show();
         changeLanguage(ii);
//            else if (ii == 2)


         return false;
      }
   }

   public interface MyOnClickListener {

      public void onClick();
   }

}
