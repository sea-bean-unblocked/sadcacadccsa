package com.echoclone.client;

import com.echoclone.network.ModNetworking;
import com.echoclone.network.RemoveClonePacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.echoclone.EchoCloneMod;

/**
 * Clone Manager GUI — custom branded UI for mr APPLE's Echo Clone mod.
 * Open with G key.
 */
public class CloneManagerScreen extends Screen {

    private static final int W = 300;
    private static final int H = 240;

    // Colors
    private static final int COL_BG        = 0xEE0d0d1a;
    private static final int COL_PANEL     = 0xCC141428;
    private static final int COL_ACCENT    = 0xFF5555ff;
    private static final int COL_ACCENT2   = 0xFF00ccff;
    private static final int COL_DARK_LINE = 0xFF2a2a5a;
    private static final int COL_WHITE     = 0xFFFFFFFF;
    private static final int COL_GREY      = 0xFFAAAAAA;
    private static final int COL_YELLOW    = 0xFFFFDD44;
    private static final int COL_GREEN     = 0xFF44FF88;
    private static final int COL_RED       = 0xFFFF5555;

    private float animTick = 0f;

    public CloneManagerScreen() {
        super(Component.translatable("echoclone.gui.title"));
    }

    @Override
    protected void init() {
        int cx = (this.width - W) / 2;
        int cy = (this.height - H) / 2;

        // ◀ prev skin
        this.addRenderableWidget(Button.builder(Component.literal("◀"), btn -> {
            ClientSkinManager.prev();
        }).pos(cx + 16, cy + 105).size(24, 22).build());

        // ▶ next skin
        this.addRenderableWidget(Button.builder(Component.literal("▶"), btn -> {
            ClientSkinManager.next();
        }).pos(cx + W - 40, cy + 105).size(24, 22).build());

        // Remove nearest
        this.addRenderableWidget(Button.builder(
                Component.translatable("echoclone.gui.remove_nearest"),
                btn -> {
                    ModNetworking.sendToServer(new RemoveClonePacket(RemoveClonePacket.Mode.NEAREST, -1));
                    this.onClose();
                }
        ).pos(cx + 16, cy + 165).size(126, 20).build());

        // Remove all
        this.addRenderableWidget(Button.builder(
                Component.translatable("echoclone.gui.remove_all"),
                btn -> {
                    ModNetworking.sendToServer(new RemoveClonePacket(RemoveClonePacket.Mode.ALL, -1));
                    this.onClose();
                }
        ).pos(cx + W - 142, cy + 165).size(126, 20).build());

        // Close
        this.addRenderableWidget(Button.builder(
                Component.literal("✕  Close"),
                btn -> this.onClose()
        ).pos(cx + W / 2 - 50, cy + 200).size(100, 20).build());
    }

    @Override
    public void tick() {
        animTick += 0.04f;
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float pt) {
        // Dim background
        this.renderBackground(g);

        int cx = (this.width - W) / 2;
        int cy = (this.height - H) / 2;

        // Outer shadow
        g.fill(cx - 2, cy - 2, cx + W + 2, cy + H + 2, 0x88000000);

        // Main panel
        g.fill(cx, cy, cx + W, cy + H, COL_BG);

        // Animated top accent bar (color pulse)
        int pulse = (int)(Math.abs(Math.sin(animTick)) * 80) + 120;
        int accentColor = 0xFF000000 | (pulse << 16) | (pulse / 2) | (255);
        g.fill(cx, cy, cx + W, cy + 3, COL_ACCENT);
        g.fill(cx, cy + H - 3, cx + W, cy + H, COL_ACCENT2);
        g.fill(cx, cy, cx + 3, cy + H, COL_ACCENT);
        g.fill(cx + W - 3, cy, cx + W, cy + H, COL_ACCENT2);

        // Title background strip
        g.fill(cx + 3, cy + 3, cx + W - 3, cy + 28, COL_PANEL);

        // Title text
        g.drawCenteredString(this.font,
                Component.literal("§b§lECHO CLONE §r§7| §e§lMOD MANAGER"),
                cx + W / 2, cy + 10, COL_WHITE);

        // Made by line
        g.drawCenteredString(this.font,
                Component.literal("§8Made by §6§lmr APPLE"),
                cx + W / 2, cy + 22, 0xFFAA8800);

        // Divider
        g.fill(cx + 10, cy + 32, cx + W - 10, cy + 33, COL_DARK_LINE);

        // --- SKIN SELECTOR SECTION ---
        g.fill(cx + 10, cy + 38, cx + W - 10, cy + 140, COL_PANEL);
        g.fill(cx + 10, cy + 38, cx + W - 10, cy + 39, COL_ACCENT);

        g.drawCenteredString(this.font,
                Component.literal("§7▸ Clone Skin"),
                cx + W / 2, cy + 43, COL_GREY);

        // Skin name with index dots
        String skinName = ClientSkinManager.getSelectedName();
        int idx = ClientSkinManager.getSelectedIndex();
        int total = ClientSkinManager.getSkinCount();

        g.drawCenteredString(this.font,
                Component.literal("§b§l" + skinName),
                cx + W / 2, cy + 108, COL_WHITE);

        // Dot indicators
        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < total; i++) {
            dots.append(i == idx ? "§b● " : "§8○ ");
        }
        g.drawCenteredString(this.font,
                Component.literal(dots.toString().trim()),
                cx + W / 2, cy + 122, COL_WHITE);

        // Skin preview box
        g.fill(cx + 50, cy + 54, cx + W - 50, cy + 100, 0xFF0a0a1a);
        g.fill(cx + 50, cy + 54, cx + W - 50, cy + 55, COL_ACCENT2);
        g.fill(cx + 50, cy + 99, cx + W - 50, cy + 100, COL_ACCENT);

        // Draw skin texture as face preview (head region of skin = top-left 8x8 of 64x64)
        ResourceLocation tex = ClientSkinManager.getSelected().texture();
        RenderSystem.setShaderTexture(0, tex);
        // Draw face: UV 8/64 to 16/64, 8/64 to 16/64 -> scaled to 40x40
        g.blit(tex, cx + W/2 - 20, cy + 57, 40, 40,
                8f, 8f, 8, 8, 64, 64);

        // Skin label below preview
        g.drawCenteredString(this.font,
                Component.literal("§7" + (idx + 1) + " / " + total),
                cx + W / 2, cy + 133, COL_GREY);

        // --- RECORD HINT ---
        g.fill(cx + 10, cy + 145, cx + W - 10, cy + 160, COL_PANEL);
        boolean isRec = RecordingManager.isRecording();
        String recStatus = isRec
                ? "§c● RECORDING §7(" + RecordingManager.getRecordedTickCount() + " ticks) §8— Press §eR §8to stop"
                : "§7Press §eR §7to start recording, §eR §7again to spawn clone";
        g.drawCenteredString(this.font,
                Component.literal(recStatus),
                cx + W / 2, cy + 151, COL_WHITE);

        // Divider before buttons
        g.fill(cx + 10, cy + 160, cx + W - 10, cy + 161, COL_DARK_LINE);

        // Section label
        g.drawCenteredString(this.font,
                Component.literal("§7▸ Manage Clones"),
                cx + W / 2, cy + 148 - 5, COL_GREY);

        // Footer branding
        g.drawCenteredString(this.font,
                Component.literal("§8Echo Clone v1.0.0  §5|  §8github: mr APPLE"),
                cx + W / 2, cy + H - 8, 0xFF555577);

        super.render(g, mx, my, pt);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
