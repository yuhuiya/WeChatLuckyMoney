package xyz.monkeytong.hongbao.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import xyz.monkeytong.hongbao.R;
import xyz.monkeytong.hongbao.activities.SettingsActivity;
import xyz.monkeytong.hongbao.activities.WebViewActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Zhongyi on 1/20/16.
 * Util for app update task.
 */
public class UpdateTask extends AsyncTask<String, String, String> {
    public static int count = 0;
    private Context context;
    private boolean isUpdateOnRelease;
    public static final String updateUrl = "https://api.github.com/repos/geeeeeeeeek/WeChatLuckyMoney/releases/latest";

    public UpdateTask(Context context, boolean needUpdate) {
        this.context = context;
        this.isUpdateOnRelease = needUpdate;
        if (this.isUpdateOnRelease)
            Toast.makeText(context, context.getString(R.string.checking_new_version), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected String doInBackground(String... uri) {

        String jsonResult = request(uri[0]);
        return jsonResult;
    }

    public static String request(String httpUrl) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            /**setRequestProperty（key,value）
             * Sets the general request property. If a property with the key already
             * exists, overwrite its value with the new value.
             *  设置通用的请求属性。如果该key对应的的属性值已经存在，那么新值将覆盖以前的值
             * <p> NOTE: HTTP requires all request properties which can
             * legally have multiple instances with the same key
             * to use a comma-seperated list syntax which enables multiple
             * properties to be appended into a single property.
             *提示：HTTP要求拥有相同key值的多个实例的所有请求属性，可以使用逗号分隔的列表语法，这样就可以将多个属性附加到单个属性中
             * @param   key     the keyword by which the request is known
             *                  (e.g., "<code>Accept</code>").
             * @param   value   the value associated with it.
             * @throws IllegalStateException if already connected
             * @throws NullPointerException if key is <CODE>null</CODE>
             * @see #getRequestProperty(java.lang.String)
             */
            connection.setRequestProperty("apikey",  "71e4b699*********cf44ebb02cd2");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        try {
            count += 1;
            JSONObject release = new JSONObject(result);

            // Get current version
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;

            String latestVersion = release.getString("tag_name");
            boolean isPreRelease = release.getBoolean("prerelease");
            if (!isPreRelease && version.compareToIgnoreCase(latestVersion) >= 0) {
                // Your version is ahead of or same as the latest.
                if (this.isUpdateOnRelease) {
                    Toast.makeText(context, R.string.update_already_latest, Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!isUpdateOnRelease) {
                    Toast.makeText(context, context.getString(R.string.update_new_seg1) + latestVersion + context.getString(R.string.update_new_seg3), Toast.LENGTH_LONG).show();
                    return;
                }
                // Need update.
                String downloadUrl = release.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");

                // Give up on the fucking DownloadManager. The downloaded apk got renamed and unable to install. Fuck.
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(browserIntent);
                Toast.makeText(context, context.getString(R.string.update_new_seg1) + latestVersion + context.getString(R.string.update_new_seg2), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (this.isUpdateOnRelease) {
                Toast.makeText(context, R.string.update_error, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void update() {
        super.execute(updateUrl);
    }
}