package dev.evelinevisuals;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

final class Config {
    static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("evelinevisuals.properties");
    static final List<String> DEFAULT_ON = List.of(
        "Watermark", "ModList", "Coordinates", "FpsPing", "ArmorHud", "PotionHud",
        "TargetHud", "AutoSprint", "HitParticles", "CritEffect", "HitSound", "Keystrokes", "TotemCounter");

    static String key(Module m) { return "m." + m.name; }

    static void load() {
        try {
            if (!Files.exists(FILE)) {
                for (Module m : Modules.ALL) m.enabled = DEFAULT_ON.contains(m.name);
                return;
            }
            Properties p = new Properties();
            try (var in = Files.newInputStream(FILE)) { p.load(in); }
            for (Module m : Modules.ALL) m.enabled = Boolean.parseBoolean(p.getProperty(key(m), "false"));
            for (String f : p.getProperty("friends", "").split(",")) if (!f.isBlank()) Friends.SET.add(f.toLowerCase());
        } catch (Exception ignored) {}
    }

    static void save() {
        try {
            Properties p = new Properties();
            for (Module m : Modules.ALL) p.setProperty(key(m), String.valueOf(m.enabled));
            p.setProperty("friends", String.join(",", Friends.SET));
            try (var out = Files.newOutputStream(FILE)) { p.store(out, "EvelineVisuals"); }
        } catch (Exception ignored) {}
    }
}
