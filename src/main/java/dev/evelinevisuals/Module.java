package dev.evelinevisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

public abstract class Module {
    public enum Category { INFO, HUD, EFFECTS, WORLD }

    public final String name;
    public final Category cat;
    public boolean enabled;

    protected Module(String name, Category cat) { this.name = name; this.cat = cat; }

    protected static MinecraftClient mc() { return MinecraftClient.getInstance(); }

    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean v) {
        if (v == enabled) return;
        enabled = v;
        if (v) onEnable(); else onDisable();
    }

    public void onEnable() {}
    public void onDisable() {}
    public void tick() {}
    public void render(DrawContext c, int w, int h) {}
    public void onAttack(Entity target) {}
}
