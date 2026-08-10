package com.yanghao.effect_display.gui.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yanghao.effect_display.EffectDisplayConfig;
import com.yanghao.effect_display.network.ClientEntityEffectsStorage;
import com.yanghao.effect_display.utils.EffectRenderUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@OnlyIn(Dist.CLIENT)
public class RenderConfigScreen extends RelativeScreen {
   private static final Logger log = LoggerFactory.getLogger(RenderConfigScreen.class);
   private ConfigSnapshot originalSnapshot;
   private LivingEntity previewEntity;
   private ExtendedSlider entitySizeSlider;
   private ExtendedSlider scaleSlider;
   private Checkbox selfPotionCheckbox;
   private Checkbox otherPotionCheckbox;
   private Checkbox blinkPotionCheckbox;
   private ExtendedSlider maxDistanceSlider;
   private ExtendedSlider maxDistanceNoLoSslider;
   private float previewYaw = 0.0F;
   private float entitySize = 60.0F;
   private long rotationStartTime = 0L;
   private static final float ROTATION_SPEED = 36.0F;
   private PoseStack poseStack;
   private static final double POSITION_STEP = 0.1;
   private int previewLeft;
   private int previewTop;
   private int previewWidth;
   private int previewHeight;
   private RelativeScreen.WidgetPosition previewEntityPos;
   private int selectedEntityIndex = 0;
   private final List<EntityType<?>> previewEntities = getAllEntityTypes();
   private CycleButton<Integer> entitySelectorButton;

   public RenderConfigScreen(Component title) {
      super(title, ResourceLocation.fromNamespaceAndPath("effect_display", "screen/render_config.json"));
   }

   @OnlyIn(Dist.CLIENT)
   protected void init() {
      super.init();
      this.initializeBasicSettings();
      this.calculatePreviewArea();
      this.addPositionControlSection();
      CycleButton<Integer> entitySelector = this.createEntitySelector();
      this.addScaleSliders();
      this.addDistanceSliders();
      this.createPreviewEntity(entitySelector);
      this.addOptionCheckboxes(entitySelector);
      this.addBottomButtons();
   }

   private void initializeBasicSettings() {
      this.rotationStartTime = System.currentTimeMillis();
      this.originalSnapshot = RenderConfigScreen.ConfigSnapshot.capture();
   }

   private void calculatePreviewArea() {
      RelativeScreen.WidgetPosition previewPanePos = this.getWidgetPosition("preview_pane");
      this.previewWidth = previewPanePos.getWidth(this.width);
      this.previewHeight = previewPanePos.getHeight(this.height);
      this.previewLeft = previewPanePos.getX(this.width);
      this.previewTop = previewPanePos.getY(this.height);
      this.previewEntityPos = this.getWidgetPosition("preview_entity");
   }

   private void addPositionControlSection() {
      this.addControlSection(() -> (Double)EffectDisplayConfig.HORIZONTAL_OFFSET.get(), (v) -> EffectDisplayConfig.HORIZONTAL_OFFSET.set(v), () -> (Double)EffectDisplayConfig.VERTICAL_OFFSET.get(), (v) -> EffectDisplayConfig.VERTICAL_OFFSET.set(v));
   }

   private CycleButton<Integer> createEntitySelector() {
      RelativeScreen.WidgetPosition previewEntityCycleButtonPos = this.getWidgetPosition("preview_entity_cycle_button");
      return this.addEntitySelector(previewEntityCycleButtonPos.getX(this.width), previewEntityCycleButtonPos.getY(this.height), previewEntityCycleButtonPos);
   }

