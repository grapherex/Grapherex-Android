package org.thoughtcrime.securesms.payments.backup;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.payments.Mnemonic;

public final class PaymentsRecoveryRepository {
  public @NonNull Mnemonic getMnemonic() {
    return GrapherexStore.paymentsValues().getPaymentsMnemonic();
  }
}
