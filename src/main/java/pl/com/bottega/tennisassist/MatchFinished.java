package pl.com.bottega.tennisassist;

import java.util.Optional;

class MatchFinished implements MatchState {

    private final Player winner;

    MatchFinished(Player winner) {
        this.winner = winner;
    }

    @Override
    public void registerFirstServingPlayer(Player player) {
        throwException();
    }

    @Override
    public void startPlay() {
        throwException();
    }

    @Override
    public void registerPoint(Player winningPlayer) {
        throwException();
    }

    @Override
    public Optional<Player> getMatchWinner() {
        return Optional.of(winner);
    }

    private void throwException() {
        throw new IllegalStateException("A match is finished");
    }
}