   private void addScaleSliders() {
      RelativeScreen.WidgetPosition previewEntityScaleExtendedSliderPos = this.getWidgetPosition("preview_entity_scale_extended_slider");
      int sliderWidth = previewEntityScaleExtendedSliderPos.getWidth(this.width);
      int sliderHeight = previewEntityScaleExtendedSliderPos.getHeight(this.height);
      this.entitySizeSlider = new ExtendedSlider(previewEntityScaleExtendedSliderPos.getX(this.width) - sliderWidth / 2, previewEntityScaleExtendedSliderPos.getY(this.height) - sliderHeight / 2, sliderWidth, sliderHeight, Component.translatable("effect_display.render_config.preview_entity_scale"), Component.literal(""), 0.1, (double)2.0F, (double)1.0F, 0.01, 4, true) {
         protected void applyValue() {
            RenderConfigScreen.this.entitySize = (float)((double)60.0F * this.getValue());
         }
      };
      this.addRenderableWidget(this.entitySizeSlider);
      RelativeScreen.WidgetPosition buffScaleExtendedSliderPos = this.getWidgetPosition("buff_scale_extended_slider");
      this.scaleSlider = new ExtendedSlider(buffScaleExtendedSliderPos.getX(this.width) - sliderWidth / 2, buffScaleExtendedSliderPos.getY(this.height) - sliderHeight / 2, sliderWidth, sliderHeight, Component.translatable("effect_display.render_config.buff_scale"), Component.literal(""), 0.1, (double)2.0F, (Double)EffectDisplayConfig.RENDER_SCALE.get(), 0.01, 4, true) {
         protected void applyValue() {
            EffectDisplayConfig.RENDER_SCALE.set(this.getValue());
         }
      };
      this.addRenderableWidget(this.scaleSlider);
   }

   private void addOptionCheckboxes(CycleButton<Integer> entitySelector) {
      RelativeScreen.WidgetPosition rotationCheckboxPos = this.getWidgetPosition("entity_rotation_checkbox");
      int checkboxHeight = rotationCheckboxPos.getHeight(this.height);
      int unitW = Math.max(this.width / 40, 4);
      Checkbox rotationCheckbox = this.addEntityRotationCheckbox(entitySelector, checkboxHeight, unitW);
      this.addSelfPotionCheckbox(rotationCheckbox, checkboxHeight, unitW);
      this.addOtherPotionCheckbox(unitW);
      this.addBlinkPotionCheckbox(unitW);
   }

   private Checkbox addEntityRotationCheckbox(CycleButton<Integer> entitySelector, int checkboxHeight, int unitW) {
      MutableComponent text = Component.translatable("effect_display.render_config.entity_rotation");
      int checkboxWidth = Math.max(checkboxHeight * 3, this.font.width(text) + unitW);
      Checkbox entityRotationCheckbox = Checkbox.builder(text, this.font).pos(entitySelector.getX() + entitySelector.getWidth() + unitW / 2, entitySelector.getY()).maxWidth(checkboxWidth).onValueChange((button, checked) -> {
         if (!checked) {
            if (this.poseStack != null) {
               this.poseStack.mulPose((new Quaternionf()).rotateY(0.0F));
            }

            this.previewYaw = 0.0F;
            this.rotationStartTime = 0L;
         } else {
            this.rotationStartTime = System.currentTimeMillis();
         }

      }).selected(true).build();
      entityRotationCheckbox.setHeight(checkboxHeight);
      this.addRenderableWidget(entityRotationCheckbox);
      return entityRotationCheckbox;
   }

