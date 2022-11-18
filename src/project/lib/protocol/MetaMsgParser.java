package project.lib.protocol;

import java.util.List;

import project.lib.protocol.MetaMessage.Body;
import project.lib.protocol.MetaMessage.Body.Atom;
import project.lib.protocol.MetaMessage.Body.Mapping;
import project.lib.protocol.scaffolding.collections.HList;
import project.lib.protocol.scaffolding.parser.Parser;
import project.lib.protocol.scaffolding.parser.Parsers;
import project.lib.protocol.scaffolding.parser.Source;

public class MetaMsgParser implements MetaMessageParser {
    private static final Parser<MetaMessage.Body.Atom> atom = Parsers.regex("[^;&\n]+").map(AtomImpl::of);
    private static final Parser<String> id = Parsers.regex("[_a-zA-Z][_a-zA-Z0-9]*");
    private static final Parser<String> key = Parsers.regex("[_a-zA-Z0-9]+");
    private static final Parser<String> at = Parsers.regex("@");
    private static final Parser<String> colon = Parsers.regex(":");
    private static final Parser<String> semicolon = Parsers.regex(";");
    private static final Parser<String> equal = Parsers.regex("=");
    private static final Parser<String> ampasand = Parsers.regex("&");
    private static final Parser<AtomRule> atomRule = key.join(equal).join(atom).map(AtomRule::of);
    private static final Parser<RuleSet> ruleSet = createRuleSet();
    private static final Parser<Mapping> mapping = ruleSet.map(MetaMsgParser::mappingOf);
    private static final Parser<RecRule> recRule = key.join(colon).join(ruleSet).join(semicolon).map(RecRule::of);
    private static final Parser<Body> body = mapping.map(x -> (Body) x).or(atom.map(x -> (Body) x));
    private static final Parser<MetaMessage> metaMessage = id.join(at).join(body)
            .map(MetaMsgParser::createMetaMessage);

    private static Parser<RuleSet> createRuleSet() {
        final Parser<RecRule> recRule = (input) -> MetaMsgParser.recRule.parse(input);
        final var atomOrRec = atomRule.map(x -> (Rule) x).or(recRule.map(x -> (Rule) x));
        final var separated = atomOrRec.separated(ampasand, 1, 0);
        return separated.map(RuleSet::of);
    }

    private static MetaMessage createMetaMessage(HList<HList<String, String>, MetaMessage.Body> list) {
        final var body = list.head;
        final var id = list.rest.rest;

        return new MetaMessage() {
            {
                this._body = body;
                this._id = id;
            }

            private final MetaMessage.Body _body;
            private final String _id;

            @Override
            public MetaMessage.Body body() {
                return this._body;
            }

            @Override
            public String identity() {
                return this._id;
            }
        };
    }

    private static Mapping mappingOf(RuleSet ruleSet) {
        final var builder = new MappingImpl.Builder();
        for (final var rule : ruleSet.rules) {
            if (rule.getClass() == RecRule.class) {
                final var r = (RecRule) rule;
                builder.add(r.key, mappingOf(r.ruleSet));
            } else {
                final var r = (AtomRule) rule;
                builder.add(r.key, r.atom);
            }
        }
        return builder.build();
    }

    public static final MetaMessageParser instance = new MetaMsgParser();

    @Override
    public MetaMessage parse(CharSequence sequence) {
        final var result = metaMessage.parse(Source.from(sequence));
        if (result == null) {
            return null;
        }
        if (!result.rest.isEmpty()) {
            return null;
        }
        return result.value;
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