# Excavated Variants

Do you want to contribute to the default confige? Feel free to open a pull request. Default config files can be placed under `common/src/main/resources/default_configs`.


## Config Format

_Note: the config version may occasionally change between versions. If the config is outdated, the game will log an error and instead load the default config._

The main config file, "excavated_variants.json", contains several options:

* **blacklist\_ores** and **blacklist\_stones**: A way of disabling specific ores/stones by name.
* **blacklist\_ids**: A way of disabling specific stone/ore combinations. The value entered here should be the path of the block ID. For instance, for Andesite Redstone Ore this would be "andesite\_redstone\_ore".
* **attempt\_ore\_generation_insertion**: Allows the world-generation changes to be toggled. Useful if another tool, such as KubeJS, is being used for ore gen.
* **attempt\_ore\_replacement**: Toggles the much slower, but more reliable, ore-gen changes. Disable to speed up world generation substantially at the cost of less-reliable replacement of the original ore with its variants.
* **priority**: Allows certain config files to be loaded before others, in the order specified within.

Configs for adding stones or ores are placed in the "excavated_variants" directory. These take the following format:

* **mod\_id**: The ID of the mod necessary for this config to load.
* **provided_stones**: A list of stones provided by the config.
* **provided_ores**: A list of ores provided by the config.

Each ore object takes the following format:

* **id**: A name for this ore. Can be shared to avoid duplicate ores.
* **stone**: A list of stones, by id, that this ore already appears in.
* **texture_location**: The location of the texture of this ore. The background material of this texture should be the same as that of the first stone listed in **stone**.
* **block\_id**: A list of blocks of this ore, in the same order as **stone**.
* **en\_name**: The name to be given to this ore in-game.
* **types**: A list of types that this ore is classified as. Matched at runtime with the types specified in stone configs.

Each stone object takes the following format:

* **id**: A name for this ore. Can be shared to avoid duplicate ores.
* **texture_location**: A list of locations of the texture of this stone.
* **block\_id**: The block ID of this stone.
* **en\_name**: The name to be given to this stone in-game.
* **types**: A list of types that this stone is classified as. Matched at runtime with the types specified in ore configs.
