# Custom F-Droid repository

BattDeck can be distributed from a custom F-Droid repository hosted by GitHub Pages at:

```text
https://zarant77.github.io/batt-deck/fdroid/repo/
```

## 1. Install fdroidserver on macOS

Install Homebrew if it is not already available, then install `fdroidserver`:

```bash
brew install fdroidserver
fdroid --version
```

Java 17, Android SDK tools, and the Android build dependencies described in the main README must also be available.

## 2. Initialize repository signing locally

F-Droid repository indexes must be signed. Generate the repository keystore locally and never commit it or its passwords:

```bash
cd fdroid
fdroid init
cd ..
```

`fdroid init` may update `fdroid/config.yml`. Preserve the public values already present in that file:

```yaml
repo_url: https://zarant77.github.io/batt-deck/fdroid/repo
repo_name: BattDeck F-Droid Repository
repo_description: Official custom F-Droid repository for BattDeck.
archive_older: 0
```

Keep all generated keystores, passwords, local environment files, and backups outside Git. Back up the repository signing key securely: clients trust this key, so losing it prevents seamless repository updates.

Because `config.yml` is a tracked public template, any password fields written there by `fdroid init` are local-only changes. Never stage them. Before committing, remove the password fields or restore the public template while retaining the generated public files under `fdroid/repo/`.

## 3. Configure BattDeck APK signing

The APK itself must also be signed consistently. By default, Gradle and the runner read `/Users/zar/Dropbox/Keys/catemup-keystore.properties`. Create this external file with the local BattDeck upload key details:

```properties
storeFile=/Users/zar/Dropbox/Keys/catemup-upload.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD
```

For another machine, set `BATTDECK_KEYSTORE_PROPERTIES=/path/to/catemup-keystore.properties`. Neither the external properties file nor the upload keystore may be committed.

## 4. Add BattDeck and update the repository

The helper performs the release build, copies the signed APK to `fdroid/repo/`, and regenerates F-Droid indexes:

```bash
./tools/build_fdroid_repo.sh
```

Equivalent manual commands for version `0.3.0` (`versionCode 3`) are:

```bash
./gradlew assembleRelease
cp app/build/outputs/apk/release/app-release.apk fdroid/repo/com.catemup.battdeck_3.apk
cd fdroid
fdroid update --create-metadata
```

Before publishing, verify that `fdroid/repo/` contains the APK, signed index files, icons, and generated metadata. Commit only the public repository output. Never commit private keys or passwords.

## 5. Publish with GitHub Pages

In GitHub open **Settings → Pages** and select **Deploy from a branch**:

1. Branch: `main`.
2. Folder: `/ (root)`.
3. Save and wait for the Pages deployment.

Serving from the branch root is preferred because this repository already stores the public F-Droid output under `fdroid/repo/`. If Pages is instead configured for `/docs`, copy the complete generated `fdroid/` directory under `docs/` and adjust `repo_url`; do not publish two competing locations.

After deployment, confirm that this URL is reachable:

```text
https://zarant77.github.io/batt-deck/fdroid/repo/index-v2.json
```

## 6. Add the repository in an F-Droid client

On the Android device:

1. Open F-Droid.
2. Open **Settings → Repositories**.
3. Choose **Add repository**.
4. Enter `https://zarant77.github.io/batt-deck/fdroid/repo/`.
5. Confirm the repository fingerprint through a trusted channel when prompted.
6. Refresh repositories, search for **BattDeck**, and install it.

The repository URL alone does not authenticate the source. Publish the signing-key fingerprint in GitHub Releases or another trusted channel so users can verify it.
