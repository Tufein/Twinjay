# TwinJay (Thor) changes

## 0.17.0-thor.1 — round four, part two

- New update screen inside the app: release notes, download with a progress bar and install
  through the system installer, without a browser or notification detour.
  The permission to install updates is requested in the same flow.
- Night mode: a warm tint over the app between chosen hours (Settings > Playback).

## 0.16.0-thor.1 — round four, part one

- Step back a few seconds when you resume after a pause of 2 minutes or more (Settings > Playback).
- Skip silence (Settings > Playback).
- Battery saver level is adjustable (default 20%).
- Download size limit: the oldest downloads go first (Settings > Downloads).
- Pause a channel for 7 days (Settings > Subscription groups > Quiet channels).
- Watch Later: "Remove duplicates".
- Diagnostics: "Send bug report" shares the app log with a summary.

## 0.15.0-thor.1 — round three, part four

- Gesture sensitivity (Settings > Playback): relaxed, normal or eager swipes and holds.
- Downloads: only between chosen hours (Settings > Downloads) and optionally shortest video first.
- History: filter by today, this week or this month.
- The subtitle and audio track you pick is remembered per channel.
- Diagnostics: battery graph for the last 24 hours (sampled every 5 minutes while the app is open).
- Backup now includes quiet channels and the per-channel speed, subtitle and audio choices.
- Already present, so not rebuilt: chapter browsing and next-video picking on the second screen, the in-app mini player.

## 0.14.0-thor.1 — round three, part three

- Pick the second-screen preset by network (Settings > Dual screen > Presets):
  Wi-Fi switches to "at home", mobile data to "on the go".
- Big clock on the dimmed second screen, handy as a desk clock.

## 0.13.0-thor.1 — round three, part two

- Swap the two screens (Settings > Dual screen > Screens). The new screen asks
  "keep this?" and moves the app back by itself after 10 seconds if nobody answers.
- SponsorBlock: choose which parts to skip (sponsors, self promotion, reminders, intros, outros).
- Merge playlists into a new one (Settings > Import playlists).
- Diagnostics: how much space downloads use per channel.

## 0.12.0-thor.1 — round three, part one

- Subscriptions feed: chips to narrow to one of your groups.
- Quiet channels (Settings > Subscription groups): stay subscribed but vanish from Home.
- Offline mode (Settings > Playback): no feed loading or background work.
- Second screen controls: quick speed buttons (0.75x to 2x).
- Library lists: "I have 10 / 20 / 40 min" shows what fits, shortest first.
- Diagnostics page (Settings): version, device, sources on/off, share logs, controller button cheat sheet.

## 0.11.0-thor.1 — the wish-list, part two

- Subscription groups (Settings > Subscription groups): make groups of channels
  and tick which channels belong in each.
- Import playlists (Settings): paste playlist links, one per line.
- Picture-in-picture window now has back 10 s, play/pause and forward 10 s buttons.
- Swapping the two screens is not included: it could leave the app stuck on the
  wrong display and cannot be tested safely.

## 0.10.0-thor.1 — the wish-list, part one

- Player options sheet: sleep timer (15, 30, 60 min or end of video), repeat a
  part (A-B) and audio only.
- Playback speed is remembered per channel.
- Search box in every library list (history, watch later, ...).
- Hide videos in the home feed by word or channel (Settings > Playback).
- Presets for the second screen: at home, on the go, gaming (Settings > Dual
  screen), and a faint clock and battery level while it is dimmed.
- Downloads: optional "only while charging" and automatic deletion after N days.
- A playback problem shows a "Share log" button.
- "What's new" dialog after an update; search box in the settings.

## 0.9.0-thor.1 — more control, less waiting

- Controller: new bindable actions "Fullscreen on / off" (default Y) and
  "Stats overlay on / off" (default right-stick click), in Settings > Controller.
- Stats for nerds: resolution, frame rate, codecs, bitrates, buffer and speed
  of the playing video (options sheet, or the controller button).
