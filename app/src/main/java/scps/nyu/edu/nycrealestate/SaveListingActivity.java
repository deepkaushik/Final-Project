package scps.nyu.edu.nycrealestate;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseObject;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemLongClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// this activity saves a real estate listing to our parse.com database
public class SaveListingActivity extends AppCompatActivity implements OnMenuItemClickListener,
        OnMenuItemLongClickListener {

    LatLng oldLatLng;

    private FragmentManager fragmentManager;
    private DialogFragment mMenuDialogFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_listing);

        fragmentManager = getSupportFragmentManager();
        initToolbar();
        initMenuFragment();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String newAddress = extras.getString("Address");
            String stringDesc = extras.getString("Description");
            String stringPrice = extras.getString("Price");
            String stringSqFt = extras.getString("SqFt");
            String stringNbrBedrooms = extras.getString("NbrBedrooms");
            String stringLat = extras.getString("Latitude") ;
            String stringLong = extras.getString("Longitude");

            EditText addressView = (EditText) findViewById(R.id.save_address);
            EditText descView = (EditText) findViewById(R.id.save_description);
            EditText priceView = (EditText) findViewById(R.id.save_price);
            EditText sqftView = (EditText) findViewById(R.id.save_sqft);
            EditText nbrBedroomsView = (EditText) findViewById(R.id.save_nbr_bedrooms);

            addressView.setText(newAddress);
            descView.setText(stringDesc);
            priceView.setText(stringPrice);
            sqftView.setText(stringSqFt);
            nbrBedroomsView.setText(stringNbrBedrooms);

            double tmpLat;
            double tmpLong;

            if ((stringLong != null) && (stringLat != null)) {
                tmpLat = Double.parseDouble(stringLat);
                tmpLong = Double.parseDouble(stringLong);
                oldLatLng = new LatLng(tmpLat,tmpLong);
            } else {
                Toast toast = Toast.makeText(this, getString(R.string.map_save_error), Toast.LENGTH_LONG);
                toast.show();
            }
        }
    }

    // save listing, reset screen, and go back to map screen
    public void SaveNewListing(View v) {
        // save listing

        EditText addressView = (EditText) findViewById(R.id.save_address);
        String newAddress = addressView.getText().toString();

        EditText descView = (EditText) findViewById(R.id.save_description);
        String newDesc = descView.getText().toString();

        Double newPrice = -1.0;
        EditText priceView = (EditText) findViewById(R.id.save_price);
        String stringPrice = priceView.getText().toString();
        if (!stringPrice.equals("")) {
            newPrice = Double.valueOf(stringPrice);
        }

        Integer newSqFt = -1;
        EditText sqftView = (EditText) findViewById(R.id.save_sqft);
        String stringSqft = sqftView.getText().toString();
        if (!stringSqft.equals("")) {
            newSqFt = Integer.valueOf(stringSqft);
        }

        Integer newNbrBedrooms = -1;
        EditText bedroomsView = (EditText) findViewById(R.id.save_nbr_bedrooms);
        String stringNbrBedrooms = bedroomsView.getText().toString();
        if (!stringNbrBedrooms.equals("")) {
            newNbrBedrooms = Integer.valueOf(stringNbrBedrooms);
        }

        // calculate new latitude and longitude from address
        try {
            Geocoder gc = new Geocoder(getApplicationContext());
            List<Address> list = gc.getFromLocationName(newAddress, 1);

            if (list.size() > 0) {
                Address googleAddress = list.get(0);

                double lat = googleAddress.getLatitude();
                double lng = googleAddress.getLongitude();
                LatLng newLatLng = new LatLng(lat,lng);

                String zipCode = "";
                try {
                    Geocoder myLocation = new Geocoder(getApplicationContext(), Locale.getDefault());
                    List<Address> addresses = myLocation.getFromLocation(newLatLng.latitude, newLatLng.longitude, 1);
                    Address address = addresses.get(0);
                    newAddress = "";
                    for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        newAddress += address.getAddressLine(i) + "~";
                    }
                    zipCode = address.getPostalCode();
                } catch (java.io.IOException e) {
                    Toast toast = Toast.makeText(this, "Invalid address, could not save listing", Toast.LENGTH_LONG);
                    toast.show();
                }

                ParseObject parseListing;

                parseListing = new ParseObject("RealEstateListings");
                parseListing.put("Description", newDesc);
                parseListing.put("Address", newAddress);
                parseListing.put("Latitude", newLatLng.latitude);
                parseListing.put("Longitude", newLatLng.longitude);
                if (newPrice > 0) {
                    parseListing.put("Price", newPrice);
                }
                if (newSqFt > 0) {
                    parseListing.put("Sqft", newSqFt);
                }
                if (newNbrBedrooms > 0) {
                    parseListing.put("NbrBedrooms", newNbrBedrooms);
                }
                parseListing.put("ZipCode", zipCode);
                parseListing.saveInBackground();

                // reset SaveListingActivity fields
                addressView.setText("");
                descView.setText("");
                priceView.setText("");
                sqftView.setText("");
                bedroomsView.setText("");

                // goto google map screen
                Intent intent = new Intent(this, GoogleMapActivity.class);
                // erase current listing in google maps
                intent.putExtra("SaveListingCommand", "clear");
                startActivity(intent);

                // confirm listing saved
                Toast toast = Toast.makeText(this, "Saved new listing", Toast.LENGTH_LONG);
                toast.show();
            } else {
                Toast toast = Toast.makeText(this, "Invalid address, could not save listing", Toast.LENGTH_LONG);
                toast.show();
            }
        } catch (java.io.IOException e) {
            Toast toast = Toast.makeText(this, "Invalid address, could not save listing", Toast.LENGTH_LONG);
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
                intent = new Intent(this, NewsActivity.class);
                break;
            case 2:
                intent = new Intent(this, GoogleMapActivity.class);
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
        if (position > 0) {
            startActivity(intent);
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
        } catch (NullPointerException e) {
            // do nothing
        }
        mToolBarTextView.setText(R.string.title_activity_save_listing);
    }

    private List<MenuObject> getMenuObjects() {
        List<MenuObject> menuObjects = new ArrayList<>();

        MenuObject close = new MenuObject("Close Menu");
        close.setResource(R.drawable.close);

        MenuObject news = new MenuObject("View News");
        news.setResource(R.drawable.news);

        MenuObject map = new MenuObject("View Map");
        map.setResource(R.drawable.map);

        MenuObject settings = new MenuObject("View Map Settings");
        settings.setResource(R.drawable.search);

        MenuObject filters = new MenuObject("View Listings Filters");
        filters.setResource(R.drawable.marker);

        MenuObject voice = new MenuObject("Google Voice Input");
        voice.setResource(R.drawable.voicesearch);

        menuObjects.add(close);
        menuObjects.add(news);
        menuObjects.add(map);
        menuObjects.add(settings);
        menuObjects.add(filters);
        menuObjects.add(voice);

        return menuObjects;
    }
}
