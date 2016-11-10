package cmsc436.com.callyourmom;

import java.util.Date;

public class CallReminder {
    private String contactName;
    private String telephoneNumber;
    private Date  nextReminder;
    private int numDaysForRemind;

    public CallReminder(String contactName, String telephoneNumber) {
        this.contactName = contactName;
        this.telephoneNumber = telephoneNumber;
        this.nextReminder = new Date();
        this.numDaysForRemind = 7;
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

    public Date getNextReminder() {
        return nextReminder;
    }

    public void setNextReminder(Date nextReminder) {
        this.nextReminder = nextReminder;
    }
}
