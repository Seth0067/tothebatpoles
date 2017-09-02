package com.seth0067.tothebatpoles;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class Pole {

	public static boolean isOneBlockNear(EntityPlayer player) {
		World world = player.getEntityWorld();
		BlockPos pos = new BlockPos(player.posX, player.posY + 0.5D, player.posZ); // not using getPosition() because it's shitted in EntityPlayerSP class
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos checkPos = pos.offset(facing);
			if (isPoleBlock(world, checkPos))
				return true;
		}
		return false;
	}

	@Nullable
	public static BlockPos findReachableBlockPos(EntityPlayer player) {
		World world = player.getEntityWorld();
		AxisAlignedBB bb = player.getEntityBoundingBox();
		MutableBlockPos pos = new MutableBlockPos();
		for (int y = MathHelper.floor_double(bb.minY); y <= bb.maxY; y++)
			for (int x = MathHelper.floor_double(bb.minX); x <= bb.maxX; x++)
				for (int z = MathHelper.floor_double(bb.minZ); z <= bb.maxZ; z++) {
					pos.setPos(x, y, z);
					if (isPoleBlock(world, pos))
						return pos.toImmutable();
				}
		return null;
	}

	@Nullable
	public static Holder findHolderFor(EntityPlayer player) {
		Entity riding = player.getRidingEntity();
		return riding instanceof Holder ? (Holder) riding : null;
	}

	public static void releaseFrom(EntityPlayer player) {
		Holder holder = findHolderFor(player);
		if (holder != null)
			holder.setDead();
	}

	public static boolean canBeHeldBy(EntityPlayer player) {
		return player.isEntityAlive() && !player.isSpectator() && !player.isRiding() && !player.isSneaking();
	}

	public static Vec3d getCenterWithY(BlockPos pos, double y) {
		return new Vec3d(pos.getX() + 0.5, y, pos.getZ() + 0.5);
	}

	public static boolean isPoleBlock(World world, BlockPos pos) {
		boolean isIronBar = isIronBar(world.getBlockState(pos));
		boolean hasSpaceAround = true;
		if (isIronBar)
			for (BlockPos checkPos : getBlocksAroundHoriz(pos, false)) {
				IBlockState state = world.getBlockState(checkPos);
				if (state.isNormalCube() || isIronBar(state)) {
					hasSpaceAround = false;
					break;
				}
			}
		return isIronBar && hasSpaceAround;
	}

	protected static boolean isIronBar(IBlockState state) {
		return state.getBlock() == Blocks.IRON_BARS;
	}
	
	public static boolean isBottomBlock(World world, BlockPos pos) {
		return !isPoleBlock(world, pos.down());
	}

	public static List<BlockPos> getBlocksAroundHoriz(BlockPos startPos, boolean includeCorners) {
		List<BlockPos> posList = new ArrayList<>();
		posList.add(startPos.west());
		posList.add(startPos.east());
		posList.add(startPos.north());
		posList.add(startPos.south());
		if (includeCorners) {
			posList.add(startPos.west().north());
			posList.add(startPos.west().south());
			posList.add(startPos.east().north());
			posList.add(startPos.east().south());
		}
		return posList;
	}
	
	public static boolean hasBlocksBelow(World world, BlockPos pos, int amount) {
		int count = 0;
		int offset = 1;
		while (count < amount && isPoleBlock(world, pos.down(offset))) {
			count++;
			offset++;
		}
		return count >= amount;
	}

	public static boolean hasBlocksAbove(World world, BlockPos pos, int amount) {
		int count = 0;
		int offset = 1;
		while (count < amount && isPoleBlock(world, pos.up(offset))) {
			count++;
			offset++;
		}
		return count >= amount;
	}

	public static boolean isLongEnoughFor(EntityPlayer player, BlockPos pos) {
		World world = player.getEntityWorld();
		int amount = MathHelper.floor_float(player.height);
		return hasBlocksAbove(world, pos, amount);
	}
}
