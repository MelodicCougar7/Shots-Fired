package net.marblednull.shotsfired;

import com.mojang.logging.LogUtils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.ModList;
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

    // conditional data loading variables
    public static final boolean TACZ_PRESENT = ModList.get().isLoaded("tacz");
    public static final boolean POINTBLANK_PRESENT = ModList.get().isLoaded("pointblank");
    public static final boolean SCGUNS_PRESENT = ModList.get().isLoaded("scguns");
    public static final boolean JEG_PRESENT = ModList.get().isLoaded("jeg"); // not confirmed to be correct
    //public static final boolean GCAA_PRESENT = ModList.get().isLoaded("gcaa"); // not confirmed to be correct
    //bugs in GCAA and GCRR prevent implementation, unfortunately. Placeholder boolean logic will remain as a reminder.

    public ShotsFired() {
        LOGGER.info("Shots Fired initializing.");
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        // currently planned to use universally for all supported mods
        MinecraftForge.EVENT_BUS.addListener(ModEvents::weaponShootEvent);

        if (TACZ_PRESENT) {
            //Registering the JSON based Config
            TACZEjectionConfig.register();
            try {
                TACZConfig.checkConfig();
                JsonBurstConfig.checkConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (POINTBLANK_PRESENT) {
            // Registering VPB's jsconf config
            VPBEjectionConfig.register();
            try {
                VPBConfig.checkConfig();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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