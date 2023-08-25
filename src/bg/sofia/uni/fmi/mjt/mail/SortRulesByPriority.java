package bg.sofia.uni.fmi.mjt.mail;

import java.util.Comparator;

public class SortRulesByPriority implements Comparator<Rule> {

    @Override
    public int compare(Rule first, Rule second) {
        return Integer.compare(first.getPriority(), second.getPriority());
    }
}
