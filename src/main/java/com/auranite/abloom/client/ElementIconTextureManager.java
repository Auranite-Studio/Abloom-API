package com.auranite.abloom.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.auranite.abloom.AbloomMod;
import com.auranite.abloom.util.IElementalType;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * Resolves element icon textures by naming convention: an element named {@code FIRE} maps to
 * {@code abloom:textures/gui/element_icons/fire.png}, and a custom element registered by
 * another mod (with namespaced damage type id, e.g. {@code yourmod:plasma_dmg}) maps to
 * {@code yourmod:textures/gui/element_icons/plasma.png}.
 * <p>
 * Textures are loaded from any active resource pack (mod jar or user resource pack) on first
 * use and cached per ResourceLocation. Missing files simply yield null so callers can fall
 * back to effect sprites or colored squares.
 */
public class ElementIconTextureManager {

    /** Cache keyed by texture location; a null value means "checked, file not found". */
    private static final Map<ResourceLocation, TextureAtlasSprite> CACHE = new ConcurrentHashMap<>();

    /**
     * Returns the icon sprite for the given elemental type, or null if no texture exists.
     * The lookup path is derived from {@link IElementalType#getElementIconTexture()}:
     * "{namespace}:gui/element_icons/{name_lowercase}" ->
     * "{namespace}/textures/gui/element_icons/{name_lowercase}.png".
     */
    public static TextureAtlasSprite getIcon(IElementalType type) {
        if (type == null) return null;
        return getIcon(type.getElementIconTexture());
    }

    /** Convenience overload: resolves an icon for any explicit texture location. */
    public static TextureAtlasSprite getIcon(ResourceLocation textureLocation) {
        if (textureLocation == null) return null;
        TextureAtlasSprite cached = CACHE.get(textureLocation);
        if (cached != null) return cached;
        if (CACHE.containsKey(textureLocation)) return null; // negative cache hit
        TextureAtlasSprite sprite = load(textureLocation);
        CACHE.put(textureLocation, sprite);
        return sprite;
    }

    private static TextureAtlasSprite load(ResourceLocation loc) {
        Minecraft minecraft = Minecraft.getInstance();
        try {
            var resources = minecraft.getResourceManager();
            var optional = resources.getResource(loc.withPrefix("textures/").withSuffix(".png"));
            if (optional.isEmpty()) return null;
            Resource resource = optional.get();
            NativeImage image;
            try (var in = resource.open()) {
                image = NativeImage.read(in);
            }
            String name = loc.getPath().replace('/', '_');
            TextureAtlasSprite sprite = new TextureAtlasSprite(name, image.getWidth(), image.getHeight(),
                    false, 1, 1, FilterMode.NEAREST, FilterMode.NEAREST);
            sprite.upload(0, 0, 0, image);
            return sprite;
        } catch (Exception e) {
            AbloomMod.LOGGER.debug("Failed to load element icon texture '{}': {}", loc, e.getMessage());
            return null;
        }
    }

    /** Clears the cache (e.g. on resource reload). */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * Detects a resource pack (re)load by comparing the identity of the active
     * {@link net.minecraft.server.packs.resources.ResourceManager} instance and drops the sprite
     * cache so textures from newly applied resource packs are picked up. Safe to call per frame.
     */
    public static void clearCacheIfReloaded() {
        try {
            var manager = Minecraft.getInstance().getResourceManager();
            if (manager != lastManager) {
                lastManager = manager;
                CACHE.clear();
            }
        } catch (Exception ignored) {
        }
    }

    private static net.minecraft.server.packs.resources.ResourceManager lastManager;
}
