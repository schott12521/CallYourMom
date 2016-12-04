package cmsc436.com.callyourmom;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class CallActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent callingIntent = getIntent();
        String number = callingIntent.getStringExtra("number");
        String id = callingIntent.getStringExtra("id");
        String name = callingIntent.getStringExtra("name");
        String days = callingIntent.getStringExtra("days");

        Log.e("ACTION", callingIntent.getAction());

        if (callingIntent.getAction().equals("call_contact")) {
            NotificationHandler notif = new NotificationHandler(this);

            CancelNotification(this, Integer.parseInt(id), notif.getNotificationIntent(name, number, id, days));

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
            finish();
        } else {
            Log.v("help", "restart notif");
            NotificationHandler notif = new NotificationHandler(this);

            CancelNotification(this, Integer.parseInt(id), notif.getNotificationIntent(name, number, id, days));


            notif.createNotification(name, number, id, days);

            finish();
        }
    }

    public void CancelNotification(Context context, int id, Intent intent) {
//        PendingIntent test = PendingIntent.getBroadcast(getApplicationContext(), id, intent, 0);
//        boolean isWorking = test != null;
//        if (isWorking) {
//            Log.d("alarm", "is pending to be deleted???");
//
////            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
////            notificationManager.cancel(id);
//
//            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//            alarmManager.cancel(test);
//
//        }
//
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(id);
    }


}
