package hu.qgears.quickjs.qpage;

import org.json.JSONObject;

public class JSONHelper {

	public static String getStringSafe(JSONObject post, String string) {
		if(post.has(string))
		{
			return post.getString(string);
		}
		return null;
	}

}
