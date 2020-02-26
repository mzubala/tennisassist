package pl.com.bottega.tennisassist;

class TiebreakInPlay implements MatchState {

    private final TennisMatch tennisMatch;
    private final String firstServingPlayerInTiebreak;
    private final TiebreakScoreCounter tiebreakScoreCounter;

    TiebreakInPlay(TennisMatch tennisMatch, String currentlyServingPlayer) {
        this.tennisMatch = tennisMatch;
        firstServingPlayerInTiebreak = currentlyServingPlayer;
        tiebreakScoreCounter = new TiebreakScoreCounter(tennisMatch.player1, tennisMatch.player2, 7);
    }

    @Override
    public void registerFirstServingPlayer(String player) {
        throw new IllegalStateException("A server has already been chosen");
    }

    @Override
    public void startPlay() {
        throw new IllegalStateException("A tiebreak is currently in progress");
    }

    @Override
    public void registerPoint(String winningPlayer) {
        tiebreakScoreCounter.increment(winningPlayer);
        if(tiebreakScoreCounter.isWon(winningPlayer)) {
            tennisMatch.finishGem(winningPlayer);
            tennisMatch.currentlyServingPlayer = firstServingPlayerInTiebreak.equals(tennisMatch.player1) ? tennisMatch.player2 : tennisMatch.player1;
        } else if(tiebreakScoreCounter.shouldChangeServingPlayer()) {
            tennisMatch.changeServingPlayer();
        }
    }
}
