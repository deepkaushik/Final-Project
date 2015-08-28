package scps.nyu.edu.nycrealestate;

import com.google.android.gms.maps.model.LatLng;

// this class stores the data for an individual real estate listing
public class RealEstateListing {

    private String desc;
    private String address;
    private LatLng location;
    private int squareFeet;
    private double price;
    private int numberBedrooms;

    // constructor
    public RealEstateListing() {
        desc = "";
        address = "";
        location = null;
        squareFeet = -1;
        price = -1;
        numberBedrooms = -1;
    }

    // constructor with all variables
    public RealEstateListing(String desc, String address, LatLng location, int squareFeet, double price, int numberBedrooms) {
        this.desc = desc;
        this.address = address;
        this.location = location;
        this.squareFeet = squareFeet;
        this.price = price;
        this.numberBedrooms = numberBedrooms;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public int getSquareFeet() {
        return squareFeet;
    }

    public void setSquareFeet(int squareFeet) {
        this.squareFeet = squareFeet;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getNumberBedrooms() {
        return numberBedrooms;
    }

    public void setNumberBedrooms(int numberBedrooms) {
        this.numberBedrooms = numberBedrooms;
    }

    // two RealEstate listings are considered equal only if their LatLng location objects contain the same coodinates
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RealEstateListing that = (RealEstateListing) o;

        return location.equals(that.location);

    }

    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
