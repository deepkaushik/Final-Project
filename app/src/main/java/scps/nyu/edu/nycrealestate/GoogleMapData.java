package scps.nyu.edu.nycrealestate;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

// this class is used to store settings for the google map, both the map settings and the current data
// (so we can rebuild the current map when going back to the Google Map screen)
// this may be bad style, but it works for now

public class GoogleMapData {

    // current real estate listings for google map
    private static RealEstateListing currentListing = null;
    private static HashMap<LatLng, RealEstateListing> savedListings = new HashMap<>();

    // current camera data
    private static float cameraZoom = 15;
    private static LatLng cameraLoc = new LatLng(40.734457, -73.993886);

    // map settings
    private static Integer mapType = GoogleMap.MAP_TYPE_NORMAL;
    private static Integer cameraTilt = 0;

    //filter for displaying real estate listings
    private static Double targetMaxPrice = null;
    private static Integer targetNbrBedrooms = null;

    public static Integer getCameraTilt() {
        return cameraTilt;
    }

    public static void setCameraTilt(Integer cameraTilt) {
        if (cameraTilt < 0 || cameraTilt > 90)  {
            throw new IllegalArgumentException("Camera tilt must be a number between 0 and 90 degrees");
        } else {
            GoogleMapData.cameraTilt = cameraTilt;
        }
    }

    public static String getMapName() {
        String mapName;
        switch (mapType) {
            case GoogleMap.MAP_TYPE_NORMAL :
                mapName = "Normal";
                break;
            case GoogleMap.MAP_TYPE_HYBRID :
                mapName = "Hybrid";
                break;
            case GoogleMap.MAP_TYPE_SATELLITE :
               mapName = "Satellite";
                break;
            case GoogleMap.MAP_TYPE_TERRAIN :
                mapName = "Terrain";
                break;
            default:
                throw new IllegalArgumentException();
        }
        return mapName;
    }

    public static Integer getMapType() {
        return mapType;
    }

    public static void setMapType(String mapName) {
        switch (mapName) {
            case "Normal":
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                break;
            case "Hybrid":
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            case "Satellite":
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            case "Terrain":
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static Double getTargetMaxPrice() {
        return targetMaxPrice;
    }

    public static void setTargetMaxPrice(Double targetMaxPrice) {
        GoogleMapData.targetMaxPrice = targetMaxPrice;
    }

    public static Integer getTargetNbrBedrooms() {
        return targetNbrBedrooms;
    }

    public static void setTargetNbrBedrooms(Integer targetNbrBedrooms) {
        GoogleMapData.targetNbrBedrooms = targetNbrBedrooms;
    }

    public static float getCameraZoom() {
        return cameraZoom;
    }

    public static void setCameraZoom(float cameraZoom) {
        GoogleMapData.cameraZoom = cameraZoom;
    }

    public static LatLng getCameraLoc() {
        return new LatLng(cameraLoc.latitude, cameraLoc.longitude);
    }

    public static void setCameraLoc(LatLng cameraLoc) {
        GoogleMapData.cameraLoc = cameraLoc;
    }

    public static boolean isCurrentLocInSavedListings(LatLng currentLocation)  {
              return savedListings.containsKey(currentLocation);
    }

    public static RealEstateListing getCurrentListing() {
        return currentListing;
    }

    public static void eraseCurrentListing() {
        currentListing = null;
    }

    public static void setCurrentListing(String currentAddress, LatLng currentLocation) {
        if  (isCurrentLocInSavedListings(currentLocation)) {
            currentListing = savedListings.get(currentLocation);
        }  else {
            currentListing = new RealEstateListing("New Listing", currentAddress, currentLocation, -1, -1, -1);
        }
    }

    public static void addSavedListing(RealEstateListing newListing) {
        savedListings.put(newListing.getLocation(), newListing);
    }

    public static HashMap<LatLng, RealEstateListing> getSavedListings() {
        return new HashMap<>(savedListings);
    }

    public static void eraseSavedListings() {
        savedListings.clear();
    }


}
