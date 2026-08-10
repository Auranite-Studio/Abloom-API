package com.yanghao.effect_display.gui.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class RelativeScreen extends Screen {
   protected static final Gson GSON = (new GsonBuilder()).create();
   private final ResourceLocation stylePath;
   protected JsonObject style;
   protected final Map<String, WidgetPosition> widgetPositions = new HashMap();
   protected int leftPos;
   protected int topPos;

   protected RelativeScreen(Component title, ResourceLocation stylePath) {
      super(title);
      this.stylePath = stylePath;
   }

   protected void init() {
      this.leftPos = 0;
      this.topPos = 0;
      Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(this.stylePath);
      if (resource.isPresent()) {
         try {
            InputStream inputStream = ((Resource)resource.get()).open();

            try {
               BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

               try {
                  this.style = (JsonObject)GSON.fromJson(reader, JsonObject.class);
               } catch (Throwable var8) {
                  try {
                     reader.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }

                  throw var8;
               }

               reader.close();
            } catch (Throwable var9) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var6) {
                     var9.addSuppressed(var6);
                  }
               }

               throw var9;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }

      this.loadWidgetPositions();
   }

   private void loadWidgetPositions() {
      if (this.style.has("widgets")) {
         JsonObject widgets = this.style.getAsJsonObject("widgets");

         for(String key : widgets.keySet()) {
            JsonObject widget = widgets.getAsJsonObject(key);
            WidgetPosition position = new WidgetPosition();
            if (widget.has("left")) {
               position.left = widget.get("left").getAsInt();
            }

            if (widget.has("top")) {
               position.top = widget.get("top").getAsInt();
            }

            if (widget.has("right")) {
               position.right = widget.get("right").getAsInt();
            }

            if (widget.has("bottom")) {
               position.bottom = widget.get("bottom").getAsInt();
            }

            if (widget.has("width")) {
               position.width = widget.get("width").getAsInt();
            }

            if (widget.has("height")) {
               position.height = widget.get("height").getAsInt();
            }

            if (widget.has("left_percent")) {
               position.leftPercent = widget.get("left_percent").getAsDouble();
            }

            if (widget.has("top_percent")) {
               position.topPercent = widget.get("top_percent").getAsDouble();
            }

            if (widget.has("bottom_percent")) {
               position.bottomPercent = widget.get("bottom_percent").getAsDouble();
            }

            if (widget.has("width_percent")) {
               position.widthPercent = widget.get("width_percent").getAsDouble();
            }

            if (widget.has("height_percent")) {
               position.heightPercent = widget.get("height_percent").getAsDouble();
            }

            if (widget.has("center_x_percent")) {
               position.centerXPercent = widget.get("center_x_percent").getAsDouble();
            }

            if (widget.has("center_y_percent")) {
               position.centerYPercent = widget.get("center_y_percent").getAsDouble();
            }

            this.widgetPositions.put(key, position);
         }
      }

   }

   protected WidgetPosition getWidgetPosition(String id) {
      return (WidgetPosition)this.widgetPositions.getOrDefault(id, new WidgetPosition());
   }

   protected static class WidgetPosition {
      public int left = 0;
      public int top = 0;
      public int right = -1;
      public int bottom = -1;
      public int width = 0;
      public int height = 0;
      public double leftPercent = (double)-1.0F;
      public double topPercent = (double)-1.0F;
      public double bottomPercent = (double)-1.0F;
      public double widthPercent = (double)-1.0F;
      public double heightPercent = (double)-1.0F;
      public double centerXPercent = (double)-1.0F;
      public double centerYPercent = (double)-1.0F;

      public int getX(int screenWidth) {
         return this.getX(screenWidth, this.getWidth(screenWidth));
      }

      public int getX(int screenWidth, int actualWidth) {
         if (this.centerXPercent >= (double)0.0F) {
            return (int)((double)screenWidth * this.centerXPercent - (double)actualWidth / (double)2.0F);
         } else if (this.leftPercent >= (double)0.0F) {
            return (int)((double)screenWidth * this.leftPercent);
         } else {
            return this.right >= 0 ? screenWidth - this.right - actualWidth : this.left;
         }
      }

      public int getY(int screenHeight) {
         return this.getY(screenHeight, this.getHeight(screenHeight));
      }

      public int getY(int screenHeight, int actualHeight) {
         if (this.centerYPercent >= (double)0.0F) {
            return (int)((double)screenHeight * this.centerYPercent - (double)actualHeight / (double)2.0F);
         } else if (this.topPercent >= (double)0.0F) {
            return (int)((double)screenHeight * this.topPercent);
         } else if (this.bottomPercent >= (double)0.0F) {
            return (int)((double)screenHeight * ((double)1.0F - this.bottomPercent)) - actualHeight;
         } else {
            return this.bottom >= 0 ? screenHeight - this.bottom - actualHeight : this.top;
         }
      }

      public int getWidth(int containerWidth) {
         return this.widthPercent >= (double)0.0F ? (int)((double)containerWidth * this.widthPercent) : this.width;
      }

      public int getHeight(int containerHeight) {
         if (this.heightPercent >= (double)0.0F) {
            return (int)((double)containerHeight * this.heightPercent);
         } else {
            return this.topPercent >= (double)0.0F && this.bottomPercent >= (double)0.0F ? (int)((double)containerHeight * ((double)1.0F - this.topPercent - this.bottomPercent)) : this.height;
         }
      }
   }
}
