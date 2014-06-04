package net.tagboa.app;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import com.microsoft.windowsazure.notifications.NotificationsHandler;

/**
 * 노티피케이션 핸들러.
 * Created by Youngjae on 2014-04-18.
 */
public class AzureNotificationHandler extends NotificationsHandler {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	Context ctx;

	@Override
	public void onReceive(Context context, Bundle bundle) {
		ctx = context;
		String nhMessage = bundle.getString("message");

		sendNotification(nhMessage);
	}

	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager)
				ctx.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 0,
				new Intent(ctx, MainActivity.class), 0);

		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(ctx)
						.setSmallIcon(R.drawable.ic_launcher)
						.setContentTitle("Notification Hub Demo")
						.setStyle(new NotificationCompat.BigTextStyle()
								.bigText("Received: " + msg))
						.setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
