package com.auranite.abloom.config;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.ModConfigSpec;

public class EffectDisplayConfig {
    public static ModConfigSpec.DoubleValue RENDER_SCALE;
    public static ModConfigSpec.DoubleValue VERTICAL_OFFSET;
    public static ModConfigSpec.DoubleValue HORIZONTAL_OFFSET;
    public static ModConfigSpec.BooleanValue SHOW_SELF_POTION;
    public static ModConfigSpec.BooleanValue SHOW_OTHER_POTION;
    public static ModConfigSpec.BooleanValue BLINK_ON_LOW_DURATION;
    public static ModConfigSpec.DoubleValue MAX_DISTANCE;
    public static ModConfigSpec.DoubleValue MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT;
    public static MobEffect BLAST_EFFECT;
    public static MobEffect CORROSIVE_EFFECT;
    private static final ModConfigSpec.Builder CLIENT_BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Set<String> DISPLAY_EFFECTS;

    public static void initEffectReferences(MobEffect blastEffect, MobEffect corrosiveEffect) {
        BLAST_EFFECT = blastEffect;
        CORROSIVE_EFFECT = corrosiveEffect;
    }

    public static double getRenderScale() {
        return (Double) RENDER_SCALE.get();
    }

    public static double getVerticalOffset() {
        return (Double) VERTICAL_OFFSET.get();
    }

    public static double getHorizontalOffset() {
        return (Double) HORIZONTAL_OFFSET.get();
    }

    public static void saveGUIBuilder() {
        CLIENT_SPEC.save();
    }

    static {
        CLIENT_BUILDER.push("render");
        RENDER_SCALE = CLIENT_BUILDER.defineInRange("scale", (double) 1.0F, 0.1, (double) 2.0F);
        VERTICAL_OFFSET = CLIENT_BUILDER.defineInRange("vertical_offset", (double) 0.0F, (double) -100.0F, (double) 100.0F);
        HORIZONTAL_OFFSET = CLIENT_BUILDER.defineInRange("horizontal_offset", (double) 0.0F, (double) -5.0F, (double) 5.0F);
        SHOW_SELF_POTION = CLIENT_BUILDER.define("show_self_potion", false);
        SHOW_OTHER_POTION = CLIENT_BUILDER.define("show_other_potion", true);
        BLINK_ON_LOW_DURATION = CLIENT_BUILDER.define("blink_on_low_duration", true);
        MAX_DISTANCE = CLIENT_BUILDER.defineInRange("max_distance", (double) 64.0F, (double) 8.0F, (double) 128.0F);
        MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT = CLIENT_BUILDER.defineInRange("max_distance_without_line_of_sight", (double) 0.0F, (double) 0.0F, (double) 64.0F);
        CLIENT_BUILDER.pop();
        CLIENT_SPEC = CLIENT_BUILDER.build();
        DISPLAY_EFFECTS = new HashSet<>();
        DISPLAY_EFFECTS.add("burn");
        DISPLAY_EFFECTS.add("wetness");
        DISPLAY_EFFECTS.add("stun");
        DISPLAY_EFFECTS.add("freeze");
        DISPLAY_EFFECTS.add("shock");
        DISPLAY_EFFECTS.add("break");
        DISPLAY_EFFECTS.add("rupture");
        DISPLAY_EFFECTS.add("bloom");
        DISPLAY_EFFECTS.add("windswept");
        DISPLAY_EFFECTS.add("corruption");
        DISPLAY_EFFECTS.add("overload");
        DISPLAY_EFFECTS.add("dispersion");
        DISPLAY_EFFECTS.add("eclipse");
    }
}
