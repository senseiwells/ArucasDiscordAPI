package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.builtin.FileDef;
import me.senseiwells.arucas.builtin.MapDef;
import me.senseiwells.arucas.builtin.StringDef;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.exceptions.RuntimeError;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.Util;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.GenericGuildEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_EVENT,
	desc = "This class is an event wrapper that you can use to access event parameters.",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordEventDef extends CreatableDefinition<GenericEvent> {
	public DiscordEventDef(Interpreter interpreter) {
		super(DISCORD_EVENT, interpreter);
	}

	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("getEventName", this::getEventName),
			MemberFunction.of("getMessage", this::getMessage),
			MemberFunction.of("getUser", this::getUser),
			MemberFunction.of("getChannel", this::getChannel),
			MemberFunction.of("getServer", this::getServer),
			MemberFunction.of("reply", 1, this::reply),
			MemberFunction.of("replyWithEmbed", 1, this::replyWithEmbed),
			MemberFunction.of("replyWithFile", 1, this::replyWithFile)
		);
	}

	@FunctionDoc(
		name = "getEventName",
		desc = "This gets the name of the event",
		returns = {STRING, "the name of the event"},
		examples = "event.getEventName();"
	)
	public String getEventName(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		return event.getClass().getSimpleName();
	}

	@FunctionDoc(
		name = "getMessage",
		desc = "This gets the message that is related to the event",
		returns = {DISCORD_MESSAGE, "the message"},
		examples = "event.getMessage();"
	)
	public Message getMessage(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		return getMessage(event);
	}

	@FunctionDoc(
		name = "getUser",
		desc = "This gets the user that is related to the event",
		returns = {DISCORD_USER, "the user"},
		examples = "event.getUser();"
	)
	public User getUser(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		return getUser(event);
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets the channel that is related to the event",
		returns = {DISCORD_CHANNEL, "the channel"},
		examples = "event.getChannel();"
	)
	public MessageChannel getChannel(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		return getChannel(event);
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets the server that is related to the event",
		returns = {DISCORD_SERVER, "the server"},
		examples = "event.getServer();"
	)
	public Guild getServer(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		return getServer(event);
	}

	@FunctionDoc(
		name = "reply",
		desc = "This replies to the event with the given message",
		params = {STRING, "message", "the message"},
		examples = "event.reply('Reply!');"
	)
	public Void reply(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		String message = arguments.nextPrimitive(StringDef.class);
		getReplyCallback(event).reply(message).complete();
		return null;
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
		examples = """
		event.replyWithEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	public Void replyWithEmbed(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		ArucasMap map = arguments.nextPrimitive(MapDef.class);
		getReplyCallback(event).replyEmbeds(DiscordUtils.parseMapAsEmbed(arguments.getInterpreter(), map)).complete();
		return null;
	}

	@FunctionDoc(
		name = "replyWithFile",
		desc = "This replies to the event with the given file",
		params = {FILE, "file", "the file"},
		examples = "event.replyWithFile(new File('/path/to/file.txt'));"
	)
	public Void replyWithFile(Arguments arguments) {
		GenericEvent event = arguments.nextPrimitive(this);
		File file = arguments.nextPrimitive(FileDef.class);
		getReplyCallback(event).replyFiles(FileUpload.fromData(file)).complete();
		return null;
	}

	private static void invalidEvent(GenericEvent event, String details) {
		throw new RuntimeError("'%s' %s".formatted(event.getClass().getSimpleName(), details));
	}

	private static IReplyCallback getReplyCallback(GenericEvent event) {
		if (event instanceof IReplyCallback iReplyCallback) {
			return iReplyCallback;
		}
		invalidEvent(event, "cannot reply");
		throw null;
	}

	private static Message getMessage(GenericEvent event) {
		return getMessage(event, "has no message");
	}

	private static Message getMessage(GenericEvent event, String error) {
		if (event instanceof MessageReceivedEvent receivedEvent) {
			return receivedEvent.getMessage();
		}
		if (event instanceof MessageUpdateEvent updateEvent) {
			return updateEvent.getMessage();
		}
		if (event instanceof GenericMessageReactionEvent reactionEvent) {
			return reactionEvent.retrieveMessage().complete();
		}
		invalidEvent(event, error);
		throw null;
	}

	private static User getUser(GenericEvent event) {
		if (event instanceof GuildBanEvent banEvent) {
			return banEvent.getUser();
		}
		if (event instanceof Interaction interactionEvent) {
			return interactionEvent.getUser();
		}
		return getMessage(event, "has no user").getAuthor();
	}

	private static MessageChannel getChannel(GenericEvent event) {
		if (event instanceof GenericChannelEvent channelEvent && channelEvent.getChannel() instanceof MessageChannel messageChannel) {
			return messageChannel;
		}
		if (event instanceof Interaction interactionEvent) {
			return interactionEvent.getMessageChannel();
		}
		return getMessage(event, "has no channel").getChannel();
	}

	private static Guild getServer(GenericEvent event) {
		if (event instanceof GenericGuildEvent guildEvent) {
			return guildEvent.getGuild();
		}
		if (event instanceof Interaction interactionEvent) {
			return interactionEvent.getGuild();
		}
		return getMessage(event, "has no server").getGuild();
	}
}
