package project.lib.protocol;

import java.io.IOException;
import java.util.List;

import project.lib.protocol.Ion.Atom;
import project.lib.protocol.Ion.Mapping;
import project.lib.scaffolding.collections.HList;
import project.lib.scaffolding.parser.Parser;
import project.lib.scaffolding.parser.Parsers;
import project.lib.scaffolding.parser.Source;

public class IonSerializer implements Serializer<Ion> {
    private static final Parser<Ion.Atom> atom = Parsers.regex("[^;&\n]+").map(AtomImpl::of);
    private static final Parser<String> key = Parsers.regex("[_a-zA-Z0-9]+");
    private static final Parser<String> colon = Parsers.regex(":");
    private static final Parser<String> semicolon = Parsers.regex(";");
    private static final Parser<String> equal = Parsers.regex("=");
    private static final Parser<String> ampasand = Parsers.regex("&");
    private static final Parser<AtomRule> atomRule = key.join(equal).join(atom).map(AtomRule::of);
    private static final Parser<RuleSet> ruleSet = createRuleSet();
    private static final Parser<Mapping> mapping = ruleSet.map(IonSerializer::mappingOf);
    private static final Parser<RecRule> recRule = key.join(colon).join(ruleSet).join(semicolon).map(RecRule::of);
    public static final Parser<Ion> parser = mapping.map(x -> (Ion) x).or(atom.map(x -> (Ion) x));

    public static final IonSerializer instance = new IonSerializer();

    private static Parser<RuleSet> createRuleSet() {
        final Parser<RecRule> recRule = (input) -> IonSerializer.recRule.parse(input);
        final var atomOrRec = atomRule.map(x -> (Rule) x).or(recRule.map(x -> (Rule) x));
        final var separated = atomOrRec.separated(ampasand, 1, 0);
        return separated.map(RuleSet::of);
    }

    private static Mapping mappingOf(RuleSet ruleSet) {
        final var builder = new MappingImpl.Builder();
        for (final var rule : ruleSet.rules) {
            if (rule instanceof RecRule r) {
                builder.add(r.key, mappingOf(r.ruleSet));
            } else {
                final var r = (AtomRule) rule;
                builder.add(r.key, r.atom);
            }
        }
        return builder.build();
    }

    @Override
    public Ion deserialize(CharSequence sequence) {
        final var result = parser.parse(Source.from(sequence));
        if (result == null) {
            return null;
        }
        return result.value;
    }

    @Override
    public void serialize(Appendable buffer, Ion value) {
        try {
            switch (value.TYPE) {
                case Ion.ATOM -> {
                    buffer.append(value.asAtom().text());
                }
                case Ion.MAPPING -> {
                    var noninitial = false;
                    for (final var entry : value.asMapping().entries()) {
                        if (noninitial) {
                            buffer.append('&');
                        } else {
                            noninitial = true;
                        }
                        final var key = entry.getKey();
                        final var val = entry.getValue();

                        buffer.append(key);
                        if (val.TYPE == Ion.ATOM) {
                            buffer.append('=');
                            serialize(buffer, val);
                        } else {
                            buffer.append(':');
                            serialize(buffer, val);
                            buffer.append(';');
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException();
        }
    }

}

class RuleSet {
    public static RuleSet of(List<Rule> rules) {
        return new RuleSet(rules.toArray(Rule[]::new));
    }

    public final Rule[] rules;

    private RuleSet(Rule[] rules) {
        this.rules = rules;
    }
}

abstract sealed class Rule permits RecRule, AtomRule {
    public final String key;

    protected Rule(String key) {
        this.key = key;
    }
}

final class RecRule extends Rule {
    public static RecRule of(HList<HList<HList<String, String>, RuleSet>, String> list) {
        final var ruleSet = list.rest.head;
        final var key = list.rest.rest.rest;
        return new RecRule(key, ruleSet);
    }

    public final RuleSet ruleSet;

    private RecRule(String key, RuleSet ruleSet) {
        super(key);
        this.ruleSet = ruleSet;
    }
}

final class AtomRule extends Rule {
    public static AtomRule of(HList<HList<String, String>, Atom> list) {
        final var atom = list.head;
        final var key = list.rest.rest;
        return new AtomRule(key, atom);
    }

    public final Atom atom;

    private AtomRule(String key, Atom atom) {
        super(key);
        this.atom = atom;
    }
}