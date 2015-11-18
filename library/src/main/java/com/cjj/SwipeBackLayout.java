package com.cjj;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by cjj on 2015/11/17.
 */
public class SwipeBackLayout extends FrameLayout {

    /**打印log的tag*/
    private final static String Tag = SwipeBackLayout.class.getSimpleName();

    /**内容界面*/
    private ViewGroup mContentFrameLayout;

    /**遮盖界面*/
    private ViewGroup mBehindFrameLayout;

    /**这个我不知道怎么注释*/
    private ViewDragHelper mViewDragHelper;

    /**手势事件类*/
    private GestureDetectorCompat mGestureDetectorCompat;

    /**左滑距离*/
    private int mSwipeLeft;

    /**左滑最大距离*/
    private int mSwipeWidth;

    /**宽度*/
    private int mWidth;

    /**高度*/
    private int mHeight;

    /**滑动监听*/
    private SwipeBackListener mSwipeBackListener;

    /**枚举，默认关闭*/
    private SwipeBackStatusEnum mStatus = SwipeBackStatusEnum.Close;

    /**
     * 构造函数，然并卵，写成习惯了
     * @param context
     */
    public SwipeBackLayout(Context context) {
        super(context);
        init();
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        mViewDragHelper = ViewDragHelper.create(this, 1.0f, new DragHelperCallback());//创建ViewDragHelper的实例，第一个参数是ViewGroup，传自己，第二个灵敏度，一般正常是1.0，不正常你自己写，第三个是回调，看下面...
        mGestureDetectorCompat = new GestureDetectorCompat(getContext(), new XScrollDetector());//手势操作，第二参数什么意思看下面
    }

    /**
     * View 中所有的子控件均被映射成xml后触发
     */
    @Override
    protected void onFinishInflate() {
        mBehindFrameLayout = (RelativeLayout) getChildAt(0);
        mContentFrameLayout = (RelativeLayout) getChildAt(1);
        mBehindFrameLayout.setClickable(true);
        mContentFrameLayout.setClickable(true);
        super.onFinishInflate();
    }

    /**
     * 手势监听回调
     */
    class XScrollDetector extends GestureDetector.SimpleOnGestureListener {//SimpleOnGestureListener为了不用重写那么多监听的帮助类
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            return Math.abs(distanceY) <= Math.abs(distanceX);//判断是否是滑动的x距离>y距离
        }
    }

    /**
     * 枚举的三种状态
     */
    public enum SwipeBackStatusEnum {

        swipe,

        Open,

        Close
    }

    public interface SwipeBackListener {
        /**打开*/
        public void onOpen();

        /**关闭*/
        public void onClose();

        /**滑动*/
        public void onSwipe(float percent);

    }


    /**
     * 事件拦截
     * @param ev
     * @return
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev) && mGestureDetectorCompat.onTouchEvent(ev);
    }

    /**
     * 事件监听，交给ViewDragHelper处理
     * @param e
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        try {
            mViewDragHelper.processTouchEvent(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 当view的大小发生变化时触发
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = mBehindFrameLayout.getMeasuredWidth();
        mHeight = mBehindFrameLayout.getMeasuredHeight();
        mSwipeWidth = (int) (mWidth * 0.15);
    }

    /**
     * 测量之后，子控件找自己的位置摆放自己
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        mBehindFrameLayout.layout(0, 0, mWidth, mHeight);
        mContentFrameLayout.layout(mSwipeLeft, 0, mSwipeLeft + mWidth, mHeight);
    }


    /**
     * 设置监听
     * @param swipeBackListener
     */
    public void setOnSwipeBackListener(SwipeBackListener swipeBackListener) {
        this.mSwipeBackListener = swipeBackListener;
    }


    /**
     * 打开
     */
    public void open() {
        if (mViewDragHelper.smoothSlideViewTo(mContentFrameLayout, mSwipeWidth, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
        if (null != mSwipeBackListener) mSwipeBackListener.onOpen();

    }


    /**
     * 关闭
     */
    public void close() {
        if (mViewDragHelper.smoothSlideViewTo(mContentFrameLayout, 0, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * 主要是回调监听
     * @param swipeLeft
     */
    private void dispatchSwipeEvent(int swipeLeft) {
        if (mSwipeBackListener == null) {
            return;
        }
        float percent = swipeLeft / (float) mSwipeWidth;
        mSwipeBackListener.onSwipe(percent);
        SwipeBackStatusEnum lastStatus = mStatus;
        if (lastStatus != getStatus() && mStatus == SwipeBackStatusEnum.Close) {
            mBehindFrameLayout.setEnabled(false);
            mSwipeBackListener.onClose();
        } else if (lastStatus != getStatus() && mStatus == SwipeBackStatusEnum.Open) {
            mBehindFrameLayout.setEnabled(true);
//            mSwipeBackListener.onOpen();
        }
    }

    /**
     * 获取当前的状态
     * @return
     */
    public SwipeBackStatusEnum getStatus() {
        int mainLeft = mContentFrameLayout.getLeft();
        if (mainLeft == 0) {
            mStatus = SwipeBackStatusEnum.Close;
        } else if (mainLeft == mSwipeWidth) {
            mStatus = SwipeBackStatusEnum.Open;
        } else {
            mStatus = SwipeBackStatusEnum.swipe;
        }
        return mStatus;
    }

    /**
     *draw() 过程 调用 该 方法
     */
    @Override
    public void computeScroll() {
        if (mViewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    /**
     * 卧槽，终于到重点部分了，好好听着，f**k,写注释好累.....不想写了
     * 推荐你看下翔哥的：http://blog.csdn.net/lmj623565791/article/details/46858663
     * 看懂我就不用解释了，啊哈哈...（打我可以，来 GitHub小伙伴交流群 477826523 ）
     */
    private class DragHelperCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mWidth;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mSwipeLeft + dx < 0) {
                return 0;
            } else if (mSwipeLeft + dx > mSwipeWidth) {
                return mSwipeWidth;
            } else {
                return left;
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.i(Tag,"onViewReleased");
            if (xvel > 0) {
                openEvent();
            } else if (xvel < 0) {
                close();
            } else if (releasedChild == mContentFrameLayout && mSwipeLeft > mSwipeWidth * 0.4) {
                openEvent();
            } else if (releasedChild == mBehindFrameLayout && mSwipeLeft > mSwipeWidth * 0.6) {
                openEvent();
            } else {
                close();
            }
        }

        public void openEvent()
        {
            if (mViewDragHelper.smoothSlideViewTo(mContentFrameLayout, mSwipeWidth, 0)) {
                ViewCompat.postInvalidateOnAnimation(SwipeBackLayout.this);
            }

            SwipeBackLayout.this.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (null != mSwipeBackListener) mSwipeBackListener.onOpen();
                }
            }, 200);
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            Log.i(Tag,"onViewPositionChanged");
            if (changedView == mContentFrameLayout) {
                mSwipeLeft = left;
            } else {
                mSwipeLeft = mSwipeLeft + left;
            }
            if (mSwipeLeft < 0) {
                mSwipeLeft = 0;
            } else if (mSwipeLeft > mSwipeWidth) {
                mSwipeLeft = mSwipeWidth;
            }
            dispatchSwipeEvent(mSwipeLeft);
            if (changedView == mBehindFrameLayout) {
                mBehindFrameLayout.layout(0, 0, mWidth, mHeight);
                mContentFrameLayout.layout(mSwipeLeft, 0, mSwipeLeft + mWidth, mHeight);
            }
            invalidate();
        }
    }
}
