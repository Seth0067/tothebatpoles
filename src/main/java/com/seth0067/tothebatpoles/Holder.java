package com.seth0067.tothebatpoles;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class Holder extends Entity {

	public static float maxSpinVelocity = 0.1f;
	public static float slideVelocity = 0.4f;

	public static final String NAME = Holder.class.getSimpleName().toLowerCase(Locale.ROOT);

	private static final float MAX_DELTA_YAW = 105f;
	private static final Vec3d UP_UNIT_VECTOR = new Vec3d(0, 1, 0);

	private static final DataParameter<Optional<BlockPos>> POLE_POS = EntityDataManager.createKey(Holder.class,
			DataSerializers.OPTIONAL_BLOCK_POS);
	private static final DataParameter<Float> SPIN_RADIUS = EntityDataManager.createKey(Holder.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> SPIN_VELOCITY = EntityDataManager.createKey(Holder.class,
			DataSerializers.FLOAT);
	private static final DataParameter<Float> SLIDE_VELOCITY = EntityDataManager.createKey(Holder.class,
			DataSerializers.FLOAT);

	private float deltaRotation;

	public Holder(World world) {
		super(world);
		this.setSize(0.5F, 0.5F);
		dataManager.register(POLE_POS, Optional.absent());
		dataManager.register(SLIDE_VELOCITY, 0f);
		dataManager.register(SPIN_RADIUS, 0f);
		dataManager.register(SPIN_VELOCITY, 0f);
	}

	public Holder(World world, EntityPlayer player, BlockPos polePos) {
		this(world);
		copyLocationAndAnglesFrom(player);
		player.startRiding(this, false);
		setPolePos(polePos);
		setSlideVelocity(slideVelocity);
		setSpinRadius(player.width * 0.8f);
	}

	public static float calcSpinVelocityFor(float deltaYaw) {
		float yawFactor = deltaYaw / MAX_DELTA_YAW;
		//		maxSpinVelocity = 0.1f; // for testing
		float spinVelocity = maxSpinVelocity * yawFactor;
		return spinVelocity;
	}

	@Override
	public AxisAlignedBB getEntityBoundingBox() {
		return super.getEntityBoundingBox();
	}

	@Override
	public double getMountedYOffset() {
		return 0;
	}

	@Override
	@Nullable
	public Entity getControllingPassenger() {
		List<Entity> passengers = getPassengers();
		return passengers.isEmpty() ? null : passengers.get(0);
	}

	public boolean isServerSide() {
		return !isClientSide();
	}

	public boolean isClientSide() {
		return getEntityWorld().isRemote;
	}

	@Override
	public void onUpdate() {
		onGround = true;

		World world = getEntityWorld();
		Entity passenger = getControllingPassenger();
		BlockPos polePos = getPolePos();
		if (!(passenger instanceof EntityPlayer) || polePos == null || !Pole.isPoleBlock(world, polePos)
				|| Pole.isBottomBlock(world, polePos)) {
			if (isServerSide())
				setDead();
			return;
		}

		passenger.onGround = true; // to prevent pushing out of the block

		// update pole pos to holder height
		if (polePos.getY() != getPosition().getY()) {
			polePos = new BlockPos(polePos.getX(), getPosition().getY(), polePos.getZ());
			setPolePos(polePos);
		}

		if (isClientSide()) {
			float headYaw = passenger.getRotationYawHead();
			float holderYaw = rotationYaw;
			float deltaYaw = MathHelper.wrapDegrees(headYaw - holderYaw);
			Message message = new Message(this, deltaYaw);
			Main.NETWORK.sendToServer(message);
		}

		// update motion vectors
		Vec3d poleCenter = Pole.getCenterWithY(polePos, posY);
		Vec3d radiusVec = getPositionVector().subtract(poleCenter).normalize();
		float spinVelocity = getSpinVelocity();
		//		spinVelocity = 0.05f; // for testing
		Vec3d spinVec = radiusVec.crossProduct(UP_UNIT_VECTOR).scale(spinVelocity);
		float spinRadius = getSpinRadius();
		float slideVelocity = getSlideVelocity();
		//				slideVelocity = 0.0f; // for testing
		Vec3d destPos = poleCenter.add(radiusVec.scale(spinRadius)).add(spinVec).addVector(0, -slideVelocity, 0);

		// update rotations
		float yaw = 180 - (float) (MathHelper.atan2(radiusVec.x, radiusVec.z) * (180 / Math.PI));
		deltaRotation = MathHelper.wrapDegrees(yaw - rotationYaw);

		setPositionAndRotation(destPos.x, destPos.y, destPos.z, yaw, rotationPitch);
	}

	@Override
	public void updatePassenger(Entity passenger) {
		super.updatePassenger(passenger);
		passenger.rotationYaw += deltaRotation;
		passenger.setRotationYawHead(passenger.getRotationYawHead() + deltaRotation);
		applyOrientationToEntity(passenger);
	}

	@Override
	public void applyOrientationToEntity(Entity entity) {
		entity.setRenderYawOffset(rotationYaw);
		float delta = MathHelper.wrapDegrees(entity.rotationYaw - rotationYaw);
		float clamped = MathHelper.clamp(delta, -MAX_DELTA_YAW, MAX_DELTA_YAW);
		entity.rotationYaw += clamped - delta;
	}

	@Override
	public boolean canBeCollidedWith() {
		return false;
	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public boolean shouldRiderSit() {
		return true;
	}

	@Override
	protected void entityInit() {
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		// TODO Auto-generated method stub

	}

	public float getSpinRadius() {
		return dataManager.get(SPIN_RADIUS).floatValue();
	}

	public void setSpinRadius(float spinRadius) {
		dataManager.set(SPIN_RADIUS, spinRadius);
	}

	public float getSpinVelocity() {
		return dataManager.get(SPIN_VELOCITY).floatValue();
	}

	public void setSpinVelocity(float spinVelocity) {
		dataManager.set(SPIN_VELOCITY, spinVelocity);
	}

	public float getSlideVelocity() {
		return dataManager.get(SLIDE_VELOCITY).floatValue();
	}

	public void setSlideVelocity(float slideVelocity) {
		dataManager.set(SLIDE_VELOCITY, slideVelocity);
	}

	@Nullable
	public BlockPos getPolePos() {
		return dataManager.get(POLE_POS).orNull();
	}

	public void setPolePos(@Nullable BlockPos pos) {
		dataManager.set(POLE_POS, Optional.fromNullable(pos));
	}

	public static class Renderer extends Render<Holder> {

		protected Renderer(RenderManager renderManager) {
			super(renderManager);
		}

		@Override
		@Nullable
		protected ResourceLocation getEntityTexture(Holder entity) {
			return null;
		}

		public static class Factory implements IRenderFactory<Holder> {
			@Override
			public Render<? super Holder> createRenderFor(RenderManager manager) {
				return new Renderer(manager);
			}
		}

		@Override
		public void doRender(Holder entity, double x, double y, double z, float entityYaw, float partialTicks) {
			super.doRender(entity, x, y, z, entityYaw, partialTicks);
		}

	}

}
