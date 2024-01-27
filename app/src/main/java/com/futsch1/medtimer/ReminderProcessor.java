package com.futsch1.medtimer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.futsch1.medtimer.database.Medicine;
import com.futsch1.medtimer.database.MedicineRepository;
import com.futsch1.medtimer.database.Reminder;
import com.futsch1.medtimer.database.ReminderEvent;

import java.time.Instant;

public class ReminderProcessor extends BroadcastReceiver {
    private final AlarmManager alarmManager;
    private final Context context;
    private final MedicineRepository medicineRepository;
    private final Notifications notifications;
    private Medicine pendingMedicine;
    private Reminder pendingReminder;

    public ReminderProcessor(Context context, MedicineRepository medicineRepository, Notifications notifications) {
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.context = context;
        this.medicineRepository = medicineRepository;
        this.notifications = notifications;
    }

    public void schedule(Instant timestamp, Medicine medicine, Reminder reminder) {
        pendingMedicine = medicine;
        pendingReminder = reminder;
        if (timestamp.isAfter(Instant.now())) {
            Intent reminderIntent = new Intent(context, ReminderProcessor.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, reminderIntent, PendingIntent.FLAG_IMMUTABLE);

            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, timestamp.toEpochMilli(), pendingIntent);

            Log.i("Scheduler", String.format("Scheduled reminder for %s to %s", pendingMedicine.name, timestamp));
        } else {
            processReminder();
        }
    }

    private void processReminder() {
        ReminderEvent reminderEvent = new ReminderEvent();
        reminderEvent.reminderId = pendingReminder.reminderId;
        reminderEvent.raisedTimestamp = Instant.now().getEpochSecond();
        reminderEvent.amount = pendingReminder.amount;
        reminderEvent.medicineName = pendingMedicine.name;
        reminderEvent.status = ReminderEvent.ReminderStatus.RAISED;

        reminderEvent.reminderEventId = (int) medicineRepository.insertReminderEvent(reminderEvent);

        notifications.showNotification(pendingMedicine.name, pendingReminder.amount, reminderEvent.reminderEventId);
        Log.i("Reminder", String.format("Show reminder for %s", reminderEvent.medicineName));
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        processReminder();
    }
}
