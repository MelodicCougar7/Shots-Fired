package net.marblednull.shotsfired;

import net.minecraft.world.item.Item;

public class BurstData {
    public Item item;
    public float delay;
    public BurstData(Item item, float delay) {
        this.item = item;
        this.delay = delay;
    }
}
