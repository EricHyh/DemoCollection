package com.hyh.web.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import com.hyh.web.SimilarPicture;

import java.text.DecimalFormat;

/**
 * Created by Eric_He on 2017/11/12.
 */

public class CustomWebView extends WebView {

    private static final String TAG = "CustomWebView";

    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mLastTouchX;
    private float mLastTouchY;

    private Bitmap mDownBitmap;
    private int mWidth;
    private int mHeight;
    private int mTouchSlop;
    private boolean mLogDraw;

    public CustomWebView(Context context) {
        super(context);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledPagingTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLogDraw) {
            Log.d(TAG, "onDraw: ");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                getParent().requestDisallowInterceptTouchEvent(true);
                mLastTouchX = mInitialTouchX = event.getX();
                mLastTouchY = mInitialTouchY = event.getY();
                mDownBitmap = getDestBitmap();
                super.onTouchEvent(event);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                float tx = mInitialTouchX - x;
                float ty = mLastTouchY - y;

                if (Math.abs(tx) > 60) {
                    mLogDraw = true;
                    Bitmap destBitmap = getDestBitmap();
                    int i = SimilarPicture.compareBitmap(mDownBitmap, destBitmap);
                    String similarity = similarity(mDownBitmap, destBitmap);
                    Log.d(TAG, "onTouchEvent: i=" + i + ", similarity=" + similarity + ", tx=" + tx);
                    if (destBitmap != null) {
                        destBitmap.recycle();
                    }
                    super.onTouchEvent(event);
                } else {
                    MotionEvent obtain = MotionEvent.obtain(event);
                    obtain.offsetLocation(0, ty);
                    super.onTouchEvent(obtain);
                }
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (mDownBitmap != null) {
                    mDownBitmap.recycle();
                    mDownBitmap = null;
                }
                mLogDraw = false;
                super.onTouchEvent(event);
            }
            break;
        }


        //return super.onTouchEvent(event);
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    private Bitmap getDestBitmap() {
        int destLeft = (int) (mInitialTouchX - 10f + 0.5f);
        int destTop = (int) (mInitialTouchY - 10f + 0.5f);
        setDrawingCacheEnabled(true);
        Bitmap drawingCache = getDrawingCache();
        if (drawingCache == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawingCache, destLeft, destTop, 20, 20);
        setDrawingCacheEnabled(false);
        return bitmap;
    }

    public static String similarity(Bitmap b, Bitmap viewBt) {
        //把图片转换为Bitmap
        int t = 0;
        int f = 0;

        Bitmap bm_one = b;
        Bitmap bm_two = viewBt;
        //保存图片所有像素个数的数组，图片宽×高
        int[] pixels_one = new int[bm_one.getWidth() * bm_one.getHeight()];
        int[] pixels_two = new int[bm_two.getWidth() * bm_two.getHeight()];
        //获取每个像素的RGB值
        bm_one.getPixels(pixels_one, 0, bm_one.getWidth(), 0, 0, bm_one.getWidth(), bm_one.getHeight());
        bm_two.getPixels(pixels_two, 0, bm_two.getWidth(), 0, 0, bm_two.getWidth(), bm_two.getHeight());
        //如果图片一个像素大于图片2的像素，就用像素少的作为循环条件。避免报错
        if (pixels_one.length >= pixels_two.length) {
            //对每一个像素的RGB值进行比较
            for (int i = 0; i < pixels_two.length; i++) {
                int clr_one = pixels_one[i];
                int clr_two = pixels_two[i];
                //RGB值一样就加一（以便算百分比）
                if (clr_one == clr_two) {
                    t++;
                } else {
                    f++;
                }
            }
        } else {
            for (int i = 0; i < pixels_one.length; i++) {
                int clr_one = pixels_one[i];
                int clr_two = pixels_two[i];
                if (clr_one == clr_two) {
                    t++;
                } else {
                    f++;
                }
            }

        }

        return "相似度为：" + myPercent(t, t + f);

    }

    /**
     * 百分比的计算
     *
     * @param y(母子)
     * @param z（分子）
     * @return 百分比（保留小数点后两位）
     * @author xupp
     */
    public static String myPercent(int y, int z) {
        String baifenbi = ""; //接受百分比的值
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        DecimalFormat df1 = new DecimalFormat("00.00%"); //##.00%   百分比格式，后面不足2位的用0补齐
        baifenbi = df1.format(fen);
        return baifenbi;
    }

}
