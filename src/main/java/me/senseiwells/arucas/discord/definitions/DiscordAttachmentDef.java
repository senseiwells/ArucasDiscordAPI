package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.builtin.FileDef;
import me.senseiwells.arucas.classes.CreatableDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.exceptions.RuntimeError;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.Util;
import net.dv8tion.jda.api.entities.Message;

import java.io.File;
import java.util.List;
import java.util.concurrent.Future;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_ATTACHMENT;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_ATTACHMENT,
	desc = "This class lets you download and manipulate discord attachments.",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordAttachmentDef extends CreatableDefinition<Message.Attachment> {
	public DiscordAttachmentDef(Interpreter interpreter) {
		super(DISCORD_ATTACHMENT, interpreter);
	}

	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("saveToFile", 1, this::saveToFile),
			MemberFunction.of("getFileName", this::getFileName),
			MemberFunction.of("getFileExtension", this::getFileExtension),
			MemberFunction.of("isImage", this::isImage),
			MemberFunction.of("isVideo", this::isVideo),
			MemberFunction.of("getUrl", this::getUrl),
			MemberFunction.of("getSize", this::getSize)
		);
	}

	@FunctionDoc(
		name = "saveToFile",
		desc = "This allows you to save an attachment to a file",
		params = {FILE, "file", "the file you want to save the attachment to"},
		returns = {FUTURE, "the future that will complete when the file has been downloaded"},
		examples = "attachment.saveToFile(new File('/home/user/Attachment.jpeg'))"
	)
	public Future<File> saveToFile(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		File file = arguments.nextPrimitive(FileDef.class);
		return RuntimeError.wrap(() -> attachment.getProxy().downloadToFile(file));
	}

	@FunctionDoc(
		name = "getFileName",
		desc = "This allows you to get the file name of the attachment",
		returns = {STRING, "the file name of the attachment"},
		examples = "attachment.getFileName()"
	)
	public String getFileName(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.getFileName();
	}

	@FunctionDoc(
		name = "getFileExtension",
		desc = "This allows you to get the file extension of the attachment",
		returns = {STRING, "the file extension of the attachment"},
		examples = "attachment.getFileExtension()"
	)
	public String getFileExtension(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.getFileExtension();
	}

	@FunctionDoc(
		name = "isImage",
		desc = "This allows you to check if the attachment is an image",
		returns = {BOOLEAN, "true if the attachment is an image, false otherwise"},
		examples = "attachment.isImage()"
	)
	public Boolean isImage(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.isImage();
	}

	@FunctionDoc(
		name = "isVideo",
		desc = "This allows you to check if the attachment is a video",
		returns = {BOOLEAN, "true if the attachment is a video, false otherwise"},
		examples = "attachment.isVideo()"
	)
	public Boolean isVideo(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.isVideo();
	}

	@FunctionDoc(
		name = "getUrl",
		desc = "This allows you to get the url of the attachment",
		returns = {STRING, "the url of the attachment"},
		examples = "attachment.getUrl()"
	)
	public String getUrl(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.getUrl();
	}

	@FunctionDoc(
		name = "getSize",
		desc = "This allows you to get the size of the attachment",
		returns = {NUMBER, "the size of the attachment in bytes"},
		examples = "attachment.getSize()"
	)
	public int getSize(Arguments arguments) {
		Message.Attachment attachment = arguments.nextPrimitive(this);
		return attachment.getSize();
	}
}
