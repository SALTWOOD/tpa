package top.saltwood.tpa;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod(Tpa.MODID)
public class Tpa {
    public static final String MODID = "tpa";
    private static final TeleportRequestManager REQUEST_MANAGER = new TeleportRequestManager();

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext, Commands.CommandSelection environment) {
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
                    TeleportRequest request = REQUEST_MANAGER.getLatestRequestFor(player.getUUID());

                    if (request == null) {
                        context.getSource().sendFailure(Component.literal("No pending requests").withStyle(ChatFormatting.RED));
                        return 0;
                    }

                    if (request.isHere) {
                        request.target.teleportTo(request.requester.getX(), request.requester.getY(), request.requester.getZ());
                        request.target.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                        request.requester.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                    } else {
                        request.requester.teleportTo(request.target.getX(), request.target.getY(), request.target.getZ());
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
                            ServerPlayer requester = EntityArgument.getPlayer(context, "player");
                            TeleportRequest request = REQUEST_MANAGER.getRequest(requester.getUUID(), player.getUUID());

                            if (request == null) {
                                context.getSource().sendFailure(Component.literal("No request from that player").withStyle(ChatFormatting.RED));
                                return 0;
                            }

                            if (request.isHere) {
                                request.target.teleportTo(request.requester.getX(), request.requester.getY(), request.requester.getZ());
                                request.target.sendSystemMessage(Component.literal("Teleporting to ").withStyle(ChatFormatting.GREEN)
                                        .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                                request.requester.sendSystemMessage(Component.literal("Request accepted by ").withStyle(ChatFormatting.GREEN)
                                        .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
                            } else {
                                request.requester.teleportTo(request.target.getX(), request.target.getY(), request.target.getZ());
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

        // back command - teleport to last death location
        dispatcher.register(Commands.literal("back")
                .executes(context -> teleBack(context.getSource()))
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

    private static int teleBack(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("Only players can use this command").withStyle(ChatFormatting.RED));
            return 0;
        }

        DeathPositionManager.PlayerDeathLocation location = DeathPositionManager.getLocation(player.getUUID());

        if (location != null) {
            player.teleportTo(location.x, location.y, location.z);
            source.sendSuccess(() -> Component.literal("Teleported to last death location").withStyle(ChatFormatting.GREEN), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("No recorded death location").withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    public Tpa() {
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTick);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    private void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            REQUEST_MANAGER.update();
        }
    }

    private static class TeleportRequest {
        private static int nextId = 0;

        final int id;
        final ServerPlayer requester;
        final ServerPlayer target;
        final boolean isHere;
        final long createdAt;

        TeleportRequest(ServerPlayer requester, ServerPlayer target, boolean isHere) {
            this.id = nextId++;
            this.requester = requester;
            this.target = target;
            this.isHere = isHere;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return System.currentTimeMillis() - createdAt > 60000;
        }
    }

    private static class TeleportRequestManager {
        private final Map<Integer, TeleportRequest> requests = new HashMap<>();
        private final Map<UUID, List<Integer>> playerRequests = new HashMap<>();

        void addRequest(ServerPlayer requester, ServerPlayer target, boolean isHere) {
            TeleportRequest request = new TeleportRequest(requester, target, isHere);
            requests.put(request.id, request);

            playerRequests.computeIfAbsent(requester.getUUID(), k -> new ArrayList<>()).add(request.id);
            playerRequests.computeIfAbsent(target.getUUID(), k -> new ArrayList<>()).add(request.id);
        }

        void removeRequest(int id) {
            TeleportRequest request = requests.remove(id);
            if (request != null) {
                playerRequests.get(request.requester.getUUID()).remove(Integer.valueOf(id));
                playerRequests.get(request.target.getUUID()).remove(Integer.valueOf(id));
            }
        }

        TeleportRequest getRequest(UUID requesterId, UUID targetId) {
            for (TeleportRequest request : requests.values()) {
                if (request.requester.getUUID().equals(requesterId) &&
                        request.target.getUUID().equals(targetId)) {
                    return request;
                }
            }
            return null;
        }

        TeleportRequest getLatestRequestFor(UUID playerId) {
            List<Integer> ids = playerRequests.get(playerId);
            if (ids == null || ids.isEmpty()) return null;

            TeleportRequest latest = null;
            for (Integer id : ids) {
                TeleportRequest request = requests.get(id);
                if (request != null && (latest == null || request.createdAt > latest.createdAt)) {
                    latest = request;
                }
            }
            return latest;
        }

        boolean hasPendingRequest(UUID requesterId, UUID targetId) {
            return getRequest(requesterId, targetId) != null;
        }

        void update() {
            List<Integer> expired = new ArrayList<>();

            for (TeleportRequest request : requests.values()) {
                if (request.isExpired()) {
                    expired.add(request.id);
                }
            }

            for (Integer id : expired) {
                TeleportRequest request = requests.get(id);
                if (request != null) {
                    MutableComponent requestMsg = Component.literal("[").withStyle(ChatFormatting.GRAY)
                            .append(request.requester.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(request.isHere ? " ← " : " → ").withStyle(ChatFormatting.WHITE))
                            .append(request.target.getDisplayName().copy().withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal("]").withStyle(ChatFormatting.GRAY));

                    request.requester.sendSystemMessage(requestMsg.copy()
                            .append(Component.literal(" Teleport request expired").withStyle(ChatFormatting.RED)));
                    request.target.sendSystemMessage(requestMsg.copy()
                            .append(Component.literal(" Teleport request expired").withStyle(ChatFormatting.RED)));
                    removeRequest(id);
                }
            }
        }
    }
}