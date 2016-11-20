package cmsc436.com.callyourmom;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


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

        holder.numContacts.setText(reminder.size() + " contact reminders");
        holder.frequency.setText("Call every " + group.getFrequencyInDays() + " days");

        list.setVisibility(View.INVISIBLE);

        // This is the on item click listener for contacts in the group
        holder.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getContext(), reminder.get(i).getContactName(), Toast.LENGTH_SHORT).show();
            }
        });
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

        public ViewHolder(View itemView) {
            super(itemView);

            list = (ListView) itemView.findViewById(R.id.list);
            numContacts = (TextView) itemView.findViewById(R.id.numContacts);
            frequency = (TextView) itemView.findViewById(R.id.groupFrequency);

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
            }
            else {
                cardView.getLayoutParams().height =
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, view.getContext().getResources().getDisplayMetrics());
                Log.v("Collapse", cardView.getLayoutParams().height + " Should be back at 150");
                list.setVisibility(View.GONE);
//                TransitionManager.beginDelayedTransition(cardView);
            }
        }
    }

}
