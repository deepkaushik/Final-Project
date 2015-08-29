
package scps.nyu.edu.nycrealestate;

// this class is used to draw the map and store the current map data
// (so we can rebuild the current map when going back to the Google Map screen)
// this may be bad sttyle, but it works for now

import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DrawGoogleMap extends GoogleMapActivity {

    Context context;
    GoogleMap map;

    // draw the google map
    public void drawMap(Context context, final GoogleMap tmpMap, LatLng cameraLoc, float cameraZoom) {
        this.context = context;
        this.map = tmpMap;

        // erase old markers on map
        map.clear();

        map.setMyLocationEnabled(true);
        float tilt = GoogleMapData.getCameraTilt();
        // set camera position, zoom and tilt
        CameraPosition x = new CameraPosition(cameraLoc, cameraZoom, tilt, 0);
        map.moveCamera(CameraUpdateFactory.newCameraPosition(x));
        // set map type
        int mapType = GoogleMapData.getMapType();
        map.setMapType(mapType);

        map.getUiSettings().setZoomControlsEnabled(true);

        if (GoogleMapData.getCurrentListing() != null) {
            //double Lat = currentListing.getLocation().latitude;
            //double Lng = currentListing.getLocation().longitude;

            // draw marker for current listing
            map.addMarker(new MarkerOptions()
                    .position(GoogleMapData.getCurrentListing().getLocation()));
        }

        loadSavedListings();

        loadInfoWindow();
    }

    // get list of ParseObjects from NYCApplication.com containing real estate listing data
    private void loadSavedListings() {

        // draw markers for saved listings
        ParseQuery<ParseObject> query = ParseQuery.getQuery("RealEstateListings");

        // select listings based on map filter
        Double maxPrice = GoogleMapData.getTargetMaxPrice();
        Integer targetNbrBedrooms = GoogleMapData.getTargetNbrBedrooms();
        if (maxPrice != null) {
            query.whereLessThanOrEqualTo("Price", maxPrice);
        }
        if (targetNbrBedrooms != null) {
            query.whereEqualTo("NbrBedrooms", targetNbrBedrooms);
        }

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> results, ParseException e) {
                if (e == null) {
                    addSavedListingMarkers(results);
                } else {
                    // something went wrong
                }
            }
        });
    }

    // draw markers for each real estate listing in parseData list
    private void addSavedListingMarkers(List<ParseObject> parseData) {
        // reset list of saved real estate listings
        GoogleMapData.eraseSavedListings();

        // each ParseObject will be a real estate listing
        for (ParseObject listing : parseData) {
            String tmpAddress = listing.getString("Address");
            String tmpDescription = listing.getString("Description");
            double tmpLatitiude = listing.getDouble("Latitude");
            double tmpLongitude = listing.getDouble("Longitude");
            int tmpSqft = listing.getInt("Sqft");
            double tmpPrice = listing.getDouble("Price");
            int tmpNbrBedrooms = listing.getInt("NbrBedrooms");
            LatLng tmpLocation = new LatLng(tmpLatitiude, tmpLongitude);

            RealEstateListing tmpListing = new RealEstateListing(tmpDescription, tmpAddress, tmpLocation, tmpSqft, tmpPrice, tmpNbrBedrooms);
            GoogleMapData.addSavedListing(tmpListing);

            final HashMap<LatLng, RealEstateListing> allListings = GoogleMapData.getSavedListings();
            if (GoogleMapData.getCurrentListing() != null) {
                allListings.put(GoogleMapData.getCurrentListing().getLocation(), GoogleMapData.getCurrentListing());
            }

            // don't add blue marker for saved listing if we already have a red marker for the current address
            if (GoogleMapData.getCurrentListing() == null || !(tmpLocation.equals(GoogleMapData.getCurrentListing().getLocation()))) {
                map.addMarker(new MarkerOptions()
                        .position(tmpLocation)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

    // load info window for each marker
    private void loadInfoWindow() {
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.listing_info_window, null);

                RealEstateListing listingData;

                LatLng markerLocation = marker.getPosition();
                if (GoogleMapData.getCurrentListing() != null && markerLocation.equals(GoogleMapData.getCurrentListing().getLocation())) {
                    listingData = GoogleMapData.getCurrentListing();
                } else {
                    listingData = GoogleMapData.getSavedListings().get(markerLocation);
                }

                TextView descView = (TextView) view.findViewById(R.id.desc);
                TextView addr1View = (TextView) view.findViewById(R.id.address1);
                TextView addr2View = (TextView) view.findViewById(R.id.address2);
                TextView priceView = (TextView) view.findViewById(R.id.price);
                TextView sqftView = (TextView) view.findViewById(R.id.sqft);
                TextView pricepersqftView = (TextView) view.findViewById(R.id.price_per_sqft);
                TextView bedroomView = (TextView) view.findViewById(R.id.nbr_bedrooms);
                TextView latlngView = (TextView) view.findViewById(R.id.lat_lng);

                descView.setText(listingData.getDesc());
                String[] address = listingData.getAddress().split("~");
                addr1View.setText("Address: " + address[0]);
                if (address.length > 1) {
                    addr2View.setText("                  " + address[1]);
                } else {
                    addr2View.setVisibility(View.GONE);
                }
                // blank amounts for current listing are saved as -1, so only print if amounts are positive
                if (listingData.getPrice() > 0) {
                    DecimalFormat df = new DecimalFormat("#,###,###.00");
                    String formattedString = df.format(listingData.getPrice());
                    priceView.setText("Price: " + "$" + formattedString);
                } else {
                    priceView.setVisibility(View.GONE);
                }
                if (listingData.getSquareFeet() > 0) {
                    sqftView.setText("Square Feet: " + listingData.getSquareFeet());
                } else {
                    sqftView.setVisibility(View.GONE);
                }
                if (listingData.getSquareFeet() > 0) {
                    DecimalFormat df = new DecimalFormat("#,###,###.00");
                    Double pricePerSquareFoot;
                    if (listingData.getSquareFeet() != 0) {
                        pricePerSquareFoot = listingData.getPrice()/listingData.getSquareFeet();
                    } else {
                        pricePerSquareFoot = 0.0;
                    }
                    String formattedString = df.format(pricePerSquareFoot);
                    pricepersqftView.setText("Price Per Square Feet: " + "$" + formattedString);
                } else {
                    pricepersqftView.setVisibility(View.GONE);
                }
                int listingNbrBedrooms = listingData.getNumberBedrooms();
                if (listingNbrBedrooms > 0) {
                    bedroomView.setText("Number of Bedrooms: " + listingNbrBedrooms);
                } else {
                    bedroomView.setVisibility(View.GONE);
                }
                double latitude = listingData.getLocation().latitude;
                double longitude = listingData.getLocation().longitude;
                latlngView.setText("Latitude: " + latitude + " Longitude: " + longitude);

                // initalize StreetEasy statistics views
                TextView avgPriceView = (TextView) view.findViewById(R.id.avg_price);
                TextView avgPricePerSqFtView = (TextView) view.findViewById(R.id.avg_price_per_sqft);
                TextView avgSqFtView = (TextView) view.findViewById(R.id.avg_sqft);
                TextView avgWomView = (TextView) view.findViewById(R.id.avg_wom);

                // get zip code from lat lng
                String zipCode = "";
                try {
                    Geocoder myLocation = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = myLocation.getFromLocation(latitude, longitude, 1);
                    zipCode = addresses.get(0).getPostalCode();
                } catch (java.io.IOException e) {
                    // ignore exception
                }

                // read SQL data using zipcode and number of bedrooms in listing as the keys
                SQLHelper helper = new SQLHelper(context, "streeteasy_stats.db");

                Double avgPrice = null;
                Double avgSqFt = null;
                Double avgWom = null;

                StreetEasyAPI streetEastStats = new StreetEasyAPI();

                Cursor cursor = helper.getCursor(zipCode, listingNbrBedrooms);
                cursor.moveToFirst();

                try {
                    if (cursor.moveToNext()) {
                        int columnAvgPrice = cursor.getColumnIndex("AVGPRICE");
                        int columnAvgSqFt = cursor.getColumnIndex("AVGSQFT");
                        int columnAvgWom = cursor.getColumnIndex("AVGWOM");

                        avgPrice = cursor.getDouble(columnAvgPrice);
                        avgSqFt = cursor.getDouble(columnAvgSqFt);
                        avgWom = cursor.getDouble(columnAvgWom);
                    }
                } finally {
                    cursor.close();
                }

                if (avgPrice == null) {
                    streetEastStats.updSQLDatabase(zipCode, Integer.toString(listingNbrBedrooms), context);
                    avgPriceView.setText(context.getResources().getString(R.string.please_wait_msg));
                    avgSqFtView.setVisibility(View.GONE);
                    avgPricePerSqFtView.setVisibility(View.GONE);
                    avgWomView.setVisibility(View.GONE);
                } else {
                    // write out zipcode averages to InfoWindow
                    String aptDesc;
                    if (listingNbrBedrooms > 0) {
                        aptDesc = "for " + listingNbrBedrooms + " Bdrm in " + zipCode + ": ";
                    } else {
                        aptDesc = "for all listings in " + zipCode + ": ";
                    }

                    DecimalFormat df;
                    String formattedString;

                    if (avgPrice != null) {
                        df = new DecimalFormat("#,###,###.00");
                        formattedString = df.format(avgPrice);
                        avgPriceView.setText("Avg Price " + aptDesc + "$" + formattedString);
                    } else {
                        avgPriceView.setVisibility(View.GONE);
                    }

                    if (avgSqFt != null) {
                        df = new DecimalFormat("#,###,###.00");
                        formattedString = df.format(avgSqFt);
                        avgSqFtView.setText("Avg Square Feet " + aptDesc + formattedString);
                    } else {
                        avgSqFtView.setVisibility(View.GONE);
                    }

                    if ((avgPrice != null) && (avgSqFt != null))  {
                        df = new DecimalFormat("#,###,###.00");
                        Double avgPricePerSquareFoot;
                        if (avgSqFt != 0) {
                            avgPricePerSquareFoot = avgPrice/avgSqFt;
                        } else {
                            avgPricePerSquareFoot = 0.0;
                        }
                        formattedString = df.format(avgPricePerSquareFoot);
                        avgPricePerSqFtView.setText("Avg Price Per SqFt " + aptDesc + "$" + formattedString);
                    } else {
                        avgPricePerSqFtView.setVisibility(View.GONE);
                    }

                    if (avgWom != null) {
                        avgWomView.setText("Avg Weeks on Market " + aptDesc + String.format( "%.2f",avgWom));
                    } else {
                        avgWomView.setVisibility(View.GONE);
                    }
                }

                return view;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        });
    }

    public float getCameraZoom() {
        return map.getCameraPosition().zoom;
    }

    public LatLng getCameraLocation() {
        return new LatLng(map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
    }
}