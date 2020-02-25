package pl.com.bottega.tennisassist

import static pl.com.bottega.tennisassist.GemScoring.*
import static pl.com.bottega.tennisassist.SetFormat.*
import static pl.com.bottega.tennisassist.SetKind.*

class TennisMatchBuilder {

    String player1
    String player2
    GemScoring gemScoring = ADVANTAGE
    SetFormat setFormat = BEST_OF_THREE
    SetKind lastSetKind = TIEBREAK

    static TennisMatchBuilder aMatch() {
        return new TennisMatchBuilder()
    }

    TennisMatchBuilder between(String player1, String player2) {
        this.player1 = player1
        this.player2 = player2
        return this
    }

    TennisMatchBuilder withGemScoring(GemScoring gemScoring) {
        this.gemScoring = gemScoring
        return this
    }

    TennisMatchBuilder withSetFormat(SetFormat setFormat) {
        this.setFormat = setFormat
        return this
    }

    TennisMatchBuilder withLastSetKind(SetKind lastSetKind) {
        this.lastSetKind = lastSetKind
        return this
    }

    TennisMatch get() {
        return new TennisMatch(player1, player2, setFormat, gemScoring, lastSetKind)
    }
}
