package cmsc436.com.callyourmom;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationManager;

/**
 * Created by Patrick on 12/3/16.
 */

public class ReminderNotification extends BroadcastReceiver {

    private static String NOTIFICATION = "notification";
    private static String NOTIFICATION_ID = "notification-id";

    public void onReceive(Context context, Intent intent){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification =  intent.getParcelableExtra(NOTIFICATION);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);
    }
}
