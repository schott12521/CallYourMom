package cmsc436.com.callyourmom;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

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

        View itemLayout = inflater.inflate(R.layout.item_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayout);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        List<CallReminder> reminder = mGroups.get(position).getRemindersInGroup();
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

        list.setVisibility(View.INVISIBLE);
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

        public ViewHolder(View itemView) {
            super(itemView);

            list = (ListView) itemView.findViewById(R.id.list);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            CardView cardView = (CardView) view.findViewById(R.id.card_view);
            // We clicked on the card
            if (list.getVisibility() == View.GONE || list.getVisibility() == View.INVISIBLE) {
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
            }
        }
    }

}
