package net.tagboa.app.model;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Youngjae on 2014-06-04.
 */
public class TagboaUrlLink {
	public int ID;
	/// <summary>
	/// URL 주소값
	/// </summary>
	public String Address;
	/// <summary>
	/// 링크 상태
	/// </summary>
	public TagboaUrlLinkStatus Status;
	/// <summary>
	/// 최근 링크 상태 확인 날짜
	/// </summary>
	public DateTime RecentStatusChangedTime;
	/// <summary>
	/// 비공개 여부
	/// </summary>
	public Boolean IsHidden;
	/// <summary>
	/// 간단 소개말
	/// </summary>
	public String Note;

	public JSONObject toJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", ID);
		jsonObject.put("Address", Address);
		jsonObject.put("Status", Status);
		jsonObject.put("RecentStatusChangedTime", RecentStatusChangedTime);
		jsonObject.put("IsHidden", IsHidden);
		jsonObject.put("Note", Note);
		return jsonObject;
	}
}
