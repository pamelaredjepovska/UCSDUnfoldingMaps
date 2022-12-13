package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;

//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/**
 * EarthquakeCityMap An application with an interactive map displaying
 * earthquake data. Author: UC San Diego Intermediate Software Development MOOC
 * team
 * 
 * @author Pamela Redjepovska Date: November 2, 2022
 */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this. It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;

	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/**
	 * This is where to find the local tiles, for working without an Internet
	 * connection
	 */
	public static String mbTilesString = "blankLight-1-3.mbtiles";

	// The map
	private UnfoldingMap map;

	// feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	public void setup() {
		size(1100, 600, OPENGL);

		if (offline) {
			map = new UnfoldingMap(this, 250, 50, 700, 500, new MBTilesMapProvider(mbTilesString));
			earthquakesURL = "2.5_week.atom"; // Same feed, saved Aug 7, 2015, for working offline
		} else {
			map = new UnfoldingMap(this, 250, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			// earthquakesURL = "2.5_week.atom";
		}

		map.zoomToLevel(2);
		MapUtils.createDefaultEventDispatcher(this, map);

		// The List you will populate with new SimplePointMarkers
		List<Marker> markers = new ArrayList<Marker>();

		// Use provided parser to collect properties for each earthquake
		// PointFeatures have a getLocation method
		List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);

		// Call createMarker (see below)
		// to create a new SimplePointMarker for each PointFeature in
		// earthquakes. Then add each new SimplePointMarker to the
		// List markers (so that it will be added to the map in the line below)
		for (PointFeature point : earthquakes) {
			// System.out.println(point.getLocation());
			// System.out.println(point.getProperties());

			// Create a marker for this point feature
			SimplePointMarker marker = createMarker(point);
			markers.add(marker);
			// break;
		}

		// Add the markers to the map so that they are displayed
		map.addMarkers(markers);
	}

	/*
	 * createMarker: A suggested helper method that takes in an earthquake feature
	 * and returns a SimplePointMarker for that earthquake
	 * 
	 * In step 3 You can use this method as-is. Call it from a loop in the setup
	 * method.
	 * 
	 * Add the proper styling to each marker based on the magnitude of the
	 * earthquake.
	 */
	private SimplePointMarker createMarker(PointFeature feature) {
		// To print all of the features in a PointFeature (so you can see what they are)
		// uncomment the line below. Note this will only print if you call createMarker
		// from setup
		// System.out.println(feature.getProperties());

		// Create a new SimplePointMarker at the location given by the PointFeature
		SimplePointMarker marker = new SimplePointMarker(feature.getLocation());

		Object magObj = feature.getProperty("magnitude");
		float mag = Float.parseFloat(magObj.toString());

		// Style the marker's size and color
		// according to the magnitude of the earthquake.
		// Don't forget about the constants THRESHOLD_MODERATE and
		// THRESHOLD_LIGHT, which are declared above.
		// Rather than comparing the magnitude to a number directly, compare
		// the magnitude to these variables (and change their value in the code
		// above if you want to change what you mean by "moderate" and "light")
		if (mag < THRESHOLD_LIGHT) // Minor - blue, small markers
		{
			// Here is an example of how to use Processing's color method to generate
			// an integer that represents the color blue.
			marker.setColor(color(0, 0, 255));
			marker.setRadius(5.0f);
		} else if (mag >= THRESHOLD_LIGHT && mag < THRESHOLD_MODERATE) // Light - yellow, medium markers
		{
			marker.setColor(color(255, 255, 0));
			marker.setRadius(10.0f);
		} else // Moderate and high - red, large markers
		{
			marker.setColor(color(255, 0, 0));
			marker.setRadius(15.0f);
		}

		// Finally return the marker
		return marker;
	}

	public void draw() {
		background(10);
		map.draw();
		addKey();
	}

	// Helper method to draw key in GUI
	private void addKey() {
		// Remember you can use Processing's graphics methods here

		// The rectangle
		fill(255);
		rect(50, 50, 150, 250);

		// The title
		fill(0);
		textSize(16);
		text("Earthquake Key", 65, 70);

		// Marker and text for moderate and high
		fill(255, 0, 0);
		ellipse(65, 100, 20, 20);
		fill(0);
		textSize(14);
		text("5.0+ Magnitude", 85, 105);

		// Marker and text for light
		fill(255, 255, 0);
		ellipse(65, 150, 16, 16);
		fill(0);
		textSize(14);
		text("4.0+ Magnitude", 85, 155);

		// Marker and text for minor
		fill(0, 0, 255);
		ellipse(65, 200, 12, 12);
		fill(0);
		textSize(14);
		text("Below 4.0+", 85, 205);

	}
}
