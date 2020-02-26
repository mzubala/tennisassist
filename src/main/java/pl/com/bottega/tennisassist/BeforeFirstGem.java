package pl.com.bottega.tennisassist;

class BeforeFirstGem implements MatchState {

    private TennisMatch tennisMatch;

    BeforeFirstGem(TennisMatch tennisMatch) {
        this.tennisMatch = tennisMatch;
    }

    @Override
    public void registerFirstServingPlayer(String player) {
        if (tennisMatch.currentlyServingPlayer != null) {
            throw new IllegalStateException("The server has already been chosen");
        }
        tennisMatch.currentlyServingPlayer = player;
    }

    @Override
    public void startPlay() {
        if (tennisMatch.currentlyServingPlayer == null) {
            throw new IllegalStateException("The server has not yet been chosen");
        }
        tennisMatch.ensureSetStarted();
        tennisMatch.startGem();
    }

    @Override
    public void registerPoint(String winningPlayer) {
        throw new IllegalStateException("No play is currently in progress");
    }

}
