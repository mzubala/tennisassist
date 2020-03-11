package pl.com.bottega.tennisassist;

class TiebreakInPlay implements MatchState {

    private final TennisMatch tennisMatch;
    private final Player firstServingPlayerInTiebreak;
    private final TiebreakScoreCounter tiebreakScoreCounter;

    TiebreakInPlay(TennisMatch tennisMatch, Player currentlyServingPlayer) {
        this.tennisMatch = tennisMatch;
        firstServingPlayerInTiebreak = currentlyServingPlayer;
        tiebreakScoreCounter = new TiebreakScoreCounter(tennisMatch.players, 7);
    }

    @Override
    public void registerFirstServingPlayer(Player player) {
        throw new IllegalStateException("A server has already been chosen");
    }

    @Override
    public void startPlay() {
        throw new IllegalStateException("A tiebreak is currently in progress");
    }

    @Override
    public void registerPoint(Player winningPlayer) {
        tiebreakScoreCounter.increment(winningPlayer);
        if(tiebreakScoreCounter.isWon(winningPlayer)) {
            tennisMatch.finishGem(winningPlayer);
            tennisMatch.currentlyServingPlayer = firstServingPlayerInTiebreak.equals(tennisMatch.players.getPlayer1()) ? tennisMatch.players.getPlayer2() : tennisMatch.players.getPlayer1();
        } else if(tiebreakScoreCounter.shouldChangeServingPlayer()) {
            tennisMatch.changeServingPlayer();
        }
    }
}
