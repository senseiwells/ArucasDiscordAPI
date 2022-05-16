package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.ArucasClass;
import me.senseiwells.arucas.api.wrappers.ArucasDefinition;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.FileValue;
import me.senseiwells.arucas.values.MapValue;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;
import static me.senseiwells.arucas.utils.ValueTypes.STRING;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_EVENT,
	desc = "This class is an event wrapper that you can use to access event parameters."
)
@ArucasClass(name = "DiscordEvent")
public class DiscordEventWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private GenericEvent event;

	@FunctionDoc(
		name = "getEventName",
		desc = "This gets the name of the event",
		returns = {STRING, "the name of the event"},
		example = "event.getEventName();"
	)
	@ArucasFunction
	public StringValue getEventName(Context context) {
		return StringValue.of(this.event.getClass().getSimpleName());
	}

	@FunctionDoc(
		name = "getMessage",
		desc = "This gets the message that is related to the event",
		returns = {DISCORD_MESSAGE, "the message"},
		throwMsgs = "... has no message",
		example = "event.getMessage();"
	)
	@ArucasFunction
	public WrapperClassValue getMessage(Context context) throws CodeError {
		return DiscordMessageWrapper.newDiscordMessage(this.getMessage(), context);
	}

	@FunctionDoc(
		name = "getUser",
		desc = "This gets the user that is related to the event",
		returns = {DISCORD_USER, "the user"},
		throwMsgs = "... has no user",
		example = "event.getUser();"
	)
	@ArucasFunction
	public WrapperClassValue getUser(Context context) throws CodeError {
		return DiscordUserWrapper.newDiscordUser(this.getUser(), context);
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets the channel that is related to the event",
		returns = {DISCORD_CHANNEL, "the channel"},
		throwMsgs = "... has no channel",
		example = "event.getChannel();"
	)
	@ArucasFunction
	public WrapperClassValue getChannel(Context context) throws CodeError {
		return DiscordChannelWrapper.newDiscordChannel(this.getChannel(), context);
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets the server that is related to the event",
		returns = {DISCORD_SERVER, "the server"},
		throwMsgs = "... has no server",
		example = "event.getServer();"
	)
	@ArucasFunction
	public WrapperClassValue getServer(Context context) throws CodeError {
		return DiscordServerWrapper.newDiscordServer(this.getServer(), context);
	}

	@FunctionDoc(
		name = "reply",
		desc = "This replies to the event with the given message",
		params = {STRING, "the message"},
		example = "event.reply('Reply!');"
	)
	@ArucasFunction
	public void reply(Context context, StringValue message) {
		this.getReplyCallback().reply(message.value).complete();
	}

	@FunctionDoc(
		name = "replyWithEmbed",
		desc = {
			"This replies to the event with the given embed map",
			"In the embed map, you can use the following keys:",
			"'title' as String, ''description' as String or List of String, 'colour'/'color' as Number",
			"'fields' as Map with keys: ('name' as String, 'value' as String, 'inline' as Boolean)",
			"and 'image' as String that is an url"
		},
		params = {MAP, "embedMap", "the embed map"},
		example = """
		event.replyWithEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	@ArucasFunction
	public void replyWithEmbed(Context context, MapValue mapValue) throws CodeError {
		this.getReplyCallback().replyEmbeds(DiscordUtils.parseMapAsEmbed(context, mapValue)).complete();
	}

	@FunctionDoc(
		name = "replyWithFile",
		desc = "This replies to the event with the given file",
		params = {FILE, "file", "the file"},
		example = "event.replyWithFile(new File('/path/to/file.txt'));"
	)
	@ArucasFunction
	public void replyWithFile(Context context, FileValue fileValue) {
		this.getReplyCallback().replyFile(fileValue.value).complete();
	}

	private RuntimeException invalidEvent(String details) {
		return new RuntimeException("'%s' %s".formatted(this.event.getClass().getSimpleName(), details));
	}

	private IReplyCallback getReplyCallback() {
		if (this.event instanceof IReplyCallback iReplyCallback) {
			return iReplyCallback;
		}
		throw this.invalidEvent("cannot reply");
	}

	private Message getMessage() {
		return this.getMessage("has no message");
	}

	private Message getMessage(String error) {
		if (this.event instanceof MessageReceivedEvent receivedEvent) {
			return receivedEvent.getMessage();
		}
		if (this.event instanceof MessageUpdateEvent updateEvent) {
			return updateEvent.getMessage();
		}
		if (this.event instanceof GenericMessageReactionEvent reactionEvent) {
			return reactionEvent.retrieveMessage().complete();
		}
		throw this.invalidEvent(error);
	}

	private User getUser() {
		if (this.event instanceof GuildBanEvent banEvent) {
			return banEvent.getUser();
		}
		if (this.event instanceof Interaction interactionEvent) {
			return interactionEvent.getUser();
		}
		return this.getMessage("has no user").getAuthor();
	}

	private MessageChannel getChannel() {
		if (this.event instanceof GenericChannelEvent channelEvent && channelEvent.getChannel() instanceof MessageChannel messageChannel) {
			return messageChannel;
		}
		if (this.event instanceof Interaction interactionEvent) {
			return interactionEvent.getMessageChannel();
		}
		return this.getMessage("has no channel").getChannel();
	}

	private Guild getServer() {
		if (this.event instanceof GenericGuildEvent guildEvent) {
			return guildEvent.getGuild();
		}
		if (this.event instanceof Interaction interactionEvent) {
			return interactionEvent.getGuild();
		}
		return this.getMessage("has no server").getGuild();
	}

	public static WrapperClassValue newDiscordEvent(GenericEvent event, Context context) throws CodeError {
		DiscordEventWrapper eventWrapper = new DiscordEventWrapper();
		eventWrapper.event = event;
		return DEFINITION.createNewDefinition(eventWrapper, context, List.of());
	}

	@Override
	public GenericEvent asJavaValue() {
		return this.event;
	}
}
