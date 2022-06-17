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
