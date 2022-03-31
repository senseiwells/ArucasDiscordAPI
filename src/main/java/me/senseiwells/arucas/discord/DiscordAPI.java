package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.ContextBuilder;

@SuppressWarnings("unused")
public class DiscordAPI {
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
	}
}
