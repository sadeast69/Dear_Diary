package com.worldremembers.deardiary.client.gui;

public record InventoryButtonBounds(int x, int y, int width, int height) {
    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }
}
