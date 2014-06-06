package net.tagboa.app.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.tokenautocomplete.TokenCompleteTextView;
import net.tagboa.app.R;
import net.tagboa.app.model.TagboaTag;

/**
 * 태그 자동완성.
 * Created by Youngjae on 2014-06-05.
 */
public class TagCompletionView  extends TokenCompleteTextView {
	// https://github.com/splitwise/TokenAutoComplete 참고.

	public TagCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected View getViewForObject(Object object) {
		TagboaTag t = (TagboaTag)object;

		LayoutInflater l = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		LinearLayout view = (LinearLayout)l.inflate(R.layout.token_tag, (ViewGroup)TagCompletionView.this.getParent(), false);
		((TextView)view.findViewById(R.id.name)).setText(t.title);

		return view;
	}

	@Override
	protected Object defaultObject(String completionText) {
		//Stupid simple example of guessing if we have an email or not
		return new TagboaTag(completionText);
	}
}
