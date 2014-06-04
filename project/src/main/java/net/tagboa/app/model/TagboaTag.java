package net.tagboa.app.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Youngjae on 2014-06-04.
 */
public class TagboaTag {

	public int ID;
	/// <summary>
	/// 제목
	/// </summary>
	public String Title;
	/// <summary>
	/// 동일한 태그 숫자
	/// </summary>
	public Integer Count;
	/// <summary>
	/// 언어
	/// </summary>
	public String Locale;
	/// <summary>
	/// 부모 태그
	/// </summary>
	public TagboaTag Parent;
	/// <summary>
	/// 그룹 ID. 아직 안쓰임. 폴더를 만들 때 쓸 수 있다.
	/// </summary>
	public Integer Group;
	/// <summary>
	/// 커리큘럼 관련 태그 여부.
	/// </summary>
	public boolean IsCurricular;

	public JSONObject toJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", ID);
		jsonObject.put("Title", Title);
		jsonObject.put("Count", Count);
		jsonObject.put("Locale", Locale);
		jsonObject.put("Parent", Parent.toJson());
		jsonObject.put("Group", Group);
		jsonObject.put("IsCurricular", IsCurricular);
		return jsonObject;
	}
}
