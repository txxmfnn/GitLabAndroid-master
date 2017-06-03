package com.bd.gitlab.fragments;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import com.melnykov.fab.FloatingActionButton;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.bd.gitlab.R;
import com.bd.gitlab.adapter.UserAdapter;
import com.bd.gitlab.model.User;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class UsersFragment extends Fragment implements OnItemClickListener, SwipeRefreshLayout.OnRefreshListener {
	
	@InjectView(R.id.add_user_button) FloatingActionButton addUserButton;
	@InjectView(R.id.fragmentList) ListView listView;
	@InjectView(R.id.error_text) TextView errorText;
    @InjectView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	
	public UsersFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_users, container, false);
        ButterKnife.inject(this, view);
		
		listView.setOnItemClickListener(this);
        addUserButton.attachToListView(listView);

        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
		
		if(Repository.selectedProject != null)
			loadData();
		
		return view;
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		ButterKnife.reset(this);
	}
	
	@Override
	public void onRefresh() {
		loadData();
	}
	
	public void loadData() {
		if(swipeLayout != null && !swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(true);
		
		if(Repository.selectedProject.getGroup() == null) {
			errorText.setVisibility(View.VISIBLE);
			errorText.setText(R.string.not_in_group);
			listView.setVisibility(View.GONE);
			addUserButton.setVisibility(View.GONE);
			if(swipeLayout != null && swipeLayout.isRefreshing())
				swipeLayout.setRefreshing(false);
			return;
		}
		
		Repository.getService().getGroupMembers(Repository.selectedProject.getGroup().getId(), usersCallback);
	}
	
	public Callback<List<User>> usersCallback = new Callback<List<User>>() {
		
		@Override
		public void success(List<User> users, Response resp) {
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			errorText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
			addUserButton.setVisibility(View.VISIBLE);
			
			Repository.userAdapter = new UserAdapter(getActivity(), users);
			listView.setAdapter(Repository.userAdapter);
			
			addUserButton.setEnabled(true);
		}
		
		@Override
		public void failure(RetrofitError e) {
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			if(e.getResponse() != null && e.getResponse().getStatus() == 404) {
				errorText.setVisibility(View.VISIBLE);
				errorText.setText(R.string.groups_not_supported);
				listView.setVisibility(View.GONE);
				addUserButton.setVisibility(View.GONE);
			}
			else {
				errorText.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				addUserButton.setVisibility(View.VISIBLE);

                RetrofitHelper.printDebugInfo(getActivity(), e);
				Crouton.makeText(getActivity(), R.string.connection_error_users, Style.ALERT).show();
				listView.setAdapter(null);
			}
		}
	};
	
	public boolean onBackPressed() {
		return false;
	}
	
	@OnClick(R.id.add_user_button)
	public void onAddUserClick() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = AddUserDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Repository.selectedUser = Repository.userAdapter.getItem(position);
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		DialogFragment newFragment = RemoveUserDialogFragment.newInstance();
		newFragment.show(ft, "dialog");
	}
}