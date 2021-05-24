package org.thoughtcrime.securesms.preferences;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;

import com.annimon.stream.Stream;

import org.signal.core.util.concurrent.SignalExecutors;
import org.thoughtcrime.securesms.ApplicationPreferencesActivity;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.components.settings.BaseSettingsAdapter;
import org.thoughtcrime.securesms.components.settings.BaseSettingsFragment;
import org.thoughtcrime.securesms.components.settings.CustomizableSingleSelectSetting;
import org.thoughtcrime.securesms.components.settings.SingleSelectSetting;
import org.thoughtcrime.securesms.database.DatabaseFactory;
import org.thoughtcrime.securesms.database.ThreadDatabase;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.keyvalue.GrapherexStore;
import org.thoughtcrime.securesms.keyvalue.KeepMessagesDuration;
import org.thoughtcrime.securesms.keyvalue.SettingsValues;
import org.thoughtcrime.securesms.mediaoverview.MediaOverviewActivity;
import org.thoughtcrime.securesms.permissions.Permissions;
import org.thoughtcrime.securesms.preferences.widgets.StoragePreferenceCategory;
import org.thoughtcrime.securesms.util.MappingModelList;
import org.thoughtcrime.securesms.util.StringUtil;

import java.text.NumberFormat;

public class StoragePreferenceFragment extends ListSummaryPreferenceFragment {

  private Preference keepMessages;
  private Preference trimLength;

  @Override
  public void onCreate(@Nullable Bundle paramBundle) {
    super.onCreate(paramBundle);

    findPreference("pref_storage_clear_message_history")
            .setOnPreferenceClickListener(new ClearMessageHistoryClickListener());

    trimLength = findPreference(SettingsValues.THREAD_TRIM_LENGTH);
    trimLength.setOnPreferenceClickListener(p -> {
      getApplicationPreferencesActivity().requireSupportActionBar().setTitle(R.string.preferences__conversation_length_limit);
      getApplicationPreferencesActivity().pushFragment(BaseSettingsFragment.create(new ConversationLengthLimitConfiguration()));
      return true;
    });

    keepMessages = findPreference(SettingsValues.KEEP_MESSAGES_DURATION);
    keepMessages.setOnPreferenceClickListener(p -> {
      getApplicationPreferencesActivity().requireSupportActionBar().setTitle(R.string.preferences__keep_messages);
      getApplicationPreferencesActivity().pushFragment(BaseSettingsFragment.create(new KeepMessagesConfiguration()));
      return true;
    });

    StoragePreferenceCategory       storageCategory = (StoragePreferenceCategory) findPreference("pref_storage_category");
    FragmentActivity                activity        = requireActivity();
    ApplicationPreferencesViewModel viewModel       = ApplicationPreferencesViewModel.getApplicationPreferencesViewModel(activity);

    storageCategory.setOnFreeUpSpace(() -> activity.startActivity(MediaOverviewActivity.forAll(activity)));

    viewModel.getStorageBreakdown().observe(activity, storageCategory::setStorage);
  }

