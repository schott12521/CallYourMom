package cmsc436.com.callyourmom;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static cmsc436.com.callyourmom.ContactActivity.reminders;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_CONTACT_REQUEST = 436;

    private SharedPreferences data;
    private ArrayList<GroupsOfReminders> groups;
    private GroupsReminderAdapter adapter;
    private RecyclerView rvReminders;

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
            Snackbar.make(this.getCurrentFocus(), "New Contact Added", Snackbar.LENGTH_SHORT).show();
            groups = populateGroupsFromSharedPreferences();
            adapter = new GroupsReminderAdapter(this, groups);
            rvReminders.swapAdapter(adapter, false);
            // NOTE I should collapse all views first
        }
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
