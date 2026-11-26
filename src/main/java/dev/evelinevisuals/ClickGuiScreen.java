package dev.evelinevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ClickGuiScreen extends Screen {
    private static final int CW = 104;

    public ClickGuiScreen() { super(Text.literal("EvelineVisuals")); }

    private static Text label(Module m) {
        return Text.literal((m.enabled ? "§a" : "§7") + m.name);
    }

    @Override
    protected void init() {
        Module.Category[] cats = Module.Category.values();
        int x0 = width / 2 - cats.length * CW / 2;
        for (int i = 0; i < cats.length; i++) {
            int y = 40;
            for (Module m : Modules.ALL) {
                if (m.cat != cats[i]) continue;
                addDrawableChild(ButtonWidget.builder(label(m), b -> {
                    m.toggle();
                    b.setMessage(label(m));
                }).dimensions(x0 + i * CW, y, 100, 18).build());
                y += 20;
            }
        }
    }

    @Override
    public void render(DrawContext c, int mx, int my, float delta) {
        super.render(c, mx, my, delta);
        Module.Category[] cats = Module.Category.values();
        int x0 = width / 2 - cats.length * CW / 2;
        for (int i = 0; i < cats.length; i++) {
            c.drawCenteredTextWithShadow(textRenderer, cats[i].name(), x0 + i * CW + 50, 26, Gfx.accent(i * 50));
        }
        c.drawCenteredTextWithShadow(textRenderer, "EvelineVisuals", width / 2, 10, Gfx.accent(0));
    }

    @Override
    public void removed() { Config.save(); }

    @Override
    public boolean shouldPause() { return false; }
}
