package cmsc436.com.callyourmom;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class CallLogService extends Service {
    public CallLogService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("Service", "the service was started");

        getApplicationContext().getContentResolver().registerContentObserver(
                android.provider.CallLog.Calls.CONTENT_URI, true,
                new CallLogObserver(new Handler()));

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class CallLogObserver extends ContentObserver {
        Handler handler;

        public CallLogObserver(Handler handler) {
            super(handler);
            this.handler = handler;
        }

        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            // This right here means that the call log has changed

            // Get most recent call
            Uri allCalls = Uri.parse("content://call_log/calls");

            Cursor c = getApplicationContext().getContentResolver().query(allCalls, null, null, null, null);

            Log.v("Called", "call log changed");

            c.moveToLast();
            String num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));// for  number
            String name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME));// for name

            // See if there is already a pending alarm intent for this reminder

            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + num));

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            boolean isWorking = (PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE) != null);
            if (isWorking) {
                Log.d("alarm", "already exists");

                alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_NO_CREATE));

                // Start new alarm

                // TODO NEW alarm (copied alarm from MainActivity, check flags if not working

                // We already have this alarm, push it back!
            } else {
                Log.d("alarm", "does not exist");
                // We don't have this alarm do no work
            }

            if(!isWorking) {
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                // System.currentTimeMillis will have to change to be equal to when the intent should fire
                // which is the reminder.numDaysForReminder * 24 * 60 * 60 sec
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
            }

            Toast.makeText(getApplicationContext(), "Called: " + num, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

        }
    }
}
