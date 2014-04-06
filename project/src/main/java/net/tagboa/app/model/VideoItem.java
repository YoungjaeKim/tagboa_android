package net.tagboa.app.model;

/**
 * 한 줄의 데이터
 * Created by Youngjae on 2014-04-06.
 */
public class VideoItem {
	public VideoItem(){}

	/**
	 * 생성자
	 * @param t 제목
	 * @param c 차시
	 * @param u1 첫번째 URL
	 * @param u2 두번째 URL
	 * @param ts 태그목록. 쉽표로 구분됨.
	 */
	public VideoItem(String t, String c, String u1, String u2, String ts){
		title = t;
		curriculum = c;
		url1 = u1;
		url2 = u2;
		tags = ts.split(",");
	}
	public String title;
	public String curriculum;
	public String[] tags;
	public String url1;
	public String url2;

	@Override
	public String toString() {
		return String.format("%s (%s): %s", title, curriculum, tags);
	}
}
