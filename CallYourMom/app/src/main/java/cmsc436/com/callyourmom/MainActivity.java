package cmsc436.com.callyourmom;

import android.Manifest;
import android.content.Intent;
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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_CONTACT_REQUEST = 436;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the permission request!
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALL_LOG}, 1);

        } else if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("Service", "started?");
            // start the service
            Intent callLogIntent = new Intent(getApplicationContext(), CallLogService.class);
            startService(callLogIntent);
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent contactIntent = new Intent(MainActivity.this, ContactActivity.class);
                startActivityForResult(contactIntent, ADD_CONTACT_REQUEST);
            }
        });

        RecyclerView rvReminders = (RecyclerView) findViewById(R.id.reminders);

        ArrayList<GroupsOfReminders> groups = new ArrayList<>();
        ArrayList<CallReminder> reminders = new ArrayList<>();
        Random rand = new Random();

        for (int i = 0; i < rand.nextInt(5) + 1; i++) {
            reminders.add(new CallReminder("hello " + i, i + ""));
        }

        for (int i = 0; i < rand.nextInt(5) + 3; i++) {
            GroupsOfReminders singleGroup = new GroupsOfReminders(reminders);
            singleGroup.setFrequencyInDays(rand.nextInt(21) + 1);
            groups.add(singleGroup);
        }

        GroupsReminderAdapter adapter = new GroupsReminderAdapter(this, groups);
        rvReminders.setAdapter(adapter);
        rvReminders.setLayoutManager(new LinearLayoutManager(this));
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
            Snackbar.make(this.getCurrentFocus(), "New Contact Added", Snackbar.LENGTH_SHORT).show();
        }
    }
}
