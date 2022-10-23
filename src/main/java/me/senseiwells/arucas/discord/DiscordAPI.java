package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.ArucasAPI;
import me.senseiwells.arucas.api.docs.parser.DocParser;
import me.senseiwells.arucas.core.Arucas;
import me.senseiwells.arucas.discord.definitions.*;
import me.senseiwells.arucas.discord.impl.DiscordBot;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;

import java.nio.file.Path;

public class DiscordAPI {
	public static final String
		DISCORD_ATTACHMENT = "DiscordAttachment",
		DISCORD_BOT = "DiscordBot",
		DISCORD_CHANNEL = "DiscordChannel",
		DISCORD_EVENT = "DiscordEvent",
		DISCORD_MESSAGE = "DiscordMessage",
		DISCORD_SERVER = "DiscordServer",
		DISCORD_USER = "DiscordUser";

	@SuppressWarnings({"unchecked", "unused"})
	public static void addDiscordAPI(ArucasAPI.Builder builder) {
		builder.addClassDefinitions(
			"discordapi.Discord",
			DiscordAttachmentDef::new,
			DiscordBotDef::new,
			DiscordChannelDef::new,
			DiscordEventDef::new,
			DiscordMessageDef::new,
			DiscordServerDef::new,
			DiscordUserDef::new
		);
		builder.addConversion(Message.Attachment.class, (a, i) -> i.create(DiscordAttachmentDef.class, a));
		builder.addConversion(DiscordBot.class, (d, i) -> i.create(DiscordBotDef.class, d));
		builder.addConversion(JDA.class, (j, i) -> i.create(DiscordBotDef.class, new DiscordBot(j, i)));
		builder.addConversion(TextChannel.class, (t, i) -> i.create(DiscordChannelDef.class, t));
		builder.addConversion(GenericEvent.class, (e, i) -> i.create(DiscordEventDef.class, e));
		builder.addConversion(Message.class, (m, i) -> i.create(DiscordMessageDef.class, m));
		builder.addConversion(Guild.class, (g, i) -> i.create(DiscordServerDef.class, g));
		builder.addConversion(User.class, (u, i) -> i.create(DiscordUserDef.class, u));
	}

	public static void main(String[] args) {
		ArucasAPI.Builder builder = new ArucasAPI.Builder();
		builder.addDefault();
		addDiscordAPI(builder);
		DocParser.generateAll(Path.of("generated"), builder.build());
	}
}
