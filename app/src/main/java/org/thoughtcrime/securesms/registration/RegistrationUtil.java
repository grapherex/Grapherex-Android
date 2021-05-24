package org.thoughtcrime.securesms.registration;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.jobs.DirectoryRefreshJob;
import org.thoughtcrime.securesms.jobs.StorageSyncJob;
import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.TextSecurePreferences;

public final class RegistrationUtil {

  private static final String TAG = Log.tag(RegistrationUtil.class);

  private RegistrationUtil() {}

  /**
   * There's several events where a registration may or may not be considered complete based on what
   * path a user has taken. This will only truly mark registration as complete if all of the
   * requirements are met.
   */
  public static void maybeMarkRegistrationComplete(@NonNull Context context) {
    if (!GrapherexStore.registrationValues().isRegistrationComplete() &&
        TextSecurePreferences.isPushRegistered(context)            &&
        !Recipient.self().getProfileName().isEmpty())
    {
      Log.i(TAG, "Marking registration completed.", new Throwable());
      GrapherexStore.registrationValues().setRegistrationComplete();
      ApplicationDependencies.getJobManager().startChain(StorageSyncJob.create())
                                             .then(new DirectoryRefreshJob(false))
                                             .enqueue();
    } else if (!GrapherexStore.registrationValues().isRegistrationComplete()) {
      Log.i(TAG, "Registration is not yet complete.", new Throwable());
    }
  }
}
