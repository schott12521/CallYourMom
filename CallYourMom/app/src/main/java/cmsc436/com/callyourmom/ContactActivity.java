package cmsc436.com.callyourmom;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Contacts;
import android.provider.ContactsContract;
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
import android.app.Activity;
import android.widget.*;
import android.view.View.*;
import android.net.*;
import android.database.Cursor;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ContactActivity extends AppCompatActivity {

    private static TextView contactName, contactNumber;
    private NumberPicker np;
    private boolean contactPicked;
    static final int PICK_CONTACT = 1;
    private static final String reminders = "remindersFile";

    private String ayy = ContactActivity.class.getSimpleName();
    private String dataString;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        contactPicked = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        contactName = (TextView) findViewById(R.id.contact_name);
        contactNumber = (TextView) findViewById(R.id.contact_number);
        np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(365);

        data = getSharedPreferences("data", 0);
        editor = data.edit();
        dataString = data.getString(reminders, "0");

        final Button mSelectContact = (Button) findViewById(R.id.chooseContact);
        mSelectContact.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });

        final Button mSubmit = (Button) findViewById(R.id.submit);
        mSubmit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactPicked) {
                    CallReminder newReminder = new CallReminder(contactName.getText().toString(), "3");
                    newReminder.setNumDaysForRemind(np.getValue());
                    try {
                        updateReminder(contactName.getText().toString(), "410-999-5555", 3);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    editor.putString(reminders, dataString);
                    finishActivity(MainActivity.ADD_CONTACT_REQUEST);
                } else {
                    Snackbar.make(v, "No Contact Selected", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_CONTACT && resultCode == RESULT_OK) {
            Uri contactData = data.getData();
            Cursor c = managedQuery(contactData, null, null, null, null);
            if (c.moveToFirst()) {
                String id = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contactName.setText(name + " " + id);
                contactPicked = true;
                String number = c.getString(c.getColumnIndexOrThrow(Contacts.People.NUMBER));

            }
        }
    }


    protected void updateReminder(String contactName, String phoneNumber, int numDays) throws JSONException {
        //Key: numDays      JSONObject
        //Val: contact

        //For contact       JSONArray
        //      Name
        //      Phone number

        JSONObject json;
        JSONArray group;
        if (dataString != null)
            json = new JSONObject(dataString);
        else
            json = new JSONObject();

        if (json.has(Integer.toString(numDays)))
            json.put(Integer.toString(numDays), new JSONArray());
        group = json.getJSONArray(Integer.toString(numDays));
        JSONObject contact = new JSONObject();
        contact.put("name", contactName);
        contact.put("number", phoneNumber);
        group.put(contact);
        json.put(Integer.toString(numDays), group);
        dataString = json.toString();

        Log.e(ayy, "TESTING ----" + dataString);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }


}
