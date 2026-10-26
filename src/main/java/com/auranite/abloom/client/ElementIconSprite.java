package com.auranite.abloom.client;

import net.minecraft.resources.ResourceLocation;

/**
 * Lightweight handle for a convention-based element icon texture that lives OUTSIDE any
 * stitched atlas (a raw PNG at an arbitrary {@link ResourceLocation}).
 * <p>
 * This intentionally does NOT extend {@code TextureAtlasSprite}: on NeoForge 1.21.1 the
 * sprite constructor requires a {@code SpriteContents}, which would force us to stitch or
 * manually upload the texture. Instead the render utilities bind {@link #textureLocation()}
 * directly as a plain GUI texture ({@code RenderSystem.setShaderTexture}) and draw a full-UV
 * (0..1) quad with the {@code Gui.BLOCK_TEXTURE} render type.
 */
public record ElementIconSprite(ResourceLocation textureLocation) {

    /** Full-texture UV corners (the PNG itself is the whole "atlas"). */
    public float getU0() { return 0.0F; }

    public float getU1() { return 1.0F; }

    public float getV0() { return 0.0F; }

    public float getV1() { return 1.0F; }
}
