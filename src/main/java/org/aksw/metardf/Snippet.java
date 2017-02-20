package org.aksw.metardf;

import org.json.*;

public class Snippet {

	public void test() {

		JSONObject obj = new JSONObject();
		String pageName = obj.getJSONObject("pageInfo").getString("pageName");

		JSONArray arr = obj.getJSONArray("posts");
		for (int i = 0; i < arr.length(); i++) {
			String post_id = arr.getJSONObject(i).getString("post_id");
		}
	}
}