  @Override
  public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
    addPreferencesFromResource(R.xml.preferences_storage);
  }

  @Override
  public void onResume() {
    super.onResume();
    ((ApplicationPreferencesActivity) requireActivity()).requireSupportActionBar().setTitle(R.string.preferences__storage);

    FragmentActivity                activity  = requireActivity();
    ApplicationPreferencesViewModel viewModel = ApplicationPreferencesViewModel.getApplicationPreferencesViewModel(activity);

    viewModel.refreshStorageBreakdown(activity.getApplicationContext());

    keepMessages.setSummary(GrapherexStore.settings().getKeepMessagesDuration().getStringResource());

    trimLength.setSummary(GrapherexStore.settings().isTrimByLengthEnabled() ? getString(R.string.preferences_storage__s_messages, NumberFormat.getInstance().format(GrapherexStore.settings().getThreadTrimLength()))
                                                                         : getString(R.string.preferences_storage__none));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
  }

  private @NonNull ApplicationPreferencesActivity getApplicationPreferencesActivity() {
    return (ApplicationPreferencesActivity) requireActivity();
  }

  private class ClearMessageHistoryClickListener implements Preference.OnPreferenceClickListener {
    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
      new AlertDialog.Builder(requireActivity())
                     .setTitle(R.string.preferences_storage__clear_message_history)
                     .setMessage(R.string.preferences_storage__this_will_delete_all_message_history_and_media_from_your_device)
                     .setPositiveButton(R.string.delete, (d, w) -> showAreYouReallySure())
                     .setNegativeButton(android.R.string.cancel, null)
                     .show();

      return true;
    }

    private void showAreYouReallySure() {
      new AlertDialog.Builder(requireActivity())
                     .setTitle(R.string.preferences_storage__are_you_sure_you_want_to_delete_all_message_history)
                     .setMessage(R.string.preferences_storage__all_message_history_will_be_permanently_removed_this_action_cannot_be_undone)
                     .setPositiveButton(R.string.preferences_storage__delete_all_now, (d, w) -> SignalExecutors.BOUNDED.execute(() -> DatabaseFactory.getThreadDatabase(ApplicationDependencies.getApplication()).deleteAllConversations()))
                     .setNegativeButton(android.R.string.cancel, null)
                     .show();
    }
  }

  public static class KeepMessagesConfiguration extends BaseSettingsFragment.Configuration implements SingleSelectSetting.SingleSelectSelectionChangedListener {

    @Override
    public void configureAdapter(@NonNull BaseSettingsAdapter adapter) {
      adapter.configureSingleSelect(this);
    }

    @Override
    public @NonNull MappingModelList getSettings() {
      KeepMessagesDuration currentDuration = GrapherexStore.settings().getKeepMessagesDuration();
      return Stream.of(KeepMessagesDuration.values())
                   .map(duration -> new SingleSelectSetting.Item(duration, activity.getString(duration.getStringResource()), null, duration.equals(currentDuration)))
                   .collect(MappingModelList.toMappingModelList());
    }

    @Override
    public void onSelectionChanged(@NonNull Object selection) {
      KeepMessagesDuration currentDuration = GrapherexStore.settings().getKeepMessagesDuration();
      KeepMessagesDuration newDuration     = (KeepMessagesDuration) selection;

      if (newDuration.ordinal() > currentDuration.ordinal()) {
        new AlertDialog.Builder(activity)
                       .setTitle(R.string.preferences_storage__delete_older_messages)
                       .setMessage(activity.getString(R.string.preferences_storage__this_will_permanently_delete_all_message_history_and_media, activity.getString(newDuration.getStringResource())))
                       .setPositiveButton(R.string.delete, (d, w) -> updateTrimByTime(newDuration))
                       .setNegativeButton(android.R.string.cancel, null)
                       .show();
      } else {
        updateTrimByTime(newDuration);
      }
    }

    private void updateTrimByTime(@NonNull KeepMessagesDuration newDuration) {
      GrapherexStore.settings().setKeepMessagesForDuration(newDuration);
      updateSettingsList();
      ApplicationDependencies.getTrimThreadsByDateManager().scheduleIfNecessary();
    }
  }

  public static class ConversationLengthLimitConfiguration extends BaseSettingsFragment.Configuration implements CustomizableSingleSelectSetting.CustomizableSingleSelectionListener {

    private static final int CUSTOM_LENGTH = -1;

    @Override
    public void configureAdapter(@NonNull BaseSettingsAdapter adapter) {
      adapter.configureSingleSelect(this);
      adapter.configureCustomizableSingleSelect(this);
    }

    @Override
    public @NonNull MappingModelList getSettings() {
      int              trimLength   = GrapherexStore.settings().isTrimByLengthEnabled() ? GrapherexStore.settings().getThreadTrimLength() : 0;
      int[]            options      = activity.getResources().getIntArray(R.array.conversation_length_limit);
      boolean          hasSelection = false;
      MappingModelList settings     = new MappingModelList();

      for (int option : options) {
        boolean isSelected = option == trimLength;
        String  text       = option == 0 ? activity.getString(R.string.preferences_storage__none)
                                         : activity.getString(R.string.preferences_storage__s_messages, NumberFormat.getInstance().format(option));

        settings.add(new SingleSelectSetting.Item(option, text, null, isSelected));

        hasSelection = hasSelection || isSelected;
      }

      int currentValue = GrapherexStore.settings().getThreadTrimLength();
      settings.add(new CustomizableSingleSelectSetting.Item(CUSTOM_LENGTH,
                                                            activity.getString(R.string.preferences_storage__custom),
                                                            !hasSelection,
                                                            currentValue,
                                                            activity.getString(R.string.preferences_storage__s_messages, NumberFormat.getInstance().format(currentValue))));
      return settings;
    }

    @SuppressLint("InflateParams")
    @Override
    public void onCustomizeClicked(@Nullable CustomizableSingleSelectSetting.Item item) {
      boolean trimLengthEnabled = GrapherexStore.settings().isTrimByLengthEnabled();
      int     trimLength        = trimLengthEnabled ? GrapherexStore.settings().getThreadTrimLength() : 0;

      View     view     = LayoutInflater.from(activity).inflate(R.layout.customizable_setting_edit_text, null, false);
      EditText editText = view.findViewById(R.id.customizable_setting_edit_text);
      if (trimLength > 0) {
        editText.setText(String.valueOf(trimLength));
      }

      AlertDialog dialog = new AlertDialog.Builder(activity)
                                          .setTitle(R.string.preferences__conversation_length_limit)
                                          .setView(view)
                                          .setPositiveButton(android.R.string.ok, (d, w) -> onSelectionChanged(Integer.parseInt(editText.getText().toString())))
                                          .setNegativeButton(android.R.string.cancel, (d, w) -> updateSettingsList())
                                          .create();

      dialog.setOnShowListener(d -> {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!TextUtils.isEmpty(editText.getText()));
        editText.requestFocus();
        editText.addTextChangedListener(new TextWatcher() {
          @Override
          public void afterTextChanged(@NonNull Editable sequence) {
            CharSequence trimmed = StringUtil.trimSequence(sequence);
            if (TextUtils.isEmpty(trimmed)) {
              sequence.replace(0, sequence.length(), "");
            } else {
              try {
                Integer.parseInt(trimmed.toString());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                return;
              } catch (NumberFormatException e) {
                String onlyDigits = trimmed.toString().replaceAll("[^\\d]", "");
                if (!onlyDigits.equals(trimmed.toString())) {
                  sequence.replace(0, sequence.length(), onlyDigits);
                }
              }
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
          }

          @Override
          public void beforeTextChanged(@NonNull CharSequence sequence, int start, int count, int after) {}

          @Override
          public void onTextChanged(@NonNull CharSequence sequence, int start, int before, int count) {}
        });
      });

      dialog.show();
    }

    @Override
    public void onSelectionChanged(@NonNull Object selection) {
      boolean trimLengthEnabled = GrapherexStore.settings().isTrimByLengthEnabled();
      int     trimLength        = trimLengthEnabled ? GrapherexStore.settings().getThreadTrimLength() : 0;
      int     newTrimLength     = (Integer) selection;

      if (newTrimLength > 0 && (!trimLengthEnabled || newTrimLength < trimLength)) {
        new AlertDialog.Builder(activity)
                       .setTitle(R.string.preferences_storage__delete_older_messages)
                       .setMessage(activity.getString(R.string.preferences_storage__this_will_permanently_trim_all_conversations_to_the_d_most_recent_messages, NumberFormat.getInstance().format(newTrimLength)))
                       .setPositiveButton(R.string.delete, (d, w) -> updateTrimByLength(newTrimLength))
                       .setNegativeButton(android.R.string.cancel, null)
                       .show();
      } else if (newTrimLength == CUSTOM_LENGTH) {
        onCustomizeClicked(null);
      } else {
        updateTrimByLength(newTrimLength);
      }
    }

    private void updateTrimByLength(int length) {
      boolean restrictingChange = !GrapherexStore.settings().isTrimByLengthEnabled() || length < GrapherexStore.settings().getThreadTrimLength();

      GrapherexStore.settings().setThreadTrimByLengthEnabled(length > 0);
      GrapherexStore.settings().setThreadTrimLength(length);
      updateSettingsList();

      if (GrapherexStore.settings().isTrimByLengthEnabled() && restrictingChange) {
        KeepMessagesDuration keepMessagesDuration = GrapherexStore.settings().getKeepMessagesDuration();

        long trimBeforeDate = keepMessagesDuration != KeepMessagesDuration.FOREVER ? System.currentTimeMillis() - keepMessagesDuration.getDuration()
                                                                                   : ThreadDatabase.NO_TRIM_BEFORE_DATE_SET;

        SignalExecutors.BOUNDED.execute(() -> DatabaseFactory.getThreadDatabase(ApplicationDependencies.getApplication()).trimAllThreads(length, trimBeforeDate));
      }
    }
  }
}
