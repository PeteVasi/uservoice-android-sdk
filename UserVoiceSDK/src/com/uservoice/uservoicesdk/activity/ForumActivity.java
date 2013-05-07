package com.uservoice.uservoicesdk.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView;
import android.widget.TextView;

import com.uservoice.uservoicesdk.R;
import com.uservoice.uservoicesdk.Session;
import com.uservoice.uservoicesdk.babayaga.Babayaga;
import com.uservoice.uservoicesdk.model.ClientConfig;
import com.uservoice.uservoicesdk.model.Forum;
import com.uservoice.uservoicesdk.model.Suggestion;
import com.uservoice.uservoicesdk.rest.Callback;
import com.uservoice.uservoicesdk.ui.DefaultCallback;
import com.uservoice.uservoicesdk.ui.PaginatedAdapter;
import com.uservoice.uservoicesdk.ui.PaginationScrollListener;
import com.uservoice.uservoicesdk.ui.SearchExpandListener;
import com.uservoice.uservoicesdk.ui.SearchQueryListener;

public class ForumActivity extends ListActivity implements SearchActivity {
	
	private List<Suggestion> suggestions;
	private Forum forum;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		suggestions = new ArrayList<Suggestion>();
		
		setListAdapter(new PaginatedAdapter<Suggestion>(this, R.layout.suggestion_item, suggestions) {
			@Override
			protected void customizeLayout(View view, Suggestion model) {
				TextView textView = (TextView) view.findViewById(R.id.suggestion_title);
				textView.setText(searchActive ? highlightResult(model.getTitle()) : model.getTitle());
				
				textView = (TextView) view.findViewById(R.id.suggestion_vote_count);
				textView.setText(String.valueOf(model.getNumberOfVotes()));
				
				textView = (TextView) view.findViewById(R.id.suggestion_votes_label);
				textView.setText(getResources().getQuantityString(R.plurals.votes, model.getNumberOfVotes()));

				textView = (TextView) view.findViewById(R.id.suggestion_status);
				if (model.getStatus() == null) {
					textView.setVisibility(View.GONE);
				} else {
					textView.setVisibility(View.VISIBLE);
					textView.setText(Html.fromHtml(String.format("<font color='%s'>%s</font>", model.getStatusColor(), model.getStatus())));
				}
			}

			@Override
			public void loadPage(int page, Callback<List<Suggestion>> callback) {
				Suggestion.loadSuggestions(forum, page, callback);
			}
			
			@Override
			public void search(String query, Callback<List<Suggestion>> callback) {
				Babayaga.track(Babayaga.Event.SEARCH_IDEAS);
				Suggestion.searchSuggestions(forum, query, callback);
			}

			@Override
			public int getTotalNumberOfObjects() {
				return forum.getNumberOfOpenSuggestions();
			}
		});
		
		getListView().setOnScrollListener(new PaginationScrollListener(getModelAdapter()) {
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (forum != null)
					super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
			}
		});
		
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Suggestion suggestion = (Suggestion) getModelAdapter().getItem(position);
				Session.getInstance().setSuggestion(suggestion);
				startActivity(new Intent(ForumActivity.this, SuggestionActivity.class));
			}
		});
		
		Babayaga.track(Babayaga.Event.VIEW_FORUM);
		
		loadClientConfig();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.forum, menu);
		menu.findItem(R.id.menu_search).setOnActionExpandListener(new SearchExpandListener(this));
		SearchView search = (SearchView) menu.findItem(R.id.menu_search).getActionView();
		search.setOnQueryTextListener(new SearchQueryListener(this));
		menu.findItem(R.id.new_idea).setVisible(Session.getInstance().getConfig().shouldShowPostIdea());
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	private void loadClientConfig() {
		if (Session.getInstance().getClientConfig() != null) {
			loadForum();
			return;
		}
		ClientConfig.loadClientConfig(new DefaultCallback<ClientConfig>(this) {
			@Override
			public void onModel(ClientConfig model) {
				Session.getInstance().setClientConfig(model);
				loadForum();
			}
		});
	}
	
	private void loadForum() {
		if (forum != null) return;
		Forum.loadForum(Session.getInstance().getConfig().getForumId(), new DefaultCallback<Forum>(this) {
			@Override
			public void onModel(Forum model) {
				forum = model;
				// TODO move these to onCreate once we do the init manager
				setTitle(forum.getName());
				getModelAdapter().loadMore();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public PaginatedAdapter<Suggestion> getModelAdapter() {
		return (PaginatedAdapter<Suggestion>) getListAdapter();
	}
}
