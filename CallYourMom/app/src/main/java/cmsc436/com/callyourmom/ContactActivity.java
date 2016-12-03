package cmsc436.com.callyourmom;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ContactActivity extends AppCompatActivity {

    private TextView contactName, contactNumber;
    private NumberPicker np;
    private boolean contactPicked;
    static final int PICK_CONTACT = 1;
    public static final String reminders = "remindersFile";

    private String ayy = ContactActivity.class.getSimpleName();
    private String dataString;
    private SharedPreferences data;
    private SharedPreferences.Editor editor;

    private String contactId;

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
                    try {
                        updateReminder(contactName.getText().toString(), contactNumber.getText().toString(), np.getValue());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent();
                    intent.putExtra("name", contactName.getText().toString());
                    intent.putExtra("number", contactNumber.getText().toString());
                    intent.putExtra("days", np.getValue());
                    intent.putExtra("id", contactId);
                    setResult(RESULT_OK, intent);
                    finish();
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
                contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));
                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                contactName.setText(name);
                contactPicked = true;
                //String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

                String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                if (hasPhone.equalsIgnoreCase("1")) {
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                    phones.moveToFirst();
                    String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactNumber.setText(number);
                    Log.e("Number", number);
                }
            }
        }
    }


    protected void updateReminder(String contactName, String phoneNumber, int numDays) throws JSONException {
        //Key: numDays      JSONObject
        //Val: contact

        //For contact       JSONArray
        //      Name
        //      Phone number

        data = getSharedPreferences("data", Context.MODE_PRIVATE);
        editor = data.edit();
        dataString = data.getString(reminders, "");

        JSONObject json;
        JSONArray group;
        if (dataString != null && !dataString.equals("") && !dataString.isEmpty())
            json = new JSONObject(dataString);
        else
            json = new JSONObject();

        if (!json.has(Integer.toString(numDays)))
            json.put(Integer.toString(numDays), new JSONArray());

        group = json.getJSONArray(Integer.toString(numDays));

        JSONObject contact = new JSONObject();
        contact.put("name", contactName);
        contact.put("number", phoneNumber);
        contact.put("id", contactId);
        group.put(contact);
        json.put(Integer.toString(numDays), group);
        dataString = json.toString();

        Log.e(ayy, "TESTING ----" + dataString);

        editor.putString(reminders, dataString);
        editor.commit();

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
