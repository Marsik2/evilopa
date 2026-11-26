package dev.evelinevisuals;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class Modules {
    public static final List<Module> ALL = new ArrayList<>();
    static Module FRIENDS;

    private static <T extends Module> T add(T m) { ALL.add(m); return m; }

    private static final EquipmentSlot[] ARMOR = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };

    public static void init() {
        // INFO
        add(new Watermark()); add(new ModList()); add(new Coordinates()); add(new Biome());
        add(new FpsPing()); add(new Speed()); add(new Direction()); add(new Cps());
        add(new Combo()); add(new Dist()); add(new DeathPoint());
        // HUD
        add(new ArmorHud()); add(new PotionHud()); add(new TargetHud()); add(new TotemCounter());
        add(new ItemCounters()); add(new PearlCooldown()); add(new Keystrokes());
        add(new DurabilityWarning()); add(new LowHealth()); add(new AimDots());
        // EFFECTS
        add(new HitParticles()); add(new CritEffect()); add(new KillEffect());
        add(new HitSound()); add(new Trails()); add(new JumpCircles());
        // WORLD
        add(new GlowEsp()); add(new Hitboxes()); add(new Fullbright()); add(new Zoom());
        add(new ClearWeather()); add(new AutoSprint());
        FRIENDS = add(new FriendProtect());
    }

    public static void renderAll(DrawContext c, int w, int h) {
        for (Module m : ALL) if (m.enabled) m.render(c, w, h);
        List<String> lines = new ArrayList<>();
        for (Module m : ALL) {
            if (m.enabled && m instanceof Info i) {
                String s = i.line();
                if (s != null) lines.add(s);
            }
        }
        if (lines.isEmpty()) return;
        int mw = 0;
        for (String s : lines) mw = Math.max(mw, Gfx.width(s));
        int y = 28;
        Gfx.panel(c, 6, y, mw + 12, lines.size() * 11 + 6);
        for (int i = 0; i < lines.size(); i++) Gfx.text(c, lines.get(i), 12, y + 4 + i * 11, 0xFFFFFFFF);
    }

    // ---------------------------------------------------------------- INFO
    static abstract class Info extends Module {
        Info(String n) { super(n, Category.INFO); }
        abstract String line();
    }

    static class Watermark extends Module {
        Watermark() { super("Watermark", Category.INFO); }
        @Override public void render(DrawContext c, int w, int h) {
            String s = "EvelineVisuals | " + mc().player.getName().getString();
            Gfx.panel(c, 6, 6, Gfx.width(s) + 12, 16);
            Gfx.text(c, s, 12, 10, Gfx.accent(0));
        }
    }

    static class ModList extends Module {
        ModList() { super("ModList", Category.INFO); }
        @Override public void render(DrawContext c, int w, int h) {
            List<Module> l = new ArrayList<>();
            for (Module m : ALL) if (m.enabled && !(m instanceof ModList)) l.add(m);
            l.sort((a, b) -> Gfx.width(b.name) - Gfx.width(a.name));
            int y = 4;
            for (Module m : l) {
                int tw = Gfx.width(m.name);
                c.fill(w - tw - 10, y, w - 2, y + 11, 0x90101018);
                c.fill(w - 2, y, w, y + 11, Gfx.accent(y * 3));
                Gfx.text(c, m.name, w - tw - 6, y + 2, Gfx.accent(y * 3));
                y += 11;
            }
        }
    }

    static class Coordinates extends Info {
        Coordinates() { super("Coordinates"); }
        @Override String line() {
            var p = mc().player;
            String s = String.format(Locale.US, "XYZ %.0f %.0f %.0f", p.getX(), p.getY(), p.getZ());
            if (mc().world.getRegistryKey() == World.OVERWORLD)
                s += String.format(Locale.US, "  (Nether %.0f %.0f)", p.getX() / 8, p.getZ() / 8);
            return s;
        }
    }

    static class Biome extends Info {
        Biome() { super("Biome"); }
        @Override String line() {
            return "Биом: " + mc().world.getBiome(mc().player.getBlockPos()).getKey()
                .map(k -> k.getValue().getPath()).orElse("?");
        }
    }

    static class FpsPing extends Info {
        FpsPing() { super("FpsPing"); }
        @Override String line() {
            int ping = 0;
            var h = mc().getNetworkHandler();
            if (h != null) {
                var e = h.getPlayerListEntry(mc().player.getUuid());
                if (e != null) ping = e.getLatency();
            }
            return "FPS " + mc().getCurrentFps() + " | Ping " + ping + "ms";
        }
    }

    static class Speed extends Info {
        Speed() { super("Speed"); }
        @Override String line() { return String.format(Locale.US, "Speed %.1f b/s", State.speed); }
    }

    static class Direction extends Info {
        Direction() { super("Direction"); }
        @Override String line() {
            return "Взгляд: " + mc().player.getHorizontalFacing().asString()
                + String.format(Locale.US, " (%.0f)", net.minecraft.util.math.MathHelper.wrapDegrees(mc().player.getYaw()));
        }
    }

    static class Cps extends Info {
        Cps() { super("CPS"); }
        @Override String line() { return "CPS " + State.cps(); }
    }

    static class Combo extends Info {
        Combo() { super("Combo"); }
        @Override String line() { return State.combo > 1 ? "Combo x" + State.combo : null; }
    }

    static class Dist extends Info {
        Dist() { super("HitDistance"); }
        @Override String line() {
            if (System.currentTimeMillis() - State.lastHitTime > 5000) return null;
            return String.format(Locale.US, "Дистанция удара %.2f", State.lastReach);
        }
    }

    static class DeathPoint extends Info {
        DeathPoint() { super("DeathPoint"); }
        @Override String line() {
            var d = State.deathPos;
            return d == null ? null : String.format(Locale.US, "Смерть: %.0f %.0f %.0f", d.x, d.y, d.z);
        }
    }

    // ----------------------------------------------------------------- HUD
    static class ArmorHud extends Module {
        ArmorHud() { super("ArmorHud", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            int y = h / 2 - 50;
            for (EquipmentSlot s : ARMOR) {
                ItemStack st = mc().player.getEquippedStack(s);
                if (!st.isEmpty()) {
                    c.drawItem(st, 6, y);
                    if (st.isDamageable()) {
                        int max = st.getMaxDamage();
                        int pct = 100 * (max - st.getDamage()) / Math.max(1, max);
                        int col = java.awt.Color.HSBtoRGB(pct / 100f * 0.33f, 0.8f, 1f) | 0xFF000000;
                        Gfx.text(c, pct + "%", 26, y + 4, col);
                    }
                }
                y += 20;
            }
        }
    }

    static class PotionHud extends Module {
        PotionHud() { super("PotionHud", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            int y = h / 2 + 40;
            for (StatusEffectInstance i : mc().player.getStatusEffects()) {
                if (i.getEffectType().equals(StatusEffects.NIGHT_VISION) && FULLBRIGHT_ON()) continue;
                String t = i.isInfinite() ? "inf"
                    : String.format("%d:%02d", i.getDuration() / 1200, (i.getDuration() / 20) % 60);
                String s = i.getEffectType().value().getName().getString() + " " + (i.getAmplifier() + 1) + "  " + t;
                Gfx.panel(c, 6, y, Gfx.width(s) + 10, 12);
                Gfx.text(c, s, 11, y + 2, i.getEffectType().value().isBeneficial() ? 0xFF77FF77 : 0xFFFF7777);
                y += 14;
            }
        }
    }

    private static boolean FULLBRIGHT_ON() {
        for (Module m : ALL) if (m instanceof Fullbright) return m.enabled;
        return false;
    }

    static class TargetHud extends Module {
        float shown;
        TargetHud() { super("TargetHud", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            LivingEntity t = State.hudTarget();
            if (t == null) return;
            float hp = t.getHealth() + t.getAbsorptionAmount();
            float max = Math.max(1f, t.getMaxHealth());
            shown += (hp - shown) * 0.15f;
            if (Math.abs(hp - shown) < 0.05f) shown = hp;
            int x = w / 2 + 30, y = h / 2 + 10, pw = 120;
            Gfx.panel(c, x, y, pw, 36);
            Gfx.text(c, t.getName().getString(), x + 6, y + 4, 0xFFFFFFFF);
            float f = Math.max(0f, Math.min(1f, shown / max));
            c.fill(x + 6, y + 18, x + pw - 6, y + 24, 0xFF303040);
            int col = java.awt.Color.HSBtoRGB(f * 0.33f, 0.8f, 1f) | 0xFF000000;
            c.fill(x + 6, y + 18, x + 6 + (int) ((pw - 12) * f), y + 24, col);
            Gfx.text(c, String.format(Locale.US, "%.1f HP", hp), x + 6, y + 26, 0xFFCCCCCC);
        }
    }

    static class TotemCounter extends Module {
        ItemStack st;
        TotemCounter() { super("TotemCounter", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            if (st == null) st = new ItemStack(Items.TOTEM_OF_UNDYING);
            int n = mc().player.getInventory().count(Items.TOTEM_OF_UNDYING);
            c.drawItem(st, w / 2 - 8, h / 2 + 20);
            Gfx.text(c, String.valueOf(n), w / 2 + 10, h / 2 + 26, n == 0 ? 0xFFFF5555 : 0xFFFFFFFF);
        }
    }

    static class ItemCounters extends Module {
        ItemStack[] st;
        ItemCounters() { super("ItemCounters", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            if (st == null) st = new ItemStack[] {
                new ItemStack(Items.GOLDEN_APPLE), new ItemStack(Items.ENCHANTED_GOLDEN_APPLE),
                new ItemStack(Items.ENDER_PEARL), new ItemStack(Items.END_CRYSTAL),
                new ItemStack(Items.EXPERIENCE_BOTTLE) };
            int y = h / 2 - 50;
            for (ItemStack s : st) {
                Item it = s.getItem();
                int n = mc().player.getInventory().count(it);
                c.drawItem(s, w - 24, y);
                String t = String.valueOf(n);
                Gfx.text(c, t, w - 28 - Gfx.width(t), y + 4, n == 0 ? 0xFF888888 : 0xFFFFFFFF);
                y += 20;
            }
        }
    }

    static class PearlCooldown extends Module {
        ItemStack st;
        PearlCooldown() { super("PearlCooldown", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            if (st == null) st = new ItemStack(Items.ENDER_PEARL);
            float p = mc().player.getItemCooldownManager().getCooldownProgress(st, 0f);
            if (p <= 0f) return;
            int x = w / 2 - 20, y = h / 2 + 44;
            c.fill(x, y, x + 40, y + 4, 0xA0101018);
            c.fill(x, y, x + (int) (40 * p), y + 4, Gfx.accent(0));
        }
    }

    static class Keystrokes extends Module {
        Keystrokes() { super("Keystrokes", Category.HUD); }
        private void key(DrawContext c, String s, int x, int y, boolean down) {
            c.fill(x, y, x + 18, y + 18, down ? 0xC0FFFFFF : 0xA0101018);
            Gfx.text(c, s, x + 6, y + 5, down ? 0xFF000000 : 0xFFFFFFFF);
        }
        @Override public void render(DrawContext c, int w, int h) {
            var o = mc().options;
            int x = w - 62, y = h - 64;
            key(c, "W", x + 20, y, o.forwardKey.isPressed());
            key(c, "A", x, y + 20, o.leftKey.isPressed());
            key(c, "S", x + 20, y + 20, o.backKey.isPressed());
            key(c, "D", x + 40, y + 20, o.rightKey.isPressed());
        }
    }

    static class DurabilityWarning extends Module {
        DurabilityWarning() { super("DurabilityWarning", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            boolean low = false;
            for (EquipmentSlot s : ARMOR) {
                ItemStack st = mc().player.getEquippedStack(s);
                if (st.isDamageable() && st.getMaxDamage() - st.getDamage() < st.getMaxDamage() * 0.15) low = true;
            }
            if (low && System.currentTimeMillis() % 1000 < 600)
                c.drawCenteredTextWithShadow(mc().textRenderer, "Броня почти сломана!", w / 2, h / 2 - 50, 0xFFFF5555);
        }
    }

    static class LowHealth extends Module {
        LowHealth() { super("LowHealth", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            if (mc().player.getHealth() > 6) return;
            int a = Math.max(20, (int) (60 + 40 * Math.sin(System.currentTimeMillis() / 150.0)));
            int col = (a << 24) | 0xFF0000, t = 8;
            c.fill(0, 0, w, t, col); c.fill(0, h - t, w, h, col);
            c.fill(0, 0, t, h, col); c.fill(w - t, 0, w, h, col);
        }
    }

    static class AimDots extends Module {
        AimDots() { super("AimDots", Category.HUD); }
        @Override public void render(DrawContext c, int w, int h) {
            boolean on = mc().targetedEntity instanceof LivingEntity;
            int col = on ? 0xFFFF3333 : Gfx.accent(0);
            int cx = w / 2, cy = h / 2, r = 6;
            c.fill(cx - r - 1, cy - r - 1, cx - r + 1, cy - r + 1, col);
            c.fill(cx + r - 1, cy - r - 1, cx + r + 1, cy - r + 1, col);
            c.fill(cx - r - 1, cy + r - 1, cx - r + 1, cy + r + 1, col);
            c.fill(cx + r - 1, cy + r - 1, cx + r + 1, cy + r + 1, col);
        }
    }

    // ------------------------------------------------------------- EFFECTS
    static class HitParticles extends Module {
        HitParticles() { super("HitParticles", Category.EFFECTS); }
        @Override public void onAttack(Entity e) { Gfx.burst(e, ParticleTypes.FIREWORK, 14, 0.3); }
    }

    static class CritEffect extends Module {
        CritEffect() { super("CritEffect", Category.EFFECTS); }
        @Override public void onAttack(Entity e) {
            Gfx.burst(e, ParticleTypes.CRIT, 20, 0.6);
            Gfx.burst(e, ParticleTypes.ENCHANTED_HIT, 10, 0.4);
        }
    }

    static class KillEffect extends Module {
        KillEffect() { super("KillEffect", Category.EFFECTS); }
        @Override public void tick() {
            LivingEntity t = State.lastTarget;
            if (t == null || State.killDone) return;
            if (System.currentTimeMillis() - State.lastHitTime > 3000) { State.killDone = true; return; }
            if (t.isDead() || t.getHealth() <= 0 || t.isRemoved()) {
                State.killDone = true;
                Gfx.burst(t, ParticleTypes.TOTEM_OF_UNDYING, 50, 0.8);
                mc().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_PLAYER_LEVELUP, 1f, 0.6f));
            }
        }
    }

    static class HitSound extends Module {
        HitSound() { super("HitSound", Category.EFFECTS); }
        @Override public void onAttack(Entity e) {
            mc().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.6f, 0.6f));
        }
    }

    static class Trails extends Module {
        Trails() { super("Trails", Category.EFFECTS); }
        @Override public void tick() {
            var p = mc().player;
            if (State.speed < 1.0) return;
            mc().world.addParticle(ParticleTypes.END_ROD, p.getX() + Gfx.r() * 0.3, p.getY() + 0.1,
                p.getZ() + Gfx.r() * 0.3, 0, 0.01, 0);
        }
    }

    static class JumpCircles extends Module {
        boolean wasGround = true;
        JumpCircles() { super("JumpCircles", Category.EFFECTS); }
        @Override public void tick() {
            var p = mc().player;
            if (wasGround && !p.isOnGround() && p.getVelocity().y > 0) {
                for (int i = 0; i < 36; i++) {
                    double a = i * Math.PI / 18;
                    mc().world.addParticle(ParticleTypes.END_ROD, p.getX() + Math.cos(a) * 0.3, p.getY() + 0.05,
                        p.getZ() + Math.sin(a) * 0.3, Math.cos(a) * 0.15, 0, Math.sin(a) * 0.15);
                }
            }
            wasGround = p.isOnGround();
        }
    }

    // --------------------------------------------------------------- WORLD
    static class GlowEsp extends Module {
        GlowEsp() { super("GlowESP", Category.WORLD); }
        @Override public void tick() {
            for (var e : mc().world.getPlayers())
                if (e != mc().player) e.setGlowing(!Friends.is(e.getName().getString()));
        }
        @Override public void onDisable() {
            if (mc().world == null) return;
            for (var e : mc().world.getPlayers()) e.setGlowing(false);
        }
    }

    static class Hitboxes extends Module {
        Hitboxes() { super("Hitboxes", Category.WORLD); }
        @Override public void tick() { mc().getEntityRenderDispatcher().setRenderHitboxes(true); }
        @Override public void onDisable() { mc().getEntityRenderDispatcher().setRenderHitboxes(false); }
    }

    static class Fullbright extends Module {
        Fullbright() { super("Fullbright", Category.WORLD); }
        @Override public void tick() {
            var p = mc().player;
            var i = p.getStatusEffect(StatusEffects.NIGHT_VISION);
            if (i == null || i.getDuration() < 600)
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1200, 0, false, false, false));
        }
        @Override public void onDisable() {
            if (mc().player != null) mc().player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }

    static class Zoom extends Module {
        Integer old;
        Zoom() { super("Zoom", Category.WORLD); }
        @Override public void tick() {
            var opt = mc().options.getFov();
            if (EvelineClient.zoomKey.isPressed()) {
                if (old == null) old = opt.getValue();
                opt.setValue(30);
            } else if (old != null) {
                opt.setValue(old);
                old = null;
            }
        }
        @Override public void onDisable() {
            if (old != null) { mc().options.getFov().setValue(old); old = null; }
        }
    }

    static class ClearWeather extends Module {
        ClearWeather() { super("ClearWeather", Category.WORLD); }
        @Override public void tick() {
            mc().world.setRainGradient(0f);
            mc().world.setThunderGradient(0f);
        }
    }

    static class AutoSprint extends Module {
        AutoSprint() { super("AutoSprint", Category.WORLD); }
        @Override public void tick() {
            var p = mc().player;
            if (mc().options.forwardKey.isPressed() && !p.horizontalCollision && !p.isSneaking()
                && p.getHungerManager().getFoodLevel() > 6) p.setSprinting(true);
        }
    }

    static class FriendProtect extends Module {
        FriendProtect() { super("FriendProtect", Category.WORLD); }
    }
}
