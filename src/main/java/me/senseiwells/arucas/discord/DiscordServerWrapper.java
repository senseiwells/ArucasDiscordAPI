package me.senseiwells.arucas.discord;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.api.wrappers.ArucasClass;
import me.senseiwells.arucas.api.wrappers.ArucasDefinition;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.*;
import me.senseiwells.arucas.values.classes.WrapperClassDefinition;
import me.senseiwells.arucas.values.classes.WrapperClassValue;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.List;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_SERVER;
import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_USER;
import static me.senseiwells.arucas.utils.ValueTypes.*;

@SuppressWarnings("unused")
@ClassDoc(
	name = DISCORD_SERVER,
	desc = "This class allows you to interact with Discord servers."
)
@ArucasClass(name = "DiscordServer")
public class DiscordServerWrapper implements IArucasWrappedClass {
	@ArucasDefinition
	public static WrapperClassDefinition DEFINITION;

	private Guild guild;

	@FunctionDoc(
		name = "ban",
		desc = "This bans a user from the server, with a reason",
		params = {
			DISCORD_USER, "user", "the user to ban",
			STRING, "reason", "the reason for the ban"
		},
		example = "server.ban(user, 'The ban hammer has struck!');"
	)
	@ArucasFunction
	public void ban(Context context, WrapperClassValue wrapperClassValue, StringValue reason) {
		DiscordUserWrapper userWrapper = wrapperClassValue.getWrapper(DiscordUserWrapper.class);
		this.guild.ban(userWrapper.getUser(), 0, reason.value).complete();
	}

	@FunctionDoc(
		name = "ban",
		desc = "This bans a user from the server",
		params = {DISCORD_USER, "user", "the user to ban"},
		example = "server.ban(user);"
	)
	@ArucasFunction
	public void ban(Context context, WrapperClassValue wrapperClassValue) {
		DiscordUserWrapper userWrapper = wrapperClassValue.getWrapper(DiscordUserWrapper.class);
		this.guild.ban(userWrapper.getUser(), 0).complete();
	}

	@FunctionDoc(
		name = "kick",
		desc = "This kicks a user from the server",
		params = {DISCORD_USER, "user", "the user to kick"},
		throwMsgs = "Member was null",
		example = "server.kick(user);"
	)
	@ArucasFunction
	public void kick(Context context, WrapperClassValue wrapperClassValue) {
		DiscordUserWrapper userWrapper = wrapperClassValue.getWrapper(DiscordUserWrapper.class);
		Member member = this.guild.getMember(userWrapper.getUser());
		if (member == null) {
			throw new RuntimeException("Member was null");
		}
		this.guild.kick(member).complete();
	}

	@FunctionDoc(
		name = "unban",
		desc = "This unbans a user from the server",
		params = {DISCORD_USER, "user", "the user to unban"},
		example = "server.unban(user);"
	)
	@ArucasFunction
	public void unban(Context context, WrapperClassValue wrapperClassValue) {
		DiscordUserWrapper userWrapper = wrapperClassValue.getWrapper(DiscordUserWrapper.class);
		this.guild.unban(userWrapper.getUser()).complete();
	}

	@FunctionDoc(
		name = "getOwnerId",
		desc = "This gets the id of the owner of the server",
		returns = {STRING, "the id of the owner"},
		example = "server.getOwnerId();"
	)
	@ArucasFunction
	public StringValue getOwnerId(Context context) {
		return StringValue.of(this.guild.getOwnerId());
	}

	@FunctionDoc(
		name = "getMemberCount",
		desc = "This gets the amount of members in the server",
		returns = {NUMBER, "the amount of members"},
		example = "server.getMemberCount();"
	)
	@ArucasFunction
	public NumberValue getMemberCount(Context context) {
		return NumberValue.of(this.guild.getMemberCount());
	}

	@FunctionDoc(
		name = "getUserFromId",
		desc = "This gets a user from the server by their id",
		params = {STRING, "userId", "the id of the user"},
		returns = {DISCORD_USER, "the user, if the user cannot be found returns null"},
		example = "server.getUserFromId('12345678901234567890123456789012');"
	)
	@ArucasFunction
	public Value getUserFromId(Context context, StringValue stringValue) throws CodeError {
		Member member = this.guild.retrieveMemberById(stringValue.value).complete();
		return member == null ? NullValue.NULL : DiscordUserWrapper.newDiscordUser(member.getUser(), context);
	}

	@FunctionDoc(
		name = "createRole",
		desc = {
			"This creates a role in the server",
			"In the role map you can have the following keys:",
			"'name' as String, 'colour'/'color' as Number, 'hoisted' as Boolean, 'mentionable as Boolean'",
			"and 'permissions' as a List of Strings, for example ['Manage Channels', 'Manage Server'], see Discord for more"
		},
		params = {MAP, "roleMap", "the map of the role"},
		example = """
		server.createRole({
			"name": "new role",
			"colour": 0xFFFFFF,
			"permissions": ["Manage Permissions", "Ban Members", "Administrator"],
			"hoisted": true,
			"mentionable": true
		});
		"""
	)
	@ArucasFunction
	public void createRole(Context context, MapValue mapValue) throws CodeError {
		DiscordUtils.parseMapAsRole(context, this.guild.createRole(), mapValue);
	}

	public static WrapperClassValue newDiscordServer(Guild guild, Context context) throws CodeError {
		DiscordServerWrapper channelWrapper = new DiscordServerWrapper();
		channelWrapper.guild = guild;
		return DEFINITION.createNewDefinition(channelWrapper, context, List.of());
	}

	@Override
	public Guild asJavaValue() {
		return this.guild;
	}
}
