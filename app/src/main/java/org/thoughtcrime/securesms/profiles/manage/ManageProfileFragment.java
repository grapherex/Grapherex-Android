package org.thoughtcrime.securesms.profiles.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.LoggingFragment;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.mediasend.AvatarSelectionActivity;
import org.thoughtcrime.securesms.mediasend.AvatarSelectionBottomSheetDialogFragment;
import org.thoughtcrime.securesms.mediasend.Media;
import org.thoughtcrime.securesms.profiles.ProfileName;
import org.thoughtcrime.securesms.profiles.manage.ManageProfileViewModel.AvatarState;
import org.thoughtcrime.securesms.recipients.Recipient;
import org.thoughtcrime.securesms.util.AvatarUtil;

import static android.app.Activity.RESULT_OK;

public class ManageProfileFragment extends LoggingFragment {

    private static final String TAG = Log.tag(ManageProfileFragment.class);
    private static final short REQUEST_CODE_SELECT_AVATAR = 31726;

    private Toolbar toolbar;
    private ProgressBar pbLoading;
    private AppCompatImageView avatarView;
    private AppCompatTextView profileNameView;
    private AppCompatTextView tvPhoneNumber;
    private AppCompatTextView tvDisplayName;
    private AlertDialog avatarProgress;

    private ManageProfileViewModel viewModel;

    @Override
    public @Nullable
    View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.manage_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        this.toolbar = view.findViewById(R.id.toolbar);
        this.pbLoading = view.findViewById(R.id.pbLoading);
        this.avatarView = view.findViewById(R.id.ivAvatar);
        this.profileNameView = view.findViewById(R.id.tvUserName);
        this.tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        this.tvDisplayName = view.findViewById(R.id.tvDisplayName);

        initializeViewModel();

        this.toolbar.setNavigationOnClickListener(v -> requireActivity().finish());
        this.avatarView.setOnClickListener(v -> onAvatarClicked());

        this.profileNameView.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(ManageProfileFragmentDirections.actionManageProfileName());
        });

//    this.usernameContainer.setOnClickListener(v -> {
//      Navigation.findNavController(v).navigate(ManageProfileFragmentDirections.actionManageUsername());
//    });
//
//    this.aboutContainer.setOnClickListener(v -> {
//      Navigation.findNavController(v).navigate(ManageProfileFragmentDirections.actionManageAbout());
//    });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_AVATAR && resultCode == RESULT_OK) {
            if (data != null && data.getBooleanExtra("delete", false)) {
                viewModel.onAvatarSelected(requireContext(), null);
                return;
            }

            Media result = data.getParcelableExtra(AvatarSelectionActivity.EXTRA_MEDIA);

            viewModel.onAvatarSelected(requireContext(), result);
        }
    }

    private void initializeViewModel() {
        viewModel = ViewModelProviders.of(this, new ManageProfileViewModel.Factory()).get(ManageProfileViewModel.class);

        viewModel.getAvatarImage().observe(getViewLifecycleOwner(), this::presentAvatarImage);
        viewModel.getPhoneNumber().observe(getViewLifecycleOwner(), this::presentPhoneNumber);
        viewModel.getAvatar().observe(getViewLifecycleOwner(), this::presentAvatar);
        viewModel.getProfileName().observe(getViewLifecycleOwner(), this::presentProfileName);
        viewModel.getEvents().observe(getViewLifecycleOwner(), this::presentEvent);
//    viewModel.getAbout().observe(getViewLifecycleOwner(), this::presentAbout);
//    viewModel.getAboutEmoji().observe(getViewLifecycleOwner(), this::presentAboutEmoji);
//
//    if (viewModel.shouldShowUsername()) {
//      viewModel.getUsername().observe(getViewLifecycleOwner(), this::presentUsername);
//    } else {
//      usernameContainer.setVisibility(View.GONE);
//    }
    }

    private void presentAvatarImage(@NonNull Recipient recipient) {
        AvatarUtil.loadIconIntoImageView(recipient, avatarView);
    }

    private void presentPhoneNumber(@NonNull String phone) {
        tvPhoneNumber.setText(phone);
    }

    private void presentAvatar(@NonNull AvatarState avatarState) {
        if (avatarState.getLoadingState() == ManageProfileViewModel.LoadingState.LOADING) {
            pbLoading.setVisibility(View.VISIBLE);
        } else if (avatarState.getLoadingState() == ManageProfileViewModel.LoadingState.LOADED) {
            pbLoading.setVisibility(View.GONE);
        }
    }

    private void presentProfileName(@Nullable ProfileName profileName) {
        if (profileName == null || profileName.isEmpty()) {
            profileNameView.setText(R.string.ManageProfileFragment_profile_name);
            tvDisplayName.setText(R.string.ManageProfileFragment_profile_name);
        } else {
            profileNameView.setText(profileName.toString());
            tvDisplayName.setText(profileName.toString());
        }
    }

//  private void presentUsername(@Nullable String username) {
//    if (username == null || username.isEmpty()) {
//      usernameView.setText(R.string.ManageProfileFragment_username);
//      usernameView.setTextColor(requireContext().getResources().getColor(R.color.signal_text_secondary));
//    } else {
//      usernameView.setText(username);
//      usernameView.setTextColor(requireContext().getResources().getColor(R.color.signal_text_primary));
//    }
//  }
//
//  private void presentAbout(@Nullable String about) {
//    if (about == null || about.isEmpty()) {
//      aboutView.setText(R.string.ManageProfileFragment_about);
//      aboutView.setTextColor(requireContext().getResources().getColor(R.color.signal_text_secondary));
//    } else {
//      aboutView.setText(about);
//      aboutView.setTextColor(requireContext().getResources().getColor(R.color.signal_text_primary));
//    }
//  }
//
//  private void presentAboutEmoji(@NonNull String aboutEmoji) {
//    if (aboutEmoji == null || aboutEmoji.isEmpty()) {
//      aboutEmojiView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_compose_24, null));
//    } else {
//      Drawable emoji = EmojiUtil.convertToDrawable(requireContext(), aboutEmoji);
//
//      if (emoji != null) {
//        aboutEmojiView.setImageDrawable(emoji);
//      } else {
//        aboutEmojiView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_compose_24, null));
//      }
//    }
//  }

    private void presentEvent(@NonNull ManageProfileViewModel.Event event) {
        switch (event) {
            case AVATAR_DISK_FAILURE:
                Toast.makeText(requireContext(), R.string.ManageProfileFragment_failed_to_set_avatar, Toast.LENGTH_LONG).show();
                break;
            case AVATAR_NETWORK_FAILURE:
                Toast.makeText(requireContext(), R.string.EditProfileNameFragment_failed_to_save_due_to_network_issues_try_again_later, Toast.LENGTH_LONG).show();
                break;
        }
    }

    private void onAvatarClicked() {
        AvatarSelectionBottomSheetDialogFragment.create(viewModel.canRemoveAvatar(),
                true,
                REQUEST_CODE_SELECT_AVATAR,
                false)
                .show(getChildFragmentManager(), null);
    }
}
