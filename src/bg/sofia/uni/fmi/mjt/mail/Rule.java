package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Rule {

    private String folderPath;
    private Set<String> subjectIncludes;
    private Set<String> subjectOrBodyIncludes;
    private Set<String> recipientsEmails;
    private String sender;
    private int priority;
    private static final String SUBJECT = "subject-includes";
    private static final String SUBJECT_OR_BODY_INCLUDES = "subject-or-body-includes";
    private static final String RECIPIENTS = "recipients-includes";
    private static final String FROM = "from";
    private static final String RULE_DEFINITION_REGEX = "[:,\\r?\\n|\\r]+";

    public Rule(String folderPath, int priority) {
        this.folderPath = folderPath;
        this.priority = priority;
        subjectIncludes = new HashSet<>();
        subjectOrBodyIncludes = new HashSet<>();
        recipientsEmails = new HashSet<>();
    }

    public boolean checkIfTwoCollectionsAreEqual(Set<String> first, Set<String> second) {
        return first.containsAll(second) && second.containsAll(first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return priority == rule.priority && folderPath.equals(rule.folderPath)
                && checkIfTwoCollectionsAreEqual(subjectIncludes, rule.subjectIncludes)
                && checkIfTwoCollectionsAreEqual(subjectOrBodyIncludes, rule.subjectOrBodyIncludes)
                && checkIfTwoCollectionsAreEqual(recipientsEmails, rule.recipientsEmails)
                && sender.equals(rule.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderPath, subjectIncludes, subjectOrBodyIncludes, recipientsEmails, sender, priority);
    }

    private void checkIfRuleAlreadyDefined(String[] words) {
        int countSubjectIncludes = 0;
        int countSubjectOrBodyIncludes = 0;
        int countRecipientsIncludes = 0;
        int countFrom = 0;

        for (String word : words) {
            switch (word) {
                case SUBJECT -> countSubjectIncludes++;
                case SUBJECT_OR_BODY_INCLUDES -> countSubjectOrBodyIncludes++;
                case RECIPIENTS -> countRecipientsIncludes++;
                case FROM -> countFrom++;
            }
        }

        if (countFrom > 1 || countSubjectOrBodyIncludes > 1 || countSubjectIncludes > 1
                || countRecipientsIncludes > 1) {

            throw new RuleAlreadyDefinedException("This rule condition already exists");
        }
    }

    public void parseRuleDefinition(String ruleDefinition) {
        String[] words = ruleDefinition.strip().split(RULE_DEFINITION_REGEX);
        checkIfRuleAlreadyDefined(words);

        for (int i = 0; i < words.length; i++) {
            switch (words[i]) {
                case SUBJECT -> {
                    i++;
                    while (!words[i].equals(SUBJECT_OR_BODY_INCLUDES) && !words[i].equals(RECIPIENTS) &&
                            !words[i].equals(FROM)) {
                        subjectIncludes.add(words[i].strip());
                        i++;
                        if (i == words.length) {
                            break;
                        }
                    }
                    i--;
                }
                case SUBJECT_OR_BODY_INCLUDES -> {
                    i++;
                    while (!words[i].equals(SUBJECT) && !words[i].equals(RECIPIENTS) &&
                            !words[i].equals(FROM)) {
                        subjectOrBodyIncludes.add(words[i].strip());
                        i++;
                        if (i == words.length) {
                            break;
                        }
                    }
                    i--;
                }
                case RECIPIENTS -> {
                    i++;
                    while (!words[i].equals(SUBJECT) && !words[i].equals(SUBJECT_OR_BODY_INCLUDES) &&
                            !words[i].equals(FROM)) {
                        recipientsEmails.add(words[i].strip());
                        i++;
                        if (i == words.length) {
                            break;
                        }
                    }
                    i--;
                }
                case FROM -> {
                    i++;
                    sender = words[i].strip();
                }
            }
        }
    }

    public boolean checkIfMailMatchesRule(Mail mail) {
        int condition = 0;

        if (sender != null && !sender.equals(mail.sender().emailAddress())) {
            return false;
        }
        if (recipientsEmails != null && !recipientsEmails.isEmpty()) {
            condition = 1;
            int flag = 0;
            for (String email : recipientsEmails) {
                if (mail.recipients().contains(email)) {
                    flag = 1;
                    break;
                }
            }
            if (flag == 0) {
                return false;
            }
        }
        if (subjectIncludes != null && !subjectIncludes.isEmpty()) {
            condition = 1;
            for (String subjectWord : subjectIncludes) {
                if (!mail.subject().contains(subjectWord)) {
                    return false;
                }
            }
        }
        if (subjectOrBodyIncludes != null && !subjectOrBodyIncludes.isEmpty()) {
            condition = 1;
            for (String subjectOrBodyWord : subjectOrBodyIncludes) {
                if (!mail.subject().contains(subjectOrBodyWord) && !mail.body().contains(subjectOrBodyWord)) {
                    return false;
                }
            }
        }

        return condition == 1;   // If all of mail's data is null or empty, the mail doesn't match
    }

    public String getFolderPath() {
        return folderPath;
    }

    public Set<String> getSubjectIncludes() {
        return subjectIncludes;
    }

    public Set<String> getSubjectOrBodyIncludes() {
        return subjectOrBodyIncludes;
    }

    public Set<String> getRecipientsEmails() {
        return recipientsEmails;
    }

    public String getSender() {
        return sender;
    }

    public int getPriority() {
        return priority;
    }
}
