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
import net.dv8tion.jda.api.entities.Emote;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_MESSAGE,
	desc = "This class allows you to interact with Discord messages."
)
@ArucasClass(name = "DiscordMessage")
public class DiscordMessageWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private Message message;

	@FunctionDoc(
		name = "getId",
		desc = "This gets the id of the message",
		returns = {STRING, "The id of the message"},
		example = "message.getId();"
	)
	@ArucasFunction
	public StringValue getId(Context context) {
		return DiscordUtils.getId(this.message);
	}

	@FunctionDoc(
		name = "getRaw",
		desc = "This gets the raw message content",
		returns = {STRING, "The raw message content"},
		example = "message.getRaw();"
	)
	@ArucasFunction
	public StringValue getRaw(Context context) {
		return this.toString(context);
	}

	@FunctionDoc(
		name = "toString",
		desc = "This gets the raw message content",
		returns = {STRING, "The raw message content"},
		example = "message.toString();"
	)
	@ArucasFunction
	public StringValue toString(Context context) {
		return StringValue.of(this.message.getContentRaw());
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets the channel the message was sent in",
		returns = {DISCORD_CHANNEL, "The channel the message was sent in"},
		example = "message.getChannel();"
	)
	@ArucasFunction
	public WrapperClassValue getChannel(Context context) throws CodeError {
		return DiscordChannelWrapper.newDiscordChannel(this.message.getChannel(), context);
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets the server the message was sent in",
		returns = {DISCORD_SERVER, "The server the message was sent in"},
		example = "message.getServer();"
	)
	@ArucasFunction
	public WrapperClassValue getServer(Context context) throws CodeError {
		return DiscordServerWrapper.newDiscordServer(this.message.getGuild(), context);
	}

	@FunctionDoc(
		name = "getAuthor",
		desc = "This gets the author of the message",
		returns = {DISCORD_USER, "The author of the message"},
		example = "message.getAuthor();"
	)
	@ArucasFunction
	public WrapperClassValue getAuthor(Context context) throws CodeError {
		return DiscordUserWrapper.newDiscordUser(this.message.getAuthor(), context);
	}

	@FunctionDoc(
		name = "getAttachments",
		desc = "This gets the attachments of the message",
		returns = {LIST, "List with the attachments of the message"},
		example = "message.getAttachments();"
	)
	@ArucasFunction
	public ListValue getAttachments(Context context) throws CodeError {
		ArucasList arucasList = new ArucasList();
		for (Message.Attachment attachment: this.message.getAttachments()) {
			arucasList.add(DiscordAttachmentWrapper.newDiscordAttachment(attachment, context));
		}
		return new ListValue(arucasList);
	}

	@FunctionDoc(
		name = "addReaction",
		desc = "This adds a reaction to the message with a specific emoji id",
		params = {STRING, "emojiId", "the emoji id"},
		throwMsgs = "... is not a valid emote id",
		example = "message.addReaction('012789012930198');"
	)
	@ArucasFunction
	public void addReaction(Context context, StringValue emoteId) {
		Emote emote = this.message.getGuild().getEmoteById(emoteId.value);
		if (emote == null) {
			throw new RuntimeException("'%s' is not a valid emote id".formatted(emoteId.value));
		}
		this.message.addReaction(emote).complete();
	}

	@FunctionDoc(
		name = "addReactionUnicode",
		desc = "This adds a reaction to the message with a specific unicode",
		params = {STRING, "unicode", "the unicode character"},
		example = "message.addReactionUnicode('\\uD83D\\uDE00');"
	)
	@ArucasFunction
	public void addReactionUnicode(Context context, StringValue unicode) {
		this.message.addReaction(unicode.value).complete();
	}

	@FunctionDoc(
		name = "removeAllReactions",
		desc = "This removes all reactions from the message",
		example = "message.removeAllReactions();"
	)
	@ArucasFunction
	public void removeAllReactions(Context context) {
		this.message.clearReactions().complete();
	}

	@FunctionDoc(
		name = "delete",
		desc = "This deletes the message",
		example = "message.delete();"
	)
	@ArucasFunction
	public void delete(Context context) {
		this.message.delete().complete();
	}

	@FunctionDoc(
		name = "pin",
		desc = "This pins the message if true, and removes if false",
		params = {BOOLEAN, "bool", "true to pin, false to unpin"},
		example = "message.pin(true);"
	)
	@ArucasFunction
	public void pin(Context context, BooleanValue booleanValue) {
		if (booleanValue.value) {
			this.message.pin().complete();
			return;
		}
		this.message.unpin().complete();
	}

	@FunctionDoc(
		name = "isPinned",
		desc = "This checks if the message is pinned",
		returns = {BOOLEAN, "true if the message is pinned, false if not"},
		example = "message.isPinned();"
	)
	@ArucasFunction
	public BooleanValue isPinned(Context context) {
		return BooleanValue.of(this.message.isPinned());
	}

	@FunctionDoc(
		name = "isEdited",
		desc = "This checks if the message is edited",
		returns = {BOOLEAN, "true if the message is edited, false if not"},
		example = "message.isEdited();"
	)
	@ArucasFunction
	public BooleanValue isEdited(Context context) {
		return BooleanValue.of(this.message.isEdited());
	}

	@FunctionDoc(
		name = "reply",
		desc = "This replies to the message with the given message",
		params = {STRING, "message", "the message"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		example = "message.reply('Replied!');"
	)
	@ArucasFunction
	public WrapperClassValue reply(Context context, StringValue message) throws CodeError {
		return newDiscordMessage(this.message.reply(message.value).complete(), context);
	}

	@FunctionDoc(
		name = "replyWithEmbed",
		desc = {
			"This replies to the message with the given embed map",
			"In the embed map, you can use the following keys:",
			"'title' as String, ''description' as String or List of String, 'colour'/'color' as Number",
			"'fields' as Map with keys: ('name' as String, 'value' as String, 'inline' as Boolean)",
			"and 'image' as String that is an url"
		},
		params = {MAP, "embedMap", "the embed map"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		example = """
		message.replyWithEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	@ArucasFunction
	public WrapperClassValue replyWithEmbed(Context context, MapValue mapValue) throws CodeError {
		Message message = this.message.replyEmbeds(DiscordUtils.parseMapAsEmbed(context, mapValue)).complete();
		return newDiscordMessage(message, context);
	}

	@FunctionDoc(
		name = "replyWithFile",
		desc = "This replies to the message with the given file",
		params = {FILE, "file", "the file"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		example = "message.replyWithFile(new File('path/to/file'));"
	)
	@ArucasFunction
	public WrapperClassValue replyWithFile(Context context, FileValue fileValue) throws CodeError {
		return newDiscordMessage(this.message.reply(fileValue.value).complete(), context);
	}

	public static WrapperClassValue newDiscordMessage(Message message, Context context) throws CodeError {
		DiscordMessageWrapper channelWrapper = new DiscordMessageWrapper();
		channelWrapper.message = message;
		return DEFINITION.createNewDefinition(channelWrapper, context, List.of());
	}

	@Override
	public Message asJavaValue() {
		return this.message;
	}
}
