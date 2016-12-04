package cmsc436.com.callyourmom;

import android.animation.ObjectAnimator;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.R.id.input;
import static cmsc436.com.callyourmom.ContactActivity.reminders;


public class GroupsReminderAdapter extends RecyclerView.Adapter<GroupsReminderAdapter.ViewHolder> {
    private List<GroupsOfReminders> mGroups;
    private Context mContext;

    public GroupsReminderAdapter(Context context, List<GroupsOfReminders> groups) {
        mGroups = groups;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View itemLayout = inflater.inflate(R.layout.group_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayout);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final GroupsOfReminders group = mGroups.get(position);
        final List<CallReminder> reminder = group.getRemindersInGroup();
        List<HashMap<String, String>> temp = new ArrayList<>();
        List<Bitmap> temp2 = new ArrayList<>();
        int i = 0;
        for (CallReminder contact : reminder) {
            temp.add(new HashMap<String, String>());

            temp.get(i).put("contactName", contact.getContactName());
            temp.get(i).put("telephoneNumber", contact.getTelephoneNumber());

            i++;
        }
        ListView list = holder.list;
        String[] from = {"contactName", "telephoneNumber"};
        int[] to = {R.id.contact_name, R.id.contact_number};
        list.setAdapter(new SimpleAdapter(getContext(), temp, R.layout.contact_item, from, to));

        if (reminder.size() == 1)
            holder.numContacts.setText(reminder.size() + " contact reminder");
        else
            holder.numContacts.setText(reminder.size() + " contact reminders");




        ContentResolver contentResolver;
        int j = 0;
        for(CallReminder contact : reminder) {

            contentResolver = getContext().getContentResolver();
            Bitmap bitmap = loadContactPhoto(contentResolver, (long)Integer.parseInt(contact.getId()));
            temp2.add(bitmap);

            j++;
        }

        for(int x = 0; x < temp2.size(); x++){
            ImageView n = (ImageView) list.findViewById(R.id.contact_photo);
            if(n != null) {
                n.setImageBitmap(temp2.get(x));
            }
        }





        holder.frequency.setText("Call every " + group.getFrequencyInDays() + " days");

        list.setVisibility(View.INVISIBLE);



        // This is the on item click listener for contacts in the group
        holder.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                CallReminder clickedReminder = reminder.get(i);

                deleteReminderDialog(clickedReminder);
            }
        });
    }




    public static Bitmap loadContactPhoto(ContentResolver cr, long id){
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null){
            return null;
        }
        try {
            return BitmapFactory.decodeStream(input);
        }
        catch(Exception e){
            return null;
        }
    }
    private void deleteReminderDialog(final CallReminder reminder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setCancelable(false);
        builder.setTitle("Delete " + reminder.getContactName() + "'s reminder?");
        builder.setMessage("Are you sure you want to delete this reminder?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                try {
                    deleteFromSharedPreferences(reminder);
                    MainActivity activity = (MainActivity) getContext();
                    activity.updateRecyclerView();

                    // TODO have to delete the alarm for the reminder
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.v("Delete", "delete this contact");
            }
        });
        builder.setNegativeButton("No ", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v("No Delete", "do not delete this contact");
            }
        });

        // Create the AlertDialog object and return it
        builder.create().show();
    }

    public void deleteFromSharedPreferences(CallReminder reminder) throws JSONException {
        SharedPreferences data = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = data.edit();
        String dataString = data.getString(reminders, "");

        JSONObject json;
        JSONArray group;
        if (dataString != null && !dataString.equals("") && !dataString.isEmpty())
            json = new JSONObject(dataString);
        else
            json = new JSONObject();

        group = json.getJSONArray(Integer.toString(reminder.getNumDaysForRemind()));

        for (int i = 0; i < group.length(); i++) {
            JSONObject obj = (JSONObject) group.get(i);
            if (obj.getString("name").equals(reminder.getContactName()) &&
                    obj.getString("number").equals(reminder.getTelephoneNumber()) &&
                    obj.getString("id").equals(reminder.getId() + "")) {
                group = removeFromJsonArray(group, i);
                Log.v("Delete", "I think we did it");
            } else {
                Log.v("Delete", "Do nothing");
            }
        }
        if (group.length() != 0)
            json.put(Integer.toString(reminder.getNumDaysForRemind()), group);
        else
            json.remove(Integer.toString(reminder.getNumDaysForRemind()));
        dataString = json.toString();

        Log.e("Removal", dataString);

        editor.putString(reminders, dataString);
        editor.apply();
    }

    public JSONArray removeFromJsonArray(JSONArray arr, int index) {
        JSONArray output = new JSONArray();
        int len = arr.length();
        for (int i = 0; i < len; i++)   {
            if (i != index) {
                try {
                    output.put(arr.get(i));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return output;
        //return this; If you need the input array in case of a failed attempt to remove an item.
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    private Context getContext() {
        return mContext;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ListView list;
        public TextView numContacts;
        public TextView frequency;
        public ImageView expansionArrow;

        private int rotationAngle = 0;

        public ViewHolder(View itemView) {
            super(itemView);

            list = (ListView) itemView.findViewById(R.id.list);
            numContacts = (TextView) itemView.findViewById(R.id.numContacts);
            frequency = (TextView) itemView.findViewById(R.id.groupFrequency);
            expansionArrow = (ImageView) itemView.findViewById(R.id.expansion_icon);


            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            CardView cardView = (CardView) view.findViewById(R.id.card_view);

            // We clicked on the card
            if (list.getVisibility() == View.GONE || list.getVisibility() == View.INVISIBLE) {
                TransitionManager.beginDelayedTransition(cardView);
                list.setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams params = cardView.getLayoutParams();
                params.height +=
                        list.getAdapter().getCount() * (int)
                                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, view.getContext().getResources().getDisplayMetrics());
                cardView.setLayoutParams(params);

                rotateArrow();
            }
            else {
                cardView.getLayoutParams().height =
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, view.getContext().getResources().getDisplayMetrics());
                Log.v("Collapse", cardView.getLayoutParams().height + " Should be back at 150");
                list.setVisibility(View.GONE);
//                TransitionManager.beginDelayedTransition(cardView);

                rotateArrow();
            }
        }

        public void rotateArrow() {
            ObjectAnimator anim = ObjectAnimator.ofFloat(expansionArrow, "rotation", rotationAngle, rotationAngle + 180);
            anim.setDuration(300);
            anim.start();
            rotationAngle += 180;
            rotationAngle = rotationAngle%360;
        }
    }

}
