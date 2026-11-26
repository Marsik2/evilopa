package dev.evelinevisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

final class State {
    static LivingEntity lastTarget;
    static long lastHitTime;
    static int combo;
    static double lastReach;
    static boolean killDone = true;

    static double speed, px, pz;
    static boolean hasPrev;
    static final Deque<Long> clicks = new ArrayDeque<>();
    static boolean wasAttack;
    static Vec3d lastPos, deathPos;

    static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p == null) return;

        if (hasPrev) speed = Math.hypot(p.getX() - px, p.getZ() - pz) * 20.0;
        px = p.getX(); pz = p.getZ(); hasPrev = true;

        boolean atk = mc.options.attackKey.isPressed();
        if (atk && !wasAttack) clicks.addLast(System.currentTimeMillis());
        wasAttack = atk;

        if (p.getHealth() <= 0 || p.isDead()) {
            if (lastPos != null) { deathPos = lastPos; lastPos = null; }
        } else {
            lastPos = p.getPos();
        }

        if (p.hurtTime > 0) combo = 0;
        if (System.currentTimeMillis() - lastHitTime > 3000) combo = 0;
    }

    static int cps() {
        long n = System.currentTimeMillis();
        while (!clicks.isEmpty() && n - clicks.peekFirst() > 1000) clicks.pollFirst();
        return clicks.size();
    }

    static void onAttack(Entity e) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (e instanceof LivingEntity l) { lastTarget = l; killDone = false; }
        lastHitTime = System.currentTimeMillis();
        combo++;
        if (mc.player != null) lastReach = mc.player.distanceTo(e);
    }

    static LivingEntity hudTarget() {
        if (lastTarget != null && System.currentTimeMillis() - lastHitTime < 5000 && lastTarget.isAlive()) return lastTarget;
        Entity e = MinecraftClient.getInstance().targetedEntity;
        return e instanceof LivingEntity l ? l : null;
    }
}
