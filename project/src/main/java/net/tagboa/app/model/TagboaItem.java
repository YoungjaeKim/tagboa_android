package net.tagboa.app.model;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * 하나의 콘텐츠 아이템 단위.
 * Created by Youngjae on 2014-06-04.
 */
public class TagboaItem {

	public int ID;
	/// <summary>
	/// 제목
	/// </summary>
	public String Title;
	/// <summary>
	/// 장르
	/// </summary>
	public String Genre;
	/// <summary>
	/// 작성자ID
	/// </summary>
	public String Author;
	/// <summary>
	/// 등급
	/// </summary>
	public Double Rating;
	/// <summary>
	/// 부가 설명
	/// </summary>
	public String Description;
	/// <summary>
	/// 접속 횟수
	/// </summary>
	public int ReadCount;

	/// <summary>
	/// 기록 시간
	/// </summary>
	public DateTime Timestamp;

	/// <summary>
	/// 태그 목록
	/// </summary>
	public List<TagboaTag> Tags;

	/// <summary>
	/// 링크 목록
	/// </summary>
	public List<TagboaUrlLink> Links;

	public JSONObject toJson() throws JSONException, UnsupportedEncodingException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", ID);
		jsonObject.put("Title", Title);
		jsonObject.put("Genre", Genre);
		jsonObject.put("Author", Author);
		jsonObject.put("Rating", Rating);
		jsonObject.put("Description", Description);
		jsonObject.put("ReadCount", ReadCount);
		jsonObject.put("Timestamp", Timestamp);
		if (Tags == null || Tags.size() == 0)
			jsonObject.put("Tags", new JSONArray());
		else {
			JSONArray jsonTopics = new JSONArray();
			for (TagboaTag tag : Tags)
				jsonTopics.put(tag.toJson());
			jsonObject.put("Tags", jsonTopics);
		}
		if (Links == null || Links.size() == 0)
			jsonObject.put("Links", new JSONArray());
		else {
			JSONArray jsonTopics = new JSONArray();
			for (TagboaUrlLink link : Links)
				jsonTopics.put(link.toJson());
			jsonObject.put("Links", jsonTopics);
		}
		return jsonObject;
	}

	public static TagboaItem fromJson(JSONObject jsonObject) throws JSONException {
		TagboaItem item = new TagboaItem();
		item.ID = jsonObject.getInt("ID");
		item.Title = jsonObject.getString("Title");
		item.Genre = jsonObject.getString("Genre");
		item.Author = jsonObject.getString("Author");
		item.Rating = jsonObject.getDouble("Rating");
		item.Description = jsonObject.getString("Description");
		item.ReadCount = jsonObject.getInt("ReadCount");
		item.Timestamp = DateTime.parse(jsonObject.getString("Timestamp"));

        if (!jsonObject.isNull("Links")) {
            JSONArray links = jsonObject.getJSONArray("Links");
            item.Links = new ArrayList<TagboaUrlLink>();
            for (int i = 0; i < links.length(); i++) {
                TagboaUrlLink topic = TagboaUrlLink.fromJson(links.getJSONObject(i));
                item.Links.add(topic);
            }
        }
        if (!jsonObject.isNull("Tags")) {
            JSONArray tags = jsonObject.getJSONArray("Tags");
            item.Tags = new ArrayList<TagboaTag>();
            for (int i = 0; i < tags.length(); i++) {
                TagboaTag tag = TagboaTag.fromJson(tags.getJSONObject(i));
                item.Tags.add(tag);
            }
        }
		return item;
	}
}
