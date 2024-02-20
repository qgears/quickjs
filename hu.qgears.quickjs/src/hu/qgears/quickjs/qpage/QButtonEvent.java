package hu.qgears.quickjs.qpage;

import org.json.JSONObject;

public class QButtonEvent {
	public final QButton host;
	public final int offsetX;
	public final int offsetY;
	public final int button;
	public QButtonEvent(QButton qButton, JSONObject post) {
		this.host=qButton;
		this.offsetX=post.getInt("offsetX");
		this.offsetY=post.getInt("offsetY");
		this.button=post.getInt("button");
	}
	@Override
	public String toString() {
		return "Button ev on: "+host.getId()+" "+offsetX+" "+offsetY+" "+button;
	}
}