   private void addSelfPotionCheckbox(Checkbox previousCheckbox, int checkboxHeight, int unitW) {
      MutableComponent selfPotionText = Component.translatable("effect_display.render_config.self_potion");
      RelativeScreen.WidgetPosition selfPotionCheckboxPos = this.getWidgetPosition("self_potion_checkbox");
      int selfPotionHeight = selfPotionCheckboxPos.getHeight(this.height);
      int selfPotionCheckboxX = previousCheckbox.getX();
      int selfPotionCheckboxY = previousCheckbox.getY() + selfPotionHeight + unitW / 2;
      int selfPotionMaxWidth = this.width - (selfPotionCheckboxX + checkboxHeight + this.font.width(selfPotionText) + unitW / 2);
      int selfPotionWidth = Math.max(this.font.width(selfPotionText) + checkboxHeight, selfPotionMaxWidth);
      this.selfPotionCheckbox = Checkbox.builder(selfPotionText, this.font).pos(selfPotionCheckboxX, selfPotionCheckboxY).maxWidth(selfPotionWidth).onValueChange((button, checked) -> EffectDisplayConfig.SHOW_SELF_POTION.set(checked)).selected((Boolean)EffectDisplayConfig.SHOW_SELF_POTION.get()).build();
      this.selfPotionCheckbox.setHeight(selfPotionHeight);
      this.addRenderableWidget(this.selfPotionCheckbox);
   }

   private void addOtherPotionCheckbox(int unitW) {
      MutableComponent otherPotionText = Component.translatable("effect_display.render_config.other_potion");
      RelativeScreen.WidgetPosition otherPotionCheckboxPos = this.getWidgetPosition("other_potion_checkbox");
      int otherPotionHeight = otherPotionCheckboxPos.getHeight(this.height);
      int otherPotionCheckboxX = this.selfPotionCheckbox.getX();
      int otherPotionCheckboxY = this.selfPotionCheckbox.getY() + otherPotionHeight + unitW / 2;
      int otherPotionWidth = this.selfPotionCheckbox.getWidth();
      this.otherPotionCheckbox = Checkbox.builder(otherPotionText, this.font).pos(otherPotionCheckboxX, otherPotionCheckboxY).maxWidth(otherPotionWidth).onValueChange((button, checked) -> EffectDisplayConfig.SHOW_OTHER_POTION.set(checked)).selected((Boolean)EffectDisplayConfig.SHOW_OTHER_POTION.get()).build();
      this.otherPotionCheckbox.setHeight(otherPotionHeight);
      this.addRenderableWidget(this.otherPotionCheckbox);
   }

   private void addBlinkPotionCheckbox(int unitW) {
      MutableComponent blinkPotionText = Component.translatable("effect_display.render_config.blink_potion");
      RelativeScreen.WidgetPosition blinkPotionCheckboxPos = this.getWidgetPosition("blink_potion_checkbox");
      int blinkPotionHeight = blinkPotionCheckboxPos.getHeight(this.height);
      int blinkPotionCheckboxX = this.otherPotionCheckbox.getX();
      int blinkPotionCheckboxY = this.otherPotionCheckbox.getY() + blinkPotionHeight + unitW / 2;
      int blinkPotionWidth = this.otherPotionCheckbox.getWidth();
      this.blinkPotionCheckbox = Checkbox.builder(blinkPotionText, this.font).pos(blinkPotionCheckboxX, blinkPotionCheckboxY).maxWidth(blinkPotionWidth).onValueChange((button, checked) -> EffectDisplayConfig.BLINK_ON_LOW_DURATION.set(checked)).selected((Boolean)EffectDisplayConfig.BLINK_ON_LOW_DURATION.get()).build();
      this.blinkPotionCheckbox.setHeight(blinkPotionHeight);
      this.addRenderableWidget(this.blinkPotionCheckbox);
   }

