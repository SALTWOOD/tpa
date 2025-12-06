package top.saltwood.tpa;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Tpa.MODID)
public class Tpa {
    public static final String MODID = "tpa";

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Command source is not a plkayer!"));
                                return 0;
                            }
                            Player src = context.getSource().getPlayer();
                            ServerPlayer dst = EntityArgument.getPlayer(context, "player");
                            if (src != null) src.teleportTo(dst.getX(), dst.getY(), dst.getZ());
                            return 1;
                        })
                )
        );
        dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    return teleBack(context.getSource());
                })
        );
    }


    private static int teleBack(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command.").withStyle(ChatFormatting.RED));
            return 0;
        }

        DeathPositionManager.PlayerDeathLocation location = DeathPositionManager.getLocation(player.getUUID());

        if (location != null) {
            ServerLevel destinationWorld = player.server.getLevel(location.dimension);

            if (destinationWorld == null) {
                source.sendFailure(Component.literal("Cannot find the dimension you last died in.").withStyle(ChatFormatting.RED));
                return 0;
            }

            player.teleportTo(destinationWorld, location.x, location.y, location.z, player.getYRot(), player.getXRot());
            source.sendSuccess(() -> Component.literal("Teleported to your last death location.").withStyle(ChatFormatting.GREEN), true);

            return 1;
        } else {
            source.sendFailure(Component.literal("You have no recorded death location.").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    public Tpa() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }
}
