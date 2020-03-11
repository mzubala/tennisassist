package pl.com.bottega.tennisassist;

import java.util.Optional;

class GemInProgress implements MatchState {

    private final TennisMatch tennisMatch;
    private final GemScoreCounter gemScoreCounter;

    GemInProgress(TennisMatch tennisMatch) {
        this.tennisMatch = tennisMatch;
        gemScoreCounter = GemScoreCounter.of(tennisMatch.players, tennisMatch.matchSettings.getGemScoringType());
    }

    @Override
    public void registerFirstServingPlayer(Player player) {
        throw new IllegalStateException("A gem is currently in progress");
    }

    @Override
    public void startPlay() {
        throw new IllegalStateException("A gem is currently in progress");
    }

    @Override
    public void registerPoint(Player winningPlayer) {
        gemScoreCounter.increment(winningPlayer);
        if (gemScoreCounter.hasWon(winningPlayer)) {
            tennisMatch.finishGem(winningPlayer);
        }
    }

    @Override
    public Optional<Score<GemPoint>> getCurrentGemScore() {
        return Optional.of(gemScoreCounter.getScore());
    }
}
