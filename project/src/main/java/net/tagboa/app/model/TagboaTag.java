package net.tagboa.app.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Youngjae on 2014-06-04.
 */
public class TagboaTag implements Serializable {

	public TagboaTag(){}
	public TagboaTag(String t){
		title = t;
	}


	public int id;
	/// <summary>
	/// 제목
	/// </summary>
	public String title;
	/// <summary>
	/// 동일한 태그 숫자
	/// </summary>
	public Integer count;
	/// <summary>
	/// 언어
	/// </summary>
	public String locale;
	/// <summary>
	/// 부모 태그
	/// </summary>
	public TagboaTag parent;
	/// <summary>
	/// 그룹 ID. 아직 안쓰임. 폴더를 만들 때 쓸 수 있다.
	/// </summary>
	public Integer group;
	/// <summary>
	/// 커리큘럼 관련 태그 여부.
	/// </summary>
	public boolean isCurricular;

	public JSONObject toJson() throws JSONException {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("ID", id);
		jsonObject.put("Title", title);
		jsonObject.put("Count", count);
		jsonObject.put("Locale", locale);
		jsonObject.put("Parent", parent.toJson());
		jsonObject.put("Group", group);
		jsonObject.put("IsCurricular", isCurricular);
		return jsonObject;
	}

	@Override
	public String toString() {
		return group == null ? title : String.format("%s (%s)", title, String.valueOf(group));
	}
}
