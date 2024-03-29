package hu.qgears.quickjs.qpage;

import java.awt.Point;

import org.json.JSONObject;

/**
 * All info known about the browser window size
 */
public class BrowserWindowSize {
	/**
	 * window.innerWidth, window.innerHeight
	 */
	public Point innerSize;
	/** window.devicePixelRatio (current zoom setting) */
	public double devicePixelRatio;
	/**
	 * window.screen.width, window.screen.height
	 */
	public Point screenSize;
	public BrowserWindowSize(JSONObject post) {
		innerSize=new Point(post.getInt("width"), post.getInt("height"));
		devicePixelRatio=post.getDouble("devicePixelRatio");
		screenSize=new Point(post.getInt("screenWidth"), post.getInt("screenHeight"));
	}
	@Override
	public String toString() {
		return "window.inner: "+innerSize+" screen: "+screenSize+" devicePixelRatio: "+devicePixelRatio+" real screen resolution: "+getResolution();
	}
	private Point getResolution() {
		return new Point((int) Math.round(devicePixelRatio*screenSize.x), (int) Math.round(devicePixelRatio*screenSize.y));
	}
}