- Quality: "Auto" now shows the resolution that is really playing
  (for example "Auto · 1080p").
- SponsorBlock segments show as orange bars on the timeline.
- Battery saver (Settings > Playback): below 20% and not charging the second
  screen dims after 5 s and video plays at 720p at most.
- Auto-download (Settings > Playback, off by default): during the background
  subscription update (Wi-Fi, charging or at least 50% battery) the newest
  videos (max 5) of channels with notifications on are downloaded.
- Import subscriptions (Settings): pick the subscriptions.csv from Google
  Takeout, or a list of channel links, and subscribe to all of them.
- Faster start: the last home feed is shown immediately while the sources
  load, and stays visible offline.

## 0.8.1-thor.1 — nicer updates, setup and sign-in; adjustable dimming

- Updates: TwinJay now checks GitHub once at start and shows a dialog with the
  new version, its size and the release notes. "Download" fetches the APK with
  the system download manager (tap the finished notification to install);
  "Later" skips that version until the next one. Settings > About > "Check for
  updates" shows the same dialog.
- Setup wizard: a proper welcome page (what TwinJay does), a "Step x of 3"
  label, a loading indicator while the built-in sources load, and a final page
  that points to the most useful settings.
- Plugin sign-in: the sign-in screen shows the site you are on, a load
  progress bar, reload, back-within-the-page, a note that it closes by itself,
  and a "Signed in" toast. A source's page shows whether you are signed in and
  has a "Sign out" button.
- Look and feel, adjustable yourself: Settings > Appearance has ready-made
  themes (TwinJay, Sunset, Forest, Grape, Ruby, Mono; one tap, editable later),
  "Pure black" for dark mode and "Text size" (90 to 130%). Settings > Dual screen
  > "Look" lets the second screen follow the main screen or stay dark / pure
  black. Text size applies to both screens.
- Second screen dimming: Settings > Dual screen now has "Dim after" (10 s to
  5 min) and "How far to dim" (black, 5%, 15%, 35%). Waking the screen always
  restores normal brightness, also when the brightness was captured while
  already dimmed.

## 0.8.0-thor.3 — fix the missing YouTube plugin

- The built-in YouTube plugin needs the Browser package, which was refused
  ("Browser is only allowed for debug and official plugins") because the
  bundled config carries no author signature. A plugin whose script is
  byte-identical to the copy shipped inside the APK is now trusted.

## 0.8.0-thor.2 — find out why YouTube is missing after a clean install

- A built-in plugin that fails to install or load is no longer swallowed: the
  reason is logged and the setup wizard retries the install once and shows the
  reason if the plugin is still missing.
- Settings > About > "Share app log": saves the app's recent log to Downloads
  and opens the share sheet.

## 0.8.0-thor.1 — first TwinJay release

- App renamed to TwinJay with a new launcher icon; most user-visible "Bluejay"
  texts now say TwinJay (the original Bluejay is still credited in Settings > About).
- Update check: Settings > About > "Check for updates" looks at this repository's
  latest GitHub release and opens the APK download. Thor builds never contact
  the upstream Bluejay update feed.
- Settings > About > "Share playback log" sends the newest stall log
  (Download/twinjay-playback-log-*.txt) through the share sheet.
- SponsorBlock (Settings > Playback, on by default on Thor): skips sponsor,
  self-promo, interaction, intro and outro segments of YouTube videos and shows
  a short "Skipped ..." toast. Only a 4-character hash prefix of the video id is
  sent to sponsor.ajay.app.
- Second screen: new "Dim second screen while playing" (Settings > Dual screen,
  on by default on Thor). After 20 s without touch while a video plays the
  second screen turns black and drops its brightness; a touch wakes it.
- Per-step video start timings (resolve, first frame) are logged and added to
  the stall log, so slow starts can be pinned on one step.
