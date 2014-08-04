package net.tagboa.app.page;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.tagboa.app.R;
import net.tagboa.app.BaseActivity;
import net.tagboa.app.model.TagboaItem;

import java.util.List;

/**
 * 질문 어댑터.
 * User: Youngjae
 * Date: 13. 4. 10
 * Time: 오전 1:08
 */
public class ItemAdapter extends ArrayAdapter<TagboaItem> {

	int resource;
	BaseActivity mActivity;
	private Context mContext;

	//Initialize adapter
	public ItemAdapter(Context context, int resource, List<TagboaItem> items) {
		super(context, resource, items);
		mContext = context;
		this.resource = resource;
		mActivity = (BaseActivity) context;
	}

	/**
	 * 아이템 하나에 대한 출력 설정.
	 *
	 * @param position
	 * @param convertView
	 * @param parent
	 * @return
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemViewHolder viewHolder;

		RelativeLayout relativeLayout;
		//Get the current alert object
		final TagboaItem item = getItem(position);

		//Inflate the view
		if (convertView == null) {
			relativeLayout = new RelativeLayout(getContext());
			String inflater = Context.LAYOUT_INFLATER_SERVICE;
			LayoutInflater vi;
			vi = (LayoutInflater) getContext().getSystemService(inflater);
			vi.inflate(resource, relativeLayout, true);
			convertView = relativeLayout;

			// UI 요소 가져오기.
			viewHolder = new ItemViewHolder();
			viewHolder.itemHeader = (LinearLayout) convertView.findViewById(R.id.linearLayoutQuestionItemHeader);
            viewHolder.itemHeaderText = (TextView) convertView.findViewById(R.id.textViewQuestionItemHeader);
            viewHolder.itemTitle = (TextView) convertView.findViewById(R.id.textViewQuestionItemTitle);
            viewHolder.itemTime = (TextView) convertView.findViewById(R.id.textViewQuestionItemTime);
            viewHolder.itemDescription = (TextView) convertView.findViewById(R.id.textViewQuestionItemDescription);
            viewHolder.itemTopics = (TextView) convertView.findViewById(R.id.textViewQuestionItemTopics);
            viewHolder.itemFace = (ImageView) convertView.findViewById(R.id.imageViewQuestionItemFace);
            viewHolder.itemPreview = (ImageView) convertView.findViewById(R.id.imageViewQuestionItemPreview);
			viewHolder.itemLabel1 = (TextView) convertView.findViewById(R.id.textViewQuestionItemLabel1);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ItemViewHolder) convertView.getTag();
		}
		viewHolder.position = position;
		viewHolder.id = item.ID;

		// 내용 할당.
		viewHolder.itemTitle.setText(item.Title);

		viewHolder.itemDescription.setText(item.Description);

		return convertView;
	}


	/**
	 * Question용 ViewHolder 패턴 헬퍼클래스.
	 * Created by Youngjae on 13. 11. 09.
	 */
	public static class ItemViewHolder {
		// ViewHolder 패턴 적용.
		// http://lucasr.org/2012/04/05/performance-tips-for-androids-listview/
		// http://android.amberfog.com/?p=296
		public int position;
		public int id;

		public LinearLayout itemHeader;
		public TextView itemTitle;
		public TextView itemTime;
		public TextView itemDescription;
		public TextView itemTopics;
		public ImageView itemFace;
		public ImageView itemPreview;
		public TextView itemHeaderText;
		public TextView itemLabel1;
	}
}