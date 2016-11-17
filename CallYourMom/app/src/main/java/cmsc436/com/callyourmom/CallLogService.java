package cmsc436.com.callyourmom;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
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
            String duration = c.getString(c.getColumnIndex(CallLog.Calls.DURATION));// for duration

            Toast.makeText(getApplicationContext(), "Called: " + num, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

        }
    }
}
