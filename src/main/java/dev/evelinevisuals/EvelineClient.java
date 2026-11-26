package dev.evelinevisuals;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.lwjgl.glfw.GLFW;

public class EvelineClient implements ClientModInitializer {
    public static KeyBinding guiKey, zoomKey;

    @Override
    public void onInitializeClient() {
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evelinevisuals.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "category.evelinevisuals"));
        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.evelinevisuals.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.evelinevisuals"));

        Modules.init();
        Config.load();

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) mc.setScreen(new ClickGuiScreen());
            if (mc.player == null || mc.world == null) return;
            State.tick();
            for (Module m : Modules.ALL) if (m.enabled) m.tick();
        });

        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden) return;
            Modules.renderAll(ctx, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (!world.isClient) return ActionResult.PASS;
            if (Modules.FRIENDS.enabled && Friends.is(entity.getName().getString())) return ActionResult.FAIL;
            State.onAttack(entity);
            for (Module m : Modules.ALL) if (m.enabled) m.onAttack(entity);
            return ActionResult.PASS;
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registry) -> dispatcher.register(
            ClientCommandManager.literal("friend")
                .then(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(ctx -> {
                        String n = StringArgumentType.getString(ctx, "name");
                        Friends.SET.add(n.toLowerCase()); Config.save();
                        ctx.getSource().sendFeedback(Text.literal("§aДрузья: добавлен " + n));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("name", StringArgumentType.word()).executes(ctx -> {
                        String n = StringArgumentType.getString(ctx, "name");
                        Friends.SET.remove(n.toLowerCase()); Config.save();
                        ctx.getSource().sendFeedback(Text.literal("§cДрузья: удалён " + n));
                        return 1;
                    })))
                .then(ClientCommandManager.literal("list").executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal("Друзья: " + String.join(", ", Friends.SET)));
                    return 1;
                }))));
    }
}
