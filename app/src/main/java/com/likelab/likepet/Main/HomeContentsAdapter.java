package com.likelab.likepet.Main;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.likelab.likepet.HomeFeed.HomeFeedActivity;
import com.likelab.likepet.R;
import com.likelab.likepet.global.RecycleUtils;
import com.likelab.likepet.volleryCustom.AppController;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by kpg1983 on 2015-11-12.
 */
public class HomeContentsAdapter extends BaseAdapter {

    Context context;
    LayoutInflater inflater;
    ArrayList<HomeContents> contentsArrayList;
    int layout;

    RequestQueue queue;
    ImageLoader imageLoader;

    //멤버변수로 해제할 Set을 생성
    private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();


    public HomeContentsAdapter(Context mContext, int mLayout, ArrayList<HomeContents> contentsArrayList) {

        context = mContext;
        inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        this.contentsArrayList = contentsArrayList;
        layout = mLayout;

        queue = AppController.getInstance().getRequestQueue();
        imageLoader = AppController.getInstance().getImageLoader();

    }

    //onDestory에서 쉽게 해제할 수 있도록 메소드 생성
    public void recycle() {

        for (WeakReference<View> ref : mRecycleList) {
            RecycleUtils.recursiveRecycle(ref.get());

        }
    }

        @Override
    public int getCount() {

        return contentsArrayList.size();
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public HomeContents getItem(int position) {
        return contentsArrayList.get(position);
    }


    //16:9
    class ViewHolder {

        ImageView imgLeftContents;
        TextView txtLeftFirstTag;
        TextView txtLeftSecondTag;

        RelativeLayout container_16_9;

    }

    //1:1 정사각형
    //왼쪽 오른쪽 두개의 컨텐츠가 들어간다
    class viewHolderSquare {

        ImageView imgLeftContents;
        ImageView imgRightContents;

        TextView txtLeftFirstTag;
        TextView txtLeftSecondTag;
        
        TextView txtRightFirstTag;
        TextView txtRightSecondTag;

        RelativeLayout container_1_1_left;
        RelativeLayout container_1_1_right;

    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {

        View itemLayout = null;
        View itemLayoutSquare = null;

        ViewHolder viewHolder = new ViewHolder();
        viewHolderSquare viewHolderSquare = new viewHolderSquare();

        if (contentsArrayList.get(position).thumbnailType.equals("1")) {
            if (itemLayout == null) {

                itemLayout = inflater.inflate(R.layout.home_odd_listview, null);

                viewHolder.imgLeftContents = (ImageView) itemLayout.findViewById(R.id.home_img_left_contents);
                viewHolder.txtLeftFirstTag = (TextView) itemLayout.findViewById(R.id.home_txt_1_1_left_first_tag);
                viewHolder.txtLeftSecondTag = (TextView) itemLayout.findViewById(R.id.home_txt_1_1_left_second_tag);

                viewHolder.container_16_9 = (RelativeLayout) itemLayout.findViewById(R.id.home_16_9_container_1);

                itemLayout.setTag(viewHolder);

            } else {

                viewHolder = (ViewHolder) itemLayout.getTag();

            }


            //viewHolder.imgLeftContents.setImageUrl(contentsArrayList.get(position).thumbnailUrl, imageLoader);
            Picasso.with(context)
                    .load(contentsArrayList.get(position).thumbnailUrl)
                    .resize(960, 480)
                    .placeholder(R.drawable.place_holder_960)
                    .centerCrop()
                    .into(viewHolder.imgLeftContents);



            //사진 1개 리스트
            //#문자를 이용하여 태그의 갯수를 파악하고 리스트에 표시한다. 태그가 없는 경우는 #을 붙이지 않거나 아예 나타내지 않는다
            String[] tag = contentsArrayList.get(position).description.split("#");

            if(tag.length == 2) {
                viewHolder.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolder.txtLeftSecondTag.setVisibility(View.INVISIBLE);
                viewHolder.txtLeftFirstTag.setText("#"+tag[1]);

            } else if(tag.length == 3){

                viewHolder.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolder.txtLeftSecondTag.setVisibility(View.VISIBLE);
                viewHolder.txtLeftFirstTag.setText("#"+tag[1]);
                viewHolder.txtLeftSecondTag.setText("#"+tag[2]);
            } else {

                viewHolder.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolder.txtLeftSecondTag.setVisibility(View.INVISIBLE);
                viewHolder.txtLeftFirstTag.setText(contentsArrayList.get(position).description);
            }

            if(contentsArrayList.get(position).description.length() == 0) {
                viewHolder.txtLeftFirstTag.setVisibility(View.INVISIBLE);
                viewHolder.txtLeftSecondTag.setVisibility(View.INVISIBLE);
            }

            viewHolder.container_16_9.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String groupId = contentsArrayList.get(position).groupId;
                    String pageId = contentsArrayList.get(position).pageId;
                    int feedCount = contentsArrayList.get(position).feedCount;

                    Intent intent = new Intent(context, HomeFeedActivity.class);
                    String tags = contentsArrayList.get(position).description;

                    intent.putExtra("TAGS", tags);

                    intent.putExtra("GROUP_ID", groupId);
                    intent.putExtra("PAGE_ID", pageId);
                    intent.putExtra("FEED_COUNT", feedCount);

                    context.startActivity(intent);
                }
            });




        } else {

            //1:1 정사각형
            if (itemLayoutSquare == null) {

                itemLayoutSquare = inflater.inflate(R.layout.home_even_listview, null);

                viewHolderSquare.imgLeftContents = (ImageView) itemLayoutSquare.findViewById(R.id.home_img_left_contents);
                viewHolderSquare.txtLeftFirstTag = (TextView) itemLayoutSquare.findViewById(R.id.home_txt_1_1_left_first_tag);
                viewHolderSquare.txtLeftSecondTag = (TextView) itemLayoutSquare.findViewById(R.id.home_txt_1_1_left_second_tag);

                viewHolderSquare.imgRightContents = (ImageView) itemLayoutSquare.findViewById(R.id.home_img_right_contents);
                viewHolderSquare.txtRightFirstTag = (TextView) itemLayoutSquare.findViewById(R.id.home_txt_1_1_right_first_tag);
                viewHolderSquare.txtRightSecondTag = (TextView) itemLayoutSquare.findViewById(R.id.home_txt_1_1_right_second_tag);

                viewHolderSquare.container_1_1_left = (RelativeLayout)itemLayoutSquare.findViewById(R.id.home_1_1_left_container);
                viewHolderSquare.container_1_1_right = (RelativeLayout)itemLayoutSquare.findViewById(R.id.home_1_1_right_container);

                itemLayoutSquare.setTag(viewHolderSquare);

            } else {

                viewHolderSquare = (viewHolderSquare)itemLayoutSquare.getTag();

            }

            Picasso.with(context)
                    .load(contentsArrayList.get(position).thumbnailUrl)
                    .resize(480, 480)
                    .placeholder(R.drawable.place_holder_960)
                    .centerCrop()
                    .into(viewHolderSquare.imgLeftContents);

            Picasso.with(context)
                    .load(contentsArrayList.get(position).thumbnailUrlRight)
                    .resize(480, 480)
                    .placeholder(R.drawable.place_holder_960)
                    .centerCrop()
                    .into(viewHolderSquare.imgRightContents);

            //viewHolderSquare.imgLeftContents.setImageUrl(contentsArrayList.get(position).thumbnailUrl, imageLoader);
            //viewHolderSquare.imgRightContents.setImageUrl(contentsArrayList.get(position).thumbnailUrlRight, imageLoader);


            //#문자를 이용하여 태그의 갯수를 파악하고 리스트에 표시한다. 태그가 없는 경우는 #을 붙이지 않거나 아예 나타내지 않는다
            String[] tag = contentsArrayList.get(position).description.split("#");
            if(tag.length == 2) {
                viewHolderSquare.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtLeftSecondTag.setVisibility(View.INVISIBLE);

                viewHolderSquare.txtLeftFirstTag.setText("#"+tag[1]);

            } else if(tag.length == 3){
                viewHolderSquare.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtLeftSecondTag.setVisibility(View.VISIBLE);

                viewHolderSquare.txtLeftFirstTag.setText("#"+tag[1]);
                viewHolderSquare.txtLeftSecondTag.setText("#"+tag[2]);
            } else {
                viewHolderSquare.txtLeftFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtLeftSecondTag.setVisibility(View.INVISIBLE);

                viewHolderSquare.txtLeftFirstTag.setText(contentsArrayList.get(position).description);
            }

            if(contentsArrayList.get(position).description.length() == 0) {
                viewHolderSquare.txtLeftFirstTag.setVisibility(View.INVISIBLE);
                viewHolderSquare.txtLeftSecondTag.setVisibility(View.INVISIBLE);
            }

            String[] tagRight = contentsArrayList.get(position).descriptionRight.split("#");

            if(tagRight.length == 2) {
                viewHolderSquare.txtRightFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtRightSecondTag.setVisibility(View.INVISIBLE);

                viewHolderSquare.txtRightFirstTag.setText("#"+tagRight[1]);

            } else if(tagRight.length == 3){
                viewHolderSquare.txtRightFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtRightSecondTag.setVisibility(View.VISIBLE);

                viewHolderSquare.txtRightFirstTag.setText("#"+tagRight[1]);
                viewHolderSquare.txtRightSecondTag.setText("#"+tagRight[2]);

            } else {
                viewHolderSquare.txtRightFirstTag.setVisibility(View.VISIBLE);
                viewHolderSquare.txtRightSecondTag.setVisibility(View.INVISIBLE);

                viewHolderSquare.txtRightFirstTag.setText(contentsArrayList.get(position).descriptionRight);
            }

            if(contentsArrayList.get(position).descriptionRight.length() == 0) {
                viewHolderSquare.txtRightFirstTag.setVisibility(View.INVISIBLE);
                viewHolderSquare.txtRightSecondTag.setVisibility(View.INVISIBLE);

            }

            viewHolderSquare.container_1_1_left.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String groupId = contentsArrayList.get(position).groupId;
                    String pageId = contentsArrayList.get(position).pageId;
                    int feedCount = contentsArrayList.get(position).feedCount;

                    Intent intent = new Intent(context, HomeFeedActivity.class);

                    String homeTag = contentsArrayList.get(position).description;

                    intent.putExtra("TAGS", homeTag);
                    intent.putExtra("GROUP_ID", groupId);
                    intent.putExtra("PAGE_ID", pageId);
                    intent.putExtra("FEED_COUNT", feedCount);

                    context.startActivity(intent);
                }
            });

            viewHolderSquare.container_1_1_right.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String groupId = contentsArrayList.get(position).groupIdRight;
                    String pageId = contentsArrayList.get(position).pageIdRight;
                    int feedCount = contentsArrayList.get(position).feedCountRight;

                    Intent intent = new Intent(context, HomeFeedActivity.class);

                    String homeTag = contentsArrayList.get(position).descriptionRight;

                    intent.putExtra("TAGS", homeTag);
                    intent.putExtra("GROUP_ID", groupId);
                    intent.putExtra("PAGE_ID", pageId);
                    intent.putExtra("FEED_COUNT", feedCount);

                    context.startActivity(intent);

                }
            });

        }

        //메모리 해제할 View를 추가
        mRecycleList.add(new WeakReference<View>(viewHolder.imgLeftContents));
        mRecycleList.add(new WeakReference<View>(viewHolderSquare.imgLeftContents));
        mRecycleList.add(new WeakReference<View>(viewHolderSquare.imgRightContents));


        if(contentsArrayList.get(position).thumbnailType.equals("0")) {
            return itemLayoutSquare;

        } else {
            return itemLayout;

        }

    }


}
