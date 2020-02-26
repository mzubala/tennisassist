package pl.com.bottega.tennisassist;

import java.util.Optional;

class MatchFinished implements MatchState {

    private final String winner;

    MatchFinished(String winner) {
        this.winner = winner;
    }

    @Override
    public void registerFirstServingPlayer(String player) {
        throwException();
    }

    @Override
    public void startPlay() {
        throwException();
    }

    @Override
    public void registerPoint(String winningPlayer) {
        throwException();
    }

    @Override
    public Optional<String> getMatchWinner() {
        return Optional.of(winner);
    }

    private void throwException() {
        throw new IllegalStateException("A match is finished");
    }
}
