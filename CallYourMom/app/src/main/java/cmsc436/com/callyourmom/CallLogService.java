package cmsc436.com.callyourmom;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

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
            ArrayList<GroupsOfReminders> groups = populateGroupsFromSharedPreferences();

            CallReminder reminder = null;
            for (GroupsOfReminders group : groups) {
                for (CallReminder reminders : group.getRemindersInGroup()) {
                    if (reminders.getContactName().equals(name))
                        reminder = reminders;
                }
            }

            // See if there is already a pending alarm intent for this reminder

            if (reminder != null) {
                NotificationHandler notif = new NotificationHandler(getApplicationContext());

                Intent intent = notif.getNotificationIntent(reminder.getContactName(), reminder.getTelephoneNumber(),
                        reminder.getId(),
                        reminder.getNumDaysForRemind() + "");

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(Integer.parseInt(reminder.getId()));

                boolean isWorking = (PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(reminder.getId()), intent, 0) != null);
                if (isWorking) {
                    Log.d("alarm", "already exists, it will be killed now");
                    Log.e("Trying to delete", reminder.getId() + " " + reminder.getContactName());
                    alarmManager.cancel(PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(reminder.getId()), intent, 0));


                    // We already have this alarm, push it back!
                } else {
                    Log.d("alarm", "does not exist");
                    // We don't have this alarm do no work
                }

                // Start new alarm
                notif.createNotification(reminder.getContactName(), reminder.getTelephoneNumber(),
                        reminder.getId(),
                        reminder.getNumDaysForRemind() + "");
            }

//            if(!isWorking) {
//                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//                // System.currentTimeMillis will have to change to be equal to when the intent should fire
//                // which is the reminder.numDaysForReminder * 24 * 60 * 60 sec
//                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
//            }
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

        }

        public ArrayList<GroupsOfReminders> populateGroupsFromSharedPreferences() {
            SharedPreferences data = getSharedPreferences("data", Context.MODE_PRIVATE);
            String dataString = data.getString(ContactActivity.reminders, "");

            ArrayList<GroupsOfReminders> groupsToReturn = new ArrayList<>();

            if (!dataString.equals("") && !dataString.isEmpty()) {
                try {
                    JSONObject jsonGroups = new JSONObject(dataString);
                    Iterator<String> groupsIterator = jsonGroups.keys();

                    while (groupsIterator.hasNext()) {
                        String groupIdentifer = groupsIterator.next();
                        JSONArray contactsInGroup = jsonGroups.getJSONArray(groupIdentifer);

                        List<CallReminder> group = new ArrayList<>();
                        for (int i = 0; i < contactsInGroup.length(); i++) {
                            JSONObject obj = contactsInGroup.getJSONObject(i);
                            CallReminder reminder =
                                    new CallReminder(
                                            obj.getString("name"),
                                            obj.getString("number"),
                                            obj.getString("id"),
                                            Integer.parseInt(groupIdentifer));
                            group.add(reminder);


                        }
                        Log.e("" + groupIdentifer, contactsInGroup.toString());

                        GroupsOfReminders singleGroup = new GroupsOfReminders(group);
                        singleGroup.setFrequencyInDays(Integer.parseInt(groupIdentifer));
                        groupsToReturn.add(singleGroup);
                    }


                } catch (Exception e) {
                }

            }

            Collections.sort(groupsToReturn, new Comparator<GroupsOfReminders>() {
                @Override
                public int compare(GroupsOfReminders groupsOfReminders, GroupsOfReminders t1) {
                    return groupsOfReminders.getFrequencyInDays() < t1.getFrequencyInDays() ? -1 : 1;
                }
            });

            return groupsToReturn;
        }
    }
}