- Default download quality is now 1080p for new installs (existing choices are kept).
- CI: every pull request builds the Thor APK and runs the unit tests; a manually
  started workflow builds and publishes the signed release APK.

### Rebuffer loop fix (also in this release)

- Second root cause, from a 1080p Thor log (stall at ~9.5 s with 68 ms buffered
  on an 89 s video): YouTube range sources were played as one open-ended
  progressive GET, which googlevideo throttles to about real time after a short
  burst. They are now played as chunked DASH (explicit byte-range requests, the
  existing `VideoHelper.convertItagSourceToChunkedDashSource`), with a fallback
  to progressive. Also fixed `IAudioSource.getHttpDataSourceFactory()`, which
  called itself endlessly.
- The log also shows ~8.5 s spent resolving the video before playback starts
  (BotGuard/PO token, cipher solve) inside the YouTube plugin; that part is not
  changed here.
- Added a background warm-up: shortly after app start the YouTube plugin resolves
  one tiny public video (jNQXAC9IVRw) so visitor data, cipher and PO token are
  ready when the user presses play. Costs a few requests per app start.
- Prefetch: the next queued video and the top 2 recommendations are resolved in
  the background while the current video plays, so tapping them skips the
  plugin round trip. The video-details cache now lives 30 minutes (was 5) so
  prefetched results are still there when tapped.
- Root cause found from a Thor playback log: for non-SABR (unmuxed progressive)
  streams, "Auto" quality loaded the first source the plugin listed, i.e. 2160p
  VP9, which a normal connection cannot sustain (playback stalled with ~50 ms
  buffered). Auto now picks the highest source up to 1080p whose bitrate fits
  within 75% of the bandwidth estimate.
- Playback stalls longer than 10 s now save a diagnostic log to Downloads.
- SABR throughput estimation ignored every response slower than 2x real time,
  so on a connection slower than the stream the estimate never left its seed
  and the server kept choosing bitrates the network could not sustain. Slow
  responses are now counted when the buffer is nearly empty (clearly
  network-limited).
- Active player: resume after a stall only once 5 s is buffered (was 1.5 s),
  and start at 1.5 s, so playback no longer flips between a few seconds of
  video and another stall.

Not built or tested here (dependencies unavailable in the cloud container) and
not verified on a physical Thor.

## 0.7.3-thor.1 — YouTube startup and buffering

- Resolve the playable source before starting comments, recommendations and
  chapters, so optional plugin work cannot take the shared engine client first.
- Seed YouTube SABR with Media3's network-aware bandwidth estimate (converted
  from bits/s to bytes/s), replacing the fixed 104,857 bytes/s fallback on this
  playback route. Subsequent SABR transfer measurements remain in control.
- Configure the active Compose player with a 750 ms startup threshold and
  1,500 ms rebuffer threshold, while continuing to load 30–60 seconds ahead
  when the source permits. The old legacy player configuration did not apply
  to this player. These thresholds are buffer targets, not measured load times.
- Add startup regressions proving optional work follows source resolution and
  superseded resolutions do not issue optional requests.

Validation: Thor ARM64/x86_64 APKs and the Android test APK build successfully.
All 93 targeted JVM tests pass with zero failures or skips. ARM64 signature and
16 KiB ZIP alignment pass; the preview signing identity is unchanged.

Actual YouTube startup time and sustained playback on a physical Thor still
need verification; cloud tests cannot reproduce the device/CDN path.

## 0.7.2-thor.1 — playback and controller reliability

- Safe relative seeking when a stream's duration is unknown, including remote
  playback without a local decoder. Sub-500 ms clips no longer produce invalid
  seek ranges, and large offsets cannot wrap to a negative position.
- Remapping a controller action replaces its original key; explicit assignments
  take precedence over another action's default without reviving removed actions.
- Speed-up/slow-down holds combine deterministically across buttons and devices,
  restore the original rate in either release order, and honor explicit speed
  selections and controller disablement.
