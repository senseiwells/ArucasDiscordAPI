package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.ArucasClass;
import me.senseiwells.arucas.api.wrappers.ArucasDefinition;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.BooleanValue;
import me.senseiwells.arucas.values.FileValue;
import me.senseiwells.arucas.values.NumberValue;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import net.dv8tion.jda.api.entities.Message;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.*;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_ATTACHMENT,
	desc = "This class lets you download and manipulate discord attachments."
)
@ArucasClass(name = "DiscordAttachment")
public class DiscordAttachmentWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private Message.Attachment attachment;

	@FunctionDoc(
		name = "saveToFile",
		desc = "This allows you to save an attachment to a file",
		params = {FILE, "file", "the file you want to save the attachment to"},
		example = "attachment.saveToFile(new File('/home/user/Attachment.jpeg'))"
	)
	@ArucasFunction
	public void saveToFile(Context context, FileValue fileValue) {
		this.attachment.downloadToFile(fileValue.value);
	}

	@FunctionDoc(
		name = "getFileName",
		desc = "This allows you to get the file name of the attachment",
		returns = {STRING, "the file name of the attachment"},
		example = "attachment.getFileName()"
	)
	@ArucasFunction
	public StringValue getFileName(Context context) {
		return StringValue.of(this.attachment.getFileName());
	}

	@FunctionDoc(
		name = "getFileExtension",
		desc = "This allows you to get the file extension of the attachment",
		returns = {STRING, "the file extension of the attachment"},
		example = "attachment.getFileExtension()"
	)
	@ArucasFunction
	public StringValue getFileExtension(Context context) {
		return StringValue.of(this.attachment.getFileExtension());
	}

	@FunctionDoc(
		name = "isImage",
		desc = "This allows you to check if the attachment is an image",
		returns = {BOOLEAN, "true if the attachment is an image, false otherwise"},
		example = "attachment.isImage()"
	)
	@ArucasFunction
	public BooleanValue isImage(Context context) {
		return BooleanValue.of(this.attachment.isImage());
	}

	@FunctionDoc(
		name = "isVideo",
		desc = "This allows you to check if the attachment is a video",
		returns = {BOOLEAN, "true if the attachment is a video, false otherwise"},
		example = "attachment.isVideo()"
	)
	@ArucasFunction
	public BooleanValue isVideo(Context context) {
		return BooleanValue.of(this.attachment.isVideo());
	}

	@FunctionDoc(
		name = "getUrl",
		desc = "This allows you to get the url of the attachment",
		returns = {STRING, "the url of the attachment"},
		example = "attachment.getUrl()"
	)
	@ArucasFunction
	public StringValue getUrl(Context context) {
		return StringValue.of(this.attachment.getUrl());
	}

	@FunctionDoc(
		name = "getSize",
		desc = "This allows you to get the size of the attachment",
		returns = {NUMBER, "the size of the attachment in bytes"},
		example = "attachment.getSize()"
	)
	@ArucasFunction
	public NumberValue getSize(Context context) {
		return NumberValue.of(this.attachment.getSize());
	}

	public static WrapperClassValue newDiscordAttachment(Message.Attachment attachment, Context context) throws CodeError {
		DiscordAttachmentWrapper attachmentWrapper = new DiscordAttachmentWrapper();
		attachmentWrapper.attachment = attachment;
		return DEFINITION.createNewDefinition(attachmentWrapper, context, List.of());
	}

	@Override
	public Message.Attachment asJavaValue() {
		return this.attachment;
	}
}
