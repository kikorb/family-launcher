# Family Launcher

A family dashboard that replaces the Google TV home screen on a Chromecast with Google TV (or any Android TV). It shows the clock, weather, a rolling 7-day calendar strip, favourite apps, and your family's shared Notion lists, all driven from the remote.

- **Home**: clock, weather, today + 6 days with events and birthdays, a dock with up to 5 favourite apps.
- **Up**: month calendar. Press OK on a day to add an event by voice or keyboard, or edit existing ones.
- **Right**: family tasks and shopping list, synced with Notion. Add by voice (🎤 Dictar) or keyboard (⌨ Añadir), pick *Quién* / *Tipo* from your Notion options, and edit existing items (✎ Editar).
- **Down**: all apps. The ★ Favoritas button picks which 5 appear on the home dock.
- **Left**: light/dark mode and wallpapers.

Spanish UI. Voice input uses the TV's built-in Google speech recognition (es-ES).

## Notion setup

Create a Notion integration, share three databases with it, and export its token before building. **The token is never stored in this repo.**

| Database | Properties used |
| --- | --- |
| Tasks | `Qué` (title), `Quién` (select), `Status` (status: Not started / In progress / Done) |
| Shopping | `Name` (title), `Type` (select), `Purchased` (checkbox) |
| Calendar | `Name` (title), `Date` (date), `Type` (select) |

The database IDs are constants at the top of `app/src/main/java/com/familyboard/tv/data/NotionClient.kt`. Replace them with yours. Select options are read from the databases at runtime and cached, so adding a person or a type in Notion makes it available on the TV.

## Build

Requires JDK 17 and the Android SDK.

```bash
export PERSONAL_NOTION=ntn_your_integration_token
./gradlew assembleRelease
```

The build fails with a clear message if `PERSONAL_NOTION` is not set. The token is compiled into the APK (`BuildConfig`), so do not publish built APKs.

The release build is signed with your local `~/.android/debug.keystore`, which is fine for sideloading. Use the release build, not debug: a debuggable build runs several times slower on a Chromecast.

## Install and make it the home screen

Enable *Developer options → Wireless debugging* on the TV, pair once (`adb pair ip:port`), then:

```bash
export TV_ADB_TARGET=<tv-ip>:<port>
scripts/tv-home.sh install   # installs the release APK and AOT-compiles it on the TV
scripts/tv-home.sh enable    # makes it the home app
scripts/tv-home.sh status
```

Android picks the highest-priority HOME activity before honouring the default-app setting, and Google's launcher outranks third-party apps. `enable` therefore disables `com.google.android.apps.tv.launcherx` for user 0. If the setup wizard's recovery screen then takes over Home, also run `adb shell pm disable-user --user 0 com.google.android.tungsten.setupwraith`.

**Recovery** if the TV ends up on a blank screen (needs wireless debugging on):

```bash
scripts/tv-home.sh recover
adb shell pm enable com.google.android.tungsten.setupwraith
```

Wireless debugging may turn itself off after a reboot. The launcher has an *Ajustes de TV* shortcut in its settings drawer to get back to system settings.

## Wallpapers

The Fedora 37, 42 and 43 default wallpapers are bundled (Fedora Project artwork, CC BY-SA 4.0). Other wallpapers are listed in `ALL_WALLPAPERS` (`data/Models.kt`) but their image files are not in the repo. Drop your own files into `app/src/main/assets/` and add entries to that list. Any entry whose file is missing is hidden at runtime.

## Notes

- The weather location is hardcoded in `WeatherClient.kt` (Open-Meteo, no key needed).
- Back on the home screen is intentionally swallowed so the launcher never drops to another app.
