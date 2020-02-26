package pl.com.bottega.tennisassist;

import static pl.com.bottega.tennisassist.TieResolutionType.SUPER_TIEBREAK;

class SupertiebreakInPlay implements MatchState {

    private TennisMatch tennisMatch;
    private TiebreakScoreCounter currentSuperTiebreakScore;

    public SupertiebreakInPlay(TennisMatch tennisMatch) {
        this.tennisMatch = tennisMatch;
        currentSuperTiebreakScore = new TiebreakScoreCounter(tennisMatch.player1, tennisMatch.player2, 10);
    }

    @Override
    public void registerFirstServingPlayer(String player) {
        throw new IllegalStateException("The server has already been chosen");
    }

    @Override
    public void startPlay() {
        throw new IllegalStateException("A supertiebreak is currently in progress");
    }

    @Override
    public void registerPoint(String winningPlayer) {
        currentSuperTiebreakScore.increment(winningPlayer);
        if (currentSuperTiebreakScore.isWon(winningPlayer)) {
            if(tennisMatch.finalSetTieResolutionType == SUPER_TIEBREAK) {
                tennisMatch.finishGem(winningPlayer);
            } else {
                tennisMatch.finishSet(winningPlayer, currentSuperTiebreakScore.getScore());
            }
        } else if (currentSuperTiebreakScore.shouldChangeServingPlayer()) {
            tennisMatch.changeServingPlayer();
        }
    }
}
