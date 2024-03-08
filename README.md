# Excavated Variants

[![CodeFactor](https://www.codefactor.io/repository/github/lukebemishprojects/excavatedvariants/badge?style=for-the-badge)](https://www.codefactor.io/repository/github/lukebemishprojects/excavatedvariants)
[![Latest Version](https://img.shields.io/modrinth/v/excavated_variants?label=latest&style=for-the-badge)](https://modrinth.com/mod/excavated_variants)

Do you want to contribute to the default variant configs? Feel free to open a pull request. Default config files can be placed under `common/src/main/resources/defaultresources/globaldata/excavated_variants/excavated_variants/`.

## Config Format

The main config file, `config/excavated_variants.json`, contains several options:

* **attempt\_worldgen\_replacement**: Toggles ore-gen changes; without this, ores won't be replaced during world gen.
* **add\_conversion\_recipes**: Toggles whether to add recipes to convert variants back to the base ore.

All other configuration is loaded through the `globalresources` folder. Generally speaking, you should make your own subdirectory or archive in this folder instead of modifying the built in file.

**Note**: If you just want to add more configs, you do not need to (and should not) do this. You can add new config files without extracting existing ones.

Configs define four main data types:

* **Ground Types**: Ground types represent classes of ores and stones. They are used to determine which new ore/stone combinations (variants) should be created. For example: `excavated_variants:overworld` is a category shared by normal minecraft stone, deepslate, and overworld ores. 
* **Ores**: Ores are types of blocks which can exist in any number of different stones. During worldgen, they are replaced with variants matching neighboring stones. For example: `excavated_variants:minecraft/iron_ore` represents both normal and deepslate iron ore, as well as any other variants generated.
* **Stones**: Stones are blocks which ores generate within. For example: `excavated_variants:minecraft/granite` represents granite, and contains information about which new variants to generate based off of granite.
* **Modifiers**: Modifiers are used to modify the properties of variants. They can be used to add tags, change block properties, prevent variant creation, and more.

Except when changed by modifiers, variants are generated for all ore/stone combinations that share a ground type and do not already have a variant.

### Ground Types

Placed in `globalresources/[folder/pack]/globaldata/[namespace]/excavated_variants/ground_type`. They are JSON files with the following structure:

* (nothing): these don't actually hold any data yet! Just use an empty JSON file: `{}`.

### Ores

Placed in `globalresources/[folder/pack]/globaldata/[namespace]/excavated_variants/ore`. They are JSON files with the following structure:

* `types`: a list of ground types that this ore can generate in. For example: `["excavated_variants:overworld"]`.
* `translations`: an object with locales as keys (such as `en_us`) and translated names of the ore as values. Translations will be prepended by the stone name, unless the translation contains `%s`, in which case the stone name will be inserted at that location.
* `tags`: a list of tags that variants of this ore will be added to, as both block and item tags.
* `blocks`: a map representing pairings of variants of this ore with stones. Each key is the identifier of a block representing an ore variant. Values take one of two forms:
  * a string representing the identifier of a stone. For example: `"excavated_variants:minecraft/granite"`. This variant will be assumed to exist if the mod who's mod ID is the namespace of the block ID is present.
  * an object with the following fields:
    * `stone`: a string representing the identifier of a stone. For example: `"excavated_variants:minecraft/granite"`.
    * `required_mods`: a list of mod IDs that must be present for this variant to be generated. For example: `["spelunkery", "create"]`.
    * `generating`: (optional, defaults to true) whether this variant can be used as a parent for new, generated variants.

### Stones

Placed in `globalresources/[folder/pack]/globaldata/[namespace]/excavated_variants/stone`. They are JSON files with the following structure:

* `types`: a list of ground types that this stone can generate in. For example: `["excavated_variants:overworld"]`.
* `translations`: an object with locales as keys (such as `en_us`) and translated names of the stone as values.
* `block`: the identifier of the block corresponding to this stone. For example: `"minecraft:granite"`.
* `ore_tags`: a list of tags that ore variants for this stone will be added to, as both block and item tags.

### Modifiers

Placed in `globalresources/[folder/pack]/globaldata/[namespace]/excavated_variants/modifier`. They are JSON files with the following structure:

* `tags`: (optional) a list of tags that variants matching the filter will be added to. Can be either block or item tags, in the format `"namespace:[blocks/items]/path"`.
* `flags`: (optional) a list of flags from the following set that will be applied to matching variants:
  * `"original_without_silk"`: the variant should drop whatever its original block would drop, unless silk touch is used. Note that this flag is irrelevant for any ore blocks that have the behaviour of dropping "raw ore chunks" or the like, and is only relevant for when the original block drops itself.
  * `"original_always"`: the variant should always drop whatever its original block would drop, even if silk touch is used.
  * `"disable"`: the variant should not be generated or recognized by the mod.
  * `"non_generating"`: the variant should not be used as a parent for new variants, but the mod is still aware of it.
* `properties`: (optional) an object with the following fields, that modifies the properties of generated blocks:
  * `destroy_time`: the time it takes to break the block.
  * `explosion_resistance`: the resistance of the block to explosions.
  * `xp`: the amount of experience dropped by the block. Can take the same sort of integer range as seen in vanilla datapacks.
* `filter`: describes which variants this modifier applies to.

#### Filters

Modifier filters can take the form of either a string or an object. As an object, they have at least one field, `type`,
which describes the type of filter. The available types are:

* `all` matches everything.
* `empty` matches nothing.
* `not` matches everything not matched by its `"filter"` field.
* `and` matches everything matched by all members of its `"filters"` field.
* `or` matches everything matched by at least one member of its `"filters"` field.

As a string, they take one of the following forms:

* `*`: matches everything.
* `~`: matches nothing.
* `~[filter]`: matches everything not matched by `[filter]`.
* `ground_type@[namespace]:[path]`: matches all variants with the given ground type.
* `stone@[namespace]:[path]`: matches all variants with the given stone.
* `ore@[namespace]:[path]`: matches all variants with the given ore.
* `mod@[mod_id]`: matches everything, but only if a mod with the given ID is present.
* `generated`: matches all variants that are generated by the mod.
* `block@[namespace]:[path]`: matches all variants with the given block ID. The namespace and path provided here can use `*` as a wildcard to match one or more characters.
