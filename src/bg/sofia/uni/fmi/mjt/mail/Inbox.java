package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderAlreadyExistsException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.FolderNotFoundException;
import bg.sofia.uni.fmi.mjt.mail.exceptions.InvalidPathException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Inbox {
    private Map<String, List<Mail>> mails;      //FolderName - Mails
    private static final String DEFAULT_FOLDER = "/inbox";
    private static final String FOLDER_SEPARATOR = "/";

    public Inbox() {
        mails = new HashMap<>();
        mails.put(DEFAULT_FOLDER, new ArrayList<>());
    }

    public boolean checkIfFolderExists(String path) {
        return mails.containsKey(path);
    }

    public void createFolder(String path) {

        if (checkIfFolderExists(path)) {
            throw new FolderAlreadyExistsException("This folder already exists for current account");
        }

        String[] subFolders = path.split(FOLDER_SEPARATOR);

        if (!(FOLDER_SEPARATOR + subFolders[1]).equals(DEFAULT_FOLDER)) {
            throw new InvalidPathException("The path does not start from the root");
        }
        int lastIndex = path.lastIndexOf(FOLDER_SEPARATOR);
        String subString = path.substring(0, lastIndex);
        if (!mails.containsKey(subString)) {
            throw new InvalidPathException("There are missing intermediate folders");
        }

        mails.put(path, new ArrayList<>());
    }

    public List<Mail> getMailsFromFolder(String folderPath) {
        if (!checkIfFolderExists(folderPath)) {
            throw new FolderNotFoundException("There is not such folder for the current account");
        }

        return mails.get(folderPath);
    }

    public void putMailInFolder(String folderPath, Mail mail) {
        mails.get(folderPath).add(mail);
    }

    public String checkForBestRule(Mail mail, Set<Rule> rule) {
        for (Rule currRule : rule) {
            if (currRule.checkIfMailMatchesRule(mail)) {
                return currRule.getFolderPath();
            }
        }

        return DEFAULT_FOLDER;
    }

    public void checkForBetterFolders(Set<Rule> rule) {
        List<Mail> inbox = mails.get(DEFAULT_FOLDER);
        Iterator<Mail> iterator = inbox.iterator();

        while (iterator.hasNext()) {
            Mail currMail = iterator.next();
            String path = checkForBestRule(currMail, rule);
            if (!path.equals(DEFAULT_FOLDER)) {
                putMailInFolder(path, currMail);
                iterator.remove();
            }
        }
    }
}
