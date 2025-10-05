package dev.puzzleshq.CRArchiveBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.puzzleshq.CRArchiveBot.Constants.channelID;
import static dev.puzzleshq.CRArchiveBot.Constants.serverID;

public class DiscordBot extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("Discord-bot");
    public static final String[] SPINNER_FRAMES = {"|", "/", "⟋", "—", "⟍", "\\"};

    public static String gitVersion;
    public static String itchVersion;
    public static boolean mismatch;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        String token = System.getenv("TOKEN");

        EnumSet<GatewayIntent> intents = EnumSet.of(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.MESSAGE_CONTENT
        );

        // To start the bot, you have to use the JDABuilder.

        // You can choose one of the factory methods to build your bot:
        // - createLight(...)
        // - createDefault(...)
        // - create(...)
        // Each of these factory methods use different defaults, you can check the documentation for more details.

        try {
            JDA jda = JDABuilder.createLight(token, intents)
                    .addEventListeners(new DiscordBot())
                    .setActivity(Activity.watching("the archive"))
                    .build();

            CommandListUpdateAction commands = jda.updateCommands();

            commands.addCommands(
                    Commands.slash("archive", "Manually update the archive")
                            // The default integration types are GUILD_INSTALL.
                            // Can't use this in DMs, and in guilds the bot isn't in.
                            .setContexts(InteractionContextType.GUILD)
                            .setDefaultPermissions(DefaultMemberPermissions.DISABLED) // only admins should be able to use this command.
            ).queue();

            jda.getRestPing().queue(ping ->
                    System.out.println("Logged in with ping: " + ping)
            );

            // If you want to access the cache, you can use awaitReady() to block the main thread until the jda instance is fully loaded
            jda.awaitReady();

            // Now we can access the fully loaded cache and show some statistics or do other cache dependent things
            System.out.println("Guilds: " + jda.getGuildCache().size());
        } catch (InterruptedException e) {
            // Thrown if the awaitReady() call is interrupted
            logger.trace(String.valueOf(e));
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild()) return;
        User author = event.getAuthor();
        Guild guild = event.getGuild();
        MessageChannelUnion channel = event.getChannel();
        Message message = event.getMessage();
        String displayMessage = message.getContentRaw();

        if (guild.getId().equals(serverID) && channel.getId().equals(channelID) && channel.getType().isMessage()) {
            if (displayMessage.contains("@Game Updates")) {

                String changelog = displayMessage.lines().skip(1).collect(Collectors.joining("\n"));
                String version = Pattern.compile("(?<=Cosmic Reach )\\d+\\.\\d+\\.\\d+(?= is out!)").matcher(displayMessage).results().findFirst().map(MatchResult::group).orElse(null);
                List<Message.Attachment> attachmentList = message.getAttachments().isEmpty() ? null : message.getAttachments();


                if (attachmentList != null) {
                    System.out.printf("[%s] %s \n %s \n",
                            version,
                            attachmentList.size(),
                            changelog
                    );
                } else {
                    System.out.printf("[%s] [%#s] %#s: %s \n",
                            event.getGuild().getName(),
                            channel,
                            author,
                            message.getContentDisplay()
                    );
                }

                CRArchiveUpdater.updateCRArchive(changelog, version, attachmentList);

            }
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        // Only accept commands from guilds
        if (event.getGuild() == null)
            return;
        switch (event.getName()) {
            case "archive":
                archive(event);
                break;
            default:
                event.reply("I can't handle that command right now :(").setEphemeral(true).queue();
        }
    }

    public void archive(SlashCommandInteractionEvent event) {
        if (!isRunning.compareAndSet(false, true)) {
            event.reply("Archival in progress").setEphemeral(true).queue();
            return;
        }

        List<String> steps = List.of("Checking git", "Checking itch", "Downloading files", "Creating release", "Uploading files");
        List<Boolean> completed = new ArrayList<>(Collections.nCopies(steps.size(), false));

//        event.deferReply().queue(hook -> {
//            hook.sendMessage("Preparing...").queue(message -> {
//                runStepsSequentially(steps, completed, message, 0)
//                        .whenComplete((_, _) -> isRunning.set(false));  // release lock
//            });
//        });
    }


    public static CompletableFuture<Void> checkGit() {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                gitVersion = "0.4.15";
            } catch (InterruptedException ignored) {
            }
        });
    }

    public static CompletableFuture<Void> checkItch() {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
                itchVersion = "0.4.15";
            } catch (InterruptedException ignored) {
            }
        });
    }

    public static CompletableFuture<Void> downloadFiles() {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        });
    }

    public static CompletableFuture<Void> createRelease() {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        });
    }

    public static CompletableFuture<Void> uploadFiles() {
        return CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {
            }
        });
    }

//    [Puzzle HQ] [#cosmic-reach-announcements] Puzzle Testing Server #annv2: @test  **Cosmic Reach 0.4.13 is out!**
//            - Added the Planteater - a passive mob that you can feed plants by dropping them on the ground as items
//    - Added Steak when killed
//    - When fed, it drops Manure after some time which fertilizes crops and saplings
//    - Do NOT put manure in the furnace!
//            - Mob spawning has been reworked, hostiles should only spawn in dark places for now
//    - However, due to lighting bugs, this may not always be the case
//            - Massively reduced the random tick rate. It now runs at 1/16th the speed.
//- Fixed bug where a friendly interceptor trap would randomly spawn a hostile laser interceptor
//- Fixed bug where friendly interceptors would not attack enemies
//- Fixed bug where friendly interceptors would attack ghost entities
//- Removed corn kernels from interceptor loot
//    image.png : https://cdn.discordapp.com/attachments/1399940979259216013/1399941923875061780/image.png?ex=688ad504&is=68898384&hm=6325ea1e7771d4b2758a9443721d0020d871e5dc4f2875df06c3a6ac5db0c81a& : net.dv8tion.jda.api.utils.NamedAttachmentProxy@622412e5 : https://media.discordapp.net/attachments/1399940979259216013/1399941923875061780/image.png?ex=688ad504&is=68898384&hm=6325ea1e7771d4b2758a9443721d0020d871e5dc4f2875df06c3a6ac5db0c81a&


}