package com.seth0067.tothebatpoles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber
public class Player {

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		EntityPlayer player = event.player;
		World world = player.getEntityWorld();
		if (event.side.isServer()) {
			BlockPos pos = Pole.findReachableBlockPos(player);
			Holder holder = Pole.findHolderFor(player);
			if (pos != null && holder == null && world.getWorldTime() % 5 == 0 && Pole.canBeHeldBy(player)
					&& Pole.isLongEnoughFor(player, pos) && Pole.hasBlocksBelow(world, pos, 2)) {
				holder = new Holder(world, player, pos);
				world.spawnEntity(holder);
			}
		}
	}

}
