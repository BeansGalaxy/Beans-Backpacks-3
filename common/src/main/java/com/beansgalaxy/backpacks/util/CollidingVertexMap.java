package com.beansgalaxy.backpacks.util;

import com.beansgalaxy.backpacks.CommonClient;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class CollidingVertexMap {
      final AABB starting_box;
      final Level level;
      final Vec3 cursor;
      public CollidingVertexMap(AABB box, Direction direction, Level level, Vec3 cursor) {
            this.direction = direction;
            this.level = level;
            this.box = box;
            this.starting_box = box;
            this.cursor = cursor;
            
            updateCords();
      }

      private void updateCords() {
            Vec3[] hang;
            Vec3[] clip;
            
            switch (direction) {
                  case NORTH -> {
                        // -Z
                        hang = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.minZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.minZ)
                              // BOT LEFT
                        };
                        clip = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.maxZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.maxZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                  }
                  case SOUTH -> {
                        // +Z
                        clip = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.minZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.minZ)
                              // BOT LEFT
                        };
                        hang = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.maxZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.maxZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                  }
                  case EAST -> {
                        // +X
                        hang = new Vec3[] {
                              new Vec3(box.maxX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.maxX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                        clip = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.minX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.minX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                  }
                  case WEST -> {
                        // -X
                        clip = new Vec3[] {
                              new Vec3(box.maxX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.maxX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.maxX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                        hang = new Vec3[] {
                              new Vec3(box.minX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.minX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.minX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                  }
                  case UP,
                       DOWN -> {
                        clip = new Vec3[] {
                              new Vec3(box.maxX, box.minY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.minY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.minX, box.minY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.minY, box.maxZ)
                              // BOT LEFT
                        };
                        hang = new Vec3[] {
                              new Vec3(box.maxX, box.maxY, box.maxZ),
                              // TOP LEFT
                              new Vec3(box.maxX, box.maxY, box.minZ),
                              // TOP RIGHT
                              new Vec3(box.minX, box.maxY, box.minZ),
                              // BOT RIGHT
                              new Vec3(box.minX, box.maxY, box.maxZ)
                              // BOT LEFT
                        };
                  }
                  default -> {
                        return;
                  }
            }
            
            clipped = clip;
            hanging = hang;
      }
      public AABB box;
      Direction direction;
      Vec3[] clipped = new Vec3[4];
      Vec3[] hanging = new Vec3[4];
      int pointer = 0;
      
      public boolean areClippedPointsStable() {
            for (pointer = 0; pointer < 4; pointer++) {
                  Vec3 v = clipped[pointer];
                  
                  Vec3 offs = new Vec3(0.1, 0.1, 0.1);
                  Iterable<VoxelShape> blockCollisions = level.getBlockCollisions(null, new AABB(v.add(offs), v.subtract(offs)));
                  
                  boolean noCollision = true;
                  for (VoxelShape collision : blockCollisions) {
                        
                        for (AABB ab : collision.toAabbs()) {
                              boolean minX = v.x >= ab.minX;
                              boolean maxX = v.x <= ab.maxX;
                              boolean minY = v.y >= ab.minY;
                              boolean maxY = v.y <= ab.maxY;
                              boolean minZ = v.z >= ab.minZ;
                              boolean maxZ = v.z <= ab.maxZ;
                              boolean contains
                                    = minX
                                    && maxX
                                    && minY
                                    && maxY
                                    && minZ
                                    && maxZ;
                              
                              if (contains) {
                                    noCollision = false;
                                    break;
                              }
                        }
                  }
                  
                  if (noCollision)
                        return false;
            }
            
            return true;
      }
      
      public void stabilizeHangingPoints() {
            Vec3 size = new Vec3(7 / 32.0, 9 / 32.0, 7 / 32.0);
            AABB aabb = new AABB(cursor.add(size), cursor.subtract(size));
            
            Vec3 center = aabb.getCenter();
            Vec3 px = new Vec3(aabb.maxX, center.y, center.z);
            Vec3 nx = new Vec3(aabb.minX, center.y, center.z);
            Vec3 pz = new Vec3(center.x, center.y, aabb.maxZ);
            Vec3 nz = new Vec3(center.x, center.y, aabb.minZ);
            
            Vec3[] face;
            Vec3[] pair;
            switch (direction) {
                  case NORTH -> {
                        face = new Vec3[] {nz};
                        pair = new Vec3[] {pz};
                  }
                  case SOUTH -> {
                        face = new Vec3[] {pz};
                        pair = new Vec3[] {nz};
                  }
                  case EAST -> {
                        face = new Vec3[] {px};
                        pair = new Vec3[] {nx};
                  }
                  case WEST -> {
                        face = new Vec3[] {nx};
                        pair = new Vec3[] {px};
                  }
                  default -> {
                        face = new Vec3[] {
                              nx,
                              nz,
                              px,
                              pz
                        };
                        pair = new Vec3[] {
                              px,
                              pz,
                              nx,
                              nz
                        };
                  }
            }
            
            int index = -1;
            for (int i = 0; i < face.length; i++) {
                  Vec3 vFace = face[i];
                  
                  BlockHitResult lineOfSightToCursor = level.clip(new ClipContext(vFace, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                  boolean noLineOfSight = HitResult.Type.BLOCK.equals(lineOfSightToCursor.getType());
                  
                  if (noLineOfSight)
                        continue;
                  
                  if (!doesCollide(vFace)) {
                        index = i;
                        break;
                  }
            }
            
            if (index != -1) {
                  int i = index;
                  do {
                        Vec3 vFace = face[i];
                        Vec3 vPair = pair[i];
                        
                        if (doesCollide(vFace))
                              continue;
                        
                        BlockHitResult lineOfSightToCursor = level.clip(new ClipContext(vFace, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        boolean hasLinOfSight = !HitResult.Type.BLOCK.equals(lineOfSightToCursor.getType());
                        
                        if (!hasLinOfSight)
                              continue;
                        
                        BlockHitResult clip = level.clip(new ClipContext(vFace, vPair, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        if (!HitResult.Type.BLOCK.equals(clip.getType()))
                              continue;
                        
                        Vec3 location = clip.getLocation();
                        Vec3 offset = location.subtract(vPair);
                        
                        if (!(Math.abs(offset.x) + Math.abs(offset.y) + Math.abs(offset.z) > 0.001))
                              continue;
                        
                        for (int j = 0; j < face.length; j++) {
                              face[j] = face[j].add(offset);
                              pair[j] = pair[j].add(offset);
                        }
                        
                        aabb = aabb.move(offset);
                        
                  } while ((i = (i + 1) % face.length) != index);
            }
            
            AABB start = aabb.move(0, 1, 0);
            AABB zone = start.expandTowards(0, -1, 0);
            Iterable<VoxelShape> iterable = level.getCollisions(null, zone);
            double yOff = 1 + Shapes.collide(Direction.Axis.Y, start, iterable, -1);
            aabb = aabb.move(0, yOff, 0);
            
            this.box = aabb;
            updateCords();
      }
      
      private boolean doesCollide(Vec3 v) {
            Vec3 offs = new Vec3(0.1, 0.1, 0.1);
            Iterable<VoxelShape> blockCollisions = level.getBlockCollisions(null, new AABB(v.add(offs), v.subtract(offs)));
            for (VoxelShape collision : blockCollisions) {
                  for (AABB ab : collision.toAabbs()) {
                        boolean contains
                              = v.x >= ab.minX
                              && v.x <= ab.maxX
                              && v.y >= ab.minY
                              && v.y <= ab.maxY
                              && v.z >= ab.minZ
                              && v.z <= ab.maxZ;
                        
                        if (contains) return true;
                  }
            }
            
            return false;
      }
      
      public void pushClippedPoints() {
            for (pointer = 0; pointer < 4; pointer++) {
                  
                  Vec3 tl = clipped[pointer];
                  Vec3 tr = clipped[(pointer + 1) % 4];
                  Vec3 bl = clipped[(pointer + 3) % 4];
                  
                  boolean doesCollide = doesCollide(tl);
                  
                  if (doesCollide)
                        continue;
                  
                  BlockHitResult clip2 = level.clip(new ClipContext(tl, tr, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                  BlockHitResult clip0 = level.clip(new ClipContext(tl, bl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                  
                  Vec3 offset;
                  boolean clip2Missed = HitResult.Type.MISS.equals(clip2.getType());
                  boolean clip0Missed = HitResult.Type.MISS.equals(clip0.getType());
                  if (clip2Missed && clip0Missed)
                        continue;
                  
                  if (!clip2Missed && !clip0Missed) {
                        Vec3 zeroed2 = clip2.getLocation().subtract(tl);
                        Vec3 zeroed0 = clip0.getLocation().subtract(tl);
                        Vec3 zeroed = zeroed2.add(zeroed0);
                        
                        double absZ = Math.abs(zeroed.z);
                        double absX = Math.abs(zeroed.x);
                        
                        if (direction.getAxis().isVertical()) {
                              if (absX < absZ) {
                                    offset = new Vec3(zeroed.x, 0, 0);
                              }
                              else offset = new Vec3(0, 0, zeroed.z);
                        }
                        else {
                              if (Math.max(absZ, absX) < Math.abs(zeroed.y)) {
                                    offset = new Vec3(zeroed.x, 0, zeroed.z);
                              }
                              else offset = new Vec3(0, zeroed.y, 0);
                        }
                  }
                  else {
                        BlockHitResult clip = clip2Missed
                              ? clip0
                              : clip2;
                        offset = clip.getLocation().subtract(tl);
                  }
                  
                  if (Math.abs(offset.x) + Math.abs(offset.y) + Math.abs(offset.z) < 0.001)
                        continue;
                  
                  move(offset);
            }
      }
      
      void move(Vec3 offset) {
            box = box.move(offset);
            updateCords();
      }
      
      public void pushHangingPoints() {
            int success = 0;
            
            Vec3[] list = {
                  null,
                  null,
                  null,
                  null
            };
            
            int count = 0;
            for (pointer = 0; pointer < 4; pointer++) {
                  Vec3 br = hanging[(pointer + 2) % 4]; // OPPOSITE
                  if (doesCollide(br)) {
                        continue;
                  }
                  else {
                        BlockHitResult clip2 = level.clip(new ClipContext(br, cursor, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        if (!clip2.getType().equals(HitResult.Type.MISS))
                              continue;
                  }
                  
                  list[pointer] = br;
                  count++;
            }
            
            if (count == 3) {
                  // HANDLE SNAPS TO SHARP-OUTER CORNERS
                  for (pointer = 0; pointer < 4; pointer++) {
                        Vec3 br = list[pointer]; // OPPOSITE
                        if (br == null)
                              continue;
                        
                        Vec3 tl = hanging[pointer];
                        Vec3 tr = hanging[(pointer + 1) % 4];
                        Vec3 bl = hanging[(pointer + 3) % 4];
                        BlockHitResult clipR = level.clip(new ClipContext(tr, tl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        Vec3 contactR = clipR.getLocation();
                        
                        BlockHitResult clipL = level.clip(new ClipContext(bl, tl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                        Vec3 contactL = clipL.getLocation();
                        
                        Vec3 offsR = tl.subtract(contactR).multiply(-1, -1, -1);
                        Vec3 offsL = tl.subtract(contactL).multiply(-1, -1, -1);
                        
                        Vec3 zeroed = offsR.add(offsL);
                        double absZ = Math.abs(zeroed.z);
                        double absX = Math.abs(zeroed.x);
                        
                        Vec3 offs;
                        if (direction.getAxis().isVertical()) {
                              if (absX < absZ) {
                                    offs = new Vec3(zeroed.x, 0, 0);
                              }
                              else offs = new Vec3(0, 0, zeroed.z);
                        }
                        else {
                              if (Math.max(absZ, absX) < Math.abs(zeroed.y)) {
                                    offs = new Vec3(zeroed.x, 0, zeroed.z);
                              }
                              else offs = new Vec3(0, zeroed.y, 0);
                        }
                        
                        move(offs);
                  }
                  
            }
            else {
                  for (pointer = 0; pointer < 4 && success < 2; pointer++) {
                        Vec3 br = list[pointer]; // OPPOSITE
                        if (br == null)
                              continue;
                        
                        Vec3 tr = hanging[(pointer + 1) % 4];
                        Vec3 bl = hanging[(pointer + 3) % 4];
                        
                        int steps = 0;
                        Vec3 offset = Vec3.ZERO;
                        
                        if (doesCollide(tr)) {
                              BlockHitResult clip2 = level.clip(new ClipContext(br, tr, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              Vec3 contact = clip2.getLocation();
                              
                              Vec3 offs = tr.subtract(contact).multiply(-1, -1, -1);
                              offset = offset.add(offs);
                              steps++;
                        }
                        
                        if (doesCollide(bl)) {
                              BlockHitResult clip2 = level.clip(new ClipContext(br, bl, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
                              Vec3 contact = clip2.getLocation();
                              
                              Vec3 offs = bl.subtract(contact).multiply(-1, -1, -1);
                              offset = offset.add(offs);
                              steps++;
                        }
                        
                        if (Vec3.ZERO == offset)
                              continue;
                        
                        move(offset);
                        success += steps;
                  }
            }
      }
}
