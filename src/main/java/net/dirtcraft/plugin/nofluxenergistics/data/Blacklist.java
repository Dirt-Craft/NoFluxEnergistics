package net.dirtcraft.plugin.nofluxenergistics.data;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ConfigSerializable
@SuppressWarnings("FieldMayBeFinal")
public class Blacklist {
    public static List<Blacklist> DEFAULTS = defaults();
    public Blacklist(){
        reason = "";
        blocks = new ArrayList<>();
    }

    private Blacklist(String a, String b, String reason){
        this.blocks = Arrays.asList(a, b);
        this.reason = reason;
    }


    @Setting(value = "Blacklisted-Blocks", comment = "Can be a full blockID or a modID.\n" +
            "Note that all blocks will be restricted from each other in this list.\n" +
            "if you want A to blacklist A&B/A&C but not B&C, make an entry for A&B and A&C.")
    private List<String> blocks;

    @Setting(value = "Reason", comment = "The reason described")
    private String reason;

    @SuppressWarnings("StringEquality")
    public void register(RestrictionManager manager){
        for (String a : blocks) for (String b : blocks) {
            if (a != b)  manager.registerBlacklist(a, b, this);
        }
    }

    public String getWarning(String placed, String adjacent) {
        String s = reason.isEmpty()? "" : "\n" + reason;
        return String.format("you cannot place %s near %s%s", placed, adjacent, s);
    }

    private static List<Blacklist> defaults(){
        return Arrays.asList(
                new Blacklist("AppliedEnergistics2",                "FluxNetworks",                     "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("AppliedEnergistics2",                "ProjectE",                         "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("AppliedEnergistics2",                "Yabba",                            "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("AppliedEnergistics2",                "StorageDrawers",                   "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("AppliedEnergistics2",                "industrialforegoing:black_hole",   "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("bonsaitrees:bonsaipot",              "thermaldynamics:duct_32",          "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("randomthings:spectreenergyinjector", "draconicevolution:energy_pylon",   "&7This is to prevent a major bug so please use a buffer block in between"),
                new Blacklist("minecraft:hopper",                   "tp:cobblegen_block",               "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("minecraft:hopper",                   "tp:iron_cobblegen_block",          "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("minecraft:hopper",                   "tp:diamond_cobblegen_block",       "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("minecraft:hopper",                   "tp:blaze_cobblegen_block",         "&7This is to prevent severe lag and/or server crashes so please use an alternative!"),
                new Blacklist("minecraft:hopper",                   "tp:emerald_cobblegen_block",       "&7This is to prevent severe lag and/or server crashes so please use an alternative!")
        );
    }

    /*
    private void registerDefaults(){
        registerBlacklist("AppliedEnergistics2", "FluxNetworks");
        registerBlacklist("AppliedEnergistics2", "ProjectE");
        registerBlacklist("AppliedEnergistics2", "Yabba");
        registerBlacklist("AppliedEnergistics2", "StorageDrawers");
        registerBlacklist("AppliedEnergistics2", "industrialforegoing:black_hole");
        registerBlacklist("bonsaitrees:bonsaipot", "thermaldynamics:duct_32");
        registerBlacklist("randomthings:spectreenergyinjector", "draconicevolution:energy_pylon");
        registerBlacklist("minecraft:hopper", "tp:cobblegen_block");
        registerBlacklist("minecraft:hopper", "tp:iron_cobblegen_block");
        registerBlacklist("minecraft:hopper", "tp:diamond_cobblegen_block");
        registerBlacklist("minecraft:hopper", "tp:blaze_cobblegen_block");
        registerBlacklist("minecraft:hopper", "tp:emerald_cobblegen_block");
    }

     */
}
