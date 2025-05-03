package net.marblednull.shotsfired;

import net.minecraft.world.item.Item;

public class DropData {
    public Item item;
    public float chance;
    public DropData(Item item, float chance) {
        this.item = item;
        this.chance = chance;
    }
}