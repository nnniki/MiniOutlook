package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.AccountNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Outlook implements MailClient {

    private Set<Account> allAccounts;
    private Map<String, Inbox> receivedMails;
    private Map<String, List<Mail>> sentMails;
    private Map<String, Set<Rule>> rules;

    private static final int MAX_PRIORITY = 1;
    private static final int MIN_PRIORITY = 10;
    private static final String MAIL_METADATA_REGEX = "[:\\r?\\n|\\r]+";
    private static final String SENDER = "sender:";
    private static final String MAIL_SENDER = "sender";
    private static final String SUBJECT = "subject";
    private static final String RECIPIENTS = "recipients";
    private static final String RECEIVED = "received";
    private static final String CSV_SEPARATOR = ",";
    private static final String TIME_SEPARATOR = ":";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    private static final String DEFAULT_FOLDER = "/inbox";
    private static final String SENT_FOLDER = "/sent";

    public Outlook() {
        allAccounts = new HashSet<>();
        receivedMails = new HashMap<>();
        sentMails = new HashMap<>();
        rules = new HashMap<>();
    }

    @Override
    public Account addNewAccount(String accountName, String email) {
        checkIfStringIsNullEmptyOrBlank(accountName, "Account can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(email, "Email can not be null, empty or blank");

        for (Account current : allAccounts) {
            if (current.name().equals(accountName)) {
                throw new AccountAlreadyExistsException("Account with this name already existed!");
            }
        }

        Account newAccount = new Account(email, accountName);
        allAccounts.add(newAccount);
        Inbox defaultInbox = new Inbox();                    // With adding new account we create /inbox folder.
        receivedMails.put(accountName, defaultInbox);

        return newAccount;
    }

    @Override
    public void createFolder(String accountName, String path) {
        checkIfStringIsNullEmptyOrBlank(path, "Path can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(accountName, "Account can not be null, empty or blank");
        checkIfAccountExists(accountName);

        Inbox inbox = receivedMails.get(accountName);
        inbox.createFolder(path);
    }

    @Override
    public void addRule(String accountName, String folderPath, String ruleDefinition, int priority) {
        checkIfStringIsNullEmptyOrBlank(folderPath, "Path can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(accountName, "Account can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(ruleDefinition, "Rule's definition can not be null, empty or blank");
        if (priority < MAX_PRIORITY || priority > MIN_PRIORITY) {
            throw new IllegalArgumentException("Priority is out of range");
        }

        checkIfAccountExists(accountName);
        checkIfFolderExists(folderPath, accountName);

        Rule newRule = new Rule(folderPath, priority);
        newRule.parseRuleDefinition(ruleDefinition);

        if (!checkIfRuleIsConflict(accountName, newRule, folderPath)) {
            if (!rules.containsKey(accountName)) {
                Set<Rule> ruleSet = new TreeSet<>(new SortRulesByPriority());
                ruleSet.add(newRule);
                rules.put(accountName, ruleSet);
            } else {
                rules.get(accountName).add(newRule);
            }

            Set<Rule> addedRules = rules.get(accountName);

            if (receivedMails.containsKey(accountName)) {
                Inbox inbox = receivedMails.get(accountName);
                inbox.checkForBetterFolders(addedRules);
            }
        }
    }

    @Override
    public void receiveMail(String accountName, String mailMetadata, String mailContent) {
        checkIfStringIsNullEmptyOrBlank(accountName, "Account can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(mailMetadata, "Mail's metadata can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(mailContent, "Mail's content can not be null, empty or blank");
        checkIfAccountExists(accountName);

        Mail mail = parseMailMetadata(mailMetadata, mailContent);
        String path;
        if (rules.containsKey(accountName)) {
            Set<Rule> accountRules = rules.get(accountName);
            Inbox inbox = receivedMails.get(accountName);
            path = inbox.checkForBestRule(mail, accountRules);
        } else {
            path = DEFAULT_FOLDER;
        }


        Inbox inbox = receivedMails.get(accountName);
        inbox.putMailInFolder(path, mail);
    }

    @Override
    public Collection<Mail> getMailsFromFolder(String account, String folderPath) {
        checkIfStringIsNullEmptyOrBlank(folderPath, "Path can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(account, "Account can not be null, empty or blank");
        checkIfAccountExists(account);

        if (folderPath.equals(SENT_FOLDER)) {
            if (!sentMails.containsKey(account)) {
                return new ArrayList<>();
            }
            return sentMails.get(account);
        }

        Inbox inbox = receivedMails.get(account);
        if (!inbox.checkIfFolderExists(folderPath)) {
            throw new FolderNotFoundException("Folder's path is invalid");
        }

        return inbox.getMailsFromFolder(folderPath);
    }

    @Override
    public void sendMail(String accountName, String mailMetadata, String mailContent) {
        checkIfStringIsNullEmptyOrBlank(accountName, "Account can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(mailMetadata, "Mail's metadata can not be null, empty or blank");
        checkIfStringIsNullEmptyOrBlank(mailContent, "Mail's content can not be null, empty or blank");

        if (!mailMetadata.contains(SENDER)) {
            mailMetadata = mailMetadata.concat(System.lineSeparator() +
                    SENDER + " " + getSenderEmailByName(accountName));
        } else {
            int firstIdx = mailMetadata.indexOf(SENDER);
            int lastIdx = mailMetadata.indexOf(System.lineSeparator(), firstIdx);
            if (lastIdx != -1) {
                String substring = mailMetadata.substring(firstIdx, lastIdx);
                mailMetadata = mailMetadata.replace(substring, SENDER + " " + getSenderEmailByName(accountName)
                        + System.lineSeparator());
            } else {
                int last = mailMetadata.charAt(mailMetadata.length() - 1);
                String substring = mailMetadata.substring(firstIdx, last);
                mailMetadata = mailMetadata.replace(substring, SENDER + " " + getSenderEmailByName(accountName)
                        + System.lineSeparator());
            }
        }

        Mail mail = parseMailMetadata(mailMetadata, mailContent);

        if (sentMails.containsKey(accountName)) {
            sentMails.get(accountName).add(mail);
        } else {
            sentMails.put(accountName, new ArrayList<>(List.of(mail)));
        }

        for (String currRecipient : mail.recipients()) {
            if (checkIfAccountWithThisEmailExists(currRecipient)) {
                receiveMail(getSenderNameByMail(currRecipient), mailMetadata, mailContent);
            }
        }
    }

    public Set<Account> getAllAccounts() {
        return allAccounts;
    }

    public Set<Rule> getAllRules(String name) {
        return rules.get(name);
    }

    public Inbox getReceived(String name) {
        return receivedMails.get(name);
    }

    public void addMailToAccount(String name, String folder, Mail mail) {
        receivedMails.get(name).putMailInFolder(folder, mail);
    }

    private void checkIfAccountExists(String accountName) {
        if (allAccounts.isEmpty()) {
            throw new AccountNotFoundException("There is not such account");
        }

        int flag = 0;
        for (Account currAccount : allAccounts) {
            if (currAccount.name().equals(accountName)) {
                flag = 1;
                break;
            }
        }
        if (flag == 0) {
            throw new AccountNotFoundException("There is not such account");
        }
    }

    private boolean checkIfAccountWithThisEmailExists(String mail) {
        if (allAccounts.isEmpty()) {
            return false;
        }

        for (Account currAccount : allAccounts) {
            if (currAccount.emailAddress().equals(mail)) {
                return true;
            }
        }

        return false;
    }

    private void checkIfFolderExists(String path, String accountName) {
        if (!receivedMails.containsKey(accountName)) {
            throw new FolderNotFoundException("There is not such folder");
        }

        Inbox inbox = receivedMails.get(accountName);

        if (!inbox.checkIfFolderExists(path)) {
            throw new FolderNotFoundException("There is not such folder");
        }
    }

    private void checkIfStringIsNullEmptyOrBlank(String value, String exceptionMessage) {
        if (value == null || value.isEmpty() || value.isBlank()) {
            throw new IllegalArgumentException(exceptionMessage);
        }
    }

    private boolean checkIfRuleIsConflict(String accountName, Rule rule, String path) {
        if (rules.isEmpty()) {
            return false;
        }

        Set<Rule> accountRules = rules.get(accountName);

        for (Rule currRule : accountRules) {
            if (currRule.getPriority() == rule.getPriority() && !currRule.getFolderPath().equals(path)) {
                if (currRule.checkIfTwoCollectionsAreEqual(currRule.getSubjectIncludes(), rule.getSubjectIncludes()) &&
                        currRule.checkIfTwoCollectionsAreEqual(currRule.getSubjectOrBodyIncludes(), rule.getSubjectOrBodyIncludes()) &&
                        currRule.checkIfTwoCollectionsAreEqual(currRule.getRecipientsEmails(), rule.getRecipientsEmails()) &&
                        currRule.getSender().equals(rule.getSender())) {

                    return true;
                }
            }
        }

        return false;
    }

    private String getSenderNameByMail(String mail) {
        for (Account curr : allAccounts) {
            if (curr.emailAddress().equals(mail)) {
                return curr.name();
            }
        }

        return null;
    }

    private String getSenderEmailByName(String name) {
        for (Account curr : allAccounts) {
            if (curr.name().equals(name)) {
                return curr.emailAddress();
            }
        }

        return null;
    }

    public Mail parseMailMetadata(String mailMetadata, String mailBody) {
        String[] words = mailMetadata.strip().split(MAIL_METADATA_REGEX);
        String senderMail = "";
        String senderName = "";
        String subject = "";
        String recipients;
        Set<String> rec = new HashSet<>();
        LocalDateTime dateTime = null;

        for (int i = 0; i < words.length; i++) {
            switch (words[i]) {
                case MAIL_SENDER -> {
                    i++;
                    senderMail = words[i].strip();
                    senderName = getSenderNameByMail(senderMail);
                }
                case SUBJECT -> {
                    i++;
                    subject = words[i].strip();
                }
                case RECIPIENTS -> {
                    i++;
                    recipients = words[i].strip();
                    String[] allRecipients = recipients.strip().split(CSV_SEPARATOR);
                    for (String currRecipient : allRecipients) {
                        rec.add(currRecipient.strip());
                    }
                }
                case RECEIVED -> {
                    i++;
                    String first = words[i].strip();
                    i++;
                    String sec = words[i].strip();
                    String time = first + TIME_SEPARATOR + sec;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
                    dateTime = LocalDateTime.parse(time, formatter);
                }
            }
        }

        Account account = new Account(senderMail, senderName);
        return new Mail(account, rec, subject, mailBody, dateTime);
    }
}

