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
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

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


import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static cmsc436.com.callyourmom.ContactActivity.reminders;
import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_CONTACT_REQUEST = 436;

    private SharedPreferences data;
    private ArrayList<GroupsOfReminders> groups;
    private GroupsReminderAdapter adapter;
    public RecyclerView rvReminders;
    public TextView emptyView;
    private CoordinatorLayout coordinatorLayout;

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

        emptyView = (TextView) findViewById(R.id.empty_text);
        rvReminders = (RecyclerView) findViewById(R.id.reminders);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorlayout);

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

            if(groups != null && groups.size() != 0) {
                Snackbar.make(coordinatorLayout, "Reminders cleared", Snackbar.LENGTH_SHORT).show();
                groups = clearData();
                adapter = new GroupsReminderAdapter(this, groups);
                rvReminders.swapAdapter(adapter, false);
                return true;
            }
            

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
                    // contacts-related0 task you need to do.
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

            if (duplicate == true) {
                Snackbar.make(coordinatorLayout, "You already have a reminder set for that contact in this group!", Snackbar.LENGTH_LONG).show();
            } else if (override == true) {
                Snackbar.make(coordinatorLayout, "Old reminder replaced with new reminder for contact", Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, "New Contact Added", Snackbar.LENGTH_SHORT).show();
            }

            updateRecyclerView();
            // NOTE I should collapse all views first
            String number = data.getStringExtra("number");
            String name = data.getStringExtra("name");
            String id = data.getStringExtra("id");
            String days = data.getStringExtra("days");

            NotificationHandler notif = new NotificationHandler(this);
            notif.createNotification(name, number, id, days);
        }
    }

    public void updateRecyclerView() {
        groups = populateGroupsFromSharedPreferences();

        if (groups.size() != 0) {
            emptyView.setVisibility(View.GONE);
            rvReminders.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            rvReminders.setVisibility(View.GONE);
        }
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

    public ArrayList<GroupsOfReminders> clearData() {
        ArrayList<GroupsOfReminders> groupsToReturn = new ArrayList<>();

        // Delete each reminder too
        for (GroupsOfReminders group : groups) {
            for (CallReminder reminder : group.getRemindersInGroup()) {
                deleteReminder(reminder);
            }
        }

        // Clear the shared preferences
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();


        return groupsToReturn;
    }

    private void deleteReminder(CallReminder reminder) {
        NotificationHandler notif = new NotificationHandler(this);

        Intent intent = notif.getNotificationIntent(reminder.getContactName(), reminder.getTelephoneNumber(),
                reminder.getId(),
                reminder.getNumDaysForRemind() + "");

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(Integer.parseInt(reminder.getId()));

        boolean isWorking = (PendingIntent.getBroadcast(this, Integer.parseInt(reminder.getId()), intent, 0) != null);
        if (isWorking) {
            Log.d("alarm", "already exists, it will be killed now");
            Log.e("Trying to delete", reminder.getId() + " " + reminder.getContactName());
            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            alarmManager.cancel(PendingIntent.getBroadcast(this, Integer.parseInt(reminder.getId()), intent, 0));


            // We already have this alarm, push it back!
        }
    }
}


