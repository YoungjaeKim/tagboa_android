package net.tagboa.app.net;

/**
 * Created by Youngjae on 2014-06-02.
 */
public enum TagboaUrl {

	ROOT(""),
	ITEMS("/items"),
	TAG("/tag"),
	URL_LINK("/urllink"), LOGIN("/token");

	/**
	 * 생성자.
	 *
	 * @param text
	 */
	private TagboaUrl(final String text) {
		this.text = text;
	}

	private final String text;

	/**
	 * 연결 주소를 HTTP로 반환. (HTTPS 주소는 {@code toString(true)}를 이용.
	 *
	 * @return
	 */
	public String toString() {
		return this.toString(false);
	}

	/**
	 * 바풀 연결 주소를 반환.
	 *
	 * @param isSecure {@code true} 면 HTTPS, {@code false}면 HTTP.
	 * @return
	 */
	public String toString(Boolean isSecure) {
		return (isSecure ? "https://" : "http://") + TagboaApi.BaseRestUrl + text;
	}

	public String toString(Boolean isSecure, Boolean isIncludeRest) {
		return (isSecure ? "https://" : "http://")
				+ (isIncludeRest
				? TagboaApi.BaseRestUrl
				: TagboaApi.BaseUrl
		) + text;
	}
}
