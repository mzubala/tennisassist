package pl.com.bottega.tennisassist

import spock.lang.Specification

import static pl.com.bottega.tennisassist.GemPoint.ADVANTAGE
import static pl.com.bottega.tennisassist.GemPoint.FIFTEEN
import static pl.com.bottega.tennisassist.GemPoint.FOURTY
import static pl.com.bottega.tennisassist.GemPoint.THIRTY
import static pl.com.bottega.tennisassist.GemPoint.ZERO
import static pl.com.bottega.tennisassist.GemScoring.NO_ADVANTAGE
import static pl.com.bottega.tennisassist.SetFormat.BEST_OF_FIVE
import static pl.com.bottega.tennisassist.SetFormat.BEST_OF_THREE
import static pl.com.bottega.tennisassist.SetFormat.BEST_OF_TWO_WITH_SUPER_TIEBREAK
import static pl.com.bottega.tennisassist.SetKind.SUPER_TIEBREAK
import static pl.com.bottega.tennisassist.SetKind.TIEBREAK
import static pl.com.bottega.tennisassist.TennisMatchBuilder.aMatch

class TennisMatchSpec extends Specification {

    private String rafa = "Rafael Nadal"
    private String roger = "Roger Federer"

    def "match score is 0:0 before the start of the first gem"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        expect:
            match.score() == new Score(roger, 0, rafa, 0)
    }

    def "nobody is serving before the draw"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        expect:
            match.currentServer().isEmpty()
    }

    def "registers the first server after the draw"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        when:
            match.registerFirstServer(rafa)

        then:
            match.currentServer() == Optional.of(rafa)
    }

    def "there is no gem score before gem starts"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        expect:
            match.gemScore().isEmpty()

        when:
            match.registerFirstServer(roger)

        then:
            match.gemScore().isEmpty()
    }

    def "there is no set score before gem starts"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        expect:
            match.setScore().isEmpty()

        when:
            match.registerFirstServer(roger)

        then:
            match.setScore().isEmpty()
    }

    def "gem score is 0 0 just after the gem start"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            match.startPlay()

        then:
            match.gemScore().isPresent()
            match.gemScore().ifPresent {
                assert it.scoreOf(rafa) == ZERO
                assert it.scoreOf(roger) == ZERO
            }
    }

    def "gem score changes when points are played in advantage mode"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withGemScoring(GemScoring.ADVANTAGE).get()
            match.registerFirstServer(rafa)
            match.startPlay()

        when:
            match.registerPoint(rafa)

        then:
            match.gemScore().get() == new GemScore(rafa, FIFTEEN, roger, ZERO)

        when:
            match.registerPoint(rafa)
            match.registerPoint(roger)

        then:
            match.gemScore().get() == new GemScore(rafa, THIRTY, roger, FIFTEEN)

        when:
            match.registerPoint(rafa)
            match.registerPoint(roger)
            match.registerPoint(roger)

        then:
            match.gemScore().get() == new GemScore(rafa, FOURTY, roger, FOURTY)

        when:
            match.registerPoint(rafa)

        then:
            match.gemScore().get() == new GemScore(rafa, ADVANTAGE, roger, FOURTY)

        when:
            match.registerPoint(roger)

        then:
            match.gemScore().get() == new GemScore(rafa, FOURTY, roger, FOURTY)
    }

    def "set score changes when players win gem in advantage mode"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withGemScoring(GemScoring.ADVANTAGE).get()
            match.registerFirstServer(rafa)

        when:
            playGem(match, rafa, roger)

        then:
            match.setScore() == Optional.of(new Score(rafa, 1, roger, 0))
    }

    def "set score changes when players win gem in no-advantage mode"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withGemScoring(NO_ADVANTAGE).get()
            match.registerFirstServer(rafa)
            match.startPlay()

        when:
            3.times {
                match.registerPoint(roger)
                match.registerPoint(rafa)
            }
            match.registerPoint(roger)

        then:
            match.setScore() == Optional.of(new Score(rafa, 0, roger, 1))
    }

    def "set score changes when players win gem after a couple deuces in advantage mode"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withGemScoring(GemScoring.ADVANTAGE).get()
            match.registerFirstServer(rafa)

        when:
            playGemWithDeuces(match, rafa, roger)

        then:
            match.setScore() == Optional.of(new Score(rafa, 1, roger, 0))
    }

    def "set score does not change until the gem is finished"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)
            match.startPlay()

        expect:
            match.setScore() == Optional.of(new Score(rafa, 0, roger, 0))

        when:
            match.registerPoint(rafa)
            match.registerPoint(roger)
            match.registerPoint(rafa)
            match.registerPoint(rafa)

        then:
            match.setScore() == Optional.of(new Score(rafa, 0, roger, 0))
    }

    def "there is no gem score just after the gem is finished"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            playGem(match, rafa, roger)

        then:
            match.gemScore() == Optional.empty()
    }

    def "set score changes when players win gems"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            playGem(match, rafa, roger)
            playGem(match, roger, rafa)

        then:
            match.setScore().get() == new Score(rafa, 1, roger, 1)

        when:
            playGem(match, rafa, roger)
            playGem(match, roger, rafa)
            playGem(match, rafa, roger)
            playGem(match, roger, rafa)
            playGem(match, rafa, roger)
            playGem(match, roger, rafa)

        then:
            match.setScore().get() == new Score(rafa, 4, roger, 4)
    }

    def "match score changes when player wins a set"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            playGem(match, rafa, roger)
            playGem(match, rafa, roger)
            playGem(match, rafa, roger)
            playGem(match, rafa, roger)
            playGem(match, rafa, roger)
            playGem(match, rafa, roger)

        then:
            match.score() == new Score(rafa, 1, roger, 0)
    }

    def "when score is 6:5 player needs to win one more gem to win a set"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            5.times {
                playGem(match, rafa, roger)
                playGem(match, roger, rafa)
            }
            playGem(match, rafa, roger)

        then:
            match.setScore().get() == new Score(rafa, 6, roger, 5)
            match.score() == new Score(rafa, 0, roger, 0)

        when:
            playGem(match, rafa, roger)

        then:
            match.setScore() == Optional.empty()
            match.score() == new Score(rafa, 1, roger, 0)
    }

    def "server changes after each gem"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        expect:
            match.currentServer().get() == rafa

        when:
            playGem(match, rafa, roger)

        then:
            match.currentServer().get() == roger

        when:
            playGem(match, rafa, roger)

        then:
            match.currentServer().get() == rafa
    }

    def "match score changes when second set is won"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)

        when:
            6.times {
                playGem(match, rafa, roger)
            }
            6.times {
                playGem(match, roger, rafa)
            }

        then:
            match.score() == new Score(rafa, 1, roger, 1)
            match.setScore().isEmpty()
    }

    def "player wins a match 2:1 in best-of-three format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_THREE).get()
            match.registerFirstServer(rafa)

        expect:
            match.winner().isEmpty()

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, roger, rafa)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, roger, rafa)

        then:
            match.winner().get() == roger
    }

    def "player wins a match 2:0 in best-of-three format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_THREE).get()
            match.registerFirstServer(rafa)

        when:
            playSet(match, rafa, roger)
            playSet(match, rafa, roger)

        then:
            match.winner().get() == rafa
    }

    def "player wins a match 3:0 in best-of-five format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_FIVE).get()
            match.registerFirstServer(rafa)

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().get() == rafa
    }

    def "player wins a match 3:1 in best-of-five format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_FIVE).get()
            match.registerFirstServer(rafa)

        when:
            playSet(match, rafa, roger)
            playSet(match, rafa, roger)
            playSet(match, roger, rafa)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().get() == rafa
    }

    def "player wins a match 3:2 in best-of-five format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_FIVE).get()
            match.registerFirstServer(rafa)

        when:
            playSet(match, rafa, roger)
            playSet(match, roger, rafa)
            playSet(match, rafa, roger)
            playSet(match, roger, rafa)

        then:
            match.winner().isEmpty()

        when:
            playSet(match, rafa, roger)

        then:
            match.winner().get() == rafa
    }

    def "player wins a set in a tiebreak"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(rafa)
            6.times {
                playGem(match, roger, rafa)
                playGem(match, rafa, roger)
            }

        when:
            match.startPlay()
            5.times {
                match.registerPoint(roger)
                match.registerPoint(rafa)
            }
            match.registerPoint(roger)
            match.registerPoint(roger)

        then:
            match.score() == new Score(roger, 1, rafa, 0)
    }

    def "wins match with tiebreak"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withLastSetKind(TIEBREAK).get()
            match.registerFirstServer(rafa)

        when:
            playSetToTiebreak(match)
            playTiebreak(match, roger, rafa)
            playSetToTiebreak(match)
            playTiebreak(match, roger, rafa)

        then:
            match.score() == new Score(roger, 2, rafa, 0)
            match.winner().get() == roger

    }

    def "wins match in best-of-two-with-supertiebreak format"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_TWO_WITH_SUPER_TIEBREAK).get()
            match.registerFirstServer(rafa)
            playSet(match, roger, rafa)
            playSet(match, rafa, roger)

        when:
            match.startPlay()
            9.times {
                match.registerPoint(rafa)
            }
            11.times {
                match.registerPoint(roger)
            }

        then:
            match.winner().get() == roger
            match.score() == new Score(roger, 2, rafa, 1)
    }

    def "wins match with 2 gems advantage in last set"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_THREE).withLastSetKind(SetKind.ADVANTAGE).get()
            match.registerFirstServer(rafa)
            playSet(match, roger, rafa)
            playSet(match, rafa, roger)

        when:
            10.times {
                playGem(match, roger, rafa)
                playGem(match, rafa, roger)
            }

        then:
            match.winner().isEmpty()
            match.score() == new Score(roger, 1, rafa, 1)
            match.setScore().get() == new Score(roger, 10, rafa, 10)

        when:
            2.times { playGem(match, rafa, roger) }

        then:
            match.winner().get() == rafa
            match.score() == new Score(roger, 1, rafa, 2)
            match.setScore().isEmpty()
    }

    def "wins match with super tiebreak in last set"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_THREE).withLastSetKind(SetKind.SUPER_TIEBREAK).get()
            match.registerFirstServer(rafa)
            playSet(match, roger, rafa)
            playSet(match, rafa, roger)
            playSetToTiebreak(match)

        when:
            match.startPlay()
            9.times {
                match.registerPoint(rafa)
            }
            9.times {
                match.registerPoint(roger)
            }
            match.registerPoint(roger)
            match.registerPoint(roger)


        then:
            match.winner().get() == roger
            match.score() == new Score(roger, 2, rafa, 1)
            match.setScore().isEmpty()
    }

    def "changes server when playing tiebreak"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withLastSetKind(TIEBREAK).get()
            match.registerFirstServer(rafa)
            playSetToTiebreak(match)

        when:
            match.startPlay()

        then:
            match.currentServer().get() == rafa

        when:
            match.registerPoint(roger) // 1 0

        then:
            match.currentServer().get() == roger

        when:
            match.registerPoint(rafa) // 1 1

        then:
            match.currentServer().get() == roger

        when:
            match.registerPoint(roger) // 2 1

        then:
            match.currentServer().get() == rafa

        when:
            3.times {
                match.registerPoint(rafa)
                match.registerPoint(roger)
            }

        then:
            match.currentServer().get() == roger
    }

    def "changes server when playing super tiebreak"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_TWO_WITH_SUPER_TIEBREAK).get()
            match.registerFirstServer(rafa)
            playSetToTiebreak(match)
            playTiebreak(match, rafa, roger)
            playSetToTiebreak(match)
            playTiebreak(match, roger, rafa)

        expect:
            match.currentServer().get() == rafa

        when:
            match.startPlay()
            match.registerPoint(roger) // 1 0

        then:
            match.currentServer().get() == roger

        when:
            match.registerPoint(rafa) // 1 1

        then:
            match.currentServer().get() == roger

        when:
            match.registerPoint(roger) // 2 1

        then:
            match.currentServer().get() == rafa

        when:
            5.times {
                match.registerPoint(rafa)
                match.registerPoint(roger)
            }

        then:
            match.currentServer().get() == roger
    }

    def "changes server correctly when tiebreak ends"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withLastSetKind(TIEBREAK).get()
            match.registerFirstServer(rafa)
            playSetToTiebreak(match)

        when:
            playTiebreak(match, roger, rafa)

        then:
            match.currentServer().get() == roger
    }

    def "does not allow to start gem if there is no first server chosen"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        when:
            match.startPlay()

        then:
            IllegalStateException ex = thrown(IllegalStateException)
            ex.message == "The server has not yet been chosen"
    }

    def "does not allow to start gem if one is already in progress"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(roger)
            match.startPlay()

        when:
            match.startPlay()

        then:
            IllegalStateException ex = thrown(IllegalStateException)
            ex.message == "A gem is currently in progress"
    }

    def "does not allow to chose a server twice"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(roger)

        when:
            match.registerFirstServer(rafa)

        then:
            IllegalStateException ex = thrown(IllegalStateException)
            ex.message == "A server has already been chosen"
    }

    def "does not allow to start a gem when a match is over"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()
            match.registerFirstServer(roger)
            playSet(match, roger, rafa)
            playSet(match, roger, rafa)

        when:
            match.startPlay()

        then:
            IllegalStateException ex = thrown(IllegalStateException)
            ex.message == "A match is finished"
    }

    def "does not allow to register a point if there is no play in progress"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).get()

        when:
            match.registerPoint(rafa)

        then:
            IllegalStateException ex = thrown(IllegalStateException)
            ex.message == "No play is currently in progress"
    }

    def "remembers result of each set"() {
        given:
            TennisMatch match = aMatch().between(roger, rafa).withSetFormat(BEST_OF_FIVE).withLastSetKind(SUPER_TIEBREAK).get()
            match.registerFirstServer(rafa)

        when:
            playSet(match, rafa, roger, 2)

        then:
            match.setScores() == [new Score(rafa, 6, roger, 2)]

        when:
            playSet(match, roger, rafa, 6)
            playSet(match, rafa, roger, 5)
            playSet(match, roger, rafa, 3)
            playSet(match, roger, rafa, 6)

        then:
            match.setScores() == [
                new Score(rafa, 6, roger, 2),
                new Score(roger, 7, rafa, 6),
                new Score(rafa, 7, roger, 5),
                new Score(roger, 6, rafa, 3),
                new Score(roger, 7, rafa, 6)
            ]
    }

    private void playGem(TennisMatch match, String winner, String looser) {
        match.startPlay()
        match.registerPoint(winner)
        match.registerPoint(looser)
        match.registerPoint(winner)
        match.registerPoint(winner)
        match.registerPoint(looser)
        match.registerPoint(winner)
    }

    private void playGemWithDeuces(TennisMatch match, String winner, String looser) {
        match.startPlay()
        match.registerPoint(winner) // 15 - 0
        match.registerPoint(looser) // 15 - 15
        match.registerPoint(winner) // 30 - 15
        match.registerPoint(winner) // 40 - 15
        match.registerPoint(looser) // 40 - 30
        match.registerPoint(looser) // 40 - 40
        match.registerPoint(looser) // 40 - A
        match.registerPoint(winner) // 40 - 40
        match.registerPoint(looser) // 40 - A
        match.registerPoint(winner) // 40 - 40
        match.registerPoint(looser) // 40 - A
        match.registerPoint(winner) // 40 - 40
        match.registerPoint(winner) // A - 40
        match.registerPoint(winner)
    }

    private void playSet(TennisMatch match, String winner, String looser, int looserGems = 3) {
        if (looserGems == 6) {
            playSetToTiebreak(match, winner, looser)
            playTiebreak(match, winner, looser)
        } else {
            looserGems.times {
                playGem(match, winner, looser)
                playGem(match, looser, winner)
            }
            ([2, 6 - looserGems].max()).times {
                playGem(match, winner, looser)
            }
        }
    }

    private void playSetToTiebreak(TennisMatch match, String player1 = roger, String player2 = rafa) {
        6.times {
            playGem(match, roger, rafa)
            playGem(match, rafa, roger)
        }
    }

    private void playTiebreak(TennisMatch match, String winner, String looser) {
        match.startPlay()
        10.times {
            match.registerPoint(winner)
            match.registerPoint(looser)
        }
        match.registerPoint(winner)
        match.registerPoint(winner)
    }
}