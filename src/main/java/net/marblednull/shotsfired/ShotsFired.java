package net.marblednull.shotsfired;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(ShotsFired.MODID)
public class ShotsFired {
    public static final String MODID = "shotsfired";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ShotsFired() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(ModEvents::weaponShootEvent);
        TACZEjectionConfig.register();
        //Registering the JSON based Config
        try {
            TACZConfig.checkConfig();
            TACZBurstConfig.checkConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

        }

    }

}