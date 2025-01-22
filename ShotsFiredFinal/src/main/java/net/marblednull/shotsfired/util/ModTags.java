package net.marblednull.shotsfired.util;

import net.marblednull.shotsfired.ShotsFired;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {
    public static class Blocks {

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(new ResourceLocation(ShotsFired.MODID, name));
        }
    }

    public static class Items {

        public static final TagKey<Item> PISTOL_BULLET = tag("pistol_bullet");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(new ResourceLocation(ShotsFired.MODID, name));
        }
    }
}
