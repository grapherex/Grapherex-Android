package org.thoughtcrime.securesms.new_registration

enum class RegistrationActionType(val type: Int) {
  NONE(-1),
  RESTORE_FROM_BACKUP_TO_DOWNLOAD(1),
  RESTORE_FROM_BACKUP(2);
}