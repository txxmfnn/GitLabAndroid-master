package com.bd.gitlab.fragments;

import java.util.List;

import android.text.InputType;
import android.widget.*;
import butterknife.ButterKnife;
import butterknife.InjectView;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;

import com.bd.gitlab.DiffActivity;
import com.bd.gitlab.R;
import com.bd.gitlab.adapter.CommitsAdapter;
import com.bd.gitlab.model.DiffLine;
import com.bd.gitlab.tools.Repository;
import com.bd.gitlab.tools.RetrofitHelper;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class CommitsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, OnItemClickListener {

    private EditText repoUrl;
	@InjectView(R.id.fragmentList) ListView listView;
    @InjectView(R.id.swipe_layout) SwipeRefreshLayout swipeLayout;
	
	public CommitsFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_commits, container, false);
		ButterKnife.inject(this, view);
		
		listView.setOnItemClickListener(this);
        repoUrl = new EditText(getActivity());
        repoUrl.setClickable(true);
        repoUrl.setCursorVisible(false);
        repoUrl.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_action_copy, 0);
        repoUrl.setFocusable(false);
        repoUrl.setFocusableInTouchMode(false);
        repoUrl.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        repoUrl.setMaxLines(1);
        repoUrl.setOnClickListener(this);
		repoUrl.setPadding(26, 26, 26, 26);
        listView.addHeaderView(repoUrl);

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
		if(Repository.selectedProject == null)
			return;
		
		repoUrl.setText("git@" + Repository.getServerUrl().replaceAll("http://", "").replaceAll("https://", "") + ":" + Repository.selectedProject.getPathWithNamespace() + ".git");
		
		if(Repository.selectedBranch == null) {
            if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

            listView.setAdapter(null);
            return;
        }
		
		if(swipeLayout != null && !swipeLayout.isRefreshing())
            swipeLayout.setRefreshing(true);
		
		Repository.getService().getCommits(Repository.selectedProject.getId(), Repository.selectedBranch.getName(), commitsCallback);
	}
	
	private Callback<List<DiffLine>> commitsCallback = new Callback<List<DiffLine>>() {
		
		@Override
		public void success(List<DiffLine> commits, Response resp) {
			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);
			
			if(commits.size() > 0)
				Repository.newestCommit = commits.get(0);
			else
				Repository.newestCommit = null;
			
			CommitsAdapter commitsAdapter = new CommitsAdapter(getActivity(), commits);
			listView.setAdapter(commitsAdapter);
		}
		
		@Override
		public void failure(RetrofitError e) {
			RetrofitHelper.printDebugInfo(getActivity(), e);

			if(swipeLayout != null && swipeLayout.isRefreshing())
                swipeLayout.setRefreshing(false);

			Crouton.makeText(getActivity(), R.string.connection_error_commits, Style.ALERT).show();
			listView.setAdapter(null);
		}
	};

    @Override
	public void onClick(View v) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", repoUrl.getText().toString());
        clipboard.setPrimaryClip(clip);
		
		Crouton.makeText(this.getActivity(), R.string.copy_notification, Style.CONFIRM).show();
	}
	
	public boolean onBackPressed() {
		return false;
	}
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Repository.selectedCommit = ((CommitsAdapter) ((HeaderViewListAdapter) listView.getAdapter()).getWrappedAdapter()).getItem(position - 1);
		startActivity(new Intent(getActivity(), DiffActivity.class));
	}
}