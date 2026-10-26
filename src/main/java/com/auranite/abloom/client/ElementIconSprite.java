package com.auranite.abloom.client;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * A lightweight {@link TextureAtlasSprite} stand-in that carries the UV coordinates of a
 * single full-size texture located at an arbitrary {@link ResourceLocation} (not necessarily
 * stitched into an atlas).
 * <p>
 * The icon is rendered by binding the sprite's own {@code atlasLocation()} as a regular
 * texture ({@code RenderSystem.setShaderTexture(0, atlas)}) and drawing a quad with these
 * UVs — see {@code EffectRenderUtil#drawCustomSprite}. Because the "atlas" contains exactly
 * one image, the UVs span the whole 0..1 range.
 */
public class ElementIconSprite extends TextureAtlasSprite {

    /** Vanilla's missing-texture sprite name — triggers NEAREST_STEF (crisp) filtering. */
    private static final String MISSING_NO_TEXTURE = "missingno";

    public ElementIconSprite(ResourceLocation textureLocation) {
        // name, width, height, pixelsPerUnit: vanilla defaults for a plain sprite
        super(MISSING_NO_TEXTURE, textureLocation, 64, 64, 64, 64, false);
    }

    /** Full-texture UV corners. */
    @Override
    public float getU0() { return 0.0F; }

    @Override
    public float getU1() { return 1.0F; }

    @Override
    public float getV0() { return 0.0F; }

    @Override
    public float getV1() { return 1.0F; }
}
