package net.tagboa.app.model;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 동영상 주소
 * Created by Youngjae on 2014-06-04.
 */
public class TagboaUrlLink {
	public int ID;
	/// <summary>
	/// URL 주소값
	/// </summary>
	public String Address = "";
	/// <summary>
	/// 링크 상태
	/// </summary>
	public TagboaUrlLinkStatus Status = TagboaUrlLinkStatus.Normal;
	/// <summary>
	/// 최근 링크 상태 확인 날짜
	/// </summary>
	public DateTime RecentStatusChangedTime;
	/// <summary>
	/// 비공개 여부
	/// </summary>
	public Boolean IsHidden = false;
	/// <summary>
	/// 간단 소개말
	/// </summary>
	public String Note = "";

	public JSONObject toJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", ID);
		jsonObject.put("Address", Address);
		jsonObject.put("Status", Status.ordinal());
		jsonObject.put("RecentStatusChangedTime", RecentStatusChangedTime);
		jsonObject.put("IsHidden", IsHidden);
		jsonObject.put("Note", Note);
		return jsonObject;
	}

    public static TagboaUrlLink fromJson(JSONObject jsonObject) throws JSONException {
        TagboaUrlLink item = new TagboaUrlLink();
        item.ID = jsonObject.getInt("ID");
        item.Address = jsonObject.getString("Address");
        item.Status = TagboaUrlLinkStatus.values()[jsonObject.getInt("Status")];
        item.RecentStatusChangedTime = !jsonObject.isNull("RecentStatusChangedTime") ? DateTime.parse(jsonObject.getString("RecentStatusChangedTime")) : null;
        item.IsHidden = jsonObject.getBoolean("IsHidden");
        item.Note = jsonObject.getString("Note");
        return item;
    }
}
