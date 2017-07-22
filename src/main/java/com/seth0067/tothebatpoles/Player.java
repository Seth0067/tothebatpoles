package com.seth0067.tothebatpoles;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

@EventBusSubscriber
public class Player {

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent event) {
		if (event.side.isServer())
			return;
//		Pole.fallSpeedReduction = 15; // that's for testing
		EntityPlayer player = event.player;
		if (Pole.isHoldedBy(player)) {
			double motionOld = player.motionY;
			double reduction = (motionOld > 0 || player.isSneaking()) ? 0 : Pole.fallSpeedReduction;
			double motionDelta = motionOld * (reduction / 100f);
			double motionNew = motionOld - motionDelta;
			if (motionNew < 0)
				player.onGround = true;
			player.motionY = motionNew;
			if (motionOld != motionNew)
				player.velocityChanged = true;
			player.fallDistance = 0;
		}
	}
}
