package com.lulu.puzzlegame.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lulu.puzzlegame.R;
import com.lulu.puzzlegame.mode.ImagePiece;
import com.lulu.puzzlegame.utils.ImageSplitterUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * create by zyj
 * on 2019/6/12
 * <p>
 * 拼图 布局
 **/
public class PuzzleGameLayout extends RelativeLayout implements View.OnClickListener {

    //分成几行几列
    private int mColumn = 4;

    //外边框
    private int mMargin;
    //内边框
    private int mPadding;

    //每个item 宽和高
    private int mItemWidth;

    //子view
    private ImageView[] mPuzzleGameItems;

    //游戏图片
    private Bitmap mBitmap;

    //切个后的碎片
    private List<ImagePiece> mItemBitmaps;

    //防止加载多次
    private boolean onece;

    //游戏面板的宽度
    private int mWidth;


    public PuzzleGameLayout(Context context) {
        this(context, null);
    }

    public PuzzleGameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleGameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
        mPadding = min(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //取宽度和高度的最小值
        mWidth = Math.min(getMeasuredWidth(), getMeasuredHeight());

        if (!onece) {
            //进行切图以及排序
            initBitmap();
            // 设置ImageView(Item)的宽高等属性
            initItem();
            onece = true;
        }

        //重新设定宽高
        setMeasuredDimension(mWidth, mWidth);

    }

    /**
     * 进行切图以及排序
     */
    private void initBitmap() {
        if (mBitmap == null) {
            mBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.image);
        }
        mItemBitmaps = ImageSplitterUtil.spliteImage(mBitmap, mColumn);
        // 使用sort完成我们的乱序
        Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {
            @Override
            public int compare(ImagePiece a, ImagePiece b) {
                return Math.random() > 0.5 ? 1 : -1;
            }
        });
    }

    /**
     * 设置ImageView(Item)的宽高等属性
     */
    private void initItem() {
        mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        mPuzzleGameItems = new ImageView[mColumn * mColumn];

        for (int i = 0; i < mPuzzleGameItems.length; i++) {
            ImageView item = new ImageView(getContext());
            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
            mPuzzleGameItems[i] = item;
            item.setId(i + 1);
            //在item中存储 index
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
            //设置item的位置和大小
            RelativeLayout.LayoutParams params = new LayoutParams(mItemWidth, mItemWidth);
            //设置item 横向间隙
            //不是最后一列
            if ((i + 1) % mColumn != 0) {
                //设置rightMargin
                params.rightMargin = mMargin;
            }
            //不是第一列，全部放在前一列的后面
            if (i % mColumn != 0) {
                params.addRule(RelativeLayout.RIGHT_OF, mPuzzleGameItems[i - 1].getId());
            }
            //不是第一行就设置topMargin 和 rule在前一行下方
            if ((i + 1) > mColumn) {
                params.topMargin = mMargin;
//                params.addRule(RelativeLayout.BELOW, mPuzzleGameItems[i - 1].getId());
                //加入为012  第二排为 345  3对应的上一个为0  3-mColumn =0
                params.addRule(RelativeLayout.BELOW, mPuzzleGameItems[i - mColumn].getId());
            }

            addView(item, params);
        }
    }

    /**
     * 获取最小值
     *
     * @param params
     * @return
     */
    private int min(int... params) {
        int minValue = params[0];
        for (int i = 0; i < params.length; i++) {
            int param = params[i];
            if (param < minValue) {
                minValue = param;
            }
        }
        return minValue;
    }

    //第一次点击选中的图片
    private ImageView mFirstImage;
    //第二次点击选中的图片
    private ImageView mSecondImage;

    @Override
    public void onClick(View v) {

        //两次点击同一个，就取消
        if (mFirstImage == v) {
            mFirstImage.setColorFilter(null);
            mFirstImage = null;
            return;
        }

        if (mFirstImage == null) {
            mFirstImage = (ImageView) v;
            mFirstImage.setColorFilter(Color.parseColor("#55ff0000"));
        } else {
            mSecondImage = (ImageView) v;
            //交换item
            exchangeItem();
        }

    }


    /**
     * 动画层
     */
    private RelativeLayout mAnimLayout;
    private boolean isAniming;

    /**
     * 交换item
     */
    private void exchangeItem() {
        mFirstImage.setColorFilter(null);

        // 构造我们的动画层
        setUpAnimLayout();

        ImageView first = new ImageView(getContext());
        final Bitmap firstBitmap = mItemBitmaps.get(getImageIdByTag((String) mFirstImage.getTag())).getBitmap();
        first.setImageBitmap(firstBitmap);
        LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
        lp.leftMargin = mFirstImage.getLeft() - mPadding;
        lp.topMargin = mFirstImage.getTop() - mPadding;
        first.setLayoutParams(lp);
        mAnimLayout.addView(first);

        ImageView second = new ImageView(getContext());
        final Bitmap secondBitmap = mItemBitmaps.get(getImageIdByTag((String) mSecondImage.getTag())).getBitmap();
        second.setImageBitmap(secondBitmap);
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
        lp2.leftMargin = mSecondImage.getLeft() - mPadding;
        lp2.topMargin = mSecondImage.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimLayout.addView(second);

        // 设置动画
        TranslateAnimation anim = new TranslateAnimation(0,
                mSecondImage.getLeft() - mFirstImage.getLeft(),
                0, mSecondImage.getTop() - mFirstImage.getTop());
        anim.setDuration(300);
        anim.setFillAfter(true);
        first.startAnimation(anim);

        TranslateAnimation animSecond = new TranslateAnimation(0,
                -mSecondImage.getLeft() + mFirstImage.getLeft(), 0, -mSecondImage.getTop()
                + mFirstImage.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        second.startAnimation(animSecond);

        // 监听动画
        anim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                mFirstImage.setVisibility(View.INVISIBLE);
                mSecondImage.setVisibility(View.INVISIBLE);

                isAniming = true;
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {

                String firstTag = (String) mFirstImage.getTag();
                String secondTag = (String) mSecondImage.getTag();

                mFirstImage.setImageBitmap(secondBitmap);
                mSecondImage.setImageBitmap(firstBitmap);

                mFirstImage.setTag(secondTag);
                mSecondImage.setTag(firstTag);

                mFirstImage.setVisibility(View.VISIBLE);
                mSecondImage.setVisibility(View.VISIBLE);

                mFirstImage = mSecondImage = null;
                mAnimLayout.removeAllViews();
                // 判断用户游戏是否成功
                checkSuccess();
                isAniming = false;
            }
        });
    }

    /**
     * 通过tag 获取id
     *
     * @param tag
     * @return
     */
    public int getImageIdByTag(String tag) {
        String[] strings = tag.split("_");
        String s = strings[0];
        return Integer.parseInt(s);
    }

    /**
     * 获取index
     *
     * @param tag
     * @return
     */
    public int getImageIndexByTag(String tag) {
        String[] strings = tag.split("_");
        String s = strings[1];
        return Integer.parseInt(s);
    }

    public void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < mPuzzleGameItems.length; i++) {
            if (getImageIndexByTag((String) mPuzzleGameItems[i].getTag()) != i) {
                isSuccess = false;
            }
        }

        if (isSuccess) {
            Toast.makeText(getContext(), "恭喜通关！", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 构造我们的动画层
     */
    private void setUpAnimLayout()
    {
        if (mAnimLayout == null)
        {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
    }
}
