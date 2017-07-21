package com.seth0067.tothebatpoles;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = Main.ID, name = Main.NAME, version = "0.1.0.0")
public class Main {

	public static final String ID = "tothebatpoles";
	public static final String NAME = "To the Bat Poles!";

	@Instance(Main.ID)
	public static Main instance;
	public static Configuration config;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		config = new Configuration(event.getSuggestedConfigurationFile());

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		String category = "Speed";
		Pole.fallSpeedReduction = config.getInt("fallSpeedReduction", category, Pole.fallSpeedReduction, 0, 100,
				"Defines fall speed reduction in percent");
		config.save();
	}

	@EventHandler
	@SideOnly(Side.CLIENT)
	public void initClient(FMLInitializationEvent event) {

	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

	}

}
