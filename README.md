# Excavated Variants

[![CodeFactor](https://www.codefactor.io/repository/github/lukebemish/excavatedvariants/badge?style=for-the-badge)](https://www.codefactor.io/repository/github/lukebemish/excavatedvariants)
[![Latest Version](https://img.shields.io/modrinth/v/excavated_variants?label=latest&style=for-the-badge)](https://modrinth.com/mod/excavated_variants)

Do you want to contribute to the default variant configs? Feel free to open a pull request. Default variant config files can be placed under `Common/src/main/resources/defaultresources/excavated_variants/excavated_variants/variants/`.


## Config Format

The main config file, `config/excavated_variants.json`, contains several options:

* **attempt\_worldgen\_replacement**: Toggles ore-gen changes; without this, ores won't be replaced during world gen.
* **add\_conversion\_recipes**: Toggles whether to add recipes to convert variants back to the base ore.
* **jei\_rei\_compat**: Toggles compatibility with JEI and REI for added conversion recipes.

All other configs are loaded through the globalresources folder. You may add new config files there directly, but if you want to extract the built-in 
configs in order to edit them, open the `defaultresources.json` config file. It should look something like as follows:
```json
{
  "extract": {
    "excavated_variants": "unextracted"
  }
}
```
Change `unextracted` to `extract` and load the game once. Now, the default configs will be available to edit in the `globalresources` folder.

**Note**: If you just want to add more configs, you do not need to (and should not) do this. You can add new config files without extracting existing ones.

Configs relating to how ores are registered are added in `globalresources/[folder]/excavated_variants/[namespace]/configs`. They can contain the following 
options:

* **blacklist**: A way of excluding certain variants from being created. It takes a list of filters. See below for the format.
* **priority**: Allows certain config files to be loaded before others, in the order specified within. These should be a namespaced location of the variant config file.

Configs for adding stones or ores are placed in `globalresources/[folder]/excavated_variants/[namespace]/variants`. These take the following format:

* **mod\_id**: The ID of the mod necessary for this config to load, or a list of IDs for the required mods.
* **provided_stones**: A list of stones provided by the config.
* **provided_ores**: A list of ores provided by the config.

Each ore object takes the following format:

* **id**: A name for this ore. Can be shared to avoid duplicate ores.
* **stone**: A list of stones, by id, that this ore already appears in.
* **ore\_name**: A list of other names for this ore, to be used when adding the ore to tags. Optional; if not supplied, defaults to **id**.
* **block\_id**: A list of blocks of this ore, in the same order as **stone**.
* **lang**: A map of locale keys to the name to be given to this ore in-game.
* **types**: A list of types that this ore is classified as. Matched at runtime with the types specified in stone configs.

Each stone object takes the following format:

* **id**: A name for this ore. Can be shared to avoid duplicate ores.
* **block\_id**: The block ID of this stone.
* **lang**: A map of locale keys to the name to be given to this stone in-game.
* **types**: A list of types that this stone is classified as. Matched at runtime with the types specified in ore configs.

Modifier configs are added in `globalresources/[folder]/excavated_variants/[namespace]/modifiers`. They allow you to configure the properties of created variants, and can contain the following options:

* **filter**: A filter for selecting variants to modify. See below for the format.
* **properties** (optional): Modifies block properties. Takes the following arguments:
  * **destroy_time** (optional): The time to destroy the block.
  * **explosion_resistance** (optional): The time to destroy the block.
  * **xp** (optional): The experience dropped by the block. Can take the same sort of integer range as seen in vanilla datapacks.
* **tags** (optional): A list of tags to add the filtered ores to. Can be either block or item tags, in the format `"namespace:<blocks/items>/path"`.
* **flags** (optional): A list of flags to apply to the variants. The following flags are recognized:
  * `"original_always"`: Variant always drops the original ore instead of the variant block.
  * `"original_without_silk""`: Variant drops the original ore instead of the variant block, but only if mined without silk touch.

Filters take the form of a string specifying the filter, such as `"ore:iron_ore"`. Can take the following forms:

* `"type:type_id"` matches every variant with an ore and stone of type `type_id`. Example: `type:nether`.  
* `stone:stone_id` matches every variant of stone type `stone_id`. Example: `stone:andesite`.
* `ore:ore_id` matches every variant of ore `ore_id`. Example: `ore:iron_ore`.
* `variant_name` matches a specific variant. Example: `andesite_iron_ore`.
* `!inverted_filter` matches everything except what `inverted_filter` matches. Example: `!andesite_iron_ore`.
* `*` matches everything.
* `~` matches nothing.

Alternatively, filters can be an object combining other filters, of the following form:
```json5
{
  type: "<type>",
  args...
}
```
The available types are:

* `all` matches everything.
* `empty` matches nothing.
* `not` matches everything not matched by its `"filter"` argument.
* `and` matches everything matched by all members of its `"filters"` argument.
* `or` matches everything matched by at least one member of its `"filters"` argument.

