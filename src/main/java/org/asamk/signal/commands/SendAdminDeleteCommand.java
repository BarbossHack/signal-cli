package org.asamk.signal.commands;

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import org.asamk.signal.commands.exceptions.CommandException;
import org.asamk.signal.commands.exceptions.UnexpectedErrorException;
import org.asamk.signal.commands.exceptions.UserErrorException;
import org.asamk.signal.manager.Manager;
import org.asamk.signal.manager.api.GroupNotFoundException;
import org.asamk.signal.manager.api.GroupSendingNotAllowedException;
import org.asamk.signal.manager.api.NotAGroupMemberException;
import org.asamk.signal.manager.api.RecipientIdentifier;
import org.asamk.signal.manager.api.UnregisteredRecipientException;
import org.asamk.signal.output.OutputWriter;
import org.asamk.signal.util.CommandUtil;

import java.io.IOException;
import java.util.Set;

import static org.asamk.signal.util.SendMessageResultUtils.outputResult;

public class SendAdminDeleteCommand implements JsonRpcLocalCommand {

    @Override
    public String getName() {
        return "sendAdminDelete";
    }

    @Override
    public void attachToSubparser(final Subparser subparser) {
        subparser.help("Send admin delete message for a previously received or sent message.");
        subparser.addArgument("-g", "--group-id", "--group").help("Specify the recipient group ID.").nargs("+");
        subparser.addArgument("--notify-self")
                .help("If self is part of recipients/groups send a normal message, not a sync message.")
                .action(Arguments.storeTrue());

        subparser.addArgument("-a", "--target-author")
                .required(true)
                .help("Specify the number of the author of the message to admin delete.");
        subparser.addArgument("-t", "--target-timestamp")
                .required(true)
                .type(long.class)
                .help("Specify the timestamp of the message to admin delete.");
        subparser.addArgument("--story")
                .help("Admin delete a story instead of a normal message")
                .action(Arguments.storeTrue());
    }

    @Override
    public void handleCommand(
            final Namespace ns,
            final Manager m,
            final OutputWriter outputWriter
    ) throws CommandException {
        final var notifySelf = Boolean.TRUE.equals(ns.getBoolean("notify-self"));
        final var groupIdStrings = ns.<String>getList("group-id");

        Set<RecipientIdentifier.Group> groupIdentifiers = CommandUtil.getGroupIdentifiers(groupIdStrings);

        if (groupIdentifiers.isEmpty()) {
            throw new UserErrorException("Admin delete requires group IDs");
        }

        final var targetAuthor = ns.getString("target-author");
        final var targetTimestamp = ns.getLong("target-timestamp");
        final var isStory = Boolean.TRUE.equals(ns.getBoolean("story"));

        final RecipientIdentifier.Single targetAuthorIdentifier = CommandUtil.getSingleRecipientIdentifier(targetAuthor,
                m.getSelfNumber());

        try {
            final var results = m.sendAdminDelete(targetAuthorIdentifier,
                    targetTimestamp,
                    groupIdentifiers,
                    notifySelf,
                    isStory);
            outputResult(outputWriter, results);
        } catch (GroupNotFoundException | NotAGroupMemberException | GroupSendingNotAllowedException e) {
            throw new UserErrorException(e.getMessage());
        } catch (IOException e) {
            throw new UnexpectedErrorException("Failed to send message: " + e.getMessage() + " (" + e.getClass()
                    .getSimpleName() + ")", e);
        } catch (UnregisteredRecipientException e) {
            throw new UserErrorException("The user " + e.getSender().getIdentifier() + " is not registered.");
        }
    }
}

