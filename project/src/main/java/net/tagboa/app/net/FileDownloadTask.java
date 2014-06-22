package net.tagboa.app.net;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import net.tagboa.app.page.MainActivity;
import org.apache.http.HttpResponse;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * URL에서 이미지 파일을 가져온다.
 * User: Youngjae
 * Date: 13. 4. 29
 * Time: 오전 2:10
 */
@TargetApi(Build.VERSION_CODES.FROYO)
public class FileDownloadTask extends AsyncTask<HttpResponse, Integer, String> {
	private static final String TAG = "FileDownloadTask";
	private Activity _activity;
	private OnResponseListener _responder;
	private String _contentUrl;
	private String _errorMessage;

	public FileDownloadTask(Activity activity, String contentUrl, OnResponseListener responseListener) {
		_activity = activity;
		_contentUrl = contentUrl;
		_responder = responseListener;
	}

	private ProgressDialog _progressDialog;

	public String DownloadFromUrl(String Url)
	{
		String filepath=null;
		try {
			URL url = new URL(Url);
			//create the new connection
			HttpURLConnection urlConnection = (HttpsURLConnection) url.openConnection();

			//set up some things on the connection
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			//and connect!
			urlConnection.connect();

			//create a new file, specifying the path, and the filename
			//which we want to save the file as.
			File file = CreateFile(null);

			if(!file.exists())
			{
				file.createNewFile();
			}

			//this will be used to write the downloaded data into the file we created
			FileOutputStream fileOutput = new FileOutputStream(file);

			//this will be used in reading the data from the internet
			InputStream inputStream = urlConnection.getInputStream();

			//this is the total size of the file
			int totalSize = urlConnection.getContentLength();
			//variable to store total downloaded bytes
			int downloadedSize = 0;

			//create a buffer...
			byte[] buffer = new byte[1024];
			int bufferLength = 0; //used to store a temporary size of the buffer

			//now, read through the input buffer and write the contents to the file
			while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
				//add the data in the buffer to the file in the file output stream (the file on the sd card
				fileOutput.write(buffer, 0, bufferLength);
				//add up the size so we know how much is downloaded
				downloadedSize += bufferLength;
				//this is where you would do something to report the prgress, like this maybe
				Log.i(TAG,"downloadedSize:"+downloadedSize+"totalSize:"+ totalSize) ;
			}
			//close the output stream when done
			fileOutput.flush();
			fileOutput.close();
			if(downloadedSize==totalSize)
				filepath=file.getPath();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			filepath=null;
			e.printStackTrace();
		}
		Log.i("filepath:"," "+filepath) ;

		return filepath;
	}

	@Override
	protected String doInBackground(HttpResponse... arg0) {
		try {
			return DownloadFromUrl(_contentUrl);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
		_progressDialog.dismiss();
	}

	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		if(_progressDialog.isShowing())
			_progressDialog.dismiss();

		if (result != null)
			_responder.onSuccess();
		else
			_responder.onFailure(_errorMessage);
	}

	@Override
	protected void onPreExecute() {
		_progressDialog = ProgressDialog.show(_activity, "", "데이터 가져오는 중...", true);
		super.onPreExecute();
	}

	/**
	 * 완료 이벤트 delegation용 인터페이스.
	 */
	public interface OnResponseListener {
		public void onSuccess();
		public void onFailure(String message);
	}


	/**
	 * 파일처리 기본 폴더.
	 *
	 * @return
	 */
	public static final String DefaultFolder = Environment.getExternalStoragePublicDirectory(
			Environment.DIRECTORY_DOWNLOADS)
			+ File.separator + MainActivity.ApplicationName
			+ File.separator;

	/**
	 * 파일 생성
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static File CreateFile(String filename) throws IOException {
		if (filename == null || "".equals(filename)) {
			String timeStamp =
					new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			filename = timeStamp + ".csv";
		}
		final File root = new File(DefaultFolder);
		if (!root.exists())
			root.mkdirs();

		return new File(root, filename);
	}
}
