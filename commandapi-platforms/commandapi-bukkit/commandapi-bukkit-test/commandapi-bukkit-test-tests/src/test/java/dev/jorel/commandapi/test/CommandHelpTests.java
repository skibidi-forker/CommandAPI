package dev.jorel.commandapi.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.MultiLiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;

/**
 * Tests to do with command help
 */
class CommandHelpTests extends TestBase {

	/*********
	 * Setup *
	 *********/

	@BeforeEach
	public void setUp() {
		super.setUp();
	}

	@AfterEach
	public void tearDown() {
		super.tearDown();
	}

	private void assertHelpTopicCreated(String name, String shortDescription, String fullDescription, CommandSender forWho) {
		// Check the help topic was added
		HelpTopic helpTopic = server.getHelpMap().getHelpTopic(name);
		assertNotNull(helpTopic, "Expected to find help topic called <" + name + ">, but null was found.");

		// Check the short description
		assertEquals(shortDescription, helpTopic.getShortText());

		// Check the full description
		assertEquals(ChatColor.translateAlternateColorCodes('&', fullDescription), helpTopic.getFullText(forWho));
	}

	/*********
	 * Tests *
	 *********/

	@Test
	void testRegisterCommandWithHelp() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Short and full description are inserted
		assertHelpTopicCreated(
			"/test", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test""", 
			player
		);
	}
	
	@Test
	void testRegisterCommandWithShortDescription() {
		new CommandAPICommand("test")
			.withShortDescription("short description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Short description appears at the start of full text because that's how `CustomHelpTopic` works
		assertHelpTopicCreated(
			"/test", 
			"short description", 
			"""
			short description
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterCommandFullDescription() {
		new CommandAPICommand("test")
			.withFullDescription("full description")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Full description replaces short description when not specified
		assertHelpTopicCreated(
			"/test", 
			"full description", 
			"""
			full description
			&6Description: &ffull description
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterCommandNoDescription() {
		new CommandAPICommand("test")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Check default message
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f/test""", 
			player
		);
	}

	@Test
	void testRegisterWithRemovedUsage() {
		new CommandAPICommand("test")
			.withUsage()
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage can be removed
		// 	Note that the result returned by `getFullText` has a trailing \n because `CustomHelpTopic` uses
		//  `shortText + "\n" + fullText`. In this situation, we have no full description, no usage, and no 
		//  aliases, so the `fullText` generated by the CommandAPI is acutally an empty string "".
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			""", 
			player
		);
	}

	@Test
	void testRegisterWithOneUsage() {
		new CommandAPICommand("test")
			.withUsage("Line one")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage generation can be overriden with one line
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &fLine one""", 
			player
		);
	}

	@Test
	void testRegisterWithMultipleUsage() {
		new CommandAPICommand("test")
			.withUsage(
				"Line one",
				"Line two",
				"Line three"
			)
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage generation can be overriden with multiple lines
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- Line one
			- Line two
			- Line three""", 
			player
		);
	}

	@Test
	void testRegisterCommandWithHelpWithAliases() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withAliases("othertest", "othercommand")
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Check the main help topic was added
		assertHelpTopicCreated(
			"/test", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothertest, othercommand""", 
			player
		);

