package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.ContextBuilder;
import me.senseiwells.arucas.utils.ValueTypes;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;

public class DiscordAPI extends ValueTypes {
	public static final String
		DISCORD_ATTACHMENT = "DiscordAttachment",
		DISCORD_BOT = "DiscordBot",
		DISCORD_CHANNEL = "DiscordChannel",
		DISCORD_EVENT = "DiscordEvent",
		DISCORD_MESSAGE = "DiscordMessage",
		DISCORD_SERVER = "DiscordServer",
		DISCORD_USER = "DiscordUser";

	public static void addDiscordAPI(ContextBuilder builder) {
		builder.addWrappers(
			"discordapi\\Discord",
			DiscordAttachmentWrapper::new,
			DiscordBotWrapper::new,
			DiscordChannelWrapper::new,
			DiscordEventWrapper::new,
			DiscordMessageWrapper::new,
			DiscordServerWrapper::new,
			DiscordUserWrapper::new
		);
		builder.addConversion(Message.Attachment.class, DiscordAttachmentWrapper::newDiscordAttachment);
		builder.addConversion(JDA.class, DiscordBotWrapper::newDiscordBot);
		builder.addConversion(MessageChannel.class, DiscordChannelWrapper::newDiscordChannel);
		builder.addConversion(GenericEvent.class, DiscordEventWrapper::newDiscordEvent);
		builder.addConversion(Message.class, DiscordMessageWrapper::newDiscordMessage);
		builder.addConversion(Guild.class, DiscordServerWrapper::newDiscordServer);
		builder.addConversion(User.class, DiscordUserWrapper::newDiscordUser);
	}
}
