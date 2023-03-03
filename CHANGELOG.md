## Excavated Variants 2.2.0

### Features
- Allow translations

## Excavated Variants 2.1.0

### Features
- Add Gems & Jewels to default config for 1.19.3 (#109)
### Fixes
- Fix #106

## Excavated Variants 2.0.1

### Fixes
- Update defaultresources

## Excavated Variants 2.0.0

### Features
- Add system to add ores to arbitrary tiers.
- Add Galosphere and Darker Depths support (#104)
- Add default support for Sullys Mod Jade Ore (#105)
### Fixes
- Fix priority settings not working
- Fix lapis ore in `minecraft` config
- Fix betternether config
- Allow new tier system to work on quilt
- Minor fix to new tier system
### Other
- Delete ae2.json5
- Update ExcavatedVariants.java
- Remove CodecUtils, change build system, and update defaultresources to utilize new features.
- Fix which jar is published and add system to override version
- Switch to new API for adding item groups with Quilt
- **BREAKING** Change group name
- Change how features are added
- Update to DynAssetGen 3.0.0

### Excavated Variants v1.0.0

- Refactor modifier/filter system slightly
- Add modifier-controlled tags
- Add modifier-controlled flags for dropping the original ore
- Add type-based filters
- Refactor code for consistent spacing, etc.
- Switch default tags to a modifier
- Fix potential registry weirdness

### Excavated Variants v0.8.0

- Internal changes to use codecutils.
- Internal changes for new DynAssetGen version.
- Changes to model merging for Forge composite models.
- Add stones/ores to tags by type.
- Change main config to toml (using codecutils).
- Extracted global resources now in a `.zip` with new defaultresources version.

### Excavated Variants v0.7.2

- Extend config/resource system for modifiers
- Fix various quirks
- Add several default configs
- Change filter system - this may cause configs to break!

### Excavated Variants v0.7.0

- Switch system for loading variant configs to use defaultresources instead of configs

### Excavated Variants v0.6.3

- Update for 1.19
- Add more default mod compat
- End fabric support (use Quilt instead!)

### Excavated Variants v0.6.2

- Fix more bugs related to loading order by making services load lazily.

### Excavated Variants v0.6.1

- Fix breaking crash on Forge due to version issues.

### Excavated Variants v0.6.0

- Switch to MultiLoader-Template from Architectury, allowing for Quilt support.
- Major internal refactoring.
- Update to post-refactor DynAssetGen versions.
- Fix game-load lag due to terrible lookup design.

### Excavated Variants v0.5.5

- Increase performance during worldgen (the way I do this is *really* hacky, so please report any issues this causes.)

### Excavated Variants v0.5.4

- Fix breaking crash on servers.

### Excavated Variants v0.5.3

- Fix bug causing creative inventory crashes.

### Excavated Variants v0.5.2

- More bugfixes, more config changes. Should fix some weird compat stuff. Now uses DynAssetGen 0.5.1+.

### Excavated Variants v0.5.1

- Bugfixes, major internal changes, more compat. Still an alpha version.

### Excavated Variants v0.5.0

- Copy blockstate properties from parent blocks; change how textures/models are generated. Definitely an alpha version.

### Excavated Variants v0.4.1

- Fix yet another tag-related bug (needs Dynamic Asset Generator 0.4.3)

### Excavated Variants v0.4.0

- Fix bug with new tag stuff.

### Excavated Variants v0.3.2

- 1.18.2 compat (*cannot run on 1.18.1*)

### Excavated Variants v0.3.1

- Better *Unearthed* compat using API
- Update dependency version.

### Excavated Variants v0.3.0

- Release version; fixed compat by resetting caches on world load.

### Excavated Variants v0.2.5

- Fix dependency version

### Excavated Variants v0.2.4

- Actually fixed the stuff from v0.2.4.
- Automatic ore-conversion recipes added.

### Excavated Variants v0.2.3

- Alpha release with world-gen improvements
- Hopefully makes ore replacement no longer cause crippling lag

### Excavated Variants v0.2.2

- Added compat for several mods.
- Fixed rare ConcurrentModificationException during world-gen.

### Excavated Variants v0.2.1

- Fix forge issue causing vanilla creative tabs to fail to load. Not sure what was up with Forge here, I figure it was some sort of DeferredRegsitry weirdness...

### Excavated Variants v0.2.0

- More default compatibility.
- Uses new library features for textures.
- Better world-gen injection (can be disabled for performance).

### Excavated Variants v0.1.0

- Initial version.
