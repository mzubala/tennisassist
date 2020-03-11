package pl.com.bottega.tennisassist;

class BreakInPlay implements MatchState {

    private TennisMatch tennisMatch;

    BreakInPlay(TennisMatch tennisMatch) {
        this.tennisMatch = tennisMatch;
    }

    @Override
    public void registerFirstServingPlayer(Player player) {
        throw new IllegalStateException("The server has already been chosen");
    }

    @Override
    public void startPlay() {
        tennisMatch.ensureSetStarted();
        if (tennisMatch.needsSuperTiebreak()) {
            tennisMatch.startSuperTiebreak();
        } else if (tennisMatch.currentSetScoreCounter.needsTiebreak()) {
            tennisMatch.startTiebreak();
        } else {
            tennisMatch.startGem();
        }
    }

    @Override
    public void registerPoint(Player winningPlayer) {
        throw new IllegalStateException("No play is currently in progress");
    }
}
