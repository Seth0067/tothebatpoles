package com.seth0067.tothebatpoles;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Message implements IMessage {

	private int holderId;
	private boolean decelerate;
	private float deltaYaw;

	public Message() {
	}

	public Message(Holder holder, boolean decelerate, float deltaYaw) {
		this.holderId = holder.getEntityId();
		this.decelerate = decelerate;
		this.deltaYaw = deltaYaw;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		holderId = buf.readInt();
		decelerate = buf.readBoolean();
		deltaYaw = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(holderId);
		buf.writeBoolean(decelerate);
		buf.writeFloat(deltaYaw);
	}

	public static class Handler implements IMessageHandler<Message, IMessage> {

		@Override
		public IMessage onMessage(Message message, MessageContext ctx) {
			WorldServer world = ctx.getServerHandler().player.getServerWorld();
			Entity entity = world.getEntityByID(message.holderId);
			if (entity instanceof Holder) {
				Holder holder = (Holder) entity;
				world.addScheduledTask(() -> {
					holder.updateSlideAcceleration(message.decelerate);
					holder.updateSpinVelocity(message.deltaYaw);
				});
			}
			return null;
		}

	}

}
