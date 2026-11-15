# Pull request checklist

- [ ] `./scripts/prepare-dependencies.sh` then `./scripts/build-thor.sh` passes (the targeted unit
      tests plus the Thor debug APK; CI runs the same Gradle steps with the same `--tests` filters)
- [ ] Release lint as the signed release runs it: `:app:lintVitalThorRelease`
- [ ] New logic gets a test that CI can reach, or the reason it cannot live in the filtered list is
      stated here (the filter list exists in both `.github/workflows/build-thor.yml` and
      `scripts/build-thor.sh` — keep them the same)
- [ ] Second-screen, dim/wake or controller changes verified on a physical AYN Thor, with the
      checklist row from `docs/validation/thor-device-checklist.md` marked
- [ ] `CHANGELOG-THOR.md` updated, and the flavour `versionName`/`versionCode` in `app/build.gradle`
      only touched as part of a release
- [ ] Upstream files kept separate from this continuation's code (see `docs/upstream-sync.md`); no
      plugin-submodule churn, no production signing key or `/opt/key.properties` material committed
- [ ] Bluejay/FUTO attribution and `LICENSE.md` left intact; see [SECURITY.md](../SECURITY.md)
