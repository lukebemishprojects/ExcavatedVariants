package com.github.lukebemish.excavated_variants.data;

import com.google.gson.annotations.Expose;

import java.util.List;

public class ModData {
    @Expose
    public String mod_id;
    @Expose
    public List<BaseStone> provided_stones;
    @Expose
    public List<BaseOre> provided_ores;

    public ModData(String id, List<BaseStone> provided_stones, List<BaseOre> provided_ores) {
        this.mod_id = id;
        this.provided_stones = provided_stones;
        this.provided_ores = provided_ores;
    }
}
