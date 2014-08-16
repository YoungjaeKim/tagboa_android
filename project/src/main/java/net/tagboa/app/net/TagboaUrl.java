package net.tagboa.app.net;

/**
 * 태그보아 관련 URL 모음
 * Created by Youngjae on 2014-06-02.
 */
public enum TagboaUrl {

	ROOT(""),
	ITEMS("/items"),
	TAG("/tag"),
	URL_LINK("/urllink"),
    LOGIN("/token"),
    ITEM("/item"),
    TAGS("/tags"),
    EXTERNAL_LOGINS("/Account/ExternalLogins"),
    USER_INFO("/Account/UserInfo"),
    REGISTER_EXTERNAL_LOGINS("/Account/RegisterExternal"),
    EXTERNAL_LOGIN("/Account/ExternalLogin"),
    FACEBOOK_LOGIN("/Account/FacebookLogin");

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
