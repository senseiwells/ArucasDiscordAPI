package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.builtin.FileDef;
import me.senseiwells.arucas.builtin.MapDef;
import me.senseiwells.arucas.builtin.NumberDef;
import me.senseiwells.arucas.builtin.StringDef;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.exceptions.RuntimeError;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.Util;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_CHANNEL;
import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_MESSAGE;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_CHANNEL,
	desc = "This class allows you to get and send messages in the channel",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordChannelDef extends CreatableDefinition<MessageChannel> {
	public DiscordChannelDef(Interpreter interpreter) {
		super(DISCORD_CHANNEL, interpreter);
	}

	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("getMessageFromId", 1, this::getMessageFromId),
			MemberFunction.of("getHistory", 1, this::getHistory),
			MemberFunction.of("markTyping", this::markTyping),
			MemberFunction.of("sendMessage", 1, this::sendMessage),
			MemberFunction.of("sendEmbed", 1, this::sendEmbed),
			MemberFunction.of("sendFile", 1, this::sendFile)
		);
	}

	@FunctionDoc(
		name = "getMessageFromId",
		desc = "This gets a message by its id",
		params = {STRING, "messageId", "the id of the message"},
		returns = {DISCORD_MESSAGE, "the message"},
		examples = "channel.getMessageFromId('12345678901234567890123456789012');"
	)
	public Message getMessageFromId(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		String messageId = arguments.nextPrimitive(StringDef.class);
		Message message = channel.getHistory().getMessageById(messageId);
		if (message == null) {
			throw new RuntimeError("Message with id " + messageId + " couldn't be found");
		}
		return message;
	}

	@FunctionDoc(
		name = "getHistory",
		desc = "This gets the last X messages",
		params = {NUMBER, "amount", "the amount of messages to get"},
		returns = {LIST, "the messages"},
		examples = "channel.getMessages(10);"
	)
	public List<Message> getHistory(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		int amount = arguments.nextPrimitive(NumberDef.class).intValue();
		return channel.getHistory().retrievePast(amount).complete();
	}

	@FunctionDoc(
		name = "markTyping",
		desc = "This marks the bot as typing in this channel, it lasts 10 seconds or until the message is sent",
		examples = "channel.markTyping();"
	)
	public Void markTyping(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		channel.sendTyping().complete();
		return null;
	}

	@FunctionDoc(
		name = "sendMessage",
		desc = "This sends a message to this channel",
		params = {STRING, "message", "the message"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		examples = "channel.sendMessage('Hello World!');"
	)
	public Object sendMessage(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		String message = arguments.nextPrimitive(StringDef.class);
		return channel.sendMessage(message).complete();
	}

	@FunctionDoc(
		name = "sendEmbed",
		desc = {
			"This sends an embed to this channel.",
			"In the embed map, you can use the following keys:",
			"'title' as String, ''description' as String or List of String, 'colour'/'color' as Number",
			"'fields' as Map with keys: ('name' as String, 'value' as String, 'inline' as Boolean)",
			"and 'image' as String that is an url"
		},
		params = {MAP, "embedMap", "the embed map"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		examples = """
		channel.sendEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	public Message sendEmbed(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		ArucasMap embed = arguments.nextPrimitive(MapDef.class);
		return channel.sendMessageEmbeds(DiscordUtils.parseMapAsEmbed(arguments.getInterpreter(), embed)).complete();
	}

	@FunctionDoc(
		name = "sendFile",
		desc = "This sends a file to this channel",
		params = {FILE, "file", "the file you want to send"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		examples = "channel.sendFile(new File('a/b/totally_real_file.txt'));"
	)
	public Message sendFile(Arguments arguments) {
		MessageChannel channel = arguments.nextPrimitive(this);
		File file = arguments.nextPrimitive(FileDef.class);
		return channel.sendFiles(FileUpload.fromData(file)).complete();
	}
}
