package bg.sofia.uni.fmi.mjt.mail;

import bg.sofia.uni.fmi.mjt.mail.exceptions.RuleAlreadyDefinedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RuleTest {

    @Test
    void testParseRuleDefinitionRuleAlreadyDefined() {
        Rule rule = new Rule("/inbox/important", 3);

        String ruleDefinition = "subject-includes: mjt, fmi, 2022" + System.lineSeparator() +
                "subject-or-body-includes: exam" + System.lineSeparator() +
                "from: niki@abv.bg" + System.lineSeparator() +
                "subject-or-body-includes: hard";

        assertThrows(RuleAlreadyDefinedException.class, () -> rule.parseRuleDefinition(ruleDefinition),
                "Rule definition can not contain same rule conditions multiple times");
    }

    @Test
    void testParseRuleDefinitionSuccessfully() {
        Rule rule = new Rule("/inbox/important", 3);

        String ruleDefinition = "subject-includes: mjt, fmi, 2022" + System.lineSeparator() +
                "subject-or-body-includes: exam" + System.lineSeparator() +
                "from: niki@abv.bg" + System.lineSeparator() +
                "recipients-includes: ivan@abv.bg, stoyo@abv.bg";

        Set<String> subjectIncludes = new HashSet<>();
        subjectIncludes.add("mjt");
        subjectIncludes.add("fmi");
        subjectIncludes.add("2022");

        Set<String> subjectBodyIncludes = new HashSet<>();
        subjectBodyIncludes.add("exam");

        Set<String> recipients = new HashSet<>();
        recipients.add("ivan@abv.bg");
        recipients.add("stoyo@abv.bg");

        String from = "niki@abv.bg";
        rule.parseRuleDefinition(ruleDefinition);

        assertEquals(from, rule.getSender(), "Invalid sender - check parsing rule definition");
        assertIterableEquals(subjectIncludes, rule.getSubjectIncludes(),
                "Invalid subject includes - check parsing rule definition");
        assertIterableEquals(subjectBodyIncludes, rule.getSubjectOrBodyIncludes(),
                "Invalid subject or body includes - check parsing rule definition");
        assertIterableEquals(recipients, rule.getRecipientsEmails(),
                "Invalid recipients - check parsing rule definition");
    }

    @Test
    void testParseRuleDefinitionMissingConditions() {
        Rule rule = new Rule("/inbox/important", 3);

        String ruleDefinition = "recipients-includes: ivan@abv.bg, stoyo@abv.bg" + System.lineSeparator() +
                "from: niki@abv.bg" + System.lineSeparator() +
                "subject-includes: mjt, fmi, 2022";

        Set<String> subjectIncludes = new HashSet<>();
        subjectIncludes.add("mjt");
        subjectIncludes.add("fmi");
        subjectIncludes.add("2022");

        Set<String> recipients = new HashSet<>();
        recipients.add("ivan@abv.bg");
        recipients.add("stoyo@abv.bg");

        String from = "niki@abv.bg";
        rule.parseRuleDefinition(ruleDefinition);

        assertEquals(from, rule.getSender(), "Invalid sender - check parsing rule definition");
        assertIterableEquals(subjectIncludes, rule.getSubjectIncludes(),
                "Invalid subject includes - check parsing rule definition");
        assertIterableEquals(recipients, rule.getRecipientsEmails(),
                "Invalid subject or body includes - check parsing rule definition");
        assertIterableEquals(new HashSet<>(), rule.getSubjectOrBodyIncludes(),
                "Missing condition should be empty");
    }

    @Test
    void testCheckIfMailMatchesRuleSuccessfully() {
        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        Rule rule = new Rule("/inbox/important", 2);
        String ruleDefinition = "recipients-includes: ivan@abv.bg, stoyo@abv.bg" + System.lineSeparator() +
                "from: niki@abv.bg" + System.lineSeparator() +
                "subject-includes: cup, final, football";

        rule.parseRuleDefinition(ruleDefinition);
        assertTrue(rule.checkIfMailMatchesRule(mail),
                "Mail was expected to match the rule, check the condition where exact or partial match were expected");

    }

    @Test
    void testCheckIfMailMatchesRuleMissingSubjectIncludes() {
        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        Rule rule = new Rule("/inbox/important", 2);
        String ruleDefinition = "recipients-includes: ivan@abv.bg, stoyo@abv.bg" + System.lineSeparator() +
                "from: niki@abv.bg" + System.lineSeparator() +
                "subject-includes: mjt, final, football";

        rule.parseRuleDefinition(ruleDefinition);
        assertFalse(rule.checkIfMailMatchesRule(mail),
                "Mail was not expected to match the rule, All subject-includes must be presented in mail");
    }

    @Test
    void testCheckIfMailMatchesRuleDifferentSender() {
        Set<String> mailRecipients = new HashSet<>();
        mailRecipients.add("stoyo@gmail.com");
        mailRecipients.add("ivan@abv.bg");

        Mail mail = new Mail(new Account("niki@abv.bg", "Nikolay"), mailRecipients, "football world cup final",
                "Everyone is watching the final today", LocalDateTime.now());

        Rule rule = new Rule("/inbox/important", 2);
        String ruleDefinition = "recipients-includes: ivan@abv.bg, stoyo@abv.bg" + System.lineSeparator() +
                "from: pesho@abv.bg" + System.lineSeparator() +
                "subject-includes: final, football";

        rule.parseRuleDefinition(ruleDefinition);
        assertFalse(rule.checkIfMailMatchesRule(mail),
                "Mail was not expected to match the rule, Sender must be exact match");
    }

}