   private void addDistanceSliders() {
      RelativeScreen.WidgetPosition maxDistanceSliderPos = this.getWidgetPosition("max_distance_slider");
      int sliderWidth = maxDistanceSliderPos.getWidth(this.width);
      int sliderHeight = maxDistanceSliderPos.getHeight(this.height);
      int sliderX = maxDistanceSliderPos.getX(this.width) - sliderWidth / 2;
      int sliderY = maxDistanceSliderPos.getY(this.height) - sliderHeight / 2;
      this.maxDistanceSlider = new ExtendedSlider(sliderX, sliderY, sliderWidth, sliderHeight, Component.translatable("effect_display.render_config.max_distance"), Component.literal(""), (double)8.0F, (double)128.0F, (Double)EffectDisplayConfig.MAX_DISTANCE.get(), (double)1.0F, 4, true) {
         protected void applyValue() {
            EffectDisplayConfig.MAX_DISTANCE.set(this.getValue());
         }
      };
      this.maxDistanceSlider.setTooltip(Tooltip.create(Component.translatable("effect_display.render_config.max_distance.tooltip")));
      this.addRenderableWidget(this.maxDistanceSlider);
      RelativeScreen.WidgetPosition maxDistanceNoLOSSliderPos = this.getWidgetPosition("max_distance_nolos_slider");
      int nolosWidth = maxDistanceNoLOSSliderPos.getWidth(this.width);
      int nolosHeight = maxDistanceNoLOSSliderPos.getHeight(this.height);
      int nolosX = maxDistanceNoLOSSliderPos.getX(this.width) - nolosWidth / 2;
      int nolosY = maxDistanceNoLOSSliderPos.getY(this.height) - nolosHeight / 2;
      this.maxDistanceNoLoSslider = new ExtendedSlider(nolosX, nolosY, nolosWidth, nolosHeight, Component.translatable("effect_display.render_config.max_distance_nolos"), Component.literal(""), (double)0.0F, (double)64.0F, (Double)EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.get(), (double)1.0F, 4, true) {
         protected void applyValue() {
            EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.set(this.getValue());
         }
      };
      this.maxDistanceNoLoSslider.setTooltip(Tooltip.create(Component.translatable("effect_display.render_config.max_distance_nolos.tooltip")));
      this.addRenderableWidget(this.maxDistanceNoLoSslider);
   }