- Disconnecting a controller releases only that device's held keys and axes.
  Another connected controller retains its active input state.
- Cancel stale comment-page requests when switching/closing videos, reject late
  results and collapse duplicate scroll requests into one in-flight page load.
- Start live chat once per video instead of reconnecting on volume, speed or
  other unrelated player-state updates.

### Validation

- `scripts/build-thor.sh --console=plain` completed successfully.
- **91 JVM tests passed, zero failures or skips:** 47 application tests and
  44 playback-control/gesture tests, including 24 new regression tests.
- **16 Android instrumentation tests passed, zero failures or skips**, on an
  Android 9 / API 28 x86_64 emulator, including three new tests for independent
  controller disconnection and held-input cleanup.
- Thor debug APKs built for arm64-v8a and x86_64, version `0.7.2-thor.1` / `7002`.
- ARM64 APK signature and 16 KiB ZIP alignment verified; the signing certificate
  matches `0.7.1-thor.1` for in-place preview updates.

Physical Thor and online-provider/casting playback still require
hardware/service validation.

## 0.7.1-thor.1 — development preview

Based on the complete Bluejay 0.7.0 source, commit
`6a762681cf489aee20f744ae357e247c92ae4b5b`.

- Add the Thor application flavor with its own package ID and app label.
- Enable the second-screen dashboard and controller input by default on fresh
  Thor installs; preserve saved preferences and custom bindings.
- Default both Thor seek directions to ten seconds.
- Observe display changes while the main activity is in the foreground. Retain
  an eligible output, replace stale outputs and recover from window/display races.
- Exclude the activity's own display, private outputs and sleeping or invalid
  displays from companion selection. Reevaluate configuration and PiP transitions.
- Cancel lifecycle retries and dismiss the companion when leaving the foreground.
- Forward gamepad input from the companion window and dispose its composition
  when dismissed, retaining normal touch and mouse behavior.
- Release held digital/analog inputs when the application stops while preserving
  binding-popup ownership; prevent pointer/touchpad coordinates from being interpreted as sticks.
- Skip upstream launch telemetry in Thor builds.
- Resolve Material You colors only on Android 12/API 31 or newer; older devices
  use the built-in light/dark palettes so filled controls retain readable contrast.
- Keep successor app updates separate from upstream APK delivery. Preserve source
  plugin updates and original project attribution.

### Validation

- `scripts/build-thor.sh --console=plain` completed successfully, including a
  repeated build using the same dependency setup.
- **67 JVM tests passed, zero failures or skips:** 38 application policy,
  settings and update tests, plus 29 gesture tests.
- **13 Android instrumentation tests passed, zero failures or skips**, on an
  Android 9 / API 28 x86_64 software emulator. These cover companion key/motion
  dispatch, pointer fallthrough, held-input release, direction reversal and
  light/dark theme contrast on pre-Material-You Android.
- Normal app startup and the Bluejay welcome screen were checked on API 28;
  the fallback palette renders readable blue controls. See the
  [startup screenshot](docs/validation/bluejay-thor-api28-welcome.png).
- Thor debug APKs built for arm64-v8a and x86_64, version `0.7.1-thor.1` / `7001`,
  application ID `com.tufein.bluejay.thor`. The Android test APK also built.
- arm64 APK signature and 16 KiB ZIP alignment verified with build tools 37.0.0.
- Dependency preparation succeeded repeatedly with both native archives matching
  their pinned SHA-256 hashes; dependency source checkouts remain unmodified.

One pre-existing gesture test still expected the old normal bottom-center
`NONE` default. It now follows upstream's intentional `MORPH_VERTICAL` change
from commit `ebab6e1fc98d4039dd59082aa1c2069439554dd3`, with an additional check
that explicit user-selected `NONE` continues to work. Production gesture
behavior is unchanged.

Physical AYN Thor display hotplug, long playback sessions, online sources,
casting and production release signing remain outside this validation.
Publication status is separate from this validation record.
