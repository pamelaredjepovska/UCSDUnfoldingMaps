package module4;

import java.util.ArrayList;
import java.util.List;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import parsing.ParseFeed;
import processing.core.PApplet;

/**
 * EarthquakeCityMap An application with an interactive map displaying
 * earthquake data. Author: UC San Diego Intermediate Software Development MOOC
 * team
 * 
 * @author Pamela Redjepovska Date: November 5, 2022
 */
public class EarthquakeCityMap extends PApplet {

	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other
	// methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of
	// earthquakes
	// per country.

	// You can ignore this. It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = false;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";

	// The map
	private UnfoldingMap map;

	// Markers for each city
	private List<Marker> cityMarkers;
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;

	public void setup() {
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
			earthquakesURL = "2.5_week.atom"; // The same feed, but saved August 7, 2015
		} else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			// earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);

		// FOR TESTING: Set earthquakesURL to be one of the testing files by
		// uncommenting
		// one of the lines below. This will work whether you are online or offline
		// earthquakesURL = "test1.atom";
		// earthquakesURL = "test2.atom";

		// WHEN TAKING THIS QUIZ: Uncomment the next line
		earthquakesURL = "quiz1.atom";

		// (2) Reading in earthquake data and geometric properties
		// STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);

		// STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		for (Feature city : cities) {
			cityMarkers.add(new CityMarker(city));
		}

		// STEP 3: read in earthquake RSS feed
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
		quakeMarkers = new ArrayList<Marker>();

		for (PointFeature feature : earthquakes) {
			// Check if LandQuake
			if (isLand(feature)) {
				quakeMarkers.add(new LandQuakeMarker(feature));
			}
			// OceanQuakes
			else {
				quakeMarkers.add(new OceanQuakeMarker(feature));
			}
			// break;
		}

		// Could be used for debugging
		printQuakes();

		// (3) Add markers to map
		// NOTE: Country markers are not added to the map. They are used
		// for their geometric properties
		map.addMarkers(quakeMarkers);
		map.addMarkers(cityMarkers);

	} // End setup

	public void draw() {
		background(0);
		map.draw();
		addKey();

	}

	// Helper method to draw key in GUI
	private void addKey() {
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		rect(25, 50, 150, 250);
		
		// Key for city marker map
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", 50, 75);

		fill(color(150, 30, 30));
		triangle(50, 100, 45, 110, 55, 110);
		fill(0, 0, 0);
		text("City Marker", 75, 105);

		fill(color(255, 255, 255));
		ellipse(50, 130, 10, 10);
		fill(0, 0, 0);
		text("Land Quake", 75, 130);

		fill(color(255, 255, 255));
		rect(45, 150, 10, 10);
		fill(0, 0, 0);
		text("Ocean Quake", 75, 155);

		fill(0, 0, 0);
		text("Size ~ Magnitude", 45, 170);

		fill(color(255, 255, 0));
		ellipse(50, 200, 13, 13);
		fill(0, 0, 0);
		text("Shallow", 75, 200);

		fill(color(0, 0, 255));
		ellipse(50, 220, 13, 13);
		fill(0, 0, 0);
		text("Intermediate", 75, 220);

		fill(color(255, 0, 0));
		ellipse(50, 240, 13, 13);
		fill(0, 0, 0);
		text("Deep", 75, 240);

		noFill();
		ellipse(50, 260, 13, 13);
		line(45, 255, 55, 265);
		line(55, 255, 45, 265);
		fill(0, 0, 0);
		text("Past Hour", 75, 260);

	}

	// Checks whether this quake occurred on land. If it did, it sets the
	// "country" property of its PointFeature to the country where it occurred
	// and returns true. Notice that the helper method isInCountry will
	// set this "country" property already. Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {

		// Loop over all the country markers.
		// For each, check if the earthquake PointFeature is in the
		// country in m. Notice that isInCountry takes a PointFeature
		// and a Marker as input.
		// If isInCountry ever returns true, isLand should return true.

		for (Marker country : countryMarkers) {
			// Finish this method using the helper method isInCountry
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}

		// Nnot inside any country
		return false;
	}

	/*
	 * prints countries with number of earthquakes as Country1: numQuakes1 Country2:
	 * numQuakes2 ... OCEAN QUAKES: numOceanQuakes
	 */
	private void printQuakes() {

		int waterQuakes = quakeMarkers.size();
		System.out.println("Ocean quakes: " + waterQuakes);
		// int i = 0;

		for (Marker cm : countryMarkers) {

			int quakeCounter = 0;
			String countryName = cm.getStringProperty("name");
			for (Marker marker : quakeMarkers) {
				EarthquakeMarker quakeMarker = (EarthquakeMarker) marker;
				if (quakeMarker.isOnLand()) {
					if (countryName.equals(quakeMarker.getStringProperty("country"))) {
						quakeCounter++;
					}
				}
				// break;
			}

			if (quakeCounter > 0) {
				System.out.println("Country: " + countryName + "\tNumber of earthquakes: " + quakeCounter);
				waterQuakes -= quakeCounter;
			}
			// i++;
		}

		System.out.println("Ocean quakes: " + waterQuakes);

	}

	// Helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake
	// feature if it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// Getting location of feature
		Location checkLoc = earthquake.getLocation();

		// Some countries represented it as MultiMarker
		// Looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if (country.getClass() == MultiMarker.class) {

			// Looping over markers making up MultiMarker, one country - multiple
			// markers/coordinates
			for (Marker marker : ((MultiMarker) country).getMarkers()) {

				// Checking if inside
				if (((AbstractShapeMarker) marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));

					// Return if is inside one
					return true;
				}
			}
		}

		// Check if inside country represented by SimplePolygonMarker, one country - one
		// pair of coordinates
		else if (((AbstractShapeMarker) country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));

			return true;
		}
		return false;
	}

}
