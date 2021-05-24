package org.thoughtcrime.securesms.megaphone;

import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (GrapherexStore.kbsValues().hasOptedOut()) {
      return false;
    }

    if (!GrapherexStore.kbsValues().hasPin()) {
      return false;
    }

    if (!GrapherexStore.pinValues().arePinRemindersEnabled()) {
      return false;
    }

    if (!TextSecurePreferences.isPushRegistered(ApplicationDependencies.getApplication())) {
      return false;
    }

    long lastSuccessTime = GrapherexStore.pinValues().getLastSuccessfulEntryTime();
    long interval        = GrapherexStore.pinValues().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
