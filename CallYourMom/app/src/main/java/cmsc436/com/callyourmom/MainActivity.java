package cmsc436.com.callyourmom;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telecom.Call;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;


import static cmsc436.com.callyourmom.ContactActivity.reminders;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_CONTACT_REQUEST = 436;

    private SharedPreferences data;
    private ArrayList<GroupsOfReminders> groups;
    private GroupsReminderAdapter adapter;
    private RecyclerView rvReminders;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the permission request!
        checkPermissions();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(MainActivity.this, ContactActivity.class);
                startActivityForResult(contactIntent, ADD_CONTACT_REQUEST);
            }
        });

        rvReminders = (RecyclerView) findViewById(R.id.reminders);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        ArrayList<GroupsOfReminders> groups = new ArrayList<>();
//        ArrayList<CallReminder> reminders = new ArrayList<>();
//        Random rand = new Random();
//
//        for (int i = 0; i < rand.nextInt(5) + 1; i++) {
//            reminders.add(new CallReminder("hello " + i, i + ""));
//        }
//
//        for (int i = 0; i < rand.nextInt(5) + 3; i++) {
//            GroupsOfReminders singleGroup = new GroupsOfReminders(reminders);
//            singleGroup.setFrequencyInDays(rand.nextInt(21) + 1);
//            groups.add(singleGroup);
//        }

        groups = populateGroupsFromSharedPreferences();
        Log.e("Groups size", groups.size() + "");
        adapter = new GroupsReminderAdapter(this, groups);
        rvReminders.setAdapter(adapter);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Both not been granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS}, 1);

        } else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {

            // contacts granted, call log not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, 1);

        } else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

            // call log granted, contacts not granted
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 1);

        } else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            // start the service
            Intent callLogIntent = new Intent(getApplicationContext(), CallLogService.class);
            startService(callLogIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Snackbar.make(this.getCurrentFocus(), "Reminders cleared", Snackbar.LENGTH_SHORT).show();
            groups = clearData();
            adapter = new GroupsReminderAdapter(this, groups);
            rvReminders.swapAdapter(adapter, false);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Intent callLogIntent = new Intent(getApplicationContext(), CallLogService.class);
                    startService(callLogIntent);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your Call Log, we need this :(", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ADD_CONTACT_REQUEST) {
            // TODO update recyclerview, add alarm reminder @vito chen
            // the alarm has to create a pending intent that will show a notification telling the user
            // to call the contact
            // Intent myIntent = new Intent("cmsc436.com.callyourmom.call" + name); <- this HAS to be the name of the alarm

            Boolean duplicate = data.getBooleanExtra("duplicate", false);
            Boolean override = data.getBooleanExtra("override", false);

            if(duplicate == true) {
                Snackbar.make(this.getCurrentFocus(), "You already have a reminder set for that contact in this group!", Snackbar.LENGTH_LONG).show();
            }
            else if(override == true){
                Snackbar.make(this.getCurrentFocus(), "Old reminder replaced with new reminder for contact", Snackbar.LENGTH_LONG).show();
            }
            else{
                Snackbar.make(this.getCurrentFocus(), "New Contact Added", Snackbar.LENGTH_SHORT).show();
            }

            updateRecyclerView();
            // NOTE I should collapse all views first

            //Intent to bring up phone call
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + data.getStringExtra("number")));
            PendingIntent pintent = PendingIntent.getActivity(this, 0, intent, 0);

            //Building notification
            Notification notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.ic_stat_call_reminder)
                    .setContentTitle("Reminder to call " + data.getStringExtra("name"))
                    .setContentText("It's been a while")
                    .addAction(0, "Call", pintent).build();

            Intent notificationIntent = new Intent(this, ReminderNotification.class);
            notificationIntent.putExtra("notification-id", 1);
            notificationIntent.putExtra("notification", notification);
            PendingIntent pNotificationIntent = PendingIntent.getBroadcast(this, Integer.parseInt(data.getStringExtra("id")), notificationIntent, PendingIntent.FLAG_ONE_SHOT);

            //Adding notification to alarm
            //alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + (Integer.parseInt(data.getStringExtra("days")) * 24 * 1000 * 3600), pNotificationIntent);
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(0, notification);
            //Testing with 5 second notification
            alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 5, pNotificationIntent);

        }
    }

    public void createNotification(String name, String number, String id, String days){

    }

    public void updateRecyclerView() {
        groups = populateGroupsFromSharedPreferences();
        adapter = new GroupsReminderAdapter(this, groups);
        rvReminders.swapAdapter(adapter, false);
    }

    public ArrayList<GroupsOfReminders> populateGroupsFromSharedPreferences() {
        data = getSharedPreferences("data", Context.MODE_PRIVATE);
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


            } catch (Exception e) {}

        }

        Collections.sort(groupsToReturn, new Comparator<GroupsOfReminders>() {
            @Override
            public int compare(GroupsOfReminders groupsOfReminders, GroupsOfReminders t1) {
                return groupsOfReminders.getFrequencyInDays() < t1.getFrequencyInDays() ? -1 : 1;
            }
        });

        return groupsToReturn;
    }

    public ArrayList<GroupsOfReminders> clearData() {
        ArrayList<GroupsOfReminders> groupsToReturn = new ArrayList<>();

        // Clear the shared preferences
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();


        return groupsToReturn;
    }
}


