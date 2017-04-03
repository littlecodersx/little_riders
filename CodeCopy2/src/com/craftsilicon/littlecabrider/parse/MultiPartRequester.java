package com.craftsilicon.littlecabrider.parse;

import java.io.File;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import com.craftsilicon.littlecabrider.utils.AndyUtils;
import com.craftsilicon.littlecabrider.utils.AppLog;
import com.craftsilicon.littlecabrider.utils.Const;
import com.oracle.webservices.internal.api.message.ContentType;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author Elluminati elluminati.in
 */
public class MultiPartRequester {

	private Map<String, String> map;
	private AsyncTaskCompleteListener mAsynclistener;
	private int serviceCode;

	private HttpClient httpclient;
	private Activity activity;
	private AsyncHttpRequest request;

	public MultiPartRequester(Activity activity, Map<String, String> map,
			int serviceCode, AsyncTaskCompleteListener asyncTaskCompleteListener) {
		this.map = map;
		this.serviceCode = serviceCode;
		this.activity = activity;

		// is Internet Connection Available...

		if (AndyUtils.isNetworkAvailable(activity)) {
			mAsynclistener = asyncTaskCompleteListener;
			request = (AsyncHttpRequest) new AsyncHttpRequest().execute(map
					.get("url"));
		} else {
			showToast("Network is not available!!!");
		}

	}

	class AsyncHttpRequest extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			map.remove("url");
			try {

				HttpPost httppost = new HttpPost(urls[0]);
				httpclient = new DefaultHttpClient();

				HttpConnectionParams.setConnectionTimeout(
						httpclient.getParams(), 60000);

				MultipartEntityBuilder builder = MultipartEntityBuilder
						.create();

				for (String key : map.keySet()) {
					AppLog.Log(key, map.get(key));
					if (key.equalsIgnoreCase(Const.Params.PICTURE)) {
						if (!TextUtils.isEmpty(map.get(key))) {
							File f = new File(map.get(key));

							builder.addBinaryBody(key, f,
									org.apache.http.entity.ContentType.MULTIPART_FORM_DATA,
									f.getName());
						}
					} else {
						builder.addTextBody(key, map.get(key), org.apache.http.entity.ContentType
								.create("text/plain", MIME.UTF8_CHARSET));
					}
					// System.out.println(key + "---->" + map.get(key));

				}
				httppost.setEntity(builder.build());

				ActivityManager manager = (ActivityManager) activity
						.getSystemService(Context.ACTIVITY_SERVICE);

				if (manager.getMemoryClass() < 25) {
					System.gc();
				}
				HttpResponse response = httpclient.execute(httppost);

				String responseBody = EntityUtils.toString(
						response.getEntity(), "UTF-8");

				return responseBody;

			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError oume) {
				System.gc();

				Toast.makeText(
						activity.getParent().getParent(),
						"Run out of memory please close the other background apps and try again!",
						Toast.LENGTH_LONG).show();
			} finally {
				if (httpclient != null)
					httpclient.getConnectionManager().shutdown();

			}

			return null;

		}

		@Override
		protected void onPostExecute(String response) {

			if (mAsynclistener != null) {
				mAsynclistener.onTaskCompleted(response, serviceCode);
			}

		}
	}

	private void showToast(String msg) {
		Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
	}

	public void cancelTask() {

		request.cancel(true);
		// System.out.println("task is canelled");

	}
}
