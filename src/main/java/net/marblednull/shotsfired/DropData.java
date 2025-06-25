package net.marblednull.shotsfired;

public class DropData {
    public String item; // cast to the minecraft item type in a class
    public float chance;
    public DropData(String item, float chance) {
        this.item = item;
        this.chance = chance;
    }
}