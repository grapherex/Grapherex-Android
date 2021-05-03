package org.thoughtcrime.securesms.profiles.edit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.signal.core.util.EditTextUtil;
import org.signal.core.util.StreamUtil;
import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.LoggingFragment;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.groups.GroupId;
import org.thoughtcrime.securesms.mediasend.AvatarSelectionActivity;
import org.thoughtcrime.securesms.mediasend.AvatarSelectionBottomSheetDialogFragment;
import org.thoughtcrime.securesms.mediasend.Media;
import org.thoughtcrime.securesms.mms.GlideApp;
import org.thoughtcrime.securesms.profiles.manage.EditProfileNameFragment;
import org.thoughtcrime.securesms.providers.BlobProvider;
import org.thoughtcrime.securesms.registration.RegistrationUtil;
import org.thoughtcrime.securesms.util.FeatureFlags;
import org.thoughtcrime.securesms.util.concurrent.SimpleTask;
import org.thoughtcrime.securesms.util.text.AfterTextChanged;

import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static org.thoughtcrime.securesms.profiles.edit.EditProfileActivity.EXCLUDE_SYSTEM;
import static org.thoughtcrime.securesms.profiles.edit.EditProfileActivity.GROUP_ID;
import static org.thoughtcrime.securesms.profiles.edit.EditProfileActivity.NEXT_BUTTON_TEXT;
import static org.thoughtcrime.securesms.profiles.edit.EditProfileActivity.NEXT_INTENT;

public class EditProfileFragment extends LoggingFragment {

    private static final String TAG = Log.tag(EditProfileFragment.class);
    private static final short REQUEST_CODE_SELECT_AVATAR = 31726;

    private AppCompatImageView ivCentralCircle;
    private AppCompatImageView ivHeaderIcon;
    private AppCompatTextView finishButton;
    private AppCompatEditText givenName;

    private Intent nextIntent;

    private EditProfileViewModel viewModel;

