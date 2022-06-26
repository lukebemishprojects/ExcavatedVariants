# Excavated Variants

Do you want to contribute to the default variant configs? Feel free to open a pull request. Default variant config files can be placed under `common/src/main/resources/defaultresources/excavated_variants/excavated_variants/variants/`.


## Config Format

The main config file, `config/excavated_variants.json`, contains several options:

* **attempt\_ore\_gen_insertion**: Allows the world-generation changes to be toggled. Useful if another tool, such as KubeJS, is being used for ore gen.
* **attempt\_worldgen\_replacement**: Toggles the much slower, but more reliable, ore-gen changes. Disable to speed up world generation substantially at the cost of less-reliable replacement of the original ore with its variants. If this is disabled, some ores will not be replaced correctly.
* **add\_conversion\_recipes**: Toggles whether to add recipes to convert variants back to the base ore.
* **jei\_rei\_compat**: Toggles compatibility with JEI and REI for added conversion recipes.
* **unobtainable\_variants**: If this is set to true, variants will drop the base ore, even with silk touch.

Configs relating to how ores are registered are added in `defaultresources/[folder]/excavated_variants/[namespace]/configs`. They can contain the following options:

* **blacklist\_ores** and **blacklist\_stones**: A way of disabling specific ores/stones by name.
* **blacklist\_ids**: A way of disabling specific stone/ore combinations. The value entered here should be the path of the block ID. For instance, for Andesite Redstone Ore this would be `andesite_redstone_ore`.
* **priority**: Allows certain config files to be loaded before others, in the order specified within. These should be a namespaced location of the variant config file.

Configs for adding stones or ores are placed in `defaultresources/[folder]/excavated_variants/[namespace]/variants`. These take the following format:

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
