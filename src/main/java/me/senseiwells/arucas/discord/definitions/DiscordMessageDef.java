package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.builtin.BooleanDef;
import me.senseiwells.arucas.builtin.FileDef;
import me.senseiwells.arucas.builtin.MapDef;
import me.senseiwells.arucas.builtin.StringDef;
import me.senseiwells.arucas.classes.ClassInstance;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.discord.DiscordUtils;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.LocatableTrace;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.impl.ArucasMap;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_MESSAGE,
	desc = "This class allows you to interact with Discord messages."
)
public class DiscordMessageDef extends CreatableDefinition<Message> {
	public DiscordMessageDef(Interpreter interpreter) {
		super(DISCORD_MESSAGE, interpreter);
	}

	@NotNull
	@Override
	public String toString$Arucas(@NotNull ClassInstance instance, @NotNull Interpreter interpreter, @NotNull LocatableTrace trace) {
		return instance.asPrimitive(this).getContentRaw();
	}

	@Nullable
	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("getId", this::getId),
			MemberFunction.of("getRaw", this::getRaw),
			MemberFunction.of("getChannel", this::getChannel),
			MemberFunction.of("getServer", this::getServer),
			MemberFunction.of("getAuthor", this::getAuthor),
			MemberFunction.of("getAttachments", this::getAttachments),
			MemberFunction.of("addReaction", 1, this::addReaction),
			MemberFunction.of("addReactionUnicode", 1, this::addReactionUnicode),
			MemberFunction.of("removeAllReactions", this::removeAllReactions),
			MemberFunction.of("delete", this::delete),
			MemberFunction.of("pin", 1, this::pin),
			MemberFunction.of("isPinned", this::isPinned),
			MemberFunction.of("isEdited", this::isEdited),
			MemberFunction.of("reply", 1, this::reply),
			MemberFunction.of("replyWithEmbed", 1, this::replyWithEmbed),
			MemberFunction.of("replyWithFile", 1, this::replyWithFile)
		);
	}

	@FunctionDoc(
		name = "getId",
		desc = "This gets the id of the message",
		returns = {STRING, "The id of the message"},
		examples = "message.getId();"
	)
	public String getId(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return DiscordUtils.getId(message);
	}

	@FunctionDoc(
		name = "getRaw",
		desc = "This gets the raw message content",
		returns = {STRING, "The raw message content"},
		examples = "message.getRaw();"
	)
	public String getRaw(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.getContentRaw();
	}

	@FunctionDoc(
		name = "getChannel",
		desc = "This gets the channel the message was sent in",
		returns = {DISCORD_CHANNEL, "The channel the message was sent in"},
		examples = "message.getChannel();"
	)
	public MessageChannelUnion getChannel(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.getChannel();
	}

	@FunctionDoc(
		name = "getServer",
		desc = "This gets the server the message was sent in",
		returns = {DISCORD_SERVER, "The server the message was sent in"},
		examples = "message.getServer();"
	)
	public Guild getServer(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.getGuild();
	}

	@FunctionDoc(
		name = "getAuthor",
		desc = "This gets the author of the message",
		returns = {DISCORD_USER, "The author of the message"},
		examples = "message.getAuthor();"
	)
	public User getAuthor(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.getAuthor();
	}

	@FunctionDoc(
		name = "getAttachments",
		desc = "This gets the attachments of the message",
		returns = {LIST, "List with the attachments of the message"},
		examples = "message.getAttachments();"
	)
	public List<Message.Attachment> getAttachments(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.getAttachments();
	}

	@FunctionDoc(
		name = "addReaction",
		desc = "This adds a reaction to the message with a specific emoji id",
		params = {STRING, "emojiId", "the emoji id"},
		examples = "message.addReaction('012789012930198');"
	)
	public Void addReaction(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		String emojiId = arguments.nextPrimitive(StringDef.class);
		Emoji emoji = message.getGuild().getEmojiById(emojiId);
		if (emoji == null) {
			throw new RuntimeException("'%s' is not a valid emoji id".formatted(emojiId));
		}
		message.addReaction(emoji).complete();
		return null;
	}

	@FunctionDoc(
		name = "addReactionUnicode",
		desc = "This adds a reaction to the message with a specific unicode",
		params = {STRING, "unicode", "the unicode character"},
		examples = "message.addReactionUnicode('\\uD83D\\uDE00');"
	)
	public Void addReactionUnicode(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		String unicode = arguments.nextPrimitive(StringDef.class);
		message.addReaction(Emoji.fromUnicode(unicode)).complete();
		return null;
	}

	@FunctionDoc(
		name = "removeAllReactions",
		desc = "This removes all reactions from the message",
		examples = "message.removeAllReactions();"
	)
	public Void removeAllReactions(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		message.clearReactions().complete();
		return null;
	}

	@FunctionDoc(
		name = "delete",
		desc = "This deletes the message",
		examples = "message.delete();"
	)
	public Void delete(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		message.delete().complete();
		return null;
	}

	@FunctionDoc(
		name = "pin",
		desc = "This pins the message if true, and removes if false",
		params = {BOOLEAN, "bool", "true to pin, false to unpin"},
		examples = "message.pin(true);"
	)
	public Void pin(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		boolean shouldPin = arguments.nextPrimitive(BooleanDef.class);
		if (shouldPin) {
			message.pin().complete();
			return null;
		}
		message.unpin().complete();
		return null;
	}

	@FunctionDoc(
		name = "isPinned",
		desc = "This checks if the message is pinned",
		returns = {BOOLEAN, "true if the message is pinned, false if not"},
		examples = "message.isPinned();"
	)
	public boolean isPinned(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.isPinned();
	}

	@FunctionDoc(
		name = "isEdited",
		desc = "This checks if the message is edited",
		returns = {BOOLEAN, "true if the message is edited, false if not"},
		examples = "message.isEdited();"
	)
	public boolean isEdited(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		return message.isEdited();
	}

	@FunctionDoc(
		name = "reply",
		desc = "This replies to the message with the given message",
		params = {STRING, "message", "the message"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		examples = "message.reply('Replied!');"
	)
	public ClassInstance reply(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		String toSend = arguments.nextPrimitive(StringDef.class);
		return this.create(message.reply(toSend).complete());
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
		examples = """
		message.replyWithEmbed({
		    'title': 'EMBED!',
		    'description': ['Wow', 'Nice'],
		    'colour': 0xFFFFFF
		});
		"""
	)
	public ClassInstance replyWithEmbed(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		ArucasMap embed = arguments.nextPrimitive(MapDef.class);
		return this.create(message.replyEmbeds(DiscordUtils.parseMapAsEmbed(arguments.getInterpreter(), embed)).complete());
	}

	@FunctionDoc(
		name = "replyWithFile",
		desc = "This replies to the message with the given file",
		params = {FILE, "file", "the file"},
		returns = {DISCORD_MESSAGE, "the message that was sent"},
		examples = "message.replyWithFile(new File('path/to/file'));"
	)
	public ClassInstance replyWithFile(Arguments arguments) {
		Message message = arguments.nextPrimitive(this);
		File file = arguments.nextPrimitive(FileDef.class);
		return this.create(message.replyFiles(FileUpload.fromData(file)).complete());
	}
}