    private Controller controller;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof Controller) {
            controller = (Controller) context;
        } else {
            throw new IllegalStateException("Context must subclass Controller");
        }
    }

    @Override
    public @Nullable
    View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_create_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        GroupId groupId = GroupId.parseNullableOrThrow(requireArguments().getString(GROUP_ID, null));

        initializeResources(view, groupId);
        initializeViewModel(requireArguments().getBoolean(EXCLUDE_SYSTEM, false), groupId, savedInstanceState != null);
        initializeProfileAvatar();
        initializeProfileName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_AVATAR && resultCode == RESULT_OK) {

            if (data != null && data.getBooleanExtra("delete", false)) {
                viewModel.setAvatar(null);
                ivCentralCircle.setImageResource(R.drawable.ic_rationale_dialog_big_green_circle);
                ivHeaderIcon.setVisibility(View.VISIBLE);
                return;
            }

            SimpleTask.run(() -> {
                        try {
                            Media result = data.getParcelableExtra(AvatarSelectionActivity.EXTRA_MEDIA);
                            InputStream stream = BlobProvider.getInstance().getStream(requireContext(), result.getUri());

                            return StreamUtil.readFully(stream);
                        } catch (IOException ioException) {
                            Log.w(TAG, ioException);
                            return null;
                        }
                    },
                    (avatarBytes) -> {
                        if (avatarBytes != null) {
                            ivHeaderIcon.setVisibility(View.GONE);
                            viewModel.setAvatar(avatarBytes);
                            GlideApp.with(EditProfileFragment.this)
                                    .load(avatarBytes)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .circleCrop()
                                    .into(ivCentralCircle);
                        } else {
                            Toast.makeText(requireActivity(), R.string.CreateProfileActivity_error_setting_profile_photo, Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void initializeViewModel(boolean excludeSystem, @Nullable GroupId groupId, boolean hasSavedInstanceState) {
        EditProfileRepository repository;

        if (groupId != null) {
            repository = new EditGroupProfileRepository(requireContext(), groupId);
        } else {
            repository = new EditSelfProfileRepository(requireContext(), excludeSystem);
        }

        EditProfileViewModel.Factory factory = new EditProfileViewModel.Factory(repository, hasSavedInstanceState, groupId);

        viewModel = ViewModelProviders.of(requireActivity(), factory)
                .get(EditProfileViewModel.class);
    }

    private void initializeResources(@NonNull View view, @Nullable GroupId groupId) {
        Bundle arguments = requireArguments();
        boolean isEditingGroup = groupId != null;

        this.ivHeaderIcon = view.findViewById(R.id.ivHeaderIcon);
        this.ivCentralCircle = view.findViewById(R.id.ivCentralCircle);
        View regArea = view.findViewById(R.id.regArea);
        this.givenName = view.findViewById(R.id.given_name);
        this.finishButton = view.findViewById(R.id.finishButton);
        this.nextIntent = arguments.getParcelable(NEXT_INTENT);

        regArea.setOnClickListener(v -> startAvatarSelection());

        ivHeaderIcon.setImageResource(R.drawable.ic_edit_profile);
        ivCentralCircle.setImageResource(R.drawable.ic_rationale_dialog_big_green_circle);
        view.findViewById(R.id.ivCamera).setVisibility(View.VISIBLE);

        if (isEditingGroup) {
            EditTextUtil.addGraphemeClusterLimitFilter(givenName, FeatureFlags.getMaxGroupNameGraphemeLength());
            givenName.addTextChangedListener(new AfterTextChanged(s -> viewModel.setGivenName(s.toString())));
            givenName.setHint(R.string.EditProfileFragment__group_name);
            givenName.requestFocus();
        } else {
            EditTextUtil.addGraphemeClusterLimitFilter(givenName, EditProfileNameFragment.NAME_MAX_GLYPHS);
            this.givenName.addTextChangedListener(new AfterTextChanged(s -> {
                EditProfileNameFragment.trimFieldToMaxByteLength(s);
                viewModel.setGivenName(s.toString());
            }));
        }

        this.finishButton.setOnClickListener(v -> {
            handleUpload();
        });

        this.finishButton.setText(arguments.getInt(NEXT_BUTTON_TEXT, R.string.CreateProfileActivity_next));
    }

    private void initializeProfileName() {
        viewModel.isFormValid().observe(getViewLifecycleOwner(), isValid -> {
            finishButton.setEnabled(isValid);
            finishButton.setAlpha(isValid ? 1f : 0.5f);
        });

        viewModel.givenName().observe(getViewLifecycleOwner(), givenName -> updateFieldIfNeeded(this.givenName, givenName));
    }

    private void initializeProfileAvatar() {
        viewModel.avatar().observe(getViewLifecycleOwner(), bytes -> {
            if (bytes == null) return;

            GlideApp.with(this)
                    .load(bytes)
                    .circleCrop()
                    .into(ivCentralCircle);
        });
    }

    private static void updateFieldIfNeeded(@NonNull EditText field, @NonNull String value) {
        String fieldTrimmed = field.getText().toString().trim();
        String valueTrimmed = value.trim();

        if (!fieldTrimmed.equals(valueTrimmed)) {
            boolean setSelectionToEnd = field.getText().length() == 0;

            field.setText(value);

            if (setSelectionToEnd) {
                field.setSelection(field.getText().length());
            }
        }
    }

    private void startAvatarSelection() {
        AvatarSelectionBottomSheetDialogFragment.create(viewModel.canRemoveProfilePhoto(),
                true,
                REQUEST_CODE_SELECT_AVATAR,
                viewModel.isGroup())
                .show(getChildFragmentManager(), null);
    }

    private void handleUpload() {
        finishButton.setEnabled(false);
        viewModel.submitProfile(uploadResult -> {
            if (uploadResult == EditProfileRepository.UploadResult.SUCCESS) {
                finishButton.setEnabled(false);
                RegistrationUtil.maybeMarkRegistrationComplete(requireContext());
                handleFinishedLegacy();
            } else {
                finishButton.setEnabled(true);
                Toast.makeText(requireContext(), R.string.CreateProfileActivity_problem_setting_profile, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleFinishedLegacy() {
        if (nextIntent != null) startActivity(nextIntent);
        controller.onProfileNameUploadCompleted();
    }

    public interface Controller {
        void onProfileNameUploadCompleted();
    }
}
