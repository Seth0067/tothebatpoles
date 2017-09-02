package com.seth0067.tothebatpoles;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Main.ID, name = Main.NAME, version = "1.0.1.0")
public class Main {

	public static final String ID = "tothebatpoles";
	public static final String NAME = "To the Bat Poles!";
	public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(ID);

	@Instance(Main.ID)
	public static Main instance;
	public static Configuration config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());
		NETWORK.registerMessage(Message.Handler.class, Message.class, 0, Side.SERVER);
	}

	@EventHandler
	@SideOnly(Side.CLIENT)
	public void preInitClient(FMLPreInitializationEvent event) {
		RenderingRegistry.registerEntityRenderingHandler(Holder.class, new Holder.Renderer.Factory());
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		EntityRegistry.registerModEntity(new ResourceLocation(ID, Holder.NAME), Holder.class, Holder.NAME, 0, this, 255,
				20, true);
		String category = "Velocity";
		Holder.slideVelocity = config.getFloat("slideVelocity", category, Holder.slideVelocity, 0f, 0.8f,
				"Defines the sliding speed down the pole.");
		Holder.maxSpinVelocity = config.getFloat("maxSpinVelocity", category, Holder.maxSpinVelocity, 0f, 0.2f,
				"Defines the maximum rotation speed around the pole.");
		config.save();
	}

	@EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {
		String category = "Client";
		Holder.switchToThirdPersonView = config.getBoolean("switchToThirdPersonView", category, Holder.switchToThirdPersonView, 
				"Enables automatic switching to a third-person view while holding the pole.");
		config.save();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

}
