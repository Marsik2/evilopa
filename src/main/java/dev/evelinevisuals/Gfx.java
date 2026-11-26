package dev.evelinevisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;

final class Gfx {
    private Gfx() {}

    static int accent(int off) {
        float hue = ((System.currentTimeMillis() / 18 + off) % 360) / 360f;
        return java.awt.Color.HSBtoRGB(hue, 0.55f, 1f) | 0xFF000000;
    }

    static void panel(DrawContext c, int x, int y, int w, int h) {
        c.fill(x, y, x + w, y + h, 0xA0101018);
        c.fill(x, y, x + 2, y + h, accent(x + y));
    }

    static void text(DrawContext c, String s, int x, int y, int col) {
        c.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, s, x, y, col);
    }

    static int width(String s) { return MinecraftClient.getInstance().textRenderer.getWidth(s); }

    static double r() { return Math.random() - 0.5; }

    static void burst(Entity e, ParticleEffect p, int n, double speed) {
        var w = MinecraftClient.getInstance().world;
        if (w == null || e == null) return;
        for (int i = 0; i < n; i++) {
            w.addParticle(p,
                e.getX() + r() * e.getWidth(),
                e.getBodyY(0.5) + r() * e.getHeight() * 0.8,
                e.getZ() + r() * e.getWidth(),
                r() * speed, r() * speed, r() * speed);
        }
    }
}
