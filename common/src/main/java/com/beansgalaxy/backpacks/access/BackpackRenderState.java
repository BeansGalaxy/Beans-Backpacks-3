package com.beansgalaxy.backpacks.access;

import com.beansgalaxy.backpacks.Constants;
import com.beansgalaxy.backpacks.traits.Traits;
import com.beansgalaxy.backpacks.traits.backpack.BackpackTraits;
import com.beansgalaxy.backpacks.util.ComponentHolder;
import com.beansgalaxy.backpacks.util.Tint;
import com.beansgalaxy.backpacks.util.ViewableBackpack;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface BackpackRenderState {
      @Nullable
      Context getBackpackRenderState();
      
      void setBackpackRenderState(Context context);
      
      @Nullable
      static Context get(EntityRenderState renderState) {
            if (renderState instanceof BackpackRenderState state) {
                  return state.getBackpackRenderState();
            }
            
            return null;
      }
      
      static Context set(EntityRenderState renderState, ItemStack backpack, ViewableBackpack viewable, float delta) {
            if (renderState instanceof BackpackRenderState state) {
                  if (backpack.isEmpty()) {
                        state.setBackpackRenderState(null);
                        return null;
                  }
                  
                  return set(state, ComponentHolder.of(backpack), viewable, delta);
            }
            
            return null;
      }
      
      static Context set(BackpackRenderState state, ComponentHolder holder, ViewableBackpack viewable, float delta) {
            ResourceLocation texture;
            BackpackTraits traits = Traits.get(holder, Traits.BACKPACK);
            if (traits == null)
                  texture = Constants.defaultLocation("null");
            else
                  texture = traits.getTexture();
            
            Tint tint;
            ResourceLocation trim;
            if (texture.equals(Constants.defaultLocation("leather"))) {
                  DyedItemColor dyedItemColor = holder.get(DataComponents.DYED_COLOR);
                  int color = dyedItemColor == null ? Constants.DEFAULT_LEATHER_COLOR : dyedItemColor.rgb();
                  tint = new Tint(color).setAlpha(1f);
                  trim = null;
            }
            else {
                  ArmorTrim armorTrim = holder.get(DataComponents.TRIM);
                  if (armorTrim != null) {
                        ResourceLocation pattern = armorTrim.pattern().value().assetId();
                        String material = armorTrim.material().value().assets().base().suffix();
                        trim = ResourceLocation.fromNamespaceAndPath(
                              pattern.getNamespace(),
                              "trims/backpacks/" + pattern.getPath() + '_' + material
                        );
                  }
                  else trim = null;
                  tint = null;
            }
            
            float headPitch = viewable.updateOpen(delta);
            Context context = new Context(texture, headPitch, trim, tint);
            state.setBackpackRenderState(context);
            return context;
      }
      
      static <AvatarlikeEntity extends Avatar & ClientAvatarEntity> void setForAvatar(AvatarlikeEntity entity, AvatarRenderState state, ItemStack backpack, ViewableBackpack viewable, float delta) {
            Context context = set(state, backpack, viewable, delta);
            if (context != null) {
                  
                  float y = 0;
                  
                  boolean fallFlying = entity.isFallFlying();
                  float fallPitch;
                  float wingSpread;
                  if (fallFlying) {
                        y += 1/32f;
                        Vec3 deltaMovement = entity.getDeltaMovement();
                        Vec3 norm = deltaMovement.normalize();
                        if (norm.y > 0)
                              wingSpread = 0;
                        else wingSpread = (float) Math.pow(-norm.y, 1.5);
                        fallPitch = 0;
                  }
                  else {
                        wingSpread = 1;
                        
                        double fallDistance = entity.fallDistance;
                        double log = Math.log(fallDistance * 3 + 1);
                        fallPitch = (float) log * -0.3f;
                  }
                  
                  float scale = state.xRot;
                  context.wingXRot = Mth.lerp(scale, 0.25f * wingSpread, 0);
                  context.wingZ = Mth.lerp(scale, fallFlying ? context.wingXRot : 4/16f, -3/32f);
                  context.wingY = y + Mth.lerp(scale, fallFlying ? -1/16f : 0, 3/16f);
                  
                  context.fallPitch = fallPitch;
            }
      }
      
      final class Context {
            public final ResourceLocation texture;
            public final float headPitch;
            public final @Nullable ResourceLocation trim;
            public final @Nullable Tint tint;
            public float wingXRot, wingY, wingZ, fallPitch;
            
            public Context(ResourceLocation texture, float headPitch, @Nullable ResourceLocation trim, @Nullable Tint tint) {
                  this.texture = texture;
                  this.headPitch = headPitch;
                  this.trim = trim;
                  this.tint = tint;
            }
            
            @Override
            public boolean equals(Object obj) {
                  if (obj == this) return true;
                  if (obj == null || obj.getClass() != this.getClass()) return false;
                  var that = (Context) obj;
                  return Objects.equals(this.texture, that.texture) &&
                        Float.floatToIntBits(this.headPitch) == Float.floatToIntBits(that.headPitch) &&
                        Objects.equals(this.trim, that.trim) &&
                        Objects.equals(this.tint, that.tint);
            }
            
            @Override
            public int hashCode() {
                  return Objects.hash(texture, headPitch, trim, tint);
            }
            
            @Override
            public String toString() {
                  return "Context[" +
                        "texture=" + texture + ", " +
                        "headPitch=" + headPitch + ", " +
                        "trim=" + trim + ", " +
                        "tint=" + tint + ']';
            }
      }
}
