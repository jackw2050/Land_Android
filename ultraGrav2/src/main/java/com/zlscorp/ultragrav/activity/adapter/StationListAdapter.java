package com.zlscorp.ultragrav.activity.adapter;

import java.util.List;

import roboguice.inject.InjectResource;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.zlscorp.ultragrav.R;
import com.zlscorp.ultragrav.model.Station;
import com.zlscorp.ultragrav.model.StationSeries;

public class StationListAdapter extends BaseExpandableListAdapter {
	
//	private static final String TAG = "StationListAdapter";
	
	private static int GROUP_TYPE_COUNT = 1;
	private static int CHILD_TYPE_COUNT = 1;
	private static int VIEW_TYPE_ID = 0;
	
	private static long STATION_ID_MASK = 1<<63;

	@Inject
	private LayoutInflater inflator;
	
	@InjectResource(R.drawable.series_down)
	private Drawable seriesDownIcon;
	
	@InjectResource(R.drawable.series_up)
	private Drawable seriesUpIcon;
	
	@InjectResource(R.drawable.series_mid)
	private Drawable seriesMidIcon;
	
	@InjectResource(R.drawable.series_end)
	private Drawable seriesEndIcon;
	
	@InjectResource(R.drawable.series_none)
	private Drawable seriesNoneIcon;
	
	@InjectResource(R.drawable.station)
	private Drawable stationIcon;
	
	private List<Station> stations;
	private List<StationSeries> series;
	
	private OnStationConfigureClickListener stationConfigureClickListener;
	
	public StationListAdapter() {
		
	}
	
	public void setOnStationConfigureClickListener(OnStationConfigureClickListener stationConfigureClickListener) {
		this.stationConfigureClickListener = stationConfigureClickListener;
	}
	
	public void setSeriesAndStations(List<StationSeries> series, List<Station> stations) {
		this.series = series;
		this.stations = stations;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		StationSeries series = (StationSeries)getGroup(groupPosition);
		Station[] stations = series.getStations().toArray(new Station[0]);
		if (childPosition<stations.length) {
			return stations[childPosition];
		} else {
			return null;
		}
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		Station station = (Station)getChild(groupPosition, childPosition);
		return station.getId() | STATION_ID_MASK;
	}
	
	@Override
	public int getChildTypeCount() {
		return CHILD_TYPE_COUNT;
	}
	
	@Override
	public int getChildType(int groupPosition, int childPosition) {
		return VIEW_TYPE_ID;
	}

	@SuppressLint("InflateParams")
    @Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, 
	        View convertView, ViewGroup parent) {
		StationSeries series = (StationSeries)getGroup(groupPosition);
		Station[] stations = series.getStations().toArray(new Station[0]);
		Station station = stations[childPosition];
		
		CellViewHolder holder;
		if (convertView==null) {
			convertView = inflator.inflate(R.layout.cell_station, null);
			holder = new CellViewHolder();
			holder.label = (TextView)convertView.findViewById(R.id.labelText);
			holder.indicator = (ImageView)convertView.findViewById(R.id.indicator);
			holder.configure = (Button)convertView.findViewById(R.id.configureButton);
			holder.configure.setOnClickListener(stationConfigureClickListener);
			convertView.setTag(holder);
		} else {
			holder = (CellViewHolder) convertView.getTag();
		}
		
		boolean last = childPosition == stations.length-1;
		Drawable expandIcon = last ? seriesEndIcon : seriesMidIcon;

		holder.label.setText(station.getStationId());
		holder.indicator.setVisibility(View.VISIBLE);
		holder.indicator.setImageDrawable(expandIcon);
		holder.configure.setVisibility(View.GONE);
		holder.configure.setTag(station);
		return convertView;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		Object group = getGroup(groupPosition);
		
		if (group instanceof Station) {
			return 0;
		} else {
			StationSeries series = (StationSeries)group;
			if (series.getStations()!=null) {
				return series.getStations().size();
			} else {
				return 0;
			}
		}
	}

	@Override
	public Object getGroup(int groupPosition) {
		
		if(groupPosition<stations.size()) {
			Station station = stations.get(groupPosition);
			return station;
		} else {
			StationSeries ser = series.get(groupPosition-stations.size());
			return ser;
		}
	}

	@Override
	public int getGroupCount() {
		if (stations!=null && series!=null) {
			return stations.size()+series.size();
		} else {
			return 0;
		}
	}

	@Override
	public long getGroupId(int groupPosition) {
		Object group = getGroup(groupPosition);
		
		if (group instanceof Station) {
			Station station = (Station)group;
			return station.getId() | STATION_ID_MASK;
		} else {
			return series.get(groupPosition-stations.size()).getId();
		}
	}


	@Override
	public int getGroupTypeCount() {
		return GROUP_TYPE_COUNT;
	}
	
	@Override
	public int getGroupType(int groupPosition) {
		return VIEW_TYPE_ID;
	}

	@SuppressLint("InflateParams")
    @Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Object group = getGroup(groupPosition);
		CellViewHolder holder;
		
		if (convertView==null) {
			convertView = inflator.inflate(R.layout.cell_station, null);
			holder = new CellViewHolder();
			holder.label = (TextView)convertView.findViewById(R.id.labelText);
			holder.indicator = (ImageView)convertView.findViewById(R.id.indicator);
			holder.configure = (Button)convertView.findViewById(R.id.configureButton);
			holder.configure.setOnClickListener(stationConfigureClickListener);
			convertView.setTag(holder);
		} else {
			holder = (CellViewHolder)convertView.getTag();
		}
		
		if (group instanceof Station) {
			// is station
			Station station = (Station)group;
			
			holder.label.setText(station.getStationId());
			holder.indicator.setImageDrawable(stationIcon);
			holder.configure.setVisibility(View.GONE);
			holder.configure.setTag(station);
		} else {
			// is series
			StationSeries series = (StationSeries)group;
			Station[] stations = series.getStations().toArray(new Station[0]);
			
			boolean empty = stations.length == 0;
			
			holder.label.setText(series.getName());
			if (empty) {
				holder.indicator.setImageDrawable(seriesNoneIcon);
			} else {
				holder.indicator.setImageDrawable(isExpanded ? seriesDownIcon : seriesUpIcon);
			}
			holder.configure.setVisibility(View.VISIBLE);
			holder.configure.setTag(series);
		}

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	
	public abstract class OnStationConfigureClickListener implements OnClickListener {
		
		public abstract void onStationSeriesConfigureClicked(StationSeries stationSeries);
		
		@Override
		public void onClick(View v) {
			Object obj = v.getTag();
			if (obj instanceof StationSeries) {
				StationSeries series = (StationSeries)obj;
				onStationSeriesConfigureClicked(series);
			}
		}
	}
	
	public abstract class OnStationClickListener implements OnChildClickListener, OnGroupClickListener {
		
		public abstract void onStationSelected(Station station);
		public abstract void onStationSeriesSelected(StationSeries stationSeries);
		
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			Station station = (Station)getChild(groupPosition, childPosition);
			onStationSelected(station);
			return true;
		}

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			Object group = getGroup(groupPosition);
			
			if (group instanceof Station) {
				Station station = (Station)group;
				onStationSelected(station);
				return true;
			} else {
				StationSeries series = (StationSeries)group;
				onStationSeriesSelected(series);
				return false;
			}
		}
	}
	
	private class CellViewHolder {
		ImageView indicator;
		TextView label;
		Button configure;
	}

}
