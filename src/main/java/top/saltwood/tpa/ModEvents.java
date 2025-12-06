package top.saltwood.tpa;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = Tpa.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) throws IOException {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        Level level = player.level();
        if (level.isClientSide) return;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        DeathPositionManager.setLocation(
                player.getUUID(),
                new DeathPositionManager.PlayerDeathLocation(x, y, z, level.dimension())
        );
    }
}
