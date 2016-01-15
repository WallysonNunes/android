package com.tormentaLabs.riobus.map;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.activeandroid.content.ContentProvider;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.tormentaLabs.riobus.R;
import com.tormentaLabs.riobus.core.controller.LineController;
import com.tormentaLabs.riobus.core.model.LineModel;
import com.tormentaLabs.riobus.core.utils.CoreUtils;
import com.tormentaLabs.riobus.history.controller.HistoryController;
import com.tormentaLabs.riobus.itinerary.ItineraryComponent;
import com.tormentaLabs.riobus.map.listener.MapComponentListener;
import com.tormentaLabs.riobus.map.utils.MapPrefs_;
import com.tormentaLabs.riobus.map.utils.MapUtils;
import com.tormentaLabs.riobus.map.view.LineMapControllerView;
import com.tormentaLabs.riobus.marker.BusMarkerConponent;
import com.tormentaLabs.riobus.marker.UserMarkerComponent;
import com.tormentaLabs.riobus.search.adapter.SearchSuggestionsCursorAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author limazix
 * @since 2.0
 * Created on 01/09/15
 */
@OptionsMenu(R.menu.map_fragment)
@EFragment(R.layout.fragment_map)
public class MapFragment extends Fragment implements MapComponentListener,
        SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    private static final String TAG = MapFragment_.class.getName();
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private SearchView searchView;
    private SearchSuggestionsCursorAdapter searchSuggestionCursorAdapter;

    @Pref
    MapPrefs_ mapPrefs;

    @Bean
    BusMarkerConponent busMapComponent;

    @Bean
    UserMarkerComponent userMarkerComponent;

    @Bean
    ItineraryComponent itineraryComponent;

    @Bean
    HistoryController historyController;

    @Bean
    LineController lineCtrl;

    @ViewById(R.id.rioBusProgressBar)
    ProgressBar progressBar;

    @ViewById(R.id.lineMapControllerView)
    LineMapControllerView lineMapControllerView;

    @OptionsMenuItem(R.id.search)
    MenuItem menuSearch;

    @AfterViews
    public void afterViews() {
        lineMapControllerView.setVisibility(View.GONE);
        mapFragment = getMapFragment();
        map = mapFragment.getMap();
        setupMap();

        userMarkerComponent.setMap(map)
                .setListener(this)
                .buildComponent();
    }

    /**
     * Used to setup global map preferences
     */
    private void setupMap() {
        if(map.getUiSettings() != null) {
            map.getUiSettings().setMapToolbarEnabled(mapPrefs.isMapToolbarEnable().get());
            map.getUiSettings().setCompassEnabled(mapPrefs.isMapCompasEnable().get());
            map.setTrafficEnabled(mapPrefs.isMapTrafficEnable().get());
        }
        map.setMyLocationEnabled(mapPrefs.isMapMyLocationEnable().get());
    }

    /**
     * Used to access the map fragment which is a child fragment called map_fragment
     * @return SupportMapFragment
     */
    private SupportMapFragment getMapFragment() {
        return (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
    }

    @UiThread
    void setProgressVisibility(int visibility) {
        progressBar.setVisibility(visibility);
    }

    @Click(R.id.userPositionButton)
    void updateUserLocation() {
        userMarkerComponent.updateUserLocation();
    }

    @OptionsItem(R.id.search)
    boolean menuSearch() {
        searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);
        searchView.setOnQueryTextListener(this);
        searchView.setOnSuggestionListener(this);
        //searchView.setQueryRefinementEnabled(true);

        setupSearchSuggestions();

        return false;
    }

    private void setupSearchSuggestions() {

        searchSuggestionCursorAdapter = new SearchSuggestionsCursorAdapter(
                getActivity(), lineCtrl.fetchCursor());

        searchView.setSuggestionsAdapter(searchSuggestionCursorAdapter);

        getActivity().getSupportLoaderManager()
                .initLoader(0, null, new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        return new CursorLoader(
                                getActivity(),
                                ContentProvider.createUri(LineModel.class, null),
                                null, null, null, null
                        );
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                        searchView.getSuggestionsAdapter().swapCursor(data);
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {
                        searchView.getSuggestionsAdapter().swapCursor(null);
                    }
                });
    }

    @Override
    public void onComponentMapReady() {
        setProgressVisibility(View.GONE);
    }

    @Override
    public void onComponentMapError(Exception error) {
        Log.e(TAG, error.getMessage());
    }

    @Override
    public boolean onQueryTextSubmit(String keyword) {

        buildBusLineMap(keyword);

        return false;
    }

    private void buildBusLineMap(String keyword) {
        if (MapUtils.isValidString(keyword)) {
            LineModel line = historyController.addLine(keyword);
            busMapComponent.setLine(line)
                    .setListener(this)
                    .setMap(map)
                    .buildComponent();

            itineraryComponent.setLine(line)
                    .setListener(this)
                    .setMap(map)
                    .buildComponent();

            setProgressVisibility(View.VISIBLE);
            lineMapControllerView.setVisibility(View.VISIBLE);
            lineMapControllerView.build(line);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        searchSuggestionCursorAdapter.changeCursor(lineCtrl.fetchCursor(newText));
        return false;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        Cursor cursor = (Cursor) searchSuggestionCursorAdapter.getItem(position);
        String suggestionLine = cursor.getString(cursor.getColumnIndex(CoreUtils.TABLE_LINES_COL_NUMBER));

        searchView.setQuery(suggestionLine, true);
        return false;
    }
}