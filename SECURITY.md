# Security and maintenance

Twinjay plays video from community source plugins on a handheld with two screens. The plugins are
the security boundary, not the app: they are third-party JavaScript that runs with the app's own
network and storage access. Report a plugin that reaches beyond its source as a security issue, not
as a playback bug.

Use GitHub private vulnerability reporting **if the repository owner has enabled it**. No separate
confidential contact is configured in this repository. If private reporting is unavailable, open a
minimal issue asking for a private contact rather than describing the exploit.

## What this build trusts

- **Source plugins** are vendored under `app/src/*/assets/sources` and can be updated by the app.
  An update keeps the previous version so Diagnostics can roll back a broken source; that rollback
  path is also the path a hostile plugin update would travel, so treat plugin sources as curated.
- **Preview signing.** Release builds read a keystore only from `/opt/key.properties`
  (`app/build.gradle`), and no production key is configured (README). Published previews keep one
  fixed preview certificate so they can update in place; that identity is not a trust anchor and is
  not the upstream signing key.
- **Local media and backups.** Downloads, watch history and the daily settings backup live under the
  app's own files directory, plus `Android/data/<app>/files/backups` for import after a reinstall —
  a location readable through ADB and by file managers on a rooted device. It contains account
  preferences and cookies for the sources you use, nothing encrypted.
- **No Widevine.** DRM-protected sources cannot play (documented in `docs/ponytail-audit.md`);
  playback paths that request them fail rather than bypassing anything.

## Maintenance procedure

- CI runs the Thor unit test filter plus the same release lint that a signed release uses, so a
  release cannot fail on lint afterwards; keep `scripts/build-thor.sh` and
  `.github/workflows/build-thor.yml` on one test list rather than two.
- `minifyEnabled false` is set for release builds: re-check `proguard-rules.pro` before turning R8 on.
- Re-run `docs/validation/thor-device-checklist.md` on a physical AYN Thor after any change to
  second-screen presentation, dim/wake behaviour or controller input, and record the tested build.

This documents the current design, not an audit. No response SLA is promised.