   private void addBottomButtons() {
      RelativeScreen.WidgetPosition saveButtonPos = this.getWidgetPosition("button_save");
      RelativeScreen.WidgetPosition resetButtonPos = this.getWidgetPosition("button_reset");
      RelativeScreen.WidgetPosition cancelButtonPos = this.getWidgetPosition("button_cancel");
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.save"), (btn) -> {
         EffectDisplayConfig.savaGUIBuilder();
         this.originalSnapshot = RenderConfigScreen.ConfigSnapshot.capture();
      }).pos(saveButtonPos.getX(this.width) - saveButtonPos.getWidth(this.width) / 2, saveButtonPos.getY(this.height)).size(saveButtonPos.getWidth(this.width), saveButtonPos.getHeight(this.height)).build());
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.reset"), (btn) -> {
         this.resetToDefaults();
         this.syncUIFromConfig();
      }).pos(resetButtonPos.getX(this.width) - resetButtonPos.getWidth(this.width) / 2, resetButtonPos.getY(this.height)).size(resetButtonPos.getWidth(this.width), resetButtonPos.getHeight(this.height)).build());
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.cancel"), (btn) -> this.onClose()).pos(cancelButtonPos.getX(this.width) - cancelButtonPos.getWidth(this.width) / 2, cancelButtonPos.getY(this.height)).size(cancelButtonPos.getWidth(this.width), cancelButtonPos.getHeight(this.height)).build());
   }

   private void createPreviewEntity(CycleButton<Integer> cycleButton) {
      if (this.previewEntity != null) {
         ClientEntityEffectsStorage.removeEntityEffects(this.previewEntity.getId());
         this.previewEntity.discard();
         this.previewEntity = null;
      }

      int attempts = 0;
      int maxAttempts = this.previewEntities.size();
      int originalIndex = this.selectedEntityIndex;

      while(this.previewEntity == null && attempts < maxAttempts) {
         this.selectedEntityIndex = (originalIndex + attempts) % this.previewEntities.size();
         EntityType<?> entityType = (EntityType)this.previewEntities.get(this.selectedEntityIndex);
         if (this.minecraft != null && this.minecraft.level != null) {
            this.previewEntity = (LivingEntity)entityType.create(this.minecraft.level);
         }

         if (this.previewEntity == null) {
            ++attempts;
         }
      }

      if (cycleButton != null && this.previewEntity != null) {
         cycleButton.setValue(this.selectedEntityIndex);
      } else if (this.entitySelectorButton != null && this.previewEntity != null) {
         this.entitySelectorButton.setValue(this.selectedEntityIndex);
      }

      if (this.previewEntity != null) {
         if (EffectRenderUtil.isWarframeModLoaded()) {
         }

         ClientEntityEffectsStorage.updateEntityEffects(this.previewEntity.getId(), Arrays.asList(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, 0), new MobEffectInstance(MobEffects.JUMP, 100, 1)));
         this.previewEntity.setYRot(0.0F);
         this.previewEntity.setYBodyRot(0.0F);
      }

   }

   private void addControlSection(Supplier<Double> horizontalSupplier, Consumer<Double> horizontalConsumer, Supplier<Double> verticalSupplier, Consumer<Double> verticalConsumer) {
      RelativeScreen.WidgetPosition titlePos = this.getWidgetPosition("control_title");
      RelativeScreen.WidgetPosition btnUpPos = this.getWidgetPosition("control_btn_up");
      RelativeScreen.WidgetPosition btnDownPos = this.getWidgetPosition("control_btn_down");
      RelativeScreen.WidgetPosition btnLeftPos = this.getWidgetPosition("control_btn_left");
      RelativeScreen.WidgetPosition btnRightPos = this.getWidgetPosition("control_btn_right");
      RelativeScreen.WidgetPosition xLabelPos = this.getWidgetPosition("control_x_label");
      RelativeScreen.WidgetPosition yLabelPos = this.getWidgetPosition("control_y_label");
      this.addRenderableWidget(new ConfigLabel(titlePos.getX(this.width), titlePos.getY(this.height), titlePos.getWidth(this.width), titlePos.getHeight(this.height), () -> Component.translatable("effect_display.render_config.position_offset").getString()));
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.up"), (btn) -> this.adjustValue(verticalSupplier, verticalConsumer, 0.1)).pos(btnUpPos.getX(this.width), btnUpPos.getY(this.height)).size(btnUpPos.getWidth(this.width), btnUpPos.getHeight(this.height)).build());
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.down"), (btn) -> this.adjustValue(verticalSupplier, verticalConsumer, -0.1)).pos(btnDownPos.getX(this.width), btnDownPos.getY(this.height)).size(btnDownPos.getWidth(this.width), btnDownPos.getHeight(this.height)).build());
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.left"), (btn) -> this.adjustValue(horizontalSupplier, horizontalConsumer, -0.1)).pos(btnLeftPos.getX(this.width), btnLeftPos.getY(this.height)).size(btnLeftPos.getWidth(this.width), btnLeftPos.getHeight(this.height)).build());
      this.addRenderableWidget(Button.builder(Component.translatable("effect_display.render_config.right"), (btn) -> this.adjustValue(horizontalSupplier, horizontalConsumer, 0.1)).pos(btnRightPos.getX(this.width), btnRightPos.getY(this.height)).size(btnRightPos.getWidth(this.width), btnRightPos.getHeight(this.height)).build());
      this.addRenderableWidget(new ConfigLabel(xLabelPos.getX(this.width), xLabelPos.getY(this.height), xLabelPos.getWidth(this.width), xLabelPos.getHeight(this.height), () -> String.format(Component.translatable("effect_display.render_config.x_value").getString(), (double)Math.round((Double)horizontalSupplier.get() * (double)10000.0F) / (double)10000.0F)));
      this.addRenderableWidget(new ConfigLabel(yLabelPos.getX(this.width), yLabelPos.getY(this.height), yLabelPos.getWidth(this.width), yLabelPos.getHeight(this.height), () -> String.format(Component.translatable("effect_display.render_config.y_value").getString(), (double)Math.round((Double)verticalSupplier.get() * (double)10000.0F) / (double)10000.0F)));
   }

   private CycleButton<Integer> addEntitySelector(int x, int y, RelativeScreen.WidgetPosition entitySelectorPos) {
      int entitySelectorWidth = entitySelectorPos.getWidth(this.width);
      int entitySelectorHeight = entitySelectorPos.getHeight(this.height);
      CycleButton<Integer> cycleButton = CycleButton.builder((value) -> {
         EntityType<?> entityType = (EntityType)this.previewEntities.get(value);
         return Component.literal(String.format(Component.translatable("effect_display.render_config.preview_entity").getString(), entityType.getDescription().getString()));
      }).withValues((Integer[])IntStream.range(0, this.previewEntities.size()).boxed().toArray((x$0) -> new Integer[x$0])).withInitialValue(this.selectedEntityIndex).displayOnlyValue().create(x - entitySelectorWidth / 2, y - entitySelectorHeight / 2, entitySelectorWidth, entitySelectorHeight, Component.translatable("effect_display.render_config.entity_type"), (btn, value) -> {
         this.selectedEntityIndex = value;
         this.createPreviewEntity(btn);
      });
      this.addRenderableWidget(cycleButton);
      this.entitySelectorButton = cycleButton;
      return cycleButton;
   }

   private void adjustValue(Supplier<Double> supplier, Consumer<Double> consumer, double amount) {
      double newValue = (Double)supplier.get() + amount;
      consumer.accept(newValue);
   }

   public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.poseStack = guiGraphics.pose();
      this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.fill(this.previewLeft - 1, this.previewTop - 1, this.previewLeft + this.previewWidth + 1, this.previewTop + this.previewHeight + 1, 16777215);
      guiGraphics.fill(this.previewLeft, this.previewTop, this.previewLeft + this.previewWidth, this.previewTop + this.previewHeight, 16777215);
      RelativeScreen.WidgetPosition controlBgPos = this.getWidgetPosition("control_area_background");
      int controlBgLeft = controlBgPos.getX(this.width);
      int controlBgBottom = controlBgPos.getY(this.height);
      int controlBgWidth = controlBgPos.getWidth(this.width);
      int controlBgHeight = controlBgPos.getHeight(this.height);
      guiGraphics.fill(controlBgLeft, controlBgBottom, controlBgLeft + controlBgWidth, controlBgBottom + controlBgHeight, -2145378272);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      if (this.previewEntity != null) {
         this.renderPreviewEntity(guiGraphics.pose(), partialTick);
      }

      RelativeScreen.WidgetPosition titlePos = this.getWidgetPosition("preview_title");
      Component titleText = Component.translatable("effect_display.render_config.buff_preview");
      int titleX = titlePos.getX(this.width, this.font.width(titleText));
      int var10001 = this.height;
      Objects.requireNonNull(this.font);
      int titleY = titlePos.getY(var10001, 9);
      guiGraphics.drawString(this.font, titleText, titleX, titleY, -1, false);
   }

   private void renderPreviewEntity(PoseStack poseStack, float partialTick) {
      if (this.previewEntity != null) {
         if (this.minecraft != null) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            poseStack.pushPose();
            int entityX = this.previewEntityPos.getX(this.width);
            int entityY = this.previewEntityPos.getY(this.height) - 10;
            if (this.rotationStartTime > 0L) {
               float elapsed = (float)(System.currentTimeMillis() - this.rotationStartTime) / 1000.0F;
               this.previewYaw = elapsed * 36.0F;
            }

            poseStack.translate((double)entityX, (double)entityY, (double)50.0F);
            poseStack.scale(1.0F, -1.0F, 1.0F);
            PoseStack effectPoseStack = new PoseStack();
            effectPoseStack.mulPose(poseStack.last().pose());
            effectPoseStack.scale(60.0F, 60.0F, 60.0F);
            poseStack.scale(this.entitySize, this.entitySize, this.entitySize);
            EntityRenderDispatcher entityRenderDispatcher = this.minecraft.getEntityRenderDispatcher();
            Camera mainCamera = this.minecraft.gameRenderer.getMainCamera();
            MultiBufferSource.BufferSource bufferSource = this.minecraft.renderBuffers().bufferSource();
            EntityRenderer<? super Entity> entityRenderer = entityRenderDispatcher.getRenderer(this.previewEntity);
            List<MobEffectInstance> entityEffects = ClientEntityEffectsStorage.getEntityEffects(this.previewEntity.getId());
            if (this.minecraft != null) {
               float baseHeight = this.previewEntity.getBbHeight() + (float)EffectDisplayConfig.getVerticalOffset();
               float scaledIconY = baseHeight * (this.entitySize / 60.0F - 1.0F);
               EffectRenderUtil.renderAllMobEffects(this.previewEntity, effectPoseStack, bufferSource, mainCamera, entityRenderer, partialTick, (double)0.0F, (double)scaledIconY, (double)0.0F, entityEffects, true);
               EffectRenderUtil.renderHealthBar(this.previewEntity, effectPoseStack, bufferSource, mainCamera, entityRenderer, partialTick, (double)0.0F, (double)scaledIconY, (double)0.0F);
            }

            poseStack.pushPose();
            poseStack.mulPose((new Quaternionf()).rotateY(((float)Math.PI / 180F) * this.previewYaw));
            entityRenderer.render(this.previewEntity, 0.0F, partialTick, poseStack, this.minecraft.renderBuffers().bufferSource(), 15728880);
            poseStack.popPose();
            if (this.minecraft != null) {
               this.minecraft.renderBuffers().bufferSource().endBatch();
            }

            poseStack.popPose();
         }

      }
   }

   private void resetToDefaults() {
      this.entitySize = 60.0F;
      this.entitySizeSlider.setValue((double)1.0F);
      EffectDisplayConfig.RENDER_SCALE.set((double)1.0F);
      EffectDisplayConfig.VERTICAL_OFFSET.set((double)0.0F);
      EffectDisplayConfig.HORIZONTAL_OFFSET.set((double)0.0F);
      EffectDisplayConfig.SHOW_SELF_POTION.set(false);
      EffectDisplayConfig.SHOW_OTHER_POTION.set(true);
      EffectDisplayConfig.BLINK_ON_LOW_DURATION.set(true);
      EffectDisplayConfig.MAX_DISTANCE.set((double)64.0F);
      EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.set((double)12.0F);
   }

   private void syncUIFromConfig() {
      if (this.scaleSlider != null) {
         this.scaleSlider.setValue((Double)EffectDisplayConfig.RENDER_SCALE.get());
      }

      if (this.selfPotionCheckbox != null) {
         boolean target = (Boolean)EffectDisplayConfig.SHOW_SELF_POTION.get();
         if (this.selfPotionCheckbox.selected() != target) {
            this.selfPotionCheckbox.onPress();
         }
      }

      if (this.otherPotionCheckbox != null) {
         boolean target = (Boolean)EffectDisplayConfig.SHOW_OTHER_POTION.get();
         if (this.otherPotionCheckbox.selected() != target) {
            this.otherPotionCheckbox.onPress();
         }
      }

      if (this.blinkPotionCheckbox != null) {
         boolean target = (Boolean)EffectDisplayConfig.BLINK_ON_LOW_DURATION.get();
         if (this.blinkPotionCheckbox.selected() != target) {
            this.blinkPotionCheckbox.onPress();
         }
      }

      if (this.maxDistanceSlider != null) {
         this.maxDistanceSlider.setValue((Double)EffectDisplayConfig.MAX_DISTANCE.get());
      }

      if (this.maxDistanceNoLoSslider != null) {
         this.maxDistanceNoLoSslider.setValue((Double)EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.get());
      }

   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (mouseX >= (double)this.previewLeft && mouseX < (double)(this.previewLeft + this.previewWidth) && mouseY >= (double)this.previewTop && mouseY < (double)(this.previewTop + this.previewHeight)) {
         this.previewYaw = 0.0F;
         this.rotationStartTime = System.currentTimeMillis();
         return true;
      } else if (this.entitySelectorButton != null && button == 1 && mouseX >= (double)this.entitySelectorButton.getX() && mouseX < (double)(this.entitySelectorButton.getX() + this.entitySelectorButton.getWidth()) && mouseY >= (double)this.entitySelectorButton.getY() && mouseY < (double)(this.entitySelectorButton.getY() + this.entitySelectorButton.getHeight())) {
         int currentValue = (Integer)this.entitySelectorButton.getValue();
         int newValue = (currentValue - 1 + this.previewEntities.size()) % this.previewEntities.size();
         this.entitySelectorButton.setValue(newValue);
         this.selectedEntityIndex = newValue;
         this.createPreviewEntity(this.entitySelectorButton);
         if (this.minecraft != null) {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         }

         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public void onClose() {
      if (this.originalSnapshot != null) {
         this.originalSnapshot.restore();
         this.syncUIFromConfig();
      }

      if (this.previewEntity != null) {
         ClientEntityEffectsStorage.removeEntityEffects(this.previewEntity.getId());
         this.previewEntity.discard();
         this.previewEntity = null;
      }

      super.onClose();
   }

   public static List<EntityType<?>> getAllEntityTypes() {
      List<EntityType<?>> types = new ArrayList();

      for(EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
         if (Minecraft.getInstance().level != null && entityType.create(Minecraft.getInstance().level) instanceof LivingEntity) {
            types.add(entityType);
         }
      }

      return types;
   }

   private static class ConfigSnapshot {
      double horizontalOffset;
      double verticalOffset;
      double renderScale;
      boolean showSelfPotion;
      boolean showOtherPotion;
      boolean blinkOnLowDuration;
      double maxDistance;
      double maxDistanceWithoutLineOfSight;

      private static ConfigSnapshot capture() {
         ConfigSnapshot snapshot = new ConfigSnapshot();
         snapshot.horizontalOffset = (Double)EffectDisplayConfig.HORIZONTAL_OFFSET.get();
         snapshot.verticalOffset = (Double)EffectDisplayConfig.VERTICAL_OFFSET.get();
         snapshot.renderScale = (Double)EffectDisplayConfig.RENDER_SCALE.get();
         snapshot.showSelfPotion = (Boolean)EffectDisplayConfig.SHOW_SELF_POTION.get();
         snapshot.showOtherPotion = (Boolean)EffectDisplayConfig.SHOW_OTHER_POTION.get();
         snapshot.blinkOnLowDuration = (Boolean)EffectDisplayConfig.BLINK_ON_LOW_DURATION.get();
         snapshot.maxDistance = (Double)EffectDisplayConfig.MAX_DISTANCE.get();
         snapshot.maxDistanceWithoutLineOfSight = (Double)EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.get();
         return snapshot;
      }

      private void restore() {
         EffectDisplayConfig.HORIZONTAL_OFFSET.set(this.horizontalOffset);
         EffectDisplayConfig.VERTICAL_OFFSET.set(this.verticalOffset);
         EffectDisplayConfig.RENDER_SCALE.set(this.renderScale);
         EffectDisplayConfig.SHOW_SELF_POTION.set(this.showSelfPotion);
         EffectDisplayConfig.SHOW_OTHER_POTION.set(this.showOtherPotion);
         EffectDisplayConfig.BLINK_ON_LOW_DURATION.set(this.blinkOnLowDuration);
         EffectDisplayConfig.MAX_DISTANCE.set(this.maxDistance);
         EffectDisplayConfig.MAX_DISTANCE_WITHOUT_LINE_OF_SIGHT.set(this.maxDistanceWithoutLineOfSight);
      }
   }
}
