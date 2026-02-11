package fr.geming400.accuratedaynightcycle2.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import fr.geming400.accuratedaynightcycle2.AccurateDayNightCycle;
import fr.geming400.accuratedaynightcycle2.utils.IpUtils;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ModCommands {
    private ModCommands() {}

    private static Text getClickHereText(String commandToExecute) {
        return Text.translatable("commands.acdn.clickHere")
                .setStyle(Style.EMPTY
                        .withColor(Formatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandToExecute))
                );
    }

    private static int disableGeolocation(CommandContext<ServerCommandSource> context, boolean removeData) {
        if (!AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(Text.translatable("commands.acdn.disableGeolocalisation.error.alreadyDisabled"));
        } else {
            AccurateDayNightCycle.CONFIG.useGeolocalisation(false);
            if (removeData) {
                AccurateDayNightCycle.CONFIG.ipAddress(AccurateDayNightCycle.IP_ADDRESS_UNSET);
                AccurateDayNightCycle.CONFIG.latitude(0);
                AccurateDayNightCycle.CONFIG.longitude(0);
            }

            context.getSource().sendFeedback(() -> Text.translatable("commands.acdn.disableGeolocalisation.success").formatted(Formatting.GREEN), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int enableGeolocationConfirm(CommandContext<ServerCommandSource> context) {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(Text.translatable("commands.acdn.enableGeolocalisation.error.alreadyEnabled", getClickHereText("/adnc disable_geolocalisation")));
        } else {
            context.getSource().sendMessage(Text.translatable("commands.acdn.enableGeolocalisation.confirm.updatingDb").formatted(Formatting.ITALIC, Formatting.GRAY));

            AccurateDayNightCycle.CONFIG.useGeolocalisation(true);
            AccurateDayNightCycle.loadOrCreateDb(false);
            context.getSource().sendFeedback(() -> Text.translatable("commands.acdn.enableGeolocalisation.confirm.success").formatted(Formatting.GREEN), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int enableGeolocation(CommandContext<ServerCommandSource> context) {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendError(Text.translatable("commands.acdn.enableGeolocalisation.error.alreadyEnabled", getClickHereText("/adnc disable_geolocalisation")));
        } else {
            context.getSource().sendFeedback(
                    () -> Text.translatable("commands.acdn.enableGeolocalisation.confirm", getClickHereText("/adnc enable_geolocalisation confirm")),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    @SuppressWarnings("SameReturnValue")
    private static int updateDb(CommandContext<ServerCommandSource> context) {
        if (AccurateDayNightCycle.CONFIG.useGeolocalisation()) {
            context.getSource().sendMessage(Text.translatable("commands.acdn.enableGeolocalisation.confirm.updatingDb").formatted(Formatting.ITALIC, Formatting.GRAY));

            // It will throw in case something weird happened
            IpUtils.Geolocation geolocation = AccurateDayNightCycle.loadOrCreateDb(true);
            if (geolocation == null) {
                AccurateDayNightCycle.LOGGER.error("Got an error while trying to update DB with command '/adnc update_db'");
                return Command.SINGLE_SUCCESS;
            }

            context.getSource().sendFeedback(() -> Text.translatable("commands.acdn.updateDb.success"), true);
        } else {
            context.getSource().sendError(
                    Text.translatable("commands.acdn.updateDb.error.notEnabled", getClickHereText("/adnc enable_geolocalisation"))
            );
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
                        ));
    }
}
