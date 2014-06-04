package net.tagboa.app.listener;

/**
 * 리프래시 관련 이벤트 리스너
 * Created by Youngjae on 2014-06-02.
 */
public interface OnRefreshListener {
	public abstract void onRefresh();
	public abstract void onError();
	public abstract void onUnAuthorized();
}
