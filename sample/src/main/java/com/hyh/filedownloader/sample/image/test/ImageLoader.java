package com.hyh.filedownloader.sample.image.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.eric.filedownloader_master.R;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Eric_He on 2016/10/27.
 */
//https://www.cnblogs.com/carlo/p/4962672.html
//https://blog.csdn.net/sw950729/article/details/53523920
public class ImageLoader {

    private static final int TAG_KEY_PATHNAME = 0;
    private static final int MESSAGE_POST_RESULT = 1;
    private Context mContext;
    private LruCache<String, Bitmap> mMemoryCache;
    private ImageReSize mImageResizer = new ImageReSize();

    private static final int CUP_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CUP_COUNT + 1;

    private static final int MAX_IMUMPOOL_SIZE = CORE_POOL_SIZE * 2 + 1;
    private static final long KEEP_ALIVE_TIME = 10L;
    private static final int DEFAULT_QUEUE_SIZE = 12;
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "ImageLoader#" + mCount.getAndIncrement());
        }
    };


    private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_IMUMPOOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(DEFAULT_QUEUE_SIZE), sThreadFactory,
            new ThreadPoolExecutor.DiscardOldestPolicy());

   /* private static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,
            MAX_IMUMPOOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),sThreadFactory);*/


    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MESSAGE_POST_RESULT) {
                LoaderResult result = (LoaderResult) msg.obj;
                ImageView imageView = result.imageView;
                String pathName = (String) imageView.getTag(TAG_KEY_PATHNAME);
                if (pathName.equals(result.pathName)) {
                    imageView.setImageBitmap(result.bitmap);
                }
            }
        }
    };

    public static ImageLoader build(Context context) {
        return new ImageLoader(context);
    }


    /**
     * nt corePoolSize,
     * int maximumPoolSize,
     * long keepAliveTime,
     * TimeUnit unit,
     * BlockingQueue<Runnable> workQueue,
     * ThreadFactory threadFactory,
     * RejectedExecutionHandler handler
     */

    private ImageLoader(Context context) {
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    public Bitmap loadBitmap(String pathName, int reqWidth, int reqHeight) {
        Bitmap bitmap = mMemoryCache.get(pathName);
        if (bitmap != null) {
            return bitmap;
        }
        Bitmap bitamap = mImageResizer.getReqSizeBitmap(pathName, reqWidth, reqHeight);
        addBitmapToMemoryCache(pathName, bitmap);
        return bitamap;
    }

    public void bindBitmap(final String pathName, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_PATHNAME, pathName);
        imageView.setImageResource(R.mipmap.ic_launcher);
        Bitmap bitmap = mMemoryCache.get(pathName);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = mImageResizer.getReqSizeBitmap(pathName, reqWidth, reqHeight);
                if (bitmap != null) {
                    addBitmapToMemoryCache(pathName, bitmap);
                    LoaderResult result = new LoaderResult(imageView, pathName, bitmap);
                    mHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(runnable);
    }


    private static class LoaderResult {
        public ImageView imageView;
        public String pathName;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String pathName, Bitmap bitmap) {
            this.imageView = imageView;
            this.pathName = pathName;
            this.bitmap = bitmap;
        }
    }
}
