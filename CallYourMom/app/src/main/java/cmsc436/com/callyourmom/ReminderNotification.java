package cmsc436.com.callyourmom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationManager;
import android.util.Log;

/**
 * Created by Patrick on 12/3/16.
 */

public class ReminderNotification extends BroadcastReceiver {

    private static String NOTIFICATION = "notification";
    private static String NOTIFICATION_ID = "notification-id";


    public ReminderNotification() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Receiver", "notif received");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        int id = Integer.parseInt(intent.getStringExtra(NOTIFICATION_ID));

        notificationManager.notify(id, notification);

    }
}