		// Check the alias topics
		//  The alias section of each alias does not include itself, but does include the main name
		//  Otherwise everything is the same as the main command
		assertHelpTopicCreated(
			"/othertest", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothercommand, test""", 
			player
		);
		assertHelpTopicCreated(
			"/othercommand", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test
			&6Aliases: &fothertest, test""", 
			player
		);
	}
	
	@Test
	void testRegisterCommandWithMultipleArguments() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withArguments(new StringArgument("arg1"))
			.withArguments(new IntegerArgument("arg2"))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Multiple arguments are stacked in the usage
		assertHelpTopicCreated(
			"/test", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f/test <arg1> <arg2>""", 
			player
		);
	}

	@Test
	void testRegisterMultipleCommands() {
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withArguments(new StringArgument("arg1"))
			.executesPlayer(P_EXEC)
			.register();

		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withArguments(new StringArgument("arg1"))
			.withArguments(new IntegerArgument("arg2"))
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Usage should be merged
		assertHelpTopicCreated(
			"/test", 
			"short description", 
			"""
			short description
			&6Description: &ffull description
			&6Usage: &f
			- /test <arg1>
			- /test <arg1> <arg2>""", 
			player
		);
	}

	@Test
	void testRegisterDeepBranches() {
		new CommandTree("test")
			.then(
				new LiteralArgument("branch1") // `/tree branch1` should not show up since it is not executable
					.then(new StringArgument("string1").executesPlayer(P_EXEC))
					.then(new IntegerArgument("integer1").executesPlayer(P_EXEC))
			)
			.then(
				new LiteralArgument("branch2").executesPlayer(P_EXEC)
					.then(new StringArgument("string2").executesPlayer(P_EXEC))
					.then(
						new IntegerArgument("integer2")
							.then(new LiteralArgument("continue").executesPlayer(P_EXEC))
					)
			)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// Each executable node should appear in the usage
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- /test branch1 <string1>
			- /test branch1 <integer1>
			- /test branch2
			- /test branch2 <string2>
			- /test branch2 <integer2> continue""", 
			player
		);
	}

	@Test
	void testRegisterLiteralArguments() {
		new CommandAPICommand("test")
			.withArguments(
				new MultiLiteralArgument("multiLiteral", "a", "b", "c"),
				new LiteralArgument("literal", "d"),
				new StringArgument("string")
			)
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player = server.addPlayer("APlayer");

		// MultiLiteralArgument unpacks to multiple LiteralArguments
		// LiteralArgument has a differnt help string, using its literal rather than its node name
		assertHelpTopicCreated(
			"/test", 
			"A command by the CommandAPITest plugin.", 
			"""
			A command by the CommandAPITest plugin.
			&6Usage: &f
			- /test a d <string>
			- /test b d <string>
			- /test c d <string>""", 
			player
		);
	}

	@Test
	void testRegisterCustomHelpTopic() {
		new CommandAPICommand("test")
			.withHelp(new HelpTopic() {
				{
					this.shortText = "short description";
				}

				@Override
				public String getFullText(CommandSender forWho) {
					return "Special full text just for " + (forWho instanceof Player player ? player.getName() : forWho);
				}

				@Override
				public boolean canSee(CommandSender sender) {
					return true;
				}
			})
			.executesPlayer(P_EXEC)
			.register();

		// Enable server to register help topics
		enableServer();
		Player player1 = server.addPlayer("Player1");
		Player player2 = server.addPlayer("Player2");

		// Custom HelpTopic allows changing text for different CommandSenders
		assertHelpTopicCreated(
			"/test", 
			"short description",
			"""
			Special full text just for Player1""", 
			player1
		);
		assertHelpTopicCreated(
			"/test", 
			"short description",
			"""
			Special full text just for Player2""", 
			player2
		);
	}

	@Test
	void testRegisterAfterServerEnabled() {
		// Enable server early
		enableServer();

		// `CommandAPIBukkit#postCommandRegistration` should still register the help topics
		new CommandAPICommand("test")
			.withHelp("short description", "full description")
			.withUsage("usage line")
			.withAliases("alias")
			.executesPlayer(P_EXEC)
			.register();

		// It is important to add the player after calling 	`.register`
		//  `CommandAPIBukkit#postCommandRegistration` also calls `Player#updateCommands`,
		//  which throws an `UnimplementedOperationException` for `PlayerMock`
		Player player = server.addPlayer("APlayer");

		// Ensure main and alias help topics exist
		assertHelpTopicCreated(
			"/test", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &fusage line
			&6Aliases: &falias""", 
			player
		);
		assertHelpTopicCreated(
			"/alias", 
			"short description",
			"""
			short description
			&6Description: &ffull description
			&6Usage: &fusage line
			&6Aliases: &ftest""", 
			player
		);
	}
}
