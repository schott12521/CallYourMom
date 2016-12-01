package cmsc436.com.callyourmom;

import java.util.Date;

public class CallReminder {
    private String contactName;
    private String telephoneNumber;
    private String id;
    private int numDaysForRemind;

    public CallReminder(String contactName, String telephoneNumber, String id, int numDaysForRemind) {
        this.contactName = contactName;
        this.telephoneNumber = telephoneNumber;
        this.id = id;
        this.numDaysForRemind = numDaysForRemind;
    }

    public int getNumDaysForRemind() {
        return numDaysForRemind;
    }

    public void setNumDaysForRemind(int numDaysForRemind) {
        this.numDaysForRemind = numDaysForRemind;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
