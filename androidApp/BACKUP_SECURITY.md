# Android Backup Security

## Overview

This document explains the Android backup configuration for the BikeManager app and the security measures implemented to protect user data.

## What Data is Excluded from Backups

The following data is explicitly excluded from Android's automatic backup system:

### 1. SQLite Database Files
- `bikemanager.db` - The main SQLite database containing all maintenance records
- `bikemanager.db-shm` - Shared memory file (SQLite WAL mode)
- `bikemanager.db-wal` - Write-Ahead Log file (SQLite WAL mode)
- `bikemanager.db-journal` - SQLite rollback journal file

### 2. SharedPreferences
- All SharedPreferences files are excluded as they may contain authentication tokens or sensitive user preferences

### 3. Device-to-Device Transfers (Android 12+)
On Android 12 and above, the same exclusions apply during device-to-device transfers initiated through the Android setup wizard.

## Why the Database is Excluded

The SQLite database is excluded from backups for the following security reasons:

### Security Concerns
1. **Data Exposure Risk**: Android backups can be extracted using `adb backup` on devices with USB debugging enabled, potentially exposing maintenance records to unauthorized access
2. **Unencrypted Storage**: While Google Drive backups are encrypted in transit and at rest, local ADB backups are not automatically encrypted unless the user sets a backup password
3. **Compliance**: Excluding sensitive data from backups is a security best practice that reduces the attack surface and data exposure risk

### Data Integrity
1. **Corruption Prevention**: Backing up SQLite database files while the app is running can lead to backup corruption, especially with journal files
2. **Consistency**: Excluding database files ensures that users don't restore potentially stale or corrupted data

## How Data is Restored

Despite excluding the database from backups, users won't lose their data when reinstalling the app:

### Firebase Sync
- All maintenance records are synchronized with Firebase Firestore in real-time
- When a user signs in after reinstalling the app, their data is automatically restored from Firebase
- This provides a more reliable and secure backup mechanism than Android's automatic backup

### What IS Backed Up
Non-sensitive app data that doesn't contain user content is still backed up:
- App cache (if not explicitly excluded)
- Other non-sensitive files

## Testing Backup Behavior

### Prerequisites
- Android device or emulator with USB debugging enabled
- ADB (Android Debug Bridge) installed on your development machine
- App installed and configured

### Method 1: Using Backup Manager (Recommended)

```bash
# Enable backup logging
adb shell setprop log.tag.BackupXmlParserLogging VERBOSE

# Trigger a backup
adb shell bmgr backupnow com.bikemanager.android

# Check backup status
adb shell bmgr list transports

# View backup logs
adb logcat -s BackupManagerService
```

### Method 2: Using ADB Backup

```bash
# Create a backup (user will be prompted to set optional password)
adb backup -f backup.ab -noapk com.bikemanager.android

# Convert backup to tar for inspection (requires Android Backup Extractor)
# Download from: https://github.com/nelenkov/android-backup-extractor
java -jar abe.jar unpack backup.ab backup.tar

# Extract tar and verify database files are NOT present
tar -xvf backup.tar
ls -la apps/com.bikemanager.android/db/
# Should NOT contain bikemanager.db files

# Verify SharedPreferences are NOT present
ls -la apps/com.bikemanager.android/sp/
# Should be empty or not exist
```

### Method 3: Inspect Built APK

```bash
# Build the app
./gradlew :androidApp:assembleDebug

# Verify backup rules are included in APK
aapt dump xmltree androidApp/build/outputs/apk/debug/androidApp-debug.apk res/xml/backup_rules.xml

# Or use Android Studio's APK Analyzer:
# Build > Analyze APK > Select androidApp-debug.apk > resources > res > xml > backup_rules.xml
```

## User Implications

### What Users Should Know

1. **Data is Safe**: Even though the database isn't backed up by Android, all maintenance records are securely stored in Firebase and will be restored when signing in

2. **Reinstallation Process**:
   - Uninstall the app
   - Reinstall from Google Play Store
   - Sign in with the same account
   - All maintenance records will automatically sync from Firebase

3. **No Data Loss**: Users won't lose any data when:
   - Switching to a new device
   - Factory resetting their device
   - Reinstalling the app
   - As long as they sign in with the same account

4. **Offline Considerations**:
   - If a user reinstalls the app while offline, the local database will be empty until they connect to the internet and sign in
   - Once online and authenticated, all data syncs automatically

### What Users DON'T Need to Know

- Technical details about backup exclusions
- SQLite database internals
- Android backup mechanisms

These implementation details are handled transparently by the app.

## Configuration Files

### backup_rules.xml
Location: `androidApp/src/main/res/xml/backup_rules.xml`

This XML file defines the exclusion rules for Android's backup system.

### AndroidManifest.xml
The manifest references the backup rules:
```xml
<application
    android:allowBackup="true"
    android:fullBackupContent="@xml/backup_rules"
    ...>
```

- `android:allowBackup="true"` - Enables Android's automatic backup for non-excluded data
- `android:fullBackupContent="@xml/backup_rules"` - References the custom backup rules

## Security Best Practices

This configuration follows Android security best practices:

1. ✅ Excludes sensitive user data from backups
2. ✅ Prevents potential data exposure through ADB backups
3. ✅ Maintains data availability through cloud sync
4. ✅ Reduces attack surface
5. ✅ Prevents database corruption from backup conflicts

## References

- [Android Backup Documentation](https://developer.android.com/guide/topics/data/autobackup)
- [Backup Configuration XML Syntax](https://developer.android.com/guide/topics/data/autobackup#xml-syntax)
- [Android Security Best Practices](https://developer.android.com/topic/security/best-practices)

## Maintenance

When updating the backup configuration:

1. Review what new data types need exclusion
2. Update `backup_rules.xml` with new exclusion rules
3. Test backup behavior using the methods above
4. Update this documentation with any changes
5. Consider user impact of any changes to backup behavior
