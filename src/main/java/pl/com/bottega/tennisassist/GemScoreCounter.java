package pl.com.bottega.tennisassist;

import static pl.com.bottega.tennisassist.GemPoint.ADVANTAGE;
import static pl.com.bottega.tennisassist.GemPoint.FOURTY;
import static pl.com.bottega.tennisassist.GemPoint.GEM;
import static pl.com.bottega.tennisassist.GemPoint.ZERO;

abstract class GemScoreCounter {

    Score<GemPoint> score;

    GemScoreCounter(String player1, String player2) {
        score = new Score<>(player1, player2, ZERO);
    }

    static GemScoreCounter of(String player1, String player2, GemScoringType gemScoringType) {
        return gemScoringType == GemScoringType.WITH_ADVANTAGE ? new AdvantageGemScoreCounter(player1, player2) : new GoldenBallGemScoreCounter(player1, player2);
    }

    abstract void increment(String player);

    boolean hasWon(String player) {
        return score.getScore(player) == GEM;
    }

    Score<GemPoint> getScore() {
        return score;
    }

    void setIncremented(String player, GemPoint currentPoints) {
        score = score.withUpdatedScore(player, GemPoint.values()[currentPoints.ordinal() + 1]);
    }
}

class AdvantageGemScoreCounter extends GemScoreCounter {

    private final String player1;
    private final String player2;

    AdvantageGemScoreCounter(String player1, String player2) {
        super(player1, player2);
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    void increment(String player) {
        GemPoint playerPoints = score.getScore(player);
        if(playerPoints == GEM) {
            throw new IllegalStateException("Cannot increment. The gem has finished.");
        }
        GemPoint otherPlayerPoints = score.getOtherPlayerScore(player);
        if(otherPlayerPoints == ADVANTAGE) {
            setDeuce();
        } else if(playerPoints == FOURTY && otherPlayerPoints != FOURTY) {
            score = score.withUpdatedScore(player, GEM);
        } else {
            setIncremented(player, playerPoints);
        }
    }

    private void setDeuce() {
        score = new Score<>(player1, FOURTY, player2, FOURTY);
    }
}

class GoldenBallGemScoreCounter extends GemScoreCounter {

    GoldenBallGemScoreCounter(String player1, String player2) {
        super(player1, player2);
    }

    @Override
    void increment(String player) {
        GemPoint playerPoints = score.getScore(player);
        if(playerPoints == GEM) {
            throw new IllegalStateException("Cannot increment. The gem has finished.");
        }
        GemPoint otherPlayerPoints = score.getOtherPlayerScore(player);
        if(playerPoints == FOURTY) {
            score = score.withUpdatedScore(player, GEM);
        } else {
            setIncremented(player, playerPoints);
        }
    }
}