package module4;

import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import processing.core.PGraphics;

/**
 * Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 *
 */
public abstract class EarthquakeMarker extends SimplePointMarker {

	// Did the earthquake occur on land? This will be set by the subclasses.
	protected boolean isOnLand;

	// SimplePointMarker has a field "radius" which is inherited
	// by Earthquake marker:
	// protected float radius;
	//
	// You will want to set this in the constructor, either
	// using the thresholds below, or a continuous function
	// based on magnitude.

	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;

	// ADD constants for colors

	// Abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);

	// Constructor
	public EarthquakeMarker(PointFeature feature) {
		super(feature.getLocation());
		// Add a radius property and then set the properties
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2 * magnitude);
		setProperties(properties);
		this.radius = 1.75f * getMagnitude();
	}

	// Calls abstract method drawEarthquake and then checks age and draws X if
	// needed
	public void draw(PGraphics pg, float x, float y) {
		// Save previous styling
		pg.pushStyle();

		// Determine color of marker from depth
		colorDetermine(pg);

		// Call abstract method implemented in child class to draw marker shape
		drawEarthquake(pg, x, y);

		// Draw X over marker if within past day
		if (this.getProperty("age").equals("Past Hour")) {
			// pg.strokeWeight(6);
			pg.line(x - this.radius, y - this.radius, x + this.radius, y + this.radius);
			// pg.strokeWeight(6);
			pg.line(x + this.radius, y - this.radius, x - this.radius, y + this.radius);
		}

		// Reset to previous styling
		pg.popStyle();

	}

	// Determine color of marker from depth, and set pg's fill color
	// using the pg.fill method.
	// We suggest: Deep = red, intermediate = blue, shallow = yellow
	// But this is up to you, of course.
	// You might find the getters below helpful.
	private void colorDetermine(PGraphics pg) {
		float d = this.getDepth();

		// Shallow - yellow
		if (d > 0 && d <= THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		} else if (d > THRESHOLD_INTERMEDIATE && d <= THRESHOLD_DEEP) // Intermediate - blue
		{
			pg.fill(0, 0, 255);
		} else // Deep - red
		{
			pg.fill(255, 0, 0);
		}
	}

	/*
	 * Getters for earthquake properties
	 */

	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}

	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());
	}

	public String getTitle() {
		return (String) getProperty("title");

	}

	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}

	public boolean isOnLand() {
		return isOnLand;
	}

}
