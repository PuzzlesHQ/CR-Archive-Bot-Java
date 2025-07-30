package dev.puzzleshq.CRArchiveBot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static dev.puzzleshq.CRArchiveBot.Constants.channelID;
import static dev.puzzleshq.CRArchiveBot.Constants.serverID;
import static org.kohsuke.github.ReactionContent.HEART;

public class DiscordBot extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger("Discord-bot");

    public static void main(String[] args) throws IOException {
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

            jda.getRestPing().queue(ping ->
                    System.out.println("Logged in with ping: " + ping)
            );

            // If you want to access the cache, you can use awaitReady() to block the main thread until the jda instance is fully loaded
            jda.awaitReady();

            // Now we can access the fully loaded cache and show some statistics or do other cache dependent things
            System.out.println("Guilds: " + jda.getGuildCache().size());
        } catch (InterruptedException e) {
            // Thrown if the awaitReady() call is interrupted
            e.printStackTrace();
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

        if (guild.getId().equals(serverID) && channel.getId().equals(channelID) && channel.getType().isMessage()) { if (displayMessage.contains("@Game Updates")){

           String changelog = displayMessage.lines().skip(1).collect(Collectors.joining("\n"));
           String version = Pattern.compile("(?<=Cosmic Reach )\\d+\\.\\d+\\.\\d+(?= is out!)").matcher(displayMessage).results().findFirst().map(MatchResult::group).orElse(null);
           List<Message.Attachment> attachmentList = message.getAttachments().isEmpty() ? null : message.getAttachments();


            if (attachmentList != null){
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

    }}

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getEmoji().equals(HEART))
            System.out.println("A user loved a message!");
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
