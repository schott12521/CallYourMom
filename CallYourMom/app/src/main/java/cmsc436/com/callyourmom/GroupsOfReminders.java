package cmsc436.com.callyourmom;

import java.util.List;

public class GroupsOfReminders {
    List<CallReminder> remindersInGroup;
    int frequencyInDays;

    public int getFrequencyInDays() {
        return frequencyInDays;
    }

    public void setFrequencyInDays(int frequencyInDays) {
        this.frequencyInDays = frequencyInDays;
    }

    public GroupsOfReminders(List<CallReminder> remindersInGroup) {
        this.remindersInGroup = remindersInGroup;
    }

    public List<CallReminder> getRemindersInGroup() {
        return remindersInGroup;
    }

    public void setRemindersInGroup(List<CallReminder> remindersInGroup) {
        this.remindersInGroup = remindersInGroup;
    }
}
