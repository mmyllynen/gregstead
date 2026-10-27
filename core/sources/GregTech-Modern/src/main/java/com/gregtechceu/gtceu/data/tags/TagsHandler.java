package com.gregtechceu.gtceu.data.tags;

import com.gregtechceu.gtceu.api.data.chemical.material.MarkerMaterials.Color;

import net.minecraft.world.item.Items;

import static com.gregtechceu.gtceu.api.data.chemical.material.ItemMaterialData.registerMaterialEntry;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.*;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class TagsHandler {

    public static void initExtraUnificationEntries() {
        registerMaterialEntry(Items.CLAY_BALL, ingot, Clay);

        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.BLACK), dye, Color.Black);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.RED), dye, Color.Red);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.GREEN), dye, Color.Green);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.BROWN), dye, Color.Brown);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.BLUE), dye, Color.Blue);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.PURPLE), dye, Color.Purple);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.CYAN), dye, Color.Cyan);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_GRAY), dye, Color.LightGray);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.GRAY), dye, Color.Gray);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.PINK), dye, Color.Pink);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.LIME), dye, Color.Lime);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.YELLOW), dye, Color.Yellow);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.LIGHT_BLUE), dye, Color.LightBlue);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.MAGENTA), dye, Color.Magenta);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.ORANGE), dye, Color.Orange);
        registerMaterialEntry(Items.DYE.pick(net.minecraft.world.item.DyeColor.WHITE), dye, Color.White);
    }
}
