package org.atmosia.simpleirc;

import net.minecraft.stat.Stat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {
    public static Map<String, HashMap<String, IRCUserModes>> StatusPrefixes = new HashMap<>();
    private static Boolean RefreshNamesFlag = false;
    public static void HandleMessage(String message) {
        // :baseduser.eu.org NOTICE * :*** Looking up your hostname...
        // :FJSAGS_Web!FJSAGS@SomeIpInfo PRIVMSG #ss15 :i need this for testing and understanding of IRC protocol...
        // General structure
        // :Source Command Channel :Content

        if (!message.startsWith(":")) return;
        message = message.substring(1);

        var splits = message.split(" ");
        var source = splits[0];
        var command = splits[1];
        var channel = splits[2];
        var content = String.join(" ", Arrays.stream(splits).toList().subList(3, splits.length));

        // So we end up with
        // Source = baseduser.eu.org
        // Command = NOTICE
        // Channel = *
        // Content = :*** Looking up your hostname...

        // If I am getting a numeric code, like this
        //  :baseduser.eu.org 001 FJSAGS :Welcome to the pissnet IRC Network FJSAGS!FJSAGS@Ip
        // Source = baseduser.eu.org
        // Command = 001
        // Channel = FJSAGS (PM)
        // Content = Welcome to the pissnet IRC Network FJSAGS!FJSAGS@Ip

        // :jupiter.baseduser.eu.org PRIVMSG #ss15 :lm
        var numeric = -1;
        try {
            numeric = Integer.parseInt(command);
        }
        catch (NumberFormatException e) {
            HandleCommand(source, command, channel, content);
            return;
        }
        if (numeric != -1)
            HandleNumeric(source, numeric, channel, content);

    }
    private static void HandleCommand(String source, String command, String channel, String content) {
        if (command.equals("NOTICE"))
        {
            ChatUtils.IRCMessageTemplates.Notice(source.split("!")[0], channel, content.replaceFirst(":", ""));
        }
        else if (command.equals("PRIVMSG")) {
            ChatUtils.IRCMessageTemplates.Message(source.split("!")[0], channel, content.replaceFirst(":", ""));

        }
        else if (command.equals("JOIN")) {
            // FJSAGS_Web!FJSAGS@Ip JOIN :#ss15-pub
            ChatUtils.IRCMessageTemplates.Join(source.split("!")[0], channel.substring(channel.indexOf(':') + 1));
            if (source.split("!")[0].equals(MainClient.irc.nickname())) {
                MainClient.irc.AddChannel(new IRCChannel(channel.substring(channel.indexOf(':') + 1), "", false), false);
            }
        }
        else if (command.equals("PART")) {
            // Source =  FJSAGS_Web!FJSAGS@Ip
            // Command = PART
            // Channel = #ss15-pub
            // Content = :Leaving
            ChatUtils.IRCMessageTemplates.Part(source.split("!")[0], channel, content.substring(channel.indexOf(':') + 1));
        }
        else if (command.equals("NICK")) {
            // :FJs NICK :FJSAGS_
            // Source = FJs
            // Command = NICK
            // Content = :FJSAGS_
            if (MainClient.irc.nickname().equals(source)) {
                MainClient.irc.SetDisplayNickname(channel.replaceFirst(":", ""));

            }
            for (var entry : StatusPrefixes.values()) {
                IRCUserModes mode = entry.remove(source);
                entry.putIfAbsent(source.split("!")[0], mode);
            }
            ChatUtils.Notify(source.split("!")[0] + " nickname is now " + channel.substring(channel.indexOf(':') + 1));
        }
        else if (command.equals("MODE")) {
            RefreshNamesFlag = true;
            MainClient.irc.SendLine("NAMES " + channel); // yeah. dont ask
        }
        else if (command.equals("QUIT")) {
            // [02:32:39] :FJSAGS_Web!FJSAGS@Ip QUIT :Quit: bye-bye six seven
            ChatUtils.IRCMessageTemplates.Quit(source.split("!")[0], content);
        }
    }
    private static void HandleNumeric(String source, Integer numeric, String channel, String content) {
        var contentInParts = content.split(" ");
        if (numeric == 1) {
            // RPL_WELCOME. Just send this to chat.
            //  :baseduser.eu.org 001 FJSAGS :Welcome to the pissnet IRC Network FJSAGS!FJSAGS@Ip
            // Source = baseduser.eu.org
            // Command = 001
            // Channel = FJSAGS (PM)
            // Content = Welcome to the pissnet IRC Network FJSAGS!FJSAGS@Ip
            ChatUtils.IRCMessageTemplates.Message(source, channel, content);
        }

        else if (numeric == 353) {
            // :baseduser.eu.org 353 FJSAGS @ #ss15 :FJSAGS FJSAGS_Web @Katee__ @router
            // Source = baseduser.eu.org
            // Command = 353
            // Channel = FJSAGS (PM)
            // Content = @ #ss15 :FJSAGS FJSAGS_Web @Katee__ @router

            String channelType = "channel ";
            if (contentInParts[0].equals("@")) {
                channelType = "secret channel ";
            }
            else if (contentInParts[0].equals("+"))
                channelType = "private channel ";
            String message = "Users on " + channelType + contentInParts[1] + ": ";
            System.out.println(content);
            var people = content.split(":")[1].split(" ");
            GetStatuses(people, contentInParts[1]);
            StringBuilder formattedPeople = new StringBuilder();
            for (String person : people) {
                formattedPeople.append(GetFormattedUser(person, channel)).append(" ");
            }
            if (!RefreshNamesFlag){
                ChatUtils.Verbose(message + formattedPeople);
            }
            RefreshNamesFlag = false;
        }
        else if (numeric == 332) {
            // [RAW <=] baseduser.eu.org 332 FJSAGS #ss15-pub :Home of the Russian Mafia since 2024-07
            // Source = baseduser.eu.org
            // Numeric = 332
            // Channel = FJSAGS (PM)
            // Content = #ss15-pub :Home of the Russian Mafia since 2024-07...
            var topic = content.split(":")[1];
            var topicChannel = content.split(":")[0];

            ChatUtils.IRCMessageTemplates.Topic(topicChannel, topic);
        }
        else if (numeric == 333) {
            // [RAW <=] baseduser.eu.org 333 FJSAGS #ss15-pub router!router@pissnet/staff/router 1764696482
            var topicAuthor = contentInParts[1].split("!")[0];
            var channelTopic = contentInParts[0];
            ChatUtils.IRCMessageTemplates.TopicSetBy(channelTopic, topicAuthor);
        }
        else if (numeric == 401) {
            // :baseduser.eu.org 401 FGSAGS :No such nick/channel
            // Source = baseduser.eu.org
            // Numeric = 401
            // Channel = FGSAGS
            // Content = :No such nick/channel
            var actualContent = content.split(":")[1];
            ChatUtils.IRCMessageTemplates.Message(source, channel, actualContent);
        } else if (numeric == 404) {
            // :baseduser.eu.org 404 FJSAGS #ss16 :No external channel messages...
            var actualContent = content.split(":")[1];
            ChatUtils.IRCMessageTemplates.Message(source, channel, actualContent);
        } else if (numeric == 433) {
            // [RAW <=] :baseduser.eu.org 433 * FJSAGS :Nickname is already in use.
            // Source = baseduser.eu.org
            // Numeric = 433
            // Channel = *
            // Content = FJSAGS :No such nick/channel
            var nicknameInUse = content.split(":")[0];

            ChatUtils.Error("Nickname <blue>" + nicknameInUse + "</blue> is already in use!");
            ChatUtils.Error("Reconnecting using a backup nickname!");
            MainClient.irc.UseBackupNickname();
        } else if (numeric == 900) {
            ChatUtils.IRCMessageTemplates.Notice(source, channel, "You are now authenticated.");
        }
        else if (numeric == 903) {
            MainClient.irc.SendLine("CAP END");
        }
    }

    private static void GetStatuses(String[] people, String channelName) {
        var statusesForChannel = new HashMap<String, IRCUserModes>();

        for (String person : people) {
            person = person.split("!")[0];
            if (person.startsWith("~")) {
                statusesForChannel.putIfAbsent(person.substring(1), IRCUserModes.OWNER);
            }
            else if (person.startsWith("&")) {
                statusesForChannel.putIfAbsent(person.substring(1), IRCUserModes.ADMIN);
            }
            else if (person.startsWith("@")) {
                statusesForChannel.putIfAbsent(person.substring(1), IRCUserModes.OPERATOR);
            }
            else if (person.startsWith("%")) {
                statusesForChannel.putIfAbsent(person.substring(1), IRCUserModes.HALF_OPERATOR);
            }
            else if (person.startsWith("+")) {
                statusesForChannel.putIfAbsent(person.substring(1), IRCUserModes.VOICE);
            }
            else {
                statusesForChannel.putIfAbsent(person, IRCUserModes.NONE);
            }
        }
        StatusPrefixes.put(channelName, statusesForChannel);
    }

    public static String GetUserModePrefix(String nickname, String channel) {
        var channelPrefixes = StatusPrefixes.getOrDefault(channel, null);
        if (channelPrefixes == null) return "";
        var prefix = channelPrefixes.getOrDefault(nickname, IRCUserModes.NONE);

        return switch (prefix) {
            case OWNER -> "<yellow>~</yellow>";
            case ADMIN -> "<gold>&</gold>";
            case OPERATOR -> "<green>@</green>";
            case HALF_OPERATOR -> "<dark_blue>%</dark_blue>";
            case VOICE -> "<aqua>+</aqua>";
            case NONE -> "";
        };
    }
    public static String GetFormattedUser(String nickname, String channel) {
        return GetUserModePrefix(nickname, channel) + "<red>" + nickname + "</red>";
    }
}
