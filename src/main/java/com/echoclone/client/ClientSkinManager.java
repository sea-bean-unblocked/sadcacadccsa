package com.echoclone.client;

import net.minecraft.resources.ResourceLocation;
import com.echoclone.EchoCloneMod;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages available clone skins on the client side.
 * All skins ship bundled with the mod - no internet required.
 */
public class ClientSkinManager {

    public record SkinEntry(String id, String displayName, ResourceLocation texture) {}

    public static final List<SkinEntry> SKINS = new ArrayList<>();

    static {
        SKINS.add(new SkinEntry("dream",       "Dream",        skin("dream_skin")));
        SKINS.add(new SkinEntry("technoblade", "Technoblade",  skin("technoblade_skin")));
        SKINS.add(new SkinEntry("notch",       "Notch",        skin("notch_skin")));
        SKINS.add(new SkinEntry("jeb",         "jeb_",         skin("jeb_skin")));
        SKINS.add(new SkinEntry("mrbeast",     "MrBeast",      skin("mrbeast_skin")));
        SKINS.add(new SkinEntry("skeppy",      "Skeppy",       skin("skeppy_skin")));
        SKINS.add(new SkinEntry("bbh",         "BadBoyHalo",   skin("bbh_skin")));
        SKINS.add(new SkinEntry("steve",       "Steve",        skin("random_skin")));
    }

    private static ResourceLocation skin(String name) {
        return new ResourceLocation(EchoCloneMod.MOD_ID,
                "textures/entity/" + name + ".png");
    }

    private static int selectedIndex = 0;

    public static SkinEntry getSelected() {
        return SKINS.get(selectedIndex);
    }

    public static String getSelectedSkinId() {
        return SKINS.get(selectedIndex).id();
    }

    public static String getSelectedName() {
        return SKINS.get(selectedIndex).displayName();
    }

    public static void next() {
        selectedIndex = (selectedIndex + 1) % SKINS.size();
    }

    public static void prev() {
        selectedIndex = (selectedIndex - 1 + SKINS.size()) % SKINS.size();
    }

    public static int getSelectedIndex() {
        return selectedIndex;
    }

    public static int getSkinCount() {
        return SKINS.size();
    }

    public static ResourceLocation getSkinTexture(String skinId) {
        return SKINS.stream()
                .filter(s -> s.id().equals(skinId))
                .map(SkinEntry::texture)
                .findFirst()
                .orElse(skin("random_skin"));
    }
}
