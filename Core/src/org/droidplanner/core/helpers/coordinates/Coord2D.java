package org.droidplanner.core.helpers.coordinates;

public class Coord2D {
	private double latitude;  // aka x
	private double longitude; // aka y

	public Coord2D(double lat, double lon) {
		set(lat, lon);
	}

	public Coord2D(Coord2D point) {
		set(point);
	}

	public void set(double lat, double lon) {
		this.latitude = lat;
		this.longitude = lon;
	}

	public void set(Coord2D coord) {
		set(coord.latitude, coord.longitude);
	}

	public double getX() {
		return latitude;
	}

	public double getY() {
		return longitude;
	}

	public double getLng() {
		return longitude;
	}

	public double getLat() {
		return latitude;
	}

	@Override
	public String toString() {
		return "lat/lon: " + getLat() + "/" + getLng();
	}

    public Coord2D dot(double scalar){
        return new Coord2D(latitude * scalar, longitude * scalar);
    }

    public Coord2D negate(){
        return new Coord2D(latitude * -1, longitude * -1);
    }

    public Coord2D subtract(Coord2D coord){
        return new Coord2D(latitude - coord.latitude, longitude - coord.longitude);
    }

    public Coord2D sum(Coord2D coord){
        return new Coord2D(latitude + coord.latitude, longitude + coord.longitude);
    }

    public static Coord2D sum(Coord2D... toBeAdded){
        double latitude = 0;
        double longitude = 0;
        for(Coord2D coord: toBeAdded){
            latitude += coord.latitude;
            longitude += coord.longitude;
        }
        return new Coord2D(latitude, longitude);
    }
}
