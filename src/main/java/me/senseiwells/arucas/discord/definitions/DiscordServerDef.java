package me.senseiwells.arucas.discord.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_SERVER;
import static me.senseiwells.arucas.discord.DiscordAPI.DISCORD_USER;
import static me.senseiwells.arucas.utils.Util.Types.*;

@ClassDoc(
	name = DISCORD_SERVER,
	desc = "This class allows you to interact with Discord servers.",
	importPath = "discordapi.Discord",
	language = Util.Language.Java
)
public class DiscordServerDef extends CreatableDefinition<Guild> {
	public DiscordServerDef(Interpreter interpreter) {
		super(DISCORD_SERVER, interpreter);
	}

	@Nullable
	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("ban", 1, this::ban),
			MemberFunction.of("kick", 1, this::kick),
			MemberFunction.of("unban", 1, this::unban),
			MemberFunction.of("getOwnerId", this::getOwnerId),
			MemberFunction.of("getMemberCount", this::getMemberCount),
			MemberFunction.of("getUserFromId", 1, this::getUserFromId),
			MemberFunction.of("createRole", 1, this::createRole)
		);
	}

	@FunctionDoc(
		name = "ban",
		desc = "This bans a user from the server",
		params = {DISCORD_USER, "user", "the user to ban"},
		examples = "server.ban(user);"
	)
	public Void ban(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		User user = arguments.nextPrimitive(DiscordUserDef.class);
		RuntimeError.wrap(() -> guild.ban(user, 0, TimeUnit.SECONDS)).complete();
		return null;
	}

	@FunctionDoc(
		name = "kick",
		desc = "This kicks a user from the server",
		params = {DISCORD_USER, "user", "the user to kick"},
		returns = {BOOLEAN, "whether the kick was successful"},
		examples = "server.kick(user);"
	)
	public boolean kick(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		User user = arguments.nextPrimitive(DiscordUserDef.class);
		Member member = RuntimeError.wrap(() -> guild.getMember(user));
		if (member == null) {
			return false;
		}
		RuntimeError.wrap(() -> guild.kick(member)).complete();
		return true;
	}

	@FunctionDoc(
		name = "unban",
		desc = "This unbans a user from the server",
		params = {DISCORD_USER, "user", "the user to unban"},
		examples = "server.unban(user);"
	)
	public Void unban(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		User user = arguments.nextPrimitive(DiscordUserDef.class);
		RuntimeError.wrap(() -> guild.unban(user)).complete();
		return null;
	}

	@FunctionDoc(
		name = "getOwnerId",
		desc = "This gets the id of the owner of the server",
		returns = {STRING, "the id of the owner"},
		examples = "server.getOwnerId();"
	)
	public String getOwnerId(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		return guild.getOwnerId();
	}

	@FunctionDoc(
		name = "getMemberCount",
		desc = "This gets the amount of members in the server",
		returns = {NUMBER, "the amount of members"},
		examples = "server.getMemberCount();"
	)
	public int getMemberCount(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		return guild.getMemberCount();
	}

	@FunctionDoc(
		name = "getUserFromId",
		desc = "This gets a user from the server by their id",
		params = {STRING, "userId", "the id of the user"},
		returns = {DISCORD_USER, "the user, if the user cannot be found returns null"},
		examples = "server.getUserFromId('12345678901234567890123456789012');"
	)
	public Member getUserFromId(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		String id = arguments.nextPrimitive(StringDef.class);
		return RuntimeError.wrap(() -> guild.retrieveMemberById(id)).complete();
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
		examples = """
		server.createRole({
			"name": "new role",
			"colour": 0xFFFFFF,
			"permissions": ["Manage Permissions", "Ban Members", "Administrator"],
			"hoisted": true,
			"mentionable": true
		});
		"""
	)
	public Void createRole(Arguments arguments) {
		Guild guild = arguments.nextPrimitive(this);
		ArucasMap roleMap = arguments.nextPrimitive(MapDef.class);
		DiscordUtils.parseMapAsRole(arguments.getInterpreter(), RuntimeError.wrap(guild::createRole), roleMap);
		return null;
	}
}
