package cmsc436.com.callyourmom;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.ALARM_SERVICE;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class NotificationHandler {
    private Context context;

    public NotificationHandler(Context context) {
        this.context = context;
    }

    public void createNotification(String name, String number, String id, String days) {
        PendingIntent pNotificationIntent =
                PendingIntent.getBroadcast(context, Integer.parseInt(id), getNotificationIntent(name, number, id, days), 0);



        //Testing with 5 second notification
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() +
        //        20 * 1000, pNotificationIntent);
        //Adding notification to alarm
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (Integer.parseInt(days)) * 24 * 1000 * 3600, pNotificationIntent);
    }

    public Intent getNotificationIntent(String name, String number, String id, String days) {
        //Intent to bring up phone call
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("number", number);
        intent.putExtra("id", id);
        intent.putExtra("days", days);
        intent.putExtra("name", name);
        intent.setAction("call_contact");
        PendingIntent pintent = PendingIntent.getActivity(context, Integer.parseInt(id), intent, 0);

        NotificationCompat.Action callAction = new NotificationCompat.Action.Builder(0, "Call", pintent).build();

        Intent dismissIntent = new Intent(context, CallActivity.class);
        dismissIntent.setAction("notification_cancelled");
        dismissIntent.putExtra("number", number);
        dismissIntent.putExtra("id", id);
        dismissIntent.putExtra("days", days);
        dismissIntent.putExtra("name", name);
        PendingIntent dismissPIntent = PendingIntent.getActivity(context, Integer.parseInt(id), dismissIntent, 0);

        NotificationCompat.Action dismissAction = new NotificationCompat.Action.Builder(0, "Dismiss", dismissPIntent).build();

        //Building notification
        Notification notification = new NotificationCompat.Builder(context)
                .setPriority(PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_stat_call_reminder)
                .setContentTitle("Reminder to call " + name)
                .setContentText("It's been a while")
                .setVibrate(new long[] { 500, 1500, 500 })
                .setVisibility(VISIBILITY_PUBLIC)
                .setOngoing(true)
                .addAction(callAction)
                .addAction(dismissAction).build();


        Intent notificationIntent = new Intent(context, ReminderNotification.class);
        notificationIntent.putExtra("notification-id", id);
        notificationIntent.putExtra("notification", notification);
        notificationIntent.setAction("notification");

        return notificationIntent;
    }
}
