package bg.sofia.uni.fmi.mjt.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InboxTest {

    @Test
    void testCreateFolderWithoutRoot() {
        Inbox inbox = new Inbox();

        assertThrows(InvalidPathException.class, () -> inbox.createFolder("/important/documents"),
                "Error: Folder path should start from root");

    }

    @Test
    void testCreateFolderMissingIntermediateFolders() {
        Inbox inbox = new Inbox();

        assertThrows(InvalidPathException.class, () -> inbox.createFolder("/inbox/important/documents"),
                "Error: Folder path mustn't contain missing intermediate folders");
    }

    @Test
    void testCreateFolderAlreadyExisted() {
        Inbox inbox = new Inbox();

        inbox.createFolder("/inbox/important");
        assertThrows(FolderAlreadyExistsException.class, () -> inbox.createFolder("/inbox/important"),
                "Error: Can not create folder which already existed");
    }

    @Test
    void testCreateFolderSuccessfully() {
        Inbox inbox = new Inbox();

        inbox.createFolder("/inbox/important");
        assertTrue(inbox.checkIfFolderExists("/inbox/important"),
                "Successfully creation of the folder was expected");
    }

    @Test
    void testGetMailsFromFolderMissingFolder() {
        Inbox inbox = new Inbox();

        assertThrows(FolderNotFoundException.class, () -> inbox.getMailsFromFolder("/inbox/news"),
                "Error: You can not get mails from missing folder!");
    }

    @Test
    void testCheckForBestRuleSuccessfully() {
        Inbox inbox = new Inbox();

        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        Rule ruleMock = mock(Rule.class);
        when(ruleMock.checkIfMailMatchesRule(mail)).thenReturn(true);
        when(ruleMock.getFolderPath()).thenReturn("/inbox/important");

        Set<Rule> rules = new HashSet<>();
        rules.add(ruleMock);

        assertEquals("/inbox/important", inbox.checkForBestRule(mail, rules),
                "Correct folder was expected when there is matched rule");
    }

    @Test
    void testCheckForBestRuleNoMatch() {
        Inbox inbox = new Inbox();

        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        Rule ruleMock = mock(Rule.class);
        when(ruleMock.checkIfMailMatchesRule(mail)).thenReturn(false);
        when(ruleMock.getFolderPath()).thenReturn("/inbox/important");

        Set<Rule> rules = new HashSet<>();
        rules.add(ruleMock);

        assertEquals("/inbox", inbox.checkForBestRule(mail, rules),
                "Default folder was expected when there is not matched rule");
    }

    @Test
    void testCheckForBetterFolders() {
        Inbox inbox = new Inbox();

        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        inbox.createFolder("/inbox/important");
        inbox.putMailInFolder("/inbox", mail);

        Rule ruleMock = mock(Rule.class);
        when(ruleMock.checkIfMailMatchesRule(mail)).thenReturn(true);
        when(ruleMock.getFolderPath()).thenReturn("/inbox/important");
        Set<Rule> rules = new HashSet<>();
        rules.add(ruleMock);
        inbox.checkForBetterFolders(rules);

        List<Mail> inboxMails = new ArrayList<>();
        List<Mail> importantMails = new ArrayList<>();
        importantMails.add(mail);

        assertIterableEquals(inboxMails, inbox.getMailsFromFolder("/inbox"),
                "Error: mail must be removed from /inbox after matching rule");
        assertIterableEquals(importantMails, inbox.getMailsFromFolder("/inbox/important"),
                "Error: mail was expected to be in new folder after matching better rule");
    }
}
