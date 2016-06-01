/*
 * Copyright 2015
 *
 * Olayinka S. Folorunso <mail@olayinkasf.com>
 * http://olayinkasf.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.olayinka.smart.tone.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.olayinka.smart.tone.AppSqlHelper;
import com.olayinka.smart.tone.Utils;
import com.olayinka.smart.tone.adapter.MediaListAdapter;
import com.olayinka.smart.tone.model.Media;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.LinkedHashSet;

import lib.olayinka.smart.tone.R;

/**
 * Created by Olayinka on 6/17/2015.
 */
public class SearchMusicActivity extends ImageCacheActivity {

    public static final String SELECTION = "media.selection";
    private LinkedHashSet<Long> mSelection;
    private MediaListAdapter mAdapter;

    @Override
    void initToolbar(Toolbar toolbar) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_music);
        try {
            setSelection();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        ListView mediaListView = (ListView) findViewById(R.id.list);
        mAdapter = new MediaListAdapter(this, MediaListAdapter.SELECTION_NONE, mSelection);
        mediaListView.setAdapter(mAdapter);
        mediaListView.setEmptyView(findViewById(R.id.emptyView));
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void setSelection() throws JSONException {
        JSONArray jsonArray = new JSONArray(getIntent().getStringExtra(SELECTION));
        mSelection = new LinkedHashSet<>(1000);
        for (int i = 0; i < jsonArray.length(); i++) {
            mSelection.add(jsonArray.getLong(i));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true); // Do not iconify the widget; expand it by default

        SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                if (newText.length() < 2) return true;
                Cursor cursor = AppSqlHelper.instance(SearchMusicActivity.this).getReadableDatabase()
                        .query(Media.TABLE, new String[]{"*"}, MediaListAdapter.SELECTION_SEARCH, new String[]{like(newText), like(newText), like(newText)}, null, null, Media.Columns.NAME);
                mAdapter.changeCursor(cursor);
                mAdapter.notifyDataSetChanged();
                return true;
            }

            private String like(String newText) {
                return "%" + newText + "%";
            }

            public boolean onQueryTextSubmit(String query) {
                return onQueryTextChange(query);
            }
        };
        searchView.setOnQueryTextListener(mQueryTextListener);

        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        if (Utils.hasIceCreamSandwich())
            searchMenuItem.expandActionView();
        else MenuItemCompat.expandActionView(searchMenuItem);

        return true;
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(SELECTION, Utils.serialize(mSelection));
        setResult(RESULT_OK, data);
        super.finish();
    }

}
