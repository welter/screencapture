package com.androidyuan.lib.screenshot;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;

import com.welter.Tcptools.TCPSend;
import com.welter.Tcptools.TCPSendfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wei on 16-12-1.
 */


public class Shotter {
    public enum ResultType{
         RTFile,RTNet;
    }
    private final Context mRefContext;//SoftReference<Context> mRefContext;
    private ImageReader mImageReader;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    public String mLocalUrl = "";

    private OnShotListener mOnShotListener;


    public Shotter(Context context, Intent data) {
        this.mRefContext = context;//SoftReference<>(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


            mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK,
                    data);

            mImageReader = ImageReader.newInstance(
                    getScreenWidth(),
                    getScreenHeight(),
                    PixelFormat.RGBA_8888,//此处必须和下面 buffer处理一致的格式 ，RGB_565在一些机器上出现兼容问题。
                    1);
            virtualDisplay();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {

        mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
                getScreenWidth(),
                getScreenHeight(),
                Resources.getSystem().getDisplayMetrics().densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);

    }

    public void startScreenShot(OnShotListener onShotListener, String loc_url) {
        mLocalUrl = loc_url;
        startScreenShot(onShotListener,ResultType.RTFile);
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void startScreenShot(OnShotListener onShotListener, final ResultType rt) {

        mOnShotListener = onShotListener;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

//            virtualDisplay();

            Handler handler = new Handler();

            handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        Image image = mImageReader.acquireLatestImage();

                                        if (rt.name()==ResultType.RTFile.name()){
                                            AsyncTaskCompat.executeParallel(new SaveTask(), image);
                                        }

                                        else
                                        {
                                            AsyncTaskCompat.executeParallel(new SendByNetworkTask(), image);
                                        }
                                    }
                                },
                    300);

        }

    }


    public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Bitmap doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);//虽然这个色彩比较费内存但是 兼容性更好
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            File fileImage = null;
            if (bitmap != null) {
                try {

                    if (TextUtils.isEmpty(mLocalUrl)) {
                        mLocalUrl = getContext().getExternalFilesDir("screenshot").getAbsoluteFile()
                                +
                                "/"
                                +
                                SystemClock.currentThreadTimeMillis() + ".png";
                    }
                    fileImage = new File(mLocalUrl);

                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                    FileOutputStream out = new FileOutputStream(fileImage);
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    fileImage = null;
                } catch (IOException e) {
                    e.printStackTrace();
                    fileImage = null;
                }
            }

            if (fileImage != null) {
                return bitmap;
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);

            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }

            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
            }

            if (mOnShotListener != null) {
                mOnShotListener.onFinish();
            }

        }
    }

    public class SendByNetworkTask extends AsyncTask<Image, Void,Void> {

        private boolean isEnd;
        private TCPSend t ;

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Image... params) {

            if (params == null || params.length < 1 || params[0] == null) {

                return null;
            }

            Image image = params[0];

            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            //每个像素的间距
            int pixelStride = planes[0].getPixelStride();
            //总的间距
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;
            Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,
                    Bitmap.Config.ARGB_8888);//虽然这个色彩比较费内存但是 兼容性更好
            bitmap.copyPixelsFromBuffer(buffer);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
            image.close();
            //File fileImage = null;
            final byte[] outData;

            if (bitmap != null) {
                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    if (out != null) {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        outData=out.toByteArray();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                t = new TCPSend("192.168.0.5",22222);
                            }
                        }).start();
                        while (t==null){

                        }
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                t.sendMessage(outData);
                                isEnd=true;
                            }
                        };
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        isEnd=false;
                        executorService.execute(runnable);
                        while (!isEnd){ }
                        out.close();
                        t.close();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        protected void onPostExecute(Void d) {
            super.onPostExecute(d);

            //if (bitmap != null && !bitmap.isRecycled()) {
            //    bitmap.recycle();
            //}

//            if (mVirtualDisplay != null) {
//                mVirtualDisplay.release();
//            }

            if (mOnShotListener != null) {
                mOnShotListener.onFinish();
            }

        }
    }

    private MediaProjectionManager getMediaProjectionManager() {

        return (MediaProjectionManager) getContext().getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);
    }

    private Context getContext() {
        return mRefContext;
    }


    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }


    // a  call back listener
    public interface OnShotListener {
        void onFinish();
    }
}
