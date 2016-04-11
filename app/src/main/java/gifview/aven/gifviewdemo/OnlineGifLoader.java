package gifview.aven.gifviewdemo;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * <br>功能简述:网络请求的Runnable
 * <br>功能详细描述:用于请求网络数据
 * <br>注意:
 * @author Aven
 **/
public class OnlineGifLoader implements Runnable {

    private Context mAppContext;
    private String mRequestUrl;
    /** 是不是活动的 */
    private boolean mActive;
    private GifLoaderLinstener mLinstener;

    /**
     * 在线Gif加载监听器
     */
    public interface GifLoaderLinstener {
        /**
         * 加载成功
         * @param is
         */
        public void onLoaded(InputStream is);

        /**
         * 加载失败
         */
        public void onFailed(String errMsg);
    }

    /**
     * constructor
     * @param context
     * @param requestUrl 请求地址
     * @param linstener 回调监听器
     */
    public OnlineGifLoader(@NonNull Context context, @NonNull String requestUrl,@NonNull GifLoaderLinstener linstener) {
        mAppContext = context;
        mRequestUrl = requestUrl;
        mLinstener = linstener;
    }

    private void getOnlineGifData() {
        if (mRequestUrl == null) {
            if (GifView.DEBUG) {
                Log.d(GifView.TAG, "requestUrl = null");
            }
            return;
        }
        try {
            URL url = new URL(mRequestUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            // connection.setRequestMethod("GET");
            int size = connection.getContentLength();
            InputStream is = connection.getInputStream();
            // mBmp = BitmapFactory.decodeStream(in);
            if (mLinstener != null) {
                mLinstener.onLoaded(is);
            }
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (mLinstener != null) {
                mLinstener.onFailed("cause IOException");
            }
        }
    }

    @Override
    public void run() {
        getOnlineGifData();
    }

    /**
     * <br>功能简述:该Runnable是否是活跃的
     * <br>功能详细描述:
     * <br>注意:
     * @return true , false
     **/
    public boolean isActive() {
        return mActive;
    }

    /**
     * <br>功能简述:设置Runnale是不是活动的
     * <br>功能详细描述:如果显示在用户界面上，则是活动的，否则设置为不活动的，有利提高加载的有先级
     * <br>注意:
     * @param active
     */
    public void setActive(boolean active) {
        this.mActive = active;
    }
}
