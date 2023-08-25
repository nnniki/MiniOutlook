package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OutlookTest {
    Outlook outlook = new Outlook();
    Inbox inboxMock = Mockito.mock(Inbox.class);

    @BeforeEach
    void setTestData() {
        outlook.addNewAccount("Nikolay", "niki@abv.bg");
        outlook.addNewAccount("Gosho", "gosho@abv.bg");
    }

    @Test
    void testAddNewAccountEmptyName() {
        String name = "";
        assertThrows(IllegalArgumentException.class, () -> outlook.addNewAccount(name, "pesho@abv.bg"),
                "IllegalArgumentException was expected when name is empty");
    }

    @Test
    void testAddNewAccountNullEmail() {

        assertThrows(IllegalArgumentException.class, () -> outlook.addNewAccount("Ivan", null),
                "IllegalArgumentException was expected when email is null");
    }

    @Test
    void testAddNewAccountAlreadyExisted() {
        assertThrows(AccountAlreadyExistsException.class, () -> outlook.addNewAccount("Nikolay", "niki@abv.bg"),
                "AccountAlreadyExistsException was expected when trying to add existed account");
    }

    @Test
    void testAddNewAccountSuccessfully() {
        outlook.addNewAccount("Ivan", "ivan@abv.bg");

        Set<Account> accounts = new HashSet<>();
        accounts.add(new Account("niki@abv.bg", "Nikolay"));
        accounts.add(new Account("gosho@abv.bg", "Gosho"));
        accounts.add(new Account("ivan@abv.bg", "Ivan"));

        assertIterableEquals(accounts, outlook.getAllAccounts(), "Error: Account should be added successfully");
    }

    @Test
    void testCreateFolderEmptyPath() {
        assertThrows(IllegalArgumentException.class, () -> outlook.createFolder("Nikolay", ""),
                "IllegalArgumentException was expected when path is empty");
    }

    @Test
    void testCreateFolderBlankName() {
        assertThrows(IllegalArgumentException.class, () -> outlook.createFolder(" ", "/inbox/doc"),
                "IllegalArgumentException was expected when name is blank");
    }

    @Test
    void testCreateFolderAccountNotFound() {
        assertThrows(AccountNotFoundException.class, () -> outlook.createFolder("Simeon", "/inbox/doc"),
                "AccountNotFoundException was expected when account is not presented");
    }

    @Test
    void testCreateFolderInvalidPathMissingIntermediateFolders() {

        Mockito.doThrow(new InvalidPathException("Invalid path")).when(inboxMock).createFolder("/inbox/important/documents");

        assertThrows(InvalidPathException.class, () -> outlook.createFolder("Nikolay", "/inbox/important/documents"),
                "InvalidPathException was expected when there are missing intermediate folders");
    }

    @Test
    void testCreateFolderInvalidPathNotStartingFromRoot() {

        Mockito.doThrow(new InvalidPathException("Invalid path")).when(inboxMock).createFolder("/important/documents");

        assertThrows(InvalidPathException.class, () -> outlook.createFolder("Nikolay", "/important/documents"),
                "InvalidPathException was expected when path doesn't start from root");
    }

    @Test
    void testCreateFolderAlreadyExist() {
        outlook.createFolder("Nikolay", "/inbox/documents");
        Mockito.doThrow(new FolderAlreadyExistsException("Folder existed")).when(inboxMock).createFolder("/inbox/documents");

        assertThrows(FolderAlreadyExistsException.class, () -> outlook.createFolder("Nikolay", "/inbox/documents"),
                "FolderAlreadyExistsException was expected when creating folder with already existing path");
    }

    @Test
    void testAddRuleWithNullName() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        assertThrows(IllegalArgumentException.class, () -> outlook.addRule(null, "/inbox/doc", ruleDefinition, 3),
                "IllegalArgumentException was expected when name is null");
    }

    @Test
    void testAddRuleWithEmptyFolderPath() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        assertThrows(IllegalArgumentException.class, () -> outlook.addRule("Nikolay", "", ruleDefinition, 3),
                "IllegalArgumentException was expected when path is empty");
    }

    @Test
    void testAddRuleWithBlankRuleDefinition() {
        assertThrows(IllegalArgumentException.class, () -> outlook.addRule("Nikolay", "/inbox/doc", " ", 3),
                "IllegalArgumentException was expected when ruleDefinition is blank");
    }

    @Test
    void testAddRuleWithInvalidPriority() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        assertThrows(IllegalArgumentException.class, () -> outlook.addRule(null, "/inbox/doc", ruleDefinition, 0),
                "IllegalArgumentException was expected when priority is out of range");
    }

    @Test
    void testAddRuleAccountNotExists() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        assertThrows(AccountNotFoundException.class, () -> outlook.addRule("Pesho", "/inbox/doc", ruleDefinition, 3),
                "AccountNotFoundException was expected when there is not account with this name");
    }

    @Test
    void testAddRuleFolderNotFound() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");

        assertThrows(FolderNotFoundException.class, () -> outlook.addRule("Nikolay", "/inbox/doc", ruleDefinition, 3),
                "FolderNotFoundException was expected when there is not folder with this path for the account");
    }

    @Test
    void testAddRuleSuccessfully() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);
        Set<Rule> rule = new TreeSet<>(new SortRulesByPriority());
        Rule newRule = new Rule("/inbox/documents", 2);
        newRule.parseRuleDefinition(ruleDefinition);
        rule.add(newRule);

        assertIterableEquals(rule, outlook.getAllRules("Nikolay"), "Successfully added rule was expected");
    }

    @Test
    void testAddRuleConflictRule() {
        String ruleDefinition = "subject-includes: mjt, izpit, 2022" + System.lineSeparator() +
                "subject-or-body-includes: izpit" + System.lineSeparator() +
                "from: niki@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.createFolder("Nikolay", "/inbox/documents/important");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);
        outlook.addRule("Nikolay", "/inbox/documents/important", ruleDefinition, 2);

        Set<Rule> result = new TreeSet<>(new SortRulesByPriority());
        Rule newRule = new Rule("/inbox/documents", 2);
        newRule.parseRuleDefinition(ruleDefinition);
        result.add(newRule);

        assertIterableEquals(result, outlook.getAllRules("Nikolay"),
                "Error: Conflict rules mustn't be added");
    }

    @Test
    void testAddRuleAndMailChangeFolder() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, niki@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);
        outlook.addMailToAccount("Nikolay", "/inbox", mail);
        List<Mail> list = new ArrayList<>();
        list.add(mail);

        String ruleDefinition = "subject-includes: MJT" + System.lineSeparator() +
                "subject-or-body-includes: best, course";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);

        assertIterableEquals(list, outlook.getReceived("Nikolay").getMailsFromFolder("/inbox/documents"),
                "Error: After adding rules, if mail in default folder matches, it moves to matched rule's path");

        assertIterableEquals(new ArrayList<>(), outlook.getReceived("Nikolay").getMailsFromFolder("/inbox"),
                "After moving in other folder mail should be deleted from default one");
    }

    @Test
    void testReceiveMailNullName() {
        String mailMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        assertThrows(IllegalArgumentException.class, () -> outlook.receiveMail(null, mailMetadata, mailBody),
                "IllegalArgumentException was expected when name is null");
    }

    @Test
    void testReceiveMailEmptyMailMetadata() {
        String mailBody = "MJT course is the best course in FMI!";

        assertThrows(IllegalArgumentException.class, () -> outlook.receiveMail("Gosho", "", mailBody),
                "IllegalArgumentException was expected when Mail's metadata is empty");
    }

    @Test
    void testReceiveMailBlankMailBody() {
        String mailMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        assertThrows(IllegalArgumentException.class, () -> outlook.receiveMail(null, mailMetadata, " "),
                "IllegalArgumentException was expected when Mail's content is blank");
    }

    @Test
    void testReceiveMailAccountNotFound() {
        String mailMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        assertThrows(AccountNotFoundException.class, () -> outlook.receiveMail("Rosi", mailMetadata, mailBody),
                "AccountNotFoundException was expected when there is not account with current name");
    }

    @Test
    void testReceiveMailNoRuleMatch() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        String ruleDefinition = "subject-includes: MJT, exam" + System.lineSeparator() +
                "subject-or-body-includes: best" + System.lineSeparator() +
                "from: gosho@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);
        outlook.receiveMail("Nikolay", mailMetadata, mailBody);
        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);
        List<Mail> list = new ArrayList<>();
        list.add(mail);

        assertIterableEquals(list, outlook.getReceived("Nikolay").getMailsFromFolder("/inbox"),
                "Error: Mail was expected to be in default folder when does not match any rule");
        assertIterableEquals(new ArrayList<>(), outlook.getReceived("Nikolay").getMailsFromFolder("/inbox/documents"),
                "Error: When mail doesn't match rule's definition, it must not be put in rule's folder");
    }

    @Test
    void testReceiveMailMatchRule() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        String ruleDefinition = "subject-includes: MJT" + System.lineSeparator() +
                "subject-or-body-includes: best, course" + System.lineSeparator() +
                "from: gosho@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);

        outlook.receiveMail("Nikolay", mailMetadata, mailBody);

        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);
        List<Mail> list = new ArrayList<>();
        list.add(mail);

        assertIterableEquals(list, outlook.getReceived("Nikolay").getMailsFromFolder("/inbox/documents"),
                "Error: Mail was expected to be in matched rule's folder");
        assertIterableEquals(new ArrayList<>(), outlook.getReceived("Nikolay").getMailsFromFolder("/inbox"),
                "Error: When mail match rule's definition, it must not be put in default folder");
    }

    @Test
    void testReceiveMailMatchedTwoRulesWithDifferentPriority() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "Modern Java Technologies is the best course in FMI!";

        String ruleDefinition = "subject-includes: MJT" + System.lineSeparator() +
                "subject-or-body-includes: best, course" + System.lineSeparator() +
                "from: gosho@abv.bg";

        String ruleDef2 = "subject-includes: Hello" + System.lineSeparator() +
                "subject-or-body-includes: best, course, MJT" + System.lineSeparator() +
                "recipients-includes: simeon@abv.bg, pesho@gmail.com, stelio@gmail.com";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.createFolder("Nikolay", "/inbox/newFolder");
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 4);
        outlook.addRule("Nikolay", "/inbox/newFolder", ruleDef2, 2);
        outlook.receiveMail("Nikolay", mailMetadata, mailBody);
        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);

        List<Mail> list = new ArrayList<>();
        list.add(mail);

        assertIterableEquals(list, outlook.getReceived("Nikolay").getMailsFromFolder("/inbox/newFolder"),
                "Error: When mail match more than one rule, it is expected to be in rule's folder with higher priority");
        assertIterableEquals(new ArrayList<>(), outlook.getReceived("Nikolay").getMailsFromFolder("/inbox/documents"),
                "Error: When mail match rule with higher priority, it must not be put in other rule's folder");
    }

    @Test
    void testGetMailsFromFolderNullAccount() {
        assertThrows(IllegalArgumentException.class, () -> outlook.getMailsFromFolder(null, "/inbox/doc"),
                "IllegalArgumentException was expected when name is null");
    }

    @Test
    void testGetMailsFromFolderEmptyFolderPath() {
        assertThrows(IllegalArgumentException.class, () -> outlook.getMailsFromFolder("Nikolay", ""),
                "IllegalArgumentException was expected when folder's path is empty");
    }

    @Test
    void testGetMailsFromFolderMissingAccount() {
        assertThrows(AccountNotFoundException.class, () -> outlook.getMailsFromFolder("Simeon", "/inbox"),
                "AccountNotFoundException was expected when there is not account with this name");
    }

    @Test
    void testGetMailsFromFolderInvalidFolderPath() {
        assertThrows(FolderNotFoundException.class, () -> outlook.getMailsFromFolder("Nikolay", "/inbox/mjt"),
                "FolderNotFoundException was expected when there is not folder with this path");
    }

    @Test
    void testGetMailsFromFolderSentFolder() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "Modern Java Technologies is the best course in FMI!";

        outlook.sendMail("Gosho", mailMetadata, mailBody);
        Collection<Mail> result = new ArrayList<>();
        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);
        result.add(mail);

        assertTrue(result.containsAll(outlook.getMailsFromFolder("Gosho", "/sent")),
                "Correct mails from sent folder were expected");
        assertTrue(outlook.getMailsFromFolder("Gosho", "/sent").containsAll(result),
                "Correct mails from sent folder were expected");
    }

    @Test
    void testGetMailsFromFolderSuccessfully() {
        String mailMetadata = "sender: gosho@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        String ruleDefinition = "subject-includes: MJT" + System.lineSeparator() +
                "subject-or-body-includes: best, course" + System.lineSeparator() +
                "from: gosho@abv.bg";

        outlook.createFolder("Nikolay", "/inbox/documents");
        outlook.receiveMail("Nikolay", mailMetadata, mailBody);
        outlook.addRule("Nikolay", "/inbox/documents", ruleDefinition, 2);
        Mail mail = outlook.parseMailMetadata(mailMetadata, mailBody);

        Collection<Mail> result = new ArrayList<>();
        result.add(mail);

        assertTrue(result.containsAll(outlook.getMailsFromFolder("Nikolay", "/inbox/documents")),
                "Correct mails from folder's path were expected");
        assertTrue(outlook.getMailsFromFolder("Nikolay", "/inbox/documents").containsAll(result),
                "Correct mails from folder's path were expected");
    }

    @Test
    void testSendMailNameNull() {
        String mailMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        assertThrows(IllegalArgumentException.class, () -> outlook.sendMail(null, mailMetadata, mailBody),
                "IllegalArgumentException was expected when name is null");
    }

    @Test
    void testSendMailEmptyBody() {
        String mailMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        assertThrows(IllegalArgumentException.class, () -> outlook.sendMail("Nikolay", mailMetadata, ""),
                "IllegalArgumentException was expected when name is null");
    }

    @Test
    void testSendMailMissingSender() {
        String mailMetadata = "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        String mailBody = "MJT course is the best course in FMI!";

        outlook.sendMail("Nikolay", mailMetadata, mailBody);
        Collection<Mail> result = new ArrayList<>();

        String fullMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        Mail mail = outlook.parseMailMetadata(fullMetadata, mailBody);
        result.add(mail);

        assertTrue(result.containsAll(outlook.getMailsFromFolder("Nikolay", "/sent")),
                "Error: If missing sender in mail metadata it should be automatically added");
        assertTrue(outlook.getMailsFromFolder("Nikolay", "/sent").containsAll(result),
                "Error: If missing sender in mail metadata it should be automatically added");
    }

    @Test
    void testSendMailWithIncorrectSender() {
        String mailMetadata = "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14" + System.lineSeparator() +
                "sender: pesho@abv.bg";

        String mailBody = "MJT course is the best course in FMI!";

        outlook.sendMail("Nikolay", mailMetadata, mailBody);
        Collection<Mail> result = new ArrayList<>();

        String correctMetadata = "sender: niki@abv.bg" + System.lineSeparator() +
                "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14";

        Mail mail = outlook.parseMailMetadata(correctMetadata, mailBody);
        result.add(mail);

        assertTrue(result.containsAll(outlook.getMailsFromFolder("Nikolay", "/sent")),
                "Error: If sender is incorrect he should be automatically changed");
        assertTrue(outlook.getMailsFromFolder("Nikolay", "/sent").containsAll(result),
                "Error: If sender is incorrect he should be automatically changed");
    }

    @Test
    void testSendMailAndCheckIfReceivedFromRecipients() {
        String mailMetadata = "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14" + System.lineSeparator() +
                "sender: pesho@abv.bg";

        String mailBody = "MJT course is the best course in FMI!";

        String ruleDefinition = "subject-includes: MJT" + System.lineSeparator() +
                "subject-or-body-includes: best, course" + System.lineSeparator() +
                "from: niki@abv.bg";

        outlook.createFolder("Gosho", "/inbox/documents");
        outlook.addRule("Gosho", "/inbox/documents", ruleDefinition, 2);

        outlook.sendMail("Nikolay", mailMetadata, mailBody);

        String fullMetadata = "subject: Hello, MJT!" + System.lineSeparator() +
                "recipients: pesho@gmail.com, gosho@abv.bg" + System.lineSeparator() +
                "received: 2022-12-08 14:14" + System.lineSeparator() +
                "sender: niki@abv.bg";
        Mail mail = outlook.parseMailMetadata(fullMetadata, mailBody);

        List<Mail> list = new ArrayList<>();
        list.add(mail);

        assertIterableEquals(list, outlook.getReceived("Gosho").getMailsFromFolder("/inbox/documents"),
                "Error: When sending mail, automatically receiving for recipients is expected");
    }
}
