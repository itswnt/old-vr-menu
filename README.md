# Old Menu — your own app
## One-time: make YOUR private signing key (this is what makes the app yours)
On your computer (needs Java):
  keytool -genkeypair -v -keystore my.jks -alias mykey -keyalg RSA -keysize 2048 -validity 10000
Pick a strong password. Keep my.jks backed up and NEVER upload it to the repo or share it.
Then: base64 -w0 my.jks   (Mac: base64 -i my.jks)
In your PRIVATE GitHub repo: Settings > Secrets and variables > Actions > New secret:
  KEYSTORE_B64 = that long text,   KS_PASS = your password
Only you can now produce updates that install over your app. Anyone else's build will be rejected by the headset.

## Build (gives you the plain .apk file, no zip)
Upload this folder to the private repo (keep .github). Actions > Build APK > Run workflow. When it finishes, open the repo "Releases" (right side) and download OldMenu.apk.
## Install
adb install -r app-release.apk   (or SideQuest). Then open Old Menu > button 1 > enable accessibility > flip the switch.
## Remove
Uninstall in library, or: adb uninstall com.oldmenuapp.quest
## Make it yours
Change the app name in AndroidManifest.xml (android:label) and the id in app/build.gradle (applicationId/namespace).
