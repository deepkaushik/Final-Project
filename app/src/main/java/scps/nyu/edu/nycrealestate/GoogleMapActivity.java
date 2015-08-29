package scps.nyu.edu.nycrealestate;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

// this activity displays the google map on the screen
public class GoogleMapActivity extends AppCompatActivity implements OnMenuItemClickListener,
        OnMenuItemLongClickListener, OnMapReadyCallback {

    private FragmentManager fragmentManager;
    private DialogFragment mMenuDialogFragment;
    private DrawGoogleMap myCustomGoogleMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);

        fragmentManager = getSupportFragmentManager();
        initToolbar();
        initMenuFragment();

        myCustomGoogleMap = new DrawGoogleMap();


        boolean loadCurrentAddress = true;
        Bundle extras = getIntent().getExtras();
        EditText addressEditText = (EditText) findViewById(R.id.address);
        if (extras != null) {
            // check if we should erase the current listing after saving
            String eraseAddress = extras.getString("SaveListingCommand");
            if (eraseAddress != null && eraseAddress.equals("clear")) {
                GoogleMapData.eraseCurrentListing();
                loadCurrentAddress = false;
            }

            // check if we should load address from google voice
            String voiceAddress = extras.getString("GoogleVoiceCommand");
            if (voiceAddress != null) {
                addressEditText.setText(voiceAddress);
                try {
                    updateCurrentListing();
                    loadCurrentAddress = false;
                } catch (InvalidParameterException e) {
                    // display any errors that occurred when updating current listing
                    String[] exceptionText = e.toString().split(":");
                    String errorText = exceptionText[1];
                    Toast toast = Toast.makeText(GoogleMapActivity.this, errorText, Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }

        if (loadCurrentAddress && GoogleMapData.getCurrentListing() != null) {
            addressEditText.setText(GoogleMapData.getCurrentListing().getAddress());
        }

        // draw map
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(GoogleMapActivity.this);  // This calls OnMapReady(..). (Asynchronously)

        // listener for address EditText field to update currentListing marker whenever user enters an address
        final EditText editText = (EditText)findViewById(R.id.address);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    try {
                        updateCurrentListing();
                    } catch (InvalidParameterException e) {
                        // display any errors that occurred when updating current listing
                        String[] exceptionText = e.toString().split(":");
                        String errorText = exceptionText[1];
                        Toast toast = Toast.makeText(GoogleMapActivity.this, errorText, Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        myCustomGoogleMap.drawMap(getApplicationContext(), map, GoogleMapData.getCameraLoc(), GoogleMapData.getCameraZoom());
    }

    // update marker for current real estate listing
    private void updateCurrentListing() {
        final EditText editText = (EditText) findViewById(R.id.address);
        List<Address> list;
        Geocoder gc = new Geocoder(getApplicationContext());
        String stringAddress = editText.getText().toString().trim();

        if (!stringAddress.equals("")) {
            try {
                list = gc.getFromLocationName(stringAddress, 1);

                if (list.size() > 0) {
                    Address googleAddress = list.get(0);

                    double lat = googleAddress.getLatitude();
                    double lng = googleAddress.getLongitude();

                    LatLng listingLatLng = new LatLng(lat, lng);

                    // set camera to point here
                    GoogleMapData.setCameraLoc(listingLatLng);

                    GoogleMapData.setCurrentListing(stringAddress, listingLatLng);

                    //refresh map
                    SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFrag.getMapAsync(GoogleMapActivity.this);
                } else {
                    throw new InvalidParameterException("Invalid address, please try again");
                }
            } catch (java.io.IOException e) {
                throw new InvalidParameterException("IO Error: " + e.toString());
            }
        } else {
            throw new InvalidParameterException("Please first enter an address");
        }
    }

    private void saveMapData() {
        GoogleMapData.setCameraLoc(myCustomGoogleMap.getCameraLocation());
        GoogleMapData.setCameraZoom(myCustomGoogleMap.getCameraZoom());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void loadCurrentListing(View v) {
        try {
            updateCurrentListing();
        } catch (InvalidParameterException e) {
            // display any errors that occurred when updating current listing
            String[] exceptionText = e.toString().split(":");
            String errorText = exceptionText[1];
            Toast toast = Toast.makeText(GoogleMapActivity.this, errorText, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public void onMenuItemLongClick(View clickedView, int position) {
        parseMenuClick(position);
    }

    @Override
    public void onMenuItemClick(View clickedView, int position) {
        parseMenuClick(position);
    }

    private void parseMenuClick(int position) {
        Intent intent = null;
        switch (position) {
            case 1:
                saveListing();
                break;
            case 2:
                intent = new Intent(this, NewsActivity.class);
                break;
            case 3:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case 4:
                intent = new Intent(this, FiltersActivity.class);
                break;
            case 5:
                intent = new Intent(this, VoiceRecognitionActivity.class);
                break;
        }
        if (position > 1) {
            startActivity(intent);
        }
    }

    // go to save listing screen
    private void saveListing() {
        // check to make sure address is valid before going to save listing page
        try {
            updateCurrentListing();

            // since the SaveListingActivity code doesn't handle updating existing database records
            // we're prohibiting the user from  trying to update data for an existing real estate listing
            if (!(GoogleMapData.isCurrentLocInSavedListings(GoogleMapData.getCurrentListing().getLocation()))) {

                Intent intent = new Intent(GoogleMapActivity.this, SaveListingActivity.class);

                if (!GoogleMapData.isCurrentLocInSavedListings(GoogleMapData.getCurrentListing().getLocation())) {
                    saveMapData();

                    intent.putExtra("Address", GoogleMapData.getCurrentListing().getAddress());
                    intent.putExtra("Longitude", Double.toString(GoogleMapData.getCurrentListing().getLocation().longitude));
                    intent.putExtra("Latitude", Double.toString(GoogleMapData.getCurrentListing().getLocation().latitude));
                } else {
                    intent.putExtra("Address", GoogleMapData.getCurrentListing().getAddress());
                    intent.putExtra("Description", GoogleMapData.getCurrentListing().getDesc());
                    if (GoogleMapData.getCurrentListing().getPrice() > 0) {
                        intent.putExtra("Price", Double.toString(GoogleMapData.getCurrentListing().getPrice()));
                    }
                    if (GoogleMapData.getCurrentListing().getSquareFeet() > 0) {
                        intent.putExtra("SqFt", Integer.toString(GoogleMapData.getCurrentListing().getSquareFeet()));
                    }
                    if (GoogleMapData.getCurrentListing().getNumberBedrooms() > 0) {
                        intent.putExtra("NbrBedrooms", Integer.toString(GoogleMapData.getCurrentListing().getNumberBedrooms()));
                    }
                    intent.putExtra("Longitude", Double.toString(GoogleMapData.getCurrentListing().getLocation().longitude));
                    intent.putExtra("Latitude", Double.toString(GoogleMapData.getCurrentListing().getLocation().latitude));
                }
                startActivity(intent);
            } else {
                Toast toast = Toast.makeText(GoogleMapActivity.this, getResources().getString(R.string.overwrite_error), Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (InvalidParameterException e) {
            // display any errors that occurred when updating current listing
            String[] exceptionText = e.toString().split(":");
            String errorText = exceptionText[1];
            Toast toast = Toast.makeText(GoogleMapActivity.this, errorText, Toast.LENGTH_LONG);
            toast.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.context_menu:
                mMenuDialogFragment.show(fragmentManager, "ContextMenuDialogFragment");
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMenuFragment() {
        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) getResources().getDimension(R.dimen.tool_bar_height));
        menuParams.setMenuObjects(getMenuObjects());
        menuParams.setClosableOutside(false);
        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mToolBarTextView = (TextView) findViewById(R.id.text_view_toolbar_title);
        setSupportActionBar(mToolbar);
        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        } catch (java.lang.NullPointerException e) {
            // do nothing
        }
        mToolBarTextView.setText(R.string.title_activity_google_map);
    }

    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject("Close Menu");
        close.setResource(R.drawable.close);

        MenuObject save = new MenuObject("Save Listing");
        save.setResource(R.drawable.save);

        MenuObject news = new MenuObject("View News");
        news.setResource(R.drawable.news);

        MenuObject settings = new MenuObject("View Map Settings");
        settings.setResource(R.drawable.search);

        MenuObject filters = new MenuObject("View Listings Filters");
        filters.setResource(R.drawable.marker);

        MenuObject voice = new MenuObject("Google Voice Input");
        voice.setResource(R.drawable.voicesearch);

        menuObjects.add(close);
        menuObjects.add(save);
        menuObjects.add(news);
        menuObjects.add(settings);
        menuObjects.add(filters);
        menuObjects.add(voice);

        return menuObjects;
    }

}
