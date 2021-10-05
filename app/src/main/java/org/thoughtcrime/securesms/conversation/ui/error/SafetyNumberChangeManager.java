package org.thoughtcrime.securesms.conversation.ui.error;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.annimon.stream.Stream;

import org.thoughtcrime.securesms.database.IdentityDatabase;
import org.thoughtcrime.securesms.database.MmsSmsDatabase;
import org.thoughtcrime.securesms.database.model.MessageRecord;
import org.thoughtcrime.securesms.recipients.RecipientId;

import java.util.Collection;
import java.util.List;

public final class SafetyNumberChangeManager {

    private SafetyNumberChangeViewModel viewModel;

    private String[] recipientIds;
    private long messageId = -1;
    private String messageType = "";

    private final FragmentActivity fragmentActivity;
    private final Callback callback;

    private int repeatCount;

    public SafetyNumberChangeManager(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
        Callback callback;
        if (fragmentActivity instanceof Callback) {
            callback = (Callback) fragmentActivity;
        } else {
            callback = null;
        }
        this.callback = callback;
        repeatCount = 10;
    }


    private void initViewModel() {
        viewModel = ViewModelProviders.of(fragmentActivity,
                new SafetyNumberChangeViewModel.Factory(Stream.of(recipientIds)
                        .map(RecipientId::from).toList(), (messageId != -1) ? messageId : null, messageType))
                .get(SafetyNumberChangeViewModel.class);

        viewModel.getTrustOrVerifyReady().observe(fragmentActivity, enabled -> {
                    if (enabled) {
                        repeatCount--;
                        if (repeatCount == 0) {
                            callback.onCanceled();
                            return;
                        }
                        new Handler().postDelayed(this::handleSendAnyway, 2000);
                    }
                }
        );
    }

    public void show(@NonNull List<IdentityDatabase.IdentityRecord> identityRecords) {
        List<String> ids = Stream.of(identityRecords)
                .filterNot(IdentityDatabase.IdentityRecord::isFirstUse)
                .map(record -> record.getRecipientId().serialize())
                .distinct()
                .toList();
        recipientIds = ids.toArray(new String[0]);
        initViewModel();
    }

    public void show(@NonNull MessageRecord messageRecord) {
        List<String> ids = Stream.of(messageRecord.getIdentityKeyMismatches())
                .map(mismatch -> mismatch.getRecipientId(fragmentActivity).serialize())
                .distinct()
                .toList();

        recipientIds = ids.toArray(new String[0]);
        messageId = messageRecord.getId();
        messageType = messageRecord.isMms() ? MmsSmsDatabase.MMS_TRANSPORT : MmsSmsDatabase.SMS_TRANSPORT;
        initViewModel();
    }

    public void showForCall(@NonNull RecipientId recipientId) {
        recipientIds = new String[]{recipientId.serialize()};
        initViewModel();
    }

    public void showForGroupCall(@NonNull List<IdentityDatabase.IdentityRecord> identityRecords) {
        List<String> ids = Stream.of(identityRecords)
                .filterNot(IdentityDatabase.IdentityRecord::isFirstUse)
                .map(record -> record.getRecipientId().serialize())
                .distinct()
                .toList();

        recipientIds = ids.toArray(new String[0]);
        initViewModel();
    }

    public void showForDuringGroupCall(@NonNull Collection<RecipientId> sourceRecipientIds) {
        List<String> ids = Stream.of(sourceRecipientIds)
                .map(RecipientId::serialize)
                .distinct()
                .toList();

        recipientIds = ids.toArray(new String[0]);
        initViewModel();
    }

    private void handleSendAnyway() {
        LiveData<TrustAndVerifyResult> trustOrVerifyResultLiveData = viewModel.trustOrVerifyChangedRecipients();

        Observer<TrustAndVerifyResult> observer = new Observer<TrustAndVerifyResult>() {
            @Override
            public void onChanged(TrustAndVerifyResult result) {
                if (callback != null) {
                    switch (result.getResult()) {
                        case TRUST_AND_VERIFY:
                            callback.onSendAnywayAfterSafetyNumberChange(result.getChangedRecipients());
                            break;
                        case TRUST_VERIFY_AND_RESEND:
                            callback.onMessageResentAfterSafetyNumberChange();
                            break;
                    }
                }
                trustOrVerifyResultLiveData.removeObserver(this);
            }
        };

        trustOrVerifyResultLiveData.observeForever(observer);
    }


    public interface Callback {
        void onSendAnywayAfterSafetyNumberChange(@NonNull List<RecipientId> changedRecipients);

        void onMessageResentAfterSafetyNumberChange();

        void onCanceled();
    }
}
