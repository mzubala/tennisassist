package pl.com.bottega.tennisassist

import static GemScoringType.*
import static MatchFormat.*
import static TieResolutionType.*

class TennisMatchBuilder {

    String player1
    String player2
    GemScoringType gemScoring = WITH_ADVANTAGE
    MatchFormat setFormat = BEST_OF_THREE
    TieResolutionType lastSetKind = TIEBREAK

    static TennisMatchBuilder aMatch() {
        return new TennisMatchBuilder()
    }

    TennisMatchBuilder between(String player1, String player2) {
        this.player1 = player1
        this.player2 = player2
        return this
    }

    TennisMatchBuilder withGemScoring(GemScoringType gemScoring) {
        this.gemScoring = gemScoring
        return this
    }

    TennisMatchBuilder withSetFormat(MatchFormat setFormat) {
        this.setFormat = setFormat
        return this
    }

    TennisMatchBuilder withLastSetKind(TieResolutionType lastSetKind) {
        this.lastSetKind = lastSetKind
        return this
    }

    TennisMatch get() {
        return new TennisMatch(player1, player2, setFormat, gemScoring, lastSetKind)
    }
}
