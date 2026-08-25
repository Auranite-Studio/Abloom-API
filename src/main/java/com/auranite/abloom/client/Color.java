package com.auranite.abloom.client;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public record Color(float r, float g, float b, float a) {
   private static final Pattern pattern = Pattern.compile("#?([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})([0-9A-F]{2})?", 2);

   public Color(int value) {
      this(Math.max(0.0F, (float)(value >> 16 & 255) / 255.0F), Math.max(0.0F, (float)(value >> 8 & 255) / 255.0F), Math.max(0.0F, (float)(value & 255) / 255.0F), Math.max(0.0F, (float)(value >> 24 & 255) / 255.0F));
   }

   public Color(java.awt.@NotNull Color color) {
      this(color.getRGB());
   }

   public int getValue() {
      int value = 0;
      value |= convertComponent(this.a) << 24;
      value |= convertComponent(this.r) << 16;
      value |= convertComponent(this.g) << 8;
      value |= convertComponent(this.b);
      return value;
   }

   public java.awt.@NotNull Color toNativeColor() {
      return new java.awt.Color(this.getValue(), true);
   }

   public @NotNull String toString() {
      return this.a == 1.0F ? String.format("#%02X%02X%02X", convertComponent(this.r), convertComponent(this.g), convertComponent(this.b)) : String.format("#%02X%02X%02X%02X", convertComponent(this.r), convertComponent(this.g), convertComponent(this.b), convertComponent(this.a));
   }

   public static @NotNull Color valueOf(@NotNull String string) {
      Objects.requireNonNull(string, "Argument 'string'");
      Matcher matcher = pattern.matcher(string);
      if (!matcher.matches()) {
         throw new IllegalArgumentException("Invalid color code syntax");
      } else {
         String alpha = matcher.group(4);
         int value;
         if (alpha != null) {
            value = parseHexComponent(alpha) << 24;
         } else {
            value = -16777216;
         }

         value |= parseHexComponent(matcher.group(1)) << 16;
         value |= parseHexComponent(matcher.group(2)) << 8;
         value |= parseHexComponent(matcher.group(3));
         return new Color(value);
      }
   }

   public static @NotNull Color lerp(@NotNull Color a, @NotNull Color b, float delta) {
      Objects.requireNonNull(a, "Argument 'a'");
      Objects.requireNonNull(b, "Argument 'b'");
      return new Color(a.r + (b.r - a.r) * delta, a.g + (b.g - a.g) * delta, a.b + (b.b - a.b) * delta, a.a + (b.a - a.a) * delta);
   }

   private static int parseHexComponent(String input) {
      return Integer.parseUnsignedInt(input, 16);
   }

   private static int convertComponent(float component) {
      return Math.max(Math.min((int)(component * 255.0F), 255), 0);
   }
}
