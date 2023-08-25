package bg.sofia.uni.fmi.mjt.mail;

import java.util.Collection;

public interface MailClient {

    Account addNewAccount(String accountName, String email);

    void createFolder(String accountName, String path);

    void addRule(String accountName, String folderPath, String ruleDefinition, int priority);

    void receiveMail(String accountName, String mailMetadata, String mailContent);

    Collection<Mail> getMailsFromFolder(String account, String folderPath);

    void sendMail(String accountName, String mailMetadata, String mailContent);

}