package excavated_variants.data;

import java.util.List;

public class ModData {
    public String id;
    public List<BaseStone> provided_stones;
    public List<BaseOre> provided_ores;

    public ModData(String id, List<BaseStone> provided_stones, List<BaseOre> provided_ores) {
        this.id = id;
        this.provided_stones = provided_stones;
        this.provided_ores = provided_ores;
    }
}
