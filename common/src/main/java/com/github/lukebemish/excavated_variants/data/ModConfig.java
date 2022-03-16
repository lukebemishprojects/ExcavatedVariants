package com.github.lukebemish.excavated_variants.data;

import com.github.lukebemish.excavated_variants.ExcavatedVariants;
import com.google.gson.*;
import com.google.gson.annotations.Expose;
import dev.architectury.platform.Platform;
import net.minecraft.resources.ResourceLocation;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModConfig {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
    public static final Path CONFIG_PATH = Platform.getConfigFolder();
    public static final String FULL_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+".json";
    public static final String DIR_PATH = CONFIG_PATH + "/"+ ExcavatedVariants.MOD_ID+"/";
    public static final int CURRENT_VERSION = 3;

    @Expose
    public int format = 0;
    @Expose
    public List<String> priority = new ArrayList<>();
    public List<ModData> mods = new ArrayList<>();
    @Expose
    public List<String> blacklist_ids = new ArrayList<>();
    @Expose
    public List<String> blacklist_stones = new ArrayList<>();
    @Expose
    public List<String> blacklist_ores = new ArrayList<>();
    @Expose
    public boolean attempt_ore_generation_insertion = true;
    @Expose
    public boolean attempt_ore_replacement = true;
    @Expose
    public boolean add_conversion_recipes = true;

    private static final String[] UNEARTHED_STONE_LIST = new String[] {"beige_limestone","conglomerate","gabbro","granodiorite",
            "grey_limestone","limestone","mudstone","phyllite","rhyolite","siltstone","slate","white_granite"};
    private static final String[] UNEARTHED_STONE_EN_LIST = new String[] {"Beige Limestone","Conglomerate","Gabbro","Granodiorite",
            "Grey Limestone","Limestone","Mudstone","Phyllite","Rhyolite","Siltstone","Slate","White Granite"};

    private static List<String> genUnearthedBlockIdList(String oreName, String... toAdd) {
        ArrayList<String> output = new ArrayList<>();

        for (String s : UNEARTHED_STONE_LIST) {
            output.add("unearthed:"+s+"_"+oreName);
        }

        output.addAll(Arrays.asList(toAdd));
        return output;
    }

    private static List<String> genUnearthedStoneIdList(String... toAdd) {
        ArrayList<String> output = new ArrayList<>();

        for (String s : UNEARTHED_STONE_LIST) {
            output.add("unearthed_"+s);
        }

        output.addAll(Arrays.asList(toAdd));
        return output;
    }

    private static List<BaseStone> genUnearthedStoneList(BaseStone... toAdd) {
        ArrayList<BaseStone> output = new ArrayList<>(Arrays.asList(toAdd));

        for (int i = 0; i < UNEARTHED_STONE_LIST.length; i++) {
            String s = UNEARTHED_STONE_LIST[i];
                String enName = UNEARTHED_STONE_EN_LIST[i];
                BaseStone stone = new BaseStone("unearthed_" + s, "unearthed:textures/block/" + s + ".png", enName, "unearthed:"+s,List.of("stone"));
                output.add(stone);
        }

        output.addAll(Arrays.asList(toAdd));

        return output;
    }

    public static ModConfig getDefault() {
        ModConfig output = new ModConfig();
        output.format = CURRENT_VERSION;
        output.mods.add(new ModData("minecraft",
                List.of(new BaseStone("stone", new ResourceLocation("minecraft","textures/block/stone.png"), "Stone", new ResourceLocation("minecraft","stone"), List.of("stone")),
                        new BaseStone("deepslate", List.of("minecraft:textures/block/deepslate.png","minecraft:textures/block/deepslate_top.png"), "Deepslate", "minecraft:deepslate", List.of("stone")),
                        new BaseStone("andesite", new ResourceLocation("minecraft","textures/block/andesite.png"), "Andesite", new ResourceLocation("minecraft","andesite"), List.of("stone")),
                        new BaseStone("diorite", new ResourceLocation("minecraft","textures/block/diorite.png"), "Diorite", new ResourceLocation("minecraft","diorite"), List.of("stone")),
                        new BaseStone("granite", new ResourceLocation("minecraft","textures/block/granite.png"), "Granite", new ResourceLocation("minecraft","granite"), List.of("stone")),
                        new BaseStone("sandstone", new ResourceLocation("minecraft","textures/block/sandstone.png"), "Sandstone", new ResourceLocation("minecraft","sandstone"), List.of("stone")),
                        new BaseStone("red_sandstone", new ResourceLocation("minecraft","textures/block/red_sandstone.png"), "Red Sandstone", new ResourceLocation("minecraft","red_sandstone"), List.of("stone")),
                        new BaseStone("dripstone", new ResourceLocation("minecraft","textures/block/dripstone_block.png"), "Dripstone", new ResourceLocation("minecraft","dripstone_block"), List.of("stone")),
                        new BaseStone("tuff", new ResourceLocation("minecraft","textures/block/tuff.png"), "Tuff", new ResourceLocation("minecraft","tuff"), List.of("stone")),
                        new BaseStone("smooth_basalt", new ResourceLocation("minecraft","textures/block/smooth_basalt.png"), "Smooth Basalt", new ResourceLocation("minecraft","smooth_basalt"), List.of("stone","nether")),
                        new BaseStone("basalt", List.of("minecraft:textures/block/basalt_top.png","minecraft:textures/block/basalt_side.png"), "Basalt", "minecraft:basalt", List.of("nether")),
                        new BaseStone("calcite", new ResourceLocation("minecraft","textures/block/calcite.png"), "Calcite", new ResourceLocation("minecraft","calcite"), List.of("stone")),
                        new BaseStone("netherrack", new ResourceLocation("minecraft","textures/block/netherrack.png"), "Netherrack", new ResourceLocation("minecraft","netherrack"), List.of("nether")),
                        new BaseStone("blackstone", List.of("minecraft:textures/block/blackstone.png","minecraft:textures/block/blackstone_top.png"), "Blackstone", "minecraft:blackstone", List.of("nether")),
                        new BaseStone("end_stone", new ResourceLocation("minecraft","textures/block/end_stone.png"), "End Stone", new ResourceLocation("minecraft","end_stone"), List.of("end"))),
                List.of(new BaseOre("coal_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/coal_ore.png"),List.of(new ResourceLocation("minecraft","coal_ore"),new ResourceLocation("minecraft","deepslate_coal_ore")), "Coal Ore", List.of("stone")),
                        new BaseOre("iron_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/iron_ore.png"),List.of(new ResourceLocation("minecraft","iron_ore"),new ResourceLocation("minecraft","deepslate_iron_ore")), "Iron Ore", List.of("stone")),
                        new BaseOre("gold_ore",List.of("stone", "deepslate","netherrack"), new ResourceLocation("minecraft","textures/block/gold_ore.png"),List.of(new ResourceLocation("minecraft","gold_ore"),new ResourceLocation("minecraft","deepslate_gold_ore"),new ResourceLocation("minecraft","nether_gold_ore")), "Gold Ore", List.of("stone","nether")),
                        new BaseOre("copper_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/copper_ore.png"),List.of(new ResourceLocation("minecraft","copper_ore"),new ResourceLocation("minecraft","deepslate_copper_ore")), "Copper Ore", List.of("stone")),
                        new BaseOre("emerald_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/emerald_ore.png"),List.of(new ResourceLocation("minecraft","emerald_ore"),new ResourceLocation("minecraft","deepslate_emerald_ore")), "Emerald Ore", List.of("stone")),
                        new BaseOre("lapis_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/lapis_ore.png"),List.of(new ResourceLocation("minecraft","lapis_ore"),new ResourceLocation("minecraft","deepslate_lapis_ore")), "Lapis Ore", List.of("stone")),
                        new BaseOre("diamond_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/diamond_ore.png"),List.of(new ResourceLocation("minecraft","diamond_ore"),new ResourceLocation("minecraft","deepslate_diamond_ore")), "Diamond Ore", List.of("stone")),
                        new BaseOre("quartz_ore", List.of("netherrack"), new ResourceLocation("minecraft","textures/block/nether_quartz_ore.png"),List.of(new ResourceLocation("minecraft","nether_quartz_ore")), "Quartz Ore", List.of("nether")),
                        new BaseOre("redstone_ore",List.of("stone", "deepslate"), new ResourceLocation("minecraft","textures/block/redstone_ore.png"),List.of(new ResourceLocation("minecraft","redstone_ore"),new ResourceLocation("minecraft","deepslate_redstone_ore")), "Redstone Ore", List.of("stone")))));
        output.mods.add(new ModData("unearthed",
                genUnearthedStoneList(
                        new BaseStone("unearthed_quartzite","unearthed:textures/block/quartzite.png", "Quartzite", "unearthed:quartzite",List.of("stone")),
                        new BaseStone("unearthed_weathered_rhyolite","unearthed:textures/block/weathered_rhyolite.png", "Weathered Rhyolite", "unearthed:weathered_rhyolite",List.of("stone")),
                        new BaseStone("unearthed_dolerite","unearthed:textures/block/dolerite.png", "Dolerite", "unearthed:dolerite",List.of("stone")),
                        new BaseStone("unearthed_schist",List.of("unearthed:textures/block/schist_end.png","unearthed:textures/block/schist_side.png"), "Schist", "unearthed:schist",List.of("stone")),
                        new BaseStone("unearthed_pillow_basalt","unearthed:textures/block/pillow_basalt.png", "Pillow Basalt", "unearthed:pillow_basalt",List.of("stone")),
                        new BaseStone("unearthed_dacite",List.of("unearthed:textures/block/dacite_end.png","unearthed:textures/block/dacite_side.png"), "Dacite", "unearthed:dacite",List.of("stone"))
                ),
                List.of(new BaseOre("coal_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_coal_ore.png",genUnearthedBlockIdList("coal_ore","unearthed:sandstone_coal_ore"), "Coal Ore", List.of("stone")),
                        new BaseOre("iron_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_iron_ore.png",genUnearthedBlockIdList("iron_ore","unearthed:sandstone_iron_ore"), "Iron Ore", List.of("stone")),
                        new BaseOre("gold_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_gold_ore.png",genUnearthedBlockIdList("gold_ore","unearthed:sandstone_gold_ore"), "Gold Ore", List.of("stone")),
                        new BaseOre("copper_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_copper_ore.png",genUnearthedBlockIdList("copper_ore","unearthed:sandstone_copper_ore"), "Copper Ore", List.of("stone")),
                        new BaseOre("lapis_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_lapis_ore.png",genUnearthedBlockIdList("lapis_ore","unearthed:sandstone_lapis_ore"), "Lapis Ore", List.of("stone")),
                        new BaseOre("emerald_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_emerald_ore.png",genUnearthedBlockIdList("emerald_ore","unearthed:sandstone_emerald_ore"), "Emerald Ore", List.of("stone")),
                        new BaseOre("diamond_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_diamond_ore.png",genUnearthedBlockIdList("diamond_ore","unearthed:sandstone_diamond_ore"), "Diamond Ore", List.of("stone")),
                        new BaseOre("redstone_ore",genUnearthedStoneIdList("sandstone"), "unearthed:textures/block/ore/beige_limestone_redstone_ore.png",genUnearthedBlockIdList("redstone_ore","unearthed:sandstone_redstone_ore"), "Redstone Ore", List.of("stone")))));
        output.mods.add(new ModData("promenade",
                List.of(new BaseStone("promenade_blunite","promenade:textures/block/blunite.png","Blunite","promenade:blunite",List.of("stone")),
                        new BaseStone("promenade_carbonite","promenade:textures/block/carbonite.png","Carbonite","promenade:carbonite",List.of("stone"))),
                List.of()));
        output.mods.add(new ModData("twigs",
                List.of(new BaseStone("twigs_schist","twigs:textures/block/schist.png","Schist","twigs:schist",List.of("stone")),
                        new BaseStone("twigs_rhyolite","twigs:textures/block/rhyolite.png","Rhyolite","twigs:rhyolite",List.of("stone")),
                        new BaseStone("twigs_bloodstone","twigs:textures/block/bloodstone.png","Bloodstone","twigs:bloodstone",List.of("nether"))),
                List.of()));
        output.mods.add(new ModData("techreborn",
                List.of(),
                List.of(new BaseOre("aluminum_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/bauxite_ore.png"),List.of(new ResourceLocation("techreborn","bauxite_ore"),new ResourceLocation("techreborn","deepslate_bauxite_ore")), "Bauxite Ore", List.of("stone")),
                        new BaseOre("cinnabar_ore",List.of("netherrack"), new ResourceLocation("techreborn","textures/block/ore/cinnabar_ore.png"),List.of(new ResourceLocation("techreborn","cinnabar_ore")), "Cinnabar Ore", List.of("nether")),
                        new BaseOre("galena_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/galena_ore.png"),List.of(new ResourceLocation("techreborn","galena_ore"),new ResourceLocation("techreborn","deepslate_galena_ore")), "Galena Ore", List.of("stone")),
                        new BaseOre("iridium_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/iridium_ore.png"),List.of(new ResourceLocation("techreborn","iridium_ore"),new ResourceLocation("techreborn","deepslate_iridium_ore")), "Iridium Ore", List.of("stone")),
                        new BaseOre("lead_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/lead_ore.png"),List.of(new ResourceLocation("techreborn","lead_ore"),new ResourceLocation("techreborn","deepslate_lead_ore")), "Lead Ore", List.of("stone")),
                        new BaseOre("peridot_ore",List.of("end_stone"), new ResourceLocation("techreborn","textures/block/ore/peridot_ore.png"),List.of(new ResourceLocation("techreborn","peridot_ore")), "Peridot Ore", List.of("end")),
                        new BaseOre("ruby_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/ruby_ore.png"),List.of(new ResourceLocation("techreborn","ruby_ore"),new ResourceLocation("techreborn","deepslate_ruby_ore")), "Ruby Ore", List.of("stone")),
                        new BaseOre("sapphire_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/sapphire_ore.png"),List.of(new ResourceLocation("techreborn","sapphire_ore"),new ResourceLocation("techreborn","deepslate_sapphire_ore")), "Sapphire Ore", List.of("stone")),
                        new BaseOre("silver_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/silver_ore.png"),List.of(new ResourceLocation("techreborn","silver_ore"),new ResourceLocation("techreborn","deepslate_silver_ore")), "Silver Ore", List.of("stone")),
                        new BaseOre("tin_ore",List.of("stone", "deepslate"), new ResourceLocation("techreborn","textures/block/ore/tin_ore.png"),List.of(new ResourceLocation("techreborn","tin_ore"),new ResourceLocation("techreborn","deepslate_tin_ore")), "Tin Ore", List.of("stone")),
                        new BaseOre("sheldonite_ore",List.of("end_stone"), new ResourceLocation("techreborn","textures/block/ore/sheldonite_ore.png"),List.of(new ResourceLocation("techreborn","sheldonite_ore")), "Sheldonite Ore", List.of("end")),
                        new BaseOre("sodalite_ore",List.of("end_stone"), new ResourceLocation("techreborn","textures/block/ore/sodalite_ore.png"),List.of(new ResourceLocation("techreborn","sodalite_ore")), "Sodalite Ore", List.of("end")),
                        new BaseOre("tungsten_ore",List.of("end_stone"), new ResourceLocation("techreborn","textures/block/ore/tungsten_ore.png"),List.of(new ResourceLocation("techreborn","tungsten_ore")), "Tungsten Ore", List.of("end")),
                        new BaseOre("pyrite_ore",List.of("netherrack"), new ResourceLocation("techreborn","textures/block/ore/pyrite_ore.png"),List.of(new ResourceLocation("techreborn","pyrite_ore")), "Pyrite Ore", List.of("nether")),
                        new BaseOre("sphalerite_ore",List.of("netherrack"), new ResourceLocation("techreborn","textures/block/ore/sphalerite_ore.png"),List.of(new ResourceLocation("techreborn","sphalerite_ore")), "Sphalerite Ore", List.of("nether")))));
        output.mods.add(new ModData("betterend",
                List.of(new BaseStone("betterend_brimstone",new ResourceLocation("betterend","textures/block/inactive_brimstone.png"),"Brimstone",new ResourceLocation("betterend","brimstone"), List.of("end")),
                        new BaseStone("betterend_umbralith",new ResourceLocation("betterend","textures/block/umbralith.png"),"Umbralith",new ResourceLocation("betterend","umbralith"), List.of("end")),
                        new BaseStone("betterend_azure_jadestone",new ResourceLocation("betterend","textures/block/azure_jadestone.png"),"Azure Jadestone",new ResourceLocation("betterend","azure_jadestone"), List.of("end")),
                        new BaseStone("betterend_sandy_jadestone",new ResourceLocation("betterend","textures/block/sandy_jadestone.png"),"Sandy Jadestone",new ResourceLocation("betterend","sandy_jadestone"), List.of("end")),
                        new BaseStone("betterend_virid_jadestone",new ResourceLocation("betterend","textures/block/virid_jadestone.png"),"Virid Jadestone",new ResourceLocation("betterend","virid_jadestone"), List.of("end")),
                        new BaseStone("betterend_sulphuric_rock",new ResourceLocation("betterend","textures/block/sulphuric_rock.png"),"Sulfuric Rock",new ResourceLocation("betterend","sulphuric_rock"), List.of("end")),
                        new BaseStone("betterend_violecite",new ResourceLocation("betterend","textures/block/violecite.png"),"Violecite",new ResourceLocation("betterend","violecite"), List.of("end")),
                        new BaseStone("betterend_flavolite",new ResourceLocation("betterend","textures/block/flavolite.png"),"Flavolite",new ResourceLocation("betterend","flavolite"), List.of("end"))),
                List.of(new BaseOre("thallasium_ore",List.of("end_stone"), new ResourceLocation("betterend","textures/block/thallasium_ore.png"),List.of(new ResourceLocation("betterend","thallasium_ore")), "Thallasium Ore", List.of("end")),
                        new BaseOre("amber_ore",List.of("end_stone"), new ResourceLocation("betterend","textures/block/amber_ore.png"),List.of(new ResourceLocation("betterend","amber_ore")), "Amber Ore", List.of("end")),
                        new BaseOre("ender_ore",List.of("end_stone"), new ResourceLocation("betterend","textures/block/ender_ore.png"),List.of(new ResourceLocation("betterend","ender_ore")), "Ender Ore", List.of("end")))));
        output.mods.add(new ModData("betternether",
                List.of(),
                List.of(new BaseOre("redstone_ore",List.of("netherrack"), "betternether:textures/block/nether_redstone_ore.png",List.of("betternether:nether_redstone_ore"), "Redstone Ore", List.of("nether")),
                        new BaseOre("lapis_ore",List.of("netherrack"), "betternether:textures/block/nether_lapis_ore.png",List.of("betternether:nether_lapis_ore"), "Lapis Ore", List.of("nether")),
                        new BaseOre("cincinnasite_ore",List.of("netherrack"), "betternether:textures/block/cincinnasite_ore.png",List.of("betternether:cincinnasite_ore"), "Cincinnasite Ore", List.of("nether")),
                        new BaseOre("ruby_ore",List.of("netherrack"), "betternether:textures/block/nether_ruby_ore.png",List.of("betternether:nether_ruby_ore"), "Ruby Ore", List.of("nether")))));
        output.mods.add(new ModData("blockus",
                List.of(new BaseStone("blockus_limestone",new ResourceLocation("blockus","textures/block/limestone.png"),"Limestone",new ResourceLocation("blockus","limestone"),List.of("stone")),
                        new BaseStone("blockus_marble",new ResourceLocation("blockus","textures/block/marble.png"),"Marble",new ResourceLocation("blockus","marble"),List.of("stone")),
                        new BaseStone("blockus_bluestone",new ResourceLocation("blockus","textures/block/bluestone.png"),"Bluestone",new ResourceLocation("blockus","bluestone"),List.of("stone"))),
                List.of()));
        output.mods.add(new ModData("byg",
                List.of(new BaseStone("byg_red_rock",new ResourceLocation("byg","textures/block/red_rock.png"),"Red Rock",new ResourceLocation("byg","red_rock"), List.of("stone")),
                        new BaseStone("byg_dacite",new ResourceLocation("byg","textures/block/dacite.png"),"Dacite",new ResourceLocation("byg","dacite"), List.of("stone")),
                        new BaseStone("byg_brimstone",new ResourceLocation("byg","textures/block/brimstone.png"),"Brimstone",new ResourceLocation("byg","brimstone"), List.of("nether")),
                        new BaseStone("byg_ether_stone",new ResourceLocation("byg","textures/block/ether_stone.png"),"Ether Stone",new ResourceLocation("byg","ether_stone"), List.of("end")),
                        new BaseStone("byg_soapstone",new ResourceLocation("byg","textures/block/soapstone.png"),"Soapstone",new ResourceLocation("byg","soapstone"), List.of("stone")),
                        new BaseStone("byg_blue_netherrack",new ResourceLocation("byg","textures/block/blue_netherrack.png"),"Blue Netherrack",new ResourceLocation("byg","blue_netherrack"), List.of("nether")),
                        new BaseStone("byg_scoria",new ResourceLocation("byg","textures/block/scoria_stone.png"),"Scoria",new ResourceLocation("byg","scoria_stone"), List.of("nether")),
                        new BaseStone("byg_travertine",new ResourceLocation("byg","textures/block/travertine.png"),"Travertine",new ResourceLocation("byg","travertine"), List.of("stone")),
                        new BaseStone("byg_rocky",new ResourceLocation("byg","textures/block/rocky_stone.png"),"Rocky",new ResourceLocation("byg","rocky_stone"), List.of("stone")),
                        new BaseStone("byg_cryptic",new ResourceLocation("byg","textures/block/cryptic_stone.png"),"Cryptic",new ResourceLocation("byg","cryptic_stone"), List.of("end")),
                        new BaseStone("byg_purpur",new ResourceLocation("byg","textures/block/purpur_stone.png"),"Purpur",new ResourceLocation("byg","purpur_stone"), List.of("end")),
                        new BaseStone("byg_mossy_stone",new ResourceLocation("byg","textures/block/mossy_stone.png"),"Mossy Stone",new ResourceLocation("byg","mossy_stone"), List.of("stone"))),
                List.of(new BaseOre("quartz_ore",List.of("byg_brimstone","byg_blue_netherrack"), new ResourceLocation("byg","textures/block/brimstone_nether_quartz_ore.png"),List.of(new ResourceLocation("byg","brimstone_nether_quartz_ore"),new ResourceLocation("byg","blue_nether_quartz_ore")), "Quartz Ore", List.of("nether")),
                        new BaseOre("gold_ore",List.of("byg_brimstone","byg_blue_netherrack"), new ResourceLocation("byg","textures/block/brimstone_nether_gold_ore.png"),List.of(new ResourceLocation("byg","brimstone_nether_gold_ore"),new ResourceLocation("byg","blue_nether_gold_ore")), "Gold Ore", List.of("nether")))));
        output.mods.add(new ModData("immersiveengineering",
                List.of(),
                List.of(new BaseOre("lead_ore",List.of("stone", "deepslate"), new ResourceLocation("immersiveengineering","textures/block/metal/ore_lead.png"),new ResourceLocation("immersiveengineering","ore_lead"), "Lead Ore", List.of("stone")),
                        new BaseOre("nickel_ore",List.of("stone", "deepslate"), new ResourceLocation("immersiveengineering","textures/block/metal/ore_nickel.png"),new ResourceLocation("immersiveengineering","ore_nickel"), "Nickel Ore", List.of("stone")),
                        new BaseOre("silver_ore",List.of("stone", "deepslate"), new ResourceLocation("immersiveengineering","textures/block/metal/ore_silver.png"),new ResourceLocation("immersiveengineering","ore_silver"), "Silver Ore", List.of("stone")),
                        new BaseOre("uranium_ore",List.of("stone", "deepslate"), new ResourceLocation("immersiveengineering","textures/block/metal/ore_uranium.png"),new ResourceLocation("immersiveengineering","ore_uranium"), "Uranium Ore", List.of("stone")),
                        new BaseOre("aluminum_ore",List.of("stone", "deepslate"), new ResourceLocation("immersiveengineering","textures/block/metal/ore_aluminum.png"),new ResourceLocation("immersiveengineering","ore_aluminum"), "Bauxite Ore", List.of("stone")))));
        output.mods.add(new ModData("quark",
                List.of(new BaseStone("quark_shale", new ResourceLocation("quark","textures/block/shale.png"), "Shale", new ResourceLocation("quark","shale"), List.of("stone")),
                        //new BaseStone("myalite", new ResourceLocation("quark","textures/block/myalite.png"), "Myalite", new ResourceLocation("quark","myalite"), List.of("end")), //Disabled for now. Doesn't work because weird locational color stuff...
                        new BaseStone("quark_limestone", new ResourceLocation("quark","textures/block/limestone.png"), "Limestone", new ResourceLocation("quark","limestone"), List.of("stone")),
                        new BaseStone("quark_jasper", new ResourceLocation("quark","textures/block/jasper.png"), "Jasper", new ResourceLocation("quark","jasper"), List.of("stone"))),
                List.of()));
        output.mods.add(new ModData("create",
                List.of(new BaseStone("create_asurine", new ResourceLocation("create","textures/block/palettes/stone_types/asurine.png"), "Asurine", new ResourceLocation("create","asurine"), List.of("stone")),
                        new BaseStone("create_crimsite", new ResourceLocation("create","textures/block/palettes/stone_types/crimsite.png"), "Crimsite", new ResourceLocation("create","crimsite"), List.of("stone")),
                        new BaseStone("create_limestone", new ResourceLocation("create","textures/block/palettes/stone_types/limestone.png"), "Limestone", new ResourceLocation("create","limestone"), List.of("stone")),
                        new BaseStone("create_ochrum", new ResourceLocation("create","textures/block/palettes/stone_types/ochrum.png"), "Ochrum", new ResourceLocation("create","ochrum"), List.of("stone")),
                        new BaseStone("create_scorchia", new ResourceLocation("create","textures/block/palettes/stone_types/scorchia.png"), "Scorchia", new ResourceLocation("create","scorchia"), List.of("stone","nether")),
                        new BaseStone("create_scoria", new ResourceLocation("create","textures/block/palettes/stone_types/scoria.png"), "Scoria", new ResourceLocation("create","scoria"), List.of("stone","nether")),
                        new BaseStone("create_veridium", new ResourceLocation("create","textures/block/palettes/stone_types/veridium.png"), "Veridium", new ResourceLocation("create","veridium"), List.of("stone"))),
                List.of(new BaseOre("zinc_ore",List.of("stone", "deepslate"), new ResourceLocation("create","textures/block/zinc_ore.png"),List.of(new ResourceLocation("create","zinc_ore"),new ResourceLocation("create","deepslate_zinc_ore")), "Zinc Ore", List.of("stone")))));
        for (ModData m : output.mods) {
            for (BaseOre o : m.provided_ores) {
                o.setupBlockId();
            }
            for (BaseStone s : m.provided_stones) {
                s.setupBlockId();
            }
        }
        output.priority = List.of("minecraft.json");
        return output;
    }

    private static ModConfig load() {
        ModConfig config = new ModConfig();
        try {
            checkExistence();
            config = GSON.fromJson(new FileReader(FULL_PATH), ModConfig.class);
            if (config.format != CURRENT_VERSION) {
                ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
                return getDefault();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JsonSyntaxException e) {
            try {
                JsonObject reader = GSON.fromJson(new FileReader(FULL_PATH), JsonObject.class);
                if (reader.has("format") && reader.get("format").isJsonPrimitive() && reader.get("format").getAsJsonPrimitive().isNumber()) {
                    JsonPrimitive p = reader.get("format").getAsJsonPrimitive();
                    try {
                        int i = p.getAsInt();
                        if (i != CURRENT_VERSION) {
                            ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
                            return getDefault();
                        }
                    } catch (NumberFormatException ignored) {}
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                ExcavatedVariants.LOGGER.error("Config existence is inconsistent! Using default config instead...");
                return getDefault();
            }
            ExcavatedVariants.LOGGER.error("Config is not the expected syntax! (It may be outdated). An attempt to load with this config would crash. Using default config instead...");
            return getDefault();
        }
        List<String> loaded = new ArrayList<>();
        for (String p : config.priority) {
            try {
                ModData data = GSON.fromJson(new FileReader(DIR_PATH + p), ModData.class);
                config.mods.add(data);
                loaded.add(p);
            } catch (FileNotFoundException e) {
                ExcavatedVariants.LOGGER.error("Could not find referenced config: "+p);
            } catch (JsonSyntaxException e) {
                ExcavatedVariants.LOGGER.error("Issue reading config: "+p);
            }
        }
        try {
            for (Path s : Files.list(Path.of(DIR_PATH)).toList()) {
                String p = s.getFileName().toString();
                if (!loaded.contains(p)) {
                    try {
                        ModData data = GSON.fromJson(new FileReader(DIR_PATH + p), ModData.class);
                        config.mods.add(data);
                    } catch (FileNotFoundException e) {
                        ExcavatedVariants.LOGGER.error("Could not find config: " + p + ".json");
                    } catch (JsonSyntaxException e) {
                        ExcavatedVariants.LOGGER.error("Issue reading config: "+p+".json");
                    }
                }
            }
        } catch (IOException e) {
            ExcavatedVariants.LOGGER.error("Could not load config directory...", e);
        }
        for (ModData m : config.mods) {
            for (BaseOre o : m.provided_ores) {
                o.setupBlockId();
            }
            for (BaseStone s : m.provided_stones) {
                s.setupBlockId();
            }
        }
        return config;
    }

    public static ModConfig get() {
        ModConfig config = load();
        if (config.format != CURRENT_VERSION) {
            ExcavatedVariants.LOGGER.error("Config is outdated! An attempt to load with this config would crash. Using default config instead...");
            return getDefault();
        }
        return config;
    }

    public static void save(ModConfig config) {
        try {
            checkExistence();
            FileWriter writer = new FileWriter(FULL_PATH);
            GSON.toJson(config, writer);
            writer.flush();
            writer.close();
            for (ModData m : config.mods) {
                FileWriter w = new FileWriter(DIR_PATH+m.mod_id +".json");
                GSON.toJson(m, w);
                w.flush();
                w.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkExistence() throws IOException {
        if (!Files.exists(CONFIG_PATH)) Files.createDirectories(CONFIG_PATH);
        if (!Files.exists(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID))) Files.createDirectories(CONFIG_PATH.resolve(ExcavatedVariants.MOD_ID));
        if (!Files.exists(Paths.get(FULL_PATH))) {
            Files.createFile(Paths.get(FULL_PATH));
            save(getDefault());
        }
    }
}
