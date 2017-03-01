package com.zlscorp.ultragrav.activity.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import roboguice.fragment.RoboDialogFragment;
import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.activity.AbstractBaseActivity;

public class FileSelectFragment extends AbstractBaseFragment {
	
	public static String TAG = "FileSelectFragment";
	
	public static final String PATH_EXTRA = "path";
	
	public static final String ALLOW_CREATE_EXTRA = "allowCreate";
	
	private static final String PATH_PREFERENCE = "path";

	@Inject
	private LayoutInflater inflator;
	
	@InjectView(R.id.directoryUpButton)
	private Button directoryUpButton;
	
	@InjectView(R.id.directoryText)
	private TextView directoryText;
	
	@InjectView(R.id.listView)
	private ListView listView;
	
	@InjectView(R.id.createFileButton)
	private Button createFileButton;
	
	private MyFileListAdapter listAdapter;
	
	private File currentDirectory;
	private List<File> dirs;
	private List<File> files;
	
	private boolean allowCreate;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	View v = inflater.inflate(R.layout.fragment_file_select, container, false);
    	return v;
    }
	
	@Override
	public void setupView(View view, Bundle savedInstanceState) {
		
        AbstractBaseActivity.fragmentName = this.getClass().getSimpleName();

        String path = null;
		Bundle args = getArguments();
		if (args != null) {
			path = args.getString(PATH_EXTRA);
			allowCreate = args.getBoolean(ALLOW_CREATE_EXTRA, true);
		} else {
			allowCreate = true;
		}

		if (allowCreate) {
	        getActivity().setTitle(R.string.title_activity_select_or_create_file);
		} else {
	        getActivity().setTitle(R.string.title_activity_select_file);
		}
		
		if (path == null) {
			SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
			path = prefs.getString(PATH_PREFERENCE, null);
		}
		
		if (path == null) {
			String state = Environment.getExternalStorageState();
			if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
				File extDir = Environment.getExternalStorageDirectory();
				if (extDir != null)	{
					path = extDir.getAbsolutePath();
				}
			}
		}

		if (path == null) {
			path = "/";
		}
		
		currentDirectory = new File(path);
		createFilesForCurrentDirectory();
		
		listAdapter = new MyFileListAdapter();
		listView.setAdapter(listAdapter);
		listView.setOnItemClickListener(listAdapter);
		
		directoryUpButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File parent = currentDirectory.getParentFile();
				if (parent != null) {
					currentDirectory = parent;
					createFilesForCurrentDirectory();
				}
			}
		});
		
		createFileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String suggestedFileName = ".csv";
				FileNameDialog dialog = FileNameDialog.newInstance(suggestedFileName);
				dialog.show(getFragmentManager(), "fileNameDialog");
			}
		});
		createFileButton.setVisibility(allowCreate ? View.VISIBLE : View.GONE);
	}
	
	private void selectFile(File selectedFile) {
	    
		final String selectedDir = selectedFile.getParent();

		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			protected Void doInBackground(Void... params) {
				SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
				prefs.edit().putString(PATH_PREFERENCE, selectedDir).commit();
				return null;
			}
		};
		task.execute();
		
		Intent result = new Intent();
		result.putExtra(PATH_EXTRA, selectedFile.getAbsolutePath());
		getActivity().setResult(Activity.RESULT_OK, result);
		getActivity().finish();
	}
	
	private void createFilesForCurrentDirectory() {
		
		directoryText.setText(currentDirectory.getAbsolutePath());
		
		dirs = new ArrayList<File>();
		files = new ArrayList<File>();
		
		File[] items = currentDirectory.listFiles();
		for (int i=0; i<items.length; i++){
			File item = items[i];
			if (item.isDirectory()) {
				dirs.add(item);
			} else if (item.isFile()) {
				files.add(item);
			}
		}
		Collections.sort(dirs, new MyFileComparator());
		Collections.sort(files, new MyFileComparator());
		
		if (listAdapter != null) {
			listAdapter.notifyDataSetChanged();
		}
	}
	
	private class MyFileComparator implements Comparator<File> {

		@Override
		public int compare(File lhs, File rhs) {
			
			String lhsName = lhs.getName();
			String rhsName = rhs.getName();
			
			return lhsName.compareTo(rhsName);
		}
	}
	
	private class MyFileListAdapter extends BaseAdapter implements OnItemClickListener {
		
		@Override
		public int getCount() {
			
			return dirs.size() + files.size();
		}

		@Override
		public Object getItem(int position) {
			if (position < dirs.size()) {
				return dirs.get(position);
			}
			
			return files.get(position-dirs.size());
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			if (convertView == null) {
				convertView = inflator.inflate(android.R.layout.simple_list_item_1, null);
			}
			
			TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
			
			Object currentObj = getItem(position);
			
			File currentFile = (File)currentObj;
			textView.setText(currentFile.isDirectory() ? currentFile.getName()+"/" : currentFile.getName());
			int colorId = currentFile.canWrite() ? android.R.color.black : android.R.color.darker_gray;
			textView.setTextColor(getResources().getColor(colorId));
			
			return convertView;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Object selectObj = listAdapter.getItem(position);
			
			File selectFile = (File)selectObj;
			if (selectFile.isDirectory() && selectFile.canWrite()) {
				currentDirectory = selectFile;
				createFilesForCurrentDirectory();
			} else if (selectFile.isFile() && selectFile.canWrite()) {
				selectFile(selectFile);
			}
		}
	}
	
	public static class FileNameDialog extends RoboDialogFragment {
		
		private static final String SUGGESTED_FILE_NAME_ARG = "sugestedFileName";
		
		@Inject
		private LayoutInflater inflator;
		
		public static FileNameDialog newInstance(String suggestedFileName) {
			FileNameDialog frag = new FileNameDialog();
	        Bundle args = new Bundle();
	        args.putString(SUGGESTED_FILE_NAME_ARG, suggestedFileName);
	        frag.setArguments(args);
	        return frag;
	    }
		
		@SuppressLint("InflateParams")
        @Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			
			String suggestedFileName = getArguments().getString(SUGGESTED_FILE_NAME_ARG);
			
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			View layout = (View)inflator.inflate(R.layout.fragment_dialog_file_name, null);
			final EditText fileNameText = (EditText)layout.findViewById(R.id.fileName);
			fileNameText.setText(suggestedFileName);
			builder.setTitle(R.string.file_name);
			builder.setView(layout);
			builder.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String text = fileNameText.getText().toString();
					if (text.length() != 0) {
						FileSelectFragment fragment = (FileSelectFragment) getFragmentManager().findFragmentByTag(FileSelectFragment.TAG);
						fragment.selectFile(new File(fragment.currentDirectory.getAbsolutePath() + "/" + text));
					}
					dialog.dismiss();
				}
			});
			builder.setNegativeButton(R.string.cancel, null);
			
			return builder.create();
		}
		
	}

}
