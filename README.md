# Excavated Variants

Do you want to contribute to the default variant configs? Feel free to open a pull request. Default variant config files can be placed under `common/src/main/resources/defaultresources/excavated_variants/excavated_variants/variants/`.


## Config Format

The main config file, `config/excavated_variants.json`, contains several options:

* **attempt\_worldgen\_replacement**: Toggles ore-gen changes; without this, ores won't be replaced during world gen.
* **add\_conversion\_recipes**: Toggles whether to add recipes to convert variants back to the base ore.
* **jei\_rei\_compat**: Toggles compatibility with JEI and REI for added conversion recipes.
* **unobtainable\_variants**: If this is set to true, variants will drop the base ore, even with silk touch.

Configs relating to how ores are registered are added in `globalresources/[folder]/excavated_variants/[namespace]/configs`. They can contain the following options:

* **blacklist**: A way of excluding certain variants from being created. It takes a list of filter strings, in the following format:
    * `stone:stone_id` filters every variant of stone type `stone_id`. Example: `stone:andesite`.
    * `ore:ore_id` filters every variant of ore `ore_id`. Example: `ore:iron_ore`.
    * `variant_name` filters a specific variant. Example: `andesite_iron_ore`.
    * `~variant_name` excludes a variant from other filters. Example: `~andesite_iron_ore`.
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
* **en\_name**: The name to be given to this ore in-game.
* **types**: A list of types that this ore is classified as. Matched at runtime with the types specified in stone configs.
* **texture\_count**: The maximum number of texture variants of this ore.

Each stone object takes the following format:

* **id**: A name for this ore. Can be shared to avoid duplicate ores.
* **block\_id**: The block ID of this stone.
* **en\_name**: The name to be given to this stone in-game.
* **types**: A list of types that this stone is classified as. Matched at runtime with the types specified in ore configs.
* **texture\_count**: The maximum number of texture variants of this stone.

Modifier configs are added in `globalresources/[folder]/excavated_variants/[namespace]/modifiers`. They allow you to configure the properties of created variants, and can contain the following options:

* **filter**: A filter for selecting variants to modify. Takes the same format as config blacklist filters.
* **destroy_time** (optional): The time to destroy the block.
* **explosion_resistance** (optional): The time to destroy the block.
* **xp** (optional): The experience dropped by the block. Can take the same sort of integer range as seen in vanilla datapacks.
