package com.bd.gitlab.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.model.Project;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;

import java.util.ArrayList;

public class DrawerAdapter extends BaseAdapter implements Filterable {

	private LayoutInflater inflater;
	private Filter filter = new FilterByName();

    private ArrayList<Project> projects;
	private ArrayList<Project> allProjects;

	public DrawerAdapter(Context context, ArrayList<Project> projects) {
		this.projects = projects;
		this.allProjects = new ArrayList<Project>(projects);
		
		if(context != null)
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return projects.size();
	}

	@Override
	public Project getItem(int position) {
		return projects.get(position);
	}

	@Override
	public long getItemId(int position) {
		return projects.get(position).getId();
	}
	
	public int getPosition(User user) {
		return projects.indexOf(user);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null) convertView = inflater.inflate(R.layout.simple_list_item, parent, false);

        final float scale = convertView.getResources().getDisplayMetrics().density;
        convertView.setMinimumHeight((int) (48 * scale + 0.5f));

        final TextView text = (TextView) convertView.findViewById(R.id.text);
		text.setText(projects.get(position).toString());

        text.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.setSelected(true);
            }
        });

		if(Repository.selectedProject != null && Repository.selectedProject.equals(projects.get(position))) {
            text.setTextColor(convertView.getResources().getColor(android.R.color.primary_text_light));
            text.setCompoundDrawablesWithIntrinsicBounds(null, null, convertView.getResources().getDrawable(R.drawable.ic_selected), null);
		}
		else {
            text.setTextColor(convertView.getResources().getColor(android.R.color.secondary_text_light));
			text.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}

		return convertView;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	private class FilterByName extends Filter {

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			projects.clear();
			for(Project project : (ArrayList<Project>) results.values)
				projects.add(project);
			notifyDataSetChanged();
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults result = new FilterResults();
			if(constraint == null || constraint.length() == 0) {
				result.values = allProjects;
				result.count = allProjects.size();
			}
            else {
				ArrayList<Project> filteredList = new ArrayList<Project>();
				for(Project project : allProjects) {
					if(project.toString().toLowerCase().contains(constraint.toString().toLowerCase()))
						filteredList.add(project);
				}
				result.values = filteredList;
				result.count = filteredList.size();
			}

			return result;
		}
	}
}
