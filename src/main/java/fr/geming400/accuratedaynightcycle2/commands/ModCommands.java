package fr.geming400.accuratedaynightcycle2.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public final class ModCommands {
    private ModCommands() {}

    private static boolean isModded(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return AccurateDayNightCycle.playersWithMod.contains(context.getSource().getPlayerOrThrow().getUuid());
    }

    /**
     * Returns a "safe" version of a translated text. If the client has the mod, the translated {@link Text} object will get sent.
     * otherwise, a {@link Text} formed with {@link Text#literal(String)} will get sent instead
     * @param player the player which will get sent the text
     * @param translationKey the translation key of the text
     * @return the text but safe
     * @see Text#translatable(String)
     */
    private static MutableText safeText(ServerPlayerEntity player, String translationKey) {
        if (AccurateDayNightCycle.playersWithMod.contains(player.getUuid())) {
            return Text.translatable(translationKey);
        } else {
            MutableText text = Text.translatable(translationKey);
            Style style = text.getStyle();

            text = Text.literal(text.getString()).setStyle(style);
            return text;
        }
    }
    /**
     * Returns a "safe" version of a translated text. If the client has the mod, the translated {@link Text} object will get sent.
     * otherwise, a {@link Text} formed with {@link Text#literal(String)} will get sent instead
     * @param commandContext the {@link CommandContext}
     * @param translationKey the translation key of the text
     * @return the text but safe
     * @see Text#translatable(String)
     */
    private static MutableText safeText(CommandContext<ServerCommandSource> commandContext, String translationKey) throws CommandSyntaxException {
        return safeText(commandContext.getSource().getPlayerOrThrow(), translationKey);
    }

    /**
     * Returns a "safe" version of a translated text. If the client has the mod, the translated {@link Text} object will get sent.
     * otherwise, a {@link Text} formed with {@link Text#literal(String)} will get sent instead
     * @param player the player which will get sent the text
     * @param translationKey the translation key of the text
     * @return the text but safe
     * @see Text#translatable(String, Object...)
     */
    private static MutableText safeText(ServerPlayerEntity player, String translationKey, Object... args) {
        if (AccurateDayNightCycle.playersWithMod.contains(player.getUuid())) {
            return Text.translatable(translationKey, args);
        } else {
            MutableText text = Text.translatable(translationKey);
            Style style = text.getStyle();

            text = Text.literal(text.getString()).setStyle(style);
            return text;
        }
    }
    /**
     * Returns a "safe" version of a translated text. If the client has the mod, the translated {@link Text} object will get sent.
     * otherwise, a {@link Text} formed with {@link Text#literal(String)} will get sent instead
     * @param commandContext the {@link CommandContext}
     * @param translationKey the translation key of the text
     * @return the text but safe
     * @see Text#translatable(String, Object...)
     */
    private static MutableText safeText(CommandContext<ServerCommandSource> commandContext, String translationKey, Object... args) throws CommandSyntaxException {
        return safeText(commandContext.getSource().getPlayerOrThrow(), translationKey, args);
    }


    private static Text getClickHereText(String commandToExecute) {
        return Text.translatable("commands.acdn.clickHere")
                .setStyle(Style.EMPTY
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToExecute))
                );
    }
    private static Text getClickHereText(CommandContext<ServerCommandSource> commandContext, String commandToExecute) throws CommandSyntaxException {
        return safeText(commandContext, "commands.acdn.clickHere")
                .setStyle(Style.EMPTY
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToExecute))
                );
    }

    private static int disableGeolocation(CommandContext<ServerCommandSource> context, boolean removeData) throws CommandSyntaxException {
        if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(safeText(context, "commands.acdn.disableGeolocalisation.error.alreadyDisabled"));
        } else {
            AccurateDayNightCycle.CONFIG.useGeolocalisation(false);
            if (removeData) {
                AccurateDayNightCycle.CONFIG.ipAddress("");
                AccurateDayNightCycle.CONFIG.latitude(0);
                AccurateDayNightCycle.CONFIG.longitude(0);
            }

            Text rejoinText = safeText(context, "commands.acdn.rejoin").formatted(Formatting.GRAY, Formatting.ITALIC);
            Text toSend = safeText(context, "commands.acdn.disableGeolocalisation.success").formatted(Formatting.GREEN);
            context.getSource().sendFeedback(() -> toSend, true);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
                context.getSource().sendFeedback(() -> rejoinText, false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int enableGeolocationConfirm(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(safeText(context, "commands.acdn.enableGeolocalisation.error.alreadyEnabled", getClickHereText(context, "/adnc disable_geolocalisation")));
        } else {
            context.getSource().sendMessage(safeText(context, "commands.acdn.enableGeolocalisation.confirm.updatingDb").formatted(Formatting.ITALIC, Formatting.GRAY));

            AccurateDayNightCycle.CONFIG.useGeolocalisation(true);
            AccurateDayNightCycle.loadOrCreateDb(false);

            if (AccurateDayNightCycle.CONFIG.accurateCelestialBodies()) {
                IpUtils.Geolocation.fromConfig().updatePlayerGeolocation(context.getSource().getPlayerOrThrow());
            }

            Text rejoinText = safeText(context, "commands.acdn.rejoin").formatted(Formatting.GRAY, Formatting.ITALIC);
            Text toSend = safeText(context, "commands.acdn.enableGeolocalisation.confirm.success").formatted(Formatting.GREEN);
            context.getSource().sendFeedback(() -> toSend, true);
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER)
                context.getSource().sendFeedback(() -> rejoinText, false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int enableGeolocation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(safeText(context, "commands.acdn.enableGeolocalisation.error.alreadyEnabled", getClickHereText(context, "/adnc disable_geolocalisation")));
        } else {
            // Bad code, but it works
            // Could've been done in a prettier way, but I don't really care here
            if (isModded(context)) {
                context.getSource().sendFeedback(
                        () -> Text.translatable("commands.acdn.enableGeolocalisation.confirm", getClickHereText("/adnc enable_geolocalisation confirm")),
                        false
                );
            } else {
                Text toSend = Text.literal(safeText(context, "commands.acdn.enableGeolocalisation.confirm").getString().replace("%s", ""))
                        .append(getClickHereText(context, "/adnc enable_geolocalisation confirm"));
                context.getSource().sendFeedback(
                        () -> toSend,
                        false
                );
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private static int updateDb(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendMessage(Text.translatable("commands.acdn.enableGeolocalisation.confirm.updatingDb").formatted(Formatting.ITALIC, Formatting.GRAY));

            // It will throw in case something weird happened
            IpUtils.Geolocation geolocation = AccurateDayNightCycle.loadOrCreateDb(true);
            if (geolocation == null) {
                AccurateDayNightCycle.LOGGER.error("Got an error while trying to update DB with command '/adnc update_db'");
                return Command.SINGLE_SUCCESS;
            }

            Text toSend = safeText(context, "commands.acdn.updateDb.success");
            context.getSource().sendFeedback(() -> toSend, true);
        } else {
            // Bad code, but it works
            // Could've been done in a prettier way, but I don't really care here
            if (isModded(context)) {
                context.getSource().sendError(
                        safeText(context, "commands.acdn.updateDb.error.notEnabled", getClickHereText(context, "/adnc enable_geolocalisation"))
                );
            } else {
                context.getSource().sendError(
                        Text.literal(safeText(context, "commands.acdn.updateDb.error.notEnabled").getString().replace("%s", ""))
                                .append(getClickHereText(context, "/adnc enable_geolocalisation"))
                );
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher
                        .register(CommandManager.literal("adnc")
                                .requires(source -> source.hasPermissionLevel(2))
                                .then(CommandManager.literal("update_db").executes(ModCommands::updateDb))
                                .then(CommandManager.literal("enable_geolocalisation").executes(ModCommands::enableGeolocation)
                                        .then(CommandManager.literal("confirm").executes(ModCommands::enableGeolocationConfirm))
                                )
                                .then(CommandManager.literal("disable_geolocalisation")
                                        .then(CommandManager.argument("remove_data", BoolArgumentType.bool())
                                                .executes(context -> disableGeolocation(context, BoolArgumentType.getBool(context, "remove_data")))
                                        )
                                        .executes(context -> disableGeolocation(context, false))
                                )
                        )
        );
    }
}
