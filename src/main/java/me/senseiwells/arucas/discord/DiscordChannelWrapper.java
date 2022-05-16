package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.ArucasClass;
import me.senseiwells.arucas.api.wrappers.ArucasDefinition;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.utils.impl.ArucasList;
import me.senseiwells.arucas.values.*;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_CHANNEL;
import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_MESSAGE;
import static me.senseiwells.arucas.utils.ValueTypes.*;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_CHANNEL,
	desc = "This class allows you to get and send messages in the channel"
)
@ArucasClass(name = "DiscordChannel")
public class DiscordChannelWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private MessageChannel channel;

	@FunctionDoc(
		name = "getMessageFromId",
		desc = "This gets a message by its id",
		params = {STRING, "messageId", "the id of the message"},
		returns = {DISCORD_MESSAGE, "the message"},
		throwMsgs = "Message with id ... couldn't be found",
		example = "channel.getMessageFromId('12345678901234567890123456789012');"
	)
	@ArucasFunction
	public WrapperClassValue getMessageFromId(Context context, StringValue messageId) throws CodeError {
		Message message = this.channel.getHistory().getMessageById(messageId.value);
		if (message == null) {
			throw new RuntimeException("Message with id " + messageId.value + " couldn't be found");
		}
		return DiscordMessageWrapper.newDiscordMessage(message, context);
	}

	@FunctionDoc(
		name = "getHistory",
		desc = "This gets the last X messages",
		params = {NUMBER, "amount", "the amount of messages to get"},
		returns = {LIST, "the messages"},
		example = "channel.getMessages(10);"
	)
	@ArucasFunction
	public ListValue getHistory(Context context, NumberValue amount) throws CodeError {
		List<Message> messages = this.channel.getHistory().retrievePast(amount.value.intValue()).complete();
		ArucasList arucasList = new ArucasList();
		for (Message message : messages) {
			arucasList.add(DiscordMessageWrapper.newDiscordMessage(message, context));
		}
		return new ListValue(arucasList);
	}

	@FunctionDoc(
		name = "markTyping",
		desc = "This marks the bot as typing in this channel, it lasts 10 seconds or until the message is sent",
		example = "channel.markTyping();"
	)
	@ArucasFunction
	public void markTyping(Context context) {
		this.channel.sendTyping().complete();
	}

	@FunctionDoc(
		name = "sendMessage",
		desc = "This sends a message to this channel",
		params = {STRING, "message", "the message"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		example = "channel.sendMessage('Hello World!');"
	)
	@ArucasFunction
	public WrapperClassValue sendMessage(Context context, StringValue message) throws CodeError {
		return DiscordMessageWrapper.newDiscordMessage(this.channel.sendMessage(message.value).complete(), context);
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
		example = """
		channel.sendEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	@ArucasFunction
	public WrapperClassValue sendEmbed(Context context, MapValue embed) throws CodeError {
		Message message = this.channel.sendMessageEmbeds(DiscordUtils.parseMapAsEmbed(context, embed)).complete();
		return DiscordMessageWrapper.newDiscordMessage(message, context);
	}

	@FunctionDoc(
		name = "sendFile",
		desc = "This sends a file to this channel",
		params = {FILE, "file", "the file you want to send"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		example = "channel.sendFile(new File('a/b/totally_real_file.txt'));"
	)
	@ArucasFunction
	public WrapperClassValue sendFile(Context context, FileValue fileValue) throws CodeError {
		return DiscordMessageWrapper.newDiscordMessage(this.channel.sendFile(fileValue.value).complete(), context);
	}

	public static WrapperClassValue newDiscordChannel(MessageChannel channel, Context context) throws CodeError {
		DiscordChannelWrapper channelWrapper = new DiscordChannelWrapper();
		channelWrapper.channel = channel;
		return DEFINITION.createNewDefinition(channelWrapper, context, List.of());
	}

	@Override
	public MessageChannel asJavaValue() {
		return this.channel;
	}
}
