package com.seth0067.tothebatpoles;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class Pole {

	public static int fallSpeedReduction = 15;

	public static boolean isOneBlockNear(EntityPlayer player) {
		World world = player.getEntityWorld();
		BlockPos pos = new BlockPos(player.posX, player.posY + 0.5D, player.posZ); // not using getPosition() because it's shitted in EntityPlayerSP class
		for (EnumFacing facing : EnumFacing.HORIZONTALS) {
			BlockPos checkPos = pos.offset(facing);
			if (isPoleBlock(world, checkPos) && isLongEnough(world, checkPos))
				return true;
		}
		return false;
	}

	public static boolean isHoldedBy(EntityPlayer player) {
		if (player.isSpectator())
			return false;
		World world = player.getEntityWorld();
		AxisAlignedBB bb = player.getEntityBoundingBox();
		for (int x = MathHelper.floor(bb.minX); x <= bb.maxX; x++)
			for (int y = MathHelper.floor(bb.minY); y <= bb.maxY; y++)
				for (int z = MathHelper.floor(bb.minZ); z <= bb.maxZ; z++) {
					BlockPos pos = new BlockPos(x, y, z);
					if (isPoleBlock(world, pos))
						return true;
				}
		return false;
	}

	protected static boolean isPoleBlock(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		return state.getBlock() == Blocks.IRON_BARS;
	}

	protected static boolean isLongEnough(World world, BlockPos pos) {
		final int minLength = 3;
		int length = 0;
		for (int offsetUp = 0, offsetDown = 1; length < minLength;) {
			boolean foundPole = false;
			if (isPoleBlock(world, pos.up(offsetUp))) {
				length++;
				offsetUp++;
				foundPole = true;
			}
			if (isPoleBlock(world, pos.down(offsetDown))) {
				length++;
				offsetDown++;
				foundPole = true;
			}
			if (!foundPole)
				break;
		}
		return length >= minLength;
	}
}
