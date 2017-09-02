package com.seth0067.tothebatpoles;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class Message implements IMessage {

	private int holderId;
	private float deltaYaw;

	public Message() {
	}

	public Message(Holder holder, float deltaYaw) {
		this.holderId = holder.getEntityId();
		this.deltaYaw = deltaYaw;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		holderId = buf.readInt();
		deltaYaw = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(holderId);
		buf.writeFloat(deltaYaw);
	}

	public static class Handler implements IMessageHandler<Message, IMessage> {

		@Override
		public IMessage onMessage(Message message, MessageContext ctx) {
			WorldServer world = ctx.getServerHandler().playerEntity.getServerWorld();
			Entity entity = world.getEntityByID(message.holderId);
			if (entity instanceof Holder) {
				Holder holder = (Holder) entity;
				float spinVelocity = Holder.calcSpinVelocityFor(message.deltaYaw);
				world.addScheduledTask(() -> {
					holder.setSpinVelocity(spinVelocity);
				});
			}
			return null;
		}

	}

}
