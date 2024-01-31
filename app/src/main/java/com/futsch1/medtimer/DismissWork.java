package com.futsch1.medtimer;

import static com.futsch1.medtimer.ActivityCodes.EXTRA_REMINDER_EVENT_ID;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.futsch1.medtimer.database.MedicineRepository;
import com.futsch1.medtimer.database.ReminderEvent;

import java.time.Instant;

public class DismissWork extends Worker {
    public DismissWork(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        MedicineRepository medicineRepository = new MedicineRepository((Application) getApplicationContext());
        ReminderEvent reminderEvent = medicineRepository.getReminderEvent(getInputData().getInt(EXTRA_REMINDER_EVENT_ID, 0));
        reminderEvent.status = ReminderEvent.ReminderStatus.SKIPPED;
        reminderEvent.processedTimestamp = Instant.now().getEpochSecond();
        medicineRepository.updateReminderEvent(reminderEvent);
        Log.i("Reminder", String.format("Dismissed reminder for %s", reminderEvent.medicineName));

        return Result.success();
    }
}