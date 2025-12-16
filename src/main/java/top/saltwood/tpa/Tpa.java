package top.saltwood.tpa;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(Tpa.MODID)
public class Tpa {
    public static final String MODID = "tpa";
    private static final TeleportRequestManager REQUEST_MANAGER = new TeleportRequestManager();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        // tpa command - request to teleport to target player
        dispatcher.register(Commands.literal("tpa")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            ServerPlayer src = context.getSource().getPlayer();
                            ServerPlayer dst = EntityArgument.getPlayer(context, "player");

                            if (src == null) {
                                context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (src == dst) {
                                context.getSource().sendFailure(Component.literal("Cannot send request to yourself").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (REQUEST_MANAGER.hasPendingRequest(src.getUUID(), dst.getUUID())) {
                                context.getSource().sendFailure(Component.literal("Request already pending").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            REQUEST_MANAGER.addRequest(src, dst, false);

                            MutableComponent requestMsg = Component.literal("[").withStyle(ChatFormatting.GRAY)
                                    .append(src.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(" → ").withStyle(ChatFormatting.WHITE))
                                    .append(dst.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal("]").withStyle(ChatFormatting.GRAY));

                            src.sendSystemMessage(Component.literal("Teleport request sent to ").withStyle(ChatFormatting.GREEN)
                                    .append(dst.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));

                            MutableComponent targetMsg = requestMsg.copy()
                                    .append(Component.literal(" wants to teleport to you").withStyle(ChatFormatting.WHITE));

                            MutableComponent acceptBtn = Component.literal("[Accept]").withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GREEN)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + src.getName().getString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Click to accept").withStyle(ChatFormatting.GREEN))));

                            MutableComponent denyBtn = Component.literal("[Deny]").withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.RED)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + src.getName().getString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Click to deny").withStyle(ChatFormatting.RED))));

                            dst.sendSystemMessage(targetMsg.append(Component.literal(" ")).append(acceptBtn).append(Component.literal(" ")).append(denyBtn));
                            dst.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1.0f, 1.0f);
                            return 1;
                        })
                )
        );

        // tpahere command - request target player to teleport to you
        dispatcher.register(Commands.literal("tpahere")
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            ServerPlayer src = context.getSource().getPlayer();
                            ServerPlayer dst = EntityArgument.getPlayer(context, "player");

                            if (src == null) {
                                context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (src == dst) {
                                context.getSource().sendFailure(Component.literal("Cannot send request to yourself").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (REQUEST_MANAGER.hasPendingRequest(src.getUUID(), dst.getUUID())) {
                                context.getSource().sendFailure(Component.literal("Request already pending").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            REQUEST_MANAGER.addRequest(src, dst, true);

                            MutableComponent requestMsg = Component.literal("[").withStyle(ChatFormatting.GRAY)
                                    .append(src.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal(" ← ").withStyle(ChatFormatting.WHITE))
                                    .append(dst.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                                    .append(Component.literal("]").withStyle(ChatFormatting.GRAY));

                            src.sendSystemMessage(Component.literal("Request sent to ").withStyle(ChatFormatting.GREEN)
                                    .append(dst.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));

                            MutableComponent targetMsg = requestMsg.copy()
                                    .append(Component.literal(" wants you to teleport to them").withStyle(ChatFormatting.WHITE));

                            MutableComponent acceptBtn = Component.literal("[Accept]").withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.GREEN)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaccept " + src.getName().getString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Click to accept").withStyle(ChatFormatting.GREEN))));

                            MutableComponent denyBtn = Component.literal("[Deny]").withStyle(Style.EMPTY
                                    .withColor(ChatFormatting.RED)
                                    .withBold(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpdeny " + src.getName().getString()))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Click to deny").withStyle(ChatFormatting.RED))));

                            dst.sendSystemMessage(targetMsg.append(Component.literal(" ")).append(acceptBtn).append(Component.literal(" ")).append(denyBtn));
                            return 1;
                        })
                )
        );

        // tpaccept command - accept teleport request
        dispatcher.register(Commands.literal("tpaccept")
                .executes(context -> {
                    if (!context.getSource().isPlayer()) {
                        context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    ServerPlayer player = context.getSource().getPlayer();

                    if (player == null) {
                        context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    TeleportRequest request = REQUEST_MANAGER.getLatestRequestFor(player.getUUID());

                    if (request == null) {
                        context.getSource().sendFailure(Component.literal("No pending requests").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (request.isHere) {
                        PositionManager.setTpa(request.target);
                        teleportTo(request.target, request.requester);
                        request.target.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                        request.requester.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                    } else {
                        PositionManager.setTpa(request.requester);
                        teleportTo(request.requester, request.target);
                        request.requester.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                        request.target.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                    }

                    REQUEST_MANAGER.removeRequest(request.id);
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer requester = EntityArgument.getPlayer(context, "player");
                            TeleportRequest request = REQUEST_MANAGER.getRequest(requester.getUUID(), player.getUUID());

                            if (request == null) {
                                context.getSource().sendFailure(Component.literal("No request from that player").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (request.isHere) {
                                PositionManager.setTpa(request.target);
                                teleportTo(request.target, request.requester);
                                request.target.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                        .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                                request.requester.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                        .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                            } else {
                                PositionManager.setTpa(request.requester);
                                teleportTo(request.requester, request.target);
                                request.requester.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                        .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                                request.target.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                        .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                            }

                            REQUEST_MANAGER.removeRequest(request.id);
                            return 1;
                        })
                )
        );

        // tpdeny command - deny teleport request
        dispatcher.register(Commands.literal("tpdeny")
                .executes(context -> {
                    if (!context.getSource().isPlayer()) {
                        context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    ServerPlayer player = context.getSource().getPlayer();

                    if (player == null) {
                        context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    TeleportRequest request = REQUEST_MANAGER.getLatestRequestFor(player.getUUID());

                    if (request == null) {
                        context.getSource().sendFailure(Component.literal("No pending requests").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    REQUEST_MANAGER.removeRequest(request.id);
                    request.requester.sendSystemMessage(Component.literal("Your teleport request was denied").withStyle(ChatFormatting.RED));
                    player.sendSystemMessage(Component.literal("Request denied").withStyle(ChatFormatting.YELLOW));
                    return 1;
                })
                .then(Commands.argument("player", EntityArgument.player())
                        .executes(context -> {
                            if (!context.getSource().isPlayer()) {
                                context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                                return 0;
                            }
                            ServerPlayer player = context.getSource().getPlayer();

                            if (player == null) {
                                context.getSource().sendFailure(Component.literal("Player is null.").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            ServerPlayer requester = EntityArgument.getPlayer(context, "player");
                            TeleportRequest request = REQUEST_MANAGER.getRequest(requester.getUUID(), player.getUUID());

                            if (request == null) {
                                context.getSource().sendFailure(Component.literal("No request from that player").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            REQUEST_MANAGER.removeRequest(request.id);
                            request.requester.sendSystemMessage(Component.literal("Your teleport request was denied").withStyle(ChatFormatting.RED));
                            player.sendSystemMessage(Component.literal("Request denied").withStyle(ChatFormatting.YELLOW));
                            return 1;
                        })
                )
        );

        // tpaback command - teleport to last death location
        dispatcher.register(Commands.literal("tpaback")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player != null) {
                        PositionManager.PlayerLocation death = PositionManager.getTpa(player.getUUID());
                        if (death != null) {
                            death.teleport(player);
                            player.sendSystemMessage(Component.literal("Teleported to your last history teleportation.").withStyle(ChatFormatting.GREEN));
                            return 1;
                        } else {
                            player.sendSystemMessage(Component.literal("You have no recorded history teleportation.").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                    }
                    context.getSource().sendFailure(Component.literal("Only players can use this command.").withStyle(ChatFormatting.RED));
                    return 0;
                })
        );

        // back command - teleport to last death location
        dispatcher.register(Commands.literal("back")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player != null) {
                        PositionManager.PlayerLocation death = PositionManager.getDeath(player.getUUID());
                        if (death != null) {
                            death.teleport(player);
                            player.sendSystemMessage(Component.literal("Teleported to your last death location.").withStyle(ChatFormatting.GREEN));
                            return 1;
                        } else {
                            player.sendSystemMessage(Component.literal("You have no recorded death location.").withStyle(ChatFormatting.RED));
                            return 0;
                        }
                    }
                    context.getSource().sendFailure(Component.literal("Only players can use this command.").withStyle(ChatFormatting.RED));
                    return 0;
                })
        );

        // suicide command
        dispatcher.register(Commands.literal("suicide")
                .executes(context -> {
                    if (!context.getSource().isPlayer()) {
                        context.getSource().sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
                        return 0;
                    }
                    Player player = context.getSource().getPlayer();
                    if (player != null) {
                        player.kill();
                        context.getSource().sendSuccess(() -> Component.literal("Success").withStyle(ChatFormatting.GREEN), true);
                        return 1;
                    }
                    return 0;
                })
        );
    }

    private static void teleportTo(ServerPlayer a, ServerPlayer b) {
        ServerLevel world = b.serverLevel();
        a.teleportTo(world, b.getX(), b.getY(), b.getZ(), b.getYRot(), b.getXRot());
    }

    public Tpa() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            REQUEST_MANAGER.update();
        }
    }
}