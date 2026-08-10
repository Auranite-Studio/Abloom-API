package com.yanghao.effect_display.gui.config;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class ConfigLabel extends AbstractWidget {
   private final Supplier<String> textSupplier;
   private int backgroundColor;

   public ConfigLabel(int x, int y, int width, int height, Supplier<String> textSupplier) {
      this(x, y, width, height, textSupplier, 0);
   }

   public ConfigLabel(int x, int y, int width, int height, Supplier<String> textSupplier, int backgroundColor) {
      super(x, y, width, height, Component.empty());
      this.textSupplier = textSupplier;
      this.backgroundColor = backgroundColor;
   }

   public void setBackgroundColor(int backgroundColor) {
      this.backgroundColor = backgroundColor;
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.backgroundColor != 0) {
         guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.backgroundColor);
      }

      guiGraphics.drawString(Minecraft.getInstance().font, (String)this.textSupplier.get(), this.getX(), this.getY(), -1, false);
   }

   protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
      narrationElementOutput.add(NarratedElementType.TITLE, Component.literal((String)this.textSupplier.get()));
   }
}
