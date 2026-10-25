# PvP Overhaul (Paper 26.3 beta)

Items: `spear_mace`, `crystal_detonator`, `blast_ward`. Give with `/pvp give <item> [player]`.
Other features: lunge (sprint-hit bonus), F-key swap with a fixed hotbar slot. All tunable in `config.yml`.

## Build
Needs Java 25 and Gradle. If you have no wrapper: `gradle wrapper`, then `./gradlew build`.
Jar lands in `build/libs/`. Put it in `plugins/` on a Paper 26.3 server.

NOTE: this code was written without compiling against the 26.3 API (no network in my sandbox).
Fix any compile errors by checking names against the Paper 26.3 javadocs, and check
the newest `paper-api` build number in `build.gradle.kts`.

## Resource pack
1. Upload `pvp-overhaul-pack.zip` somewhere with a DIRECT download link (GitHub release asset works).
2. Get its SHA-1:  Linux/Mac `sha1sum pvp-overhaul-pack.zip`, Windows `certutil -hashfile pvp-overhaul-pack.zip SHA1`
3. In `plugins/PvpOverhaul/config.yml` set `resource-pack.enabled: true`, `url`, `sha1`, then `/pvp reload`
   (applies on next join). Re-compute the SHA-1 every time you change the pack.
4. pack.mcmeta uses min_format 88 and max_format 999. If the client warns about versions, set min_format
   to the resource pack format for your exact 26.3 build.

## Adding another item
1. Add an id in `ItemRegistry` (+ `create` case, optional recipe).
2. Add `assets/pvp/items/<id>.json`, `models/item/<id>.json`, `textures/item/<id>.png` to the pack.

## GitHub build
Push this folder to a GitHub repo (branch `main`). The workflow in `.github/workflows/build.yml` builds the
jar and zips `resource-pack/`, then publishes both on the repo's Releases page under the tag `latest`.
Pack URL: `https://github.com/<user>/<repo>/releases/download/latest/pvp-overhaul-pack.zip`
The SHA-1 is printed on the workflow run's summary page. Recompute/copy it whenever the pack changes.
