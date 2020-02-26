package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pl.com.bottega.tennisassist.MatchFormat.BEST_OF_FIVE;
import static pl.com.bottega.tennisassist.MatchFormat.BEST_OF_THREE;
import static pl.com.bottega.tennisassist.TieResolutionType.ADVANTAGE;
import static pl.com.bottega.tennisassist.TieResolutionType.SUPER_TIEBREAK;

public class TennisMatch {

    private final String player1;
    private final String player2;
    private final MatchFormat matchFormat;
    private final GemScoringType gemScoringType;
    private final TieResolutionType finalSetTieResolutionType;
    private final Score<Integer> matchScore;
    private MatchState currentState = new BeforeFirstGem();
    private String currentlyServingPlayer;
    private Score<GemPoint> currentGemScore;
    private Score<Integer> currentSetScore;
    private Score<Integer> currentTiebreakScore;
    private Score<Integer> currentSuperTiebreakScore;
    private String matchWinner;
    private String firstServingPlayerInTiebreak;
    private List<Score> scoresOfSets = new LinkedList<>();

    public TennisMatch(String player1, String player2, MatchFormat matchFormat, GemScoringType gemScoringType, TieResolutionType finalSetTieResolutionType) {
        this.player1 = player1;
        this.player2 = player2;
        this.matchFormat = matchFormat;
        this.gemScoringType = gemScoringType;
        this.finalSetTieResolutionType = finalSetTieResolutionType;
        this.matchScore = new Score(player1, player2, 0);
    }

    public Score getMatchScore() {
        return matchScore;
    }

    public Optional<String> getCurrentServingPlayer() {
        return Optional.ofNullable(currentlyServingPlayer);
    }

    public void registerFirstServingPlayer(String player) {
        this.currentState.registerFirstServingPlayer(player);
    }

    public void startPlay() {
        currentState.startPlay();
        if (currentSetScore == null) {
            currentSetScore = new Score(player1, player2, 0);
        }
    }

    public Optional<Score<GemPoint>> getCurrentGemScore() {
        return Optional.ofNullable(currentGemScore);
    }

    public void registerPoint(String winningPlayer) {
        currentState.registerPoint(winningPlayer);
    }

    public Optional<Score> getCurrentSetScore() {
        return Optional.ofNullable(currentSetScore);
    }

    public Optional<String> getMatchWinner() {
        return Optional.ofNullable(matchWinner);
    }

    public List<Score> getScoresOfSets() {
        return Collections.unmodifiableList(scoresOfSets);
    }

    private abstract class MatchState {
        abstract void registerFirstServingPlayer(String player);

        abstract void startPlay();

        abstract void registerPoint(String winningPlayer);
    }

    private class BeforeFirstGem extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            if (currentlyServingPlayer != null) {
                throw new IllegalStateException("A server has already been chosen");
            }
            currentlyServingPlayer = player;
        }

        @Override
        void startPlay() {
            if (currentlyServingPlayer == null) {
                throw new IllegalStateException("A server has not yet been chosen");
            }
            startGem();
        }

        @Override
        void registerPoint(String winningPlayer) {
            throw new IllegalStateException("No play is currently in progress");
        }
    }

    private class GemInProgress extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A gem is currently in progress");
        }

        @Override
        void startPlay() {
            throw new IllegalStateException("A gem is currently in progress");
        }

        @Override
        void registerPoint(String winningPlayer) {
            String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
            GemPoint winningPlayerScore = currentGemScore.getScore(winningPlayer);
            GemPoint loosingPlayerScore = currentGemScore.getScore(loosingPlayer);
            if (gemScoringType == GemScoringType.WITH_ADVANTAGE && loosingPlayerScore == GemPoint.ADVANTAGE) {
                currentGemScore.setScore(loosingPlayer, GemPoint.FOURTY);
            } else if (
                (gemScoringType == GemScoringType.WITH_ADVANTAGE && (winningPlayerScore == GemPoint.ADVANTAGE || (winningPlayerScore == GemPoint.FOURTY && loosingPlayerScore.compareTo(GemPoint.FOURTY) < 0)))
                    || (gemScoringType == GemScoringType.WITH_GOLDEN_BALL && winningPlayerScore == GemPoint.FOURTY)
            ) {
                currentSetScore.setScore(winningPlayer, currentSetScore.getScore(winningPlayer) + 1);
                if (currentSetScore.getScore(winningPlayer) >= 6 && currentSetScore.getScore(winningPlayer) - currentSetScore.getScore(loosingPlayer) >= 2) {
                    matchScore.setScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
                    scoresOfSets.add(currentSetScore);
                    currentSetScore = null;
                    if ((matchFormat == BEST_OF_THREE && matchScore.getScore(winningPlayer) == 2) || (matchFormat == BEST_OF_FIVE && matchScore.getScore(winningPlayer) == 3)) {
                        finishMatch(winningPlayer);
                    }
                }
                startBreak();
            } else {
                currentGemScore.setScore(winningPlayer, GemPoint.values()[winningPlayerScore.ordinal() + 1]);
            }
        }
    }

    private void finishMatch(String winner) {
        matchWinner = winner;
        currentState = new MatchFinished();
    }

    private void startBreak() {
        currentGemScore = null;
        currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
        currentState = new BreakInPlay();
    }

    private void startSuperTiebreak() {
        currentSuperTiebreakScore = new Score(player1, player2, 0);
        currentState = new SupertiebreakInPlay();
    }

    private void startTiebreak() {
        currentTiebreakScore = new Score(player1, player2, 0);
        firstServingPlayerInTiebreak = currentlyServingPlayer;
    }

    private void startGem() {
        currentGemScore = new Score(player1, player2, GemPoint.ZERO);
        currentState = new GemInProgress();
    }

    private class BreakInPlay extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A server has already been chosen");
        }

        @Override
        void startPlay() {
            if (
                (matchFormat == MatchFormat.BEST_OF_TWO_WITH_SUPER_TIEBREAK && matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1) ||
                    (finalSetTieResolutionType == SUPER_TIEBREAK && currentSetScore.getScore(player1) == 6 && currentSetScore.getScore(player2) == 6 &&
                        (
                            (matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1 && matchFormat == BEST_OF_THREE) ||
                                (matchScore.getScore(player1) == 2 && matchScore.getScore(player2) == 2 && matchFormat == BEST_OF_FIVE)
                        )
                    )
            ) {
                startSuperTiebreak();
            } else if (currentSetScore.getScore(player1) == 6 && currentSetScore.getScore(player2) == 6 &&
                !(finalSetTieResolutionType == ADVANTAGE && (
                    (matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1 && matchFormat == BEST_OF_THREE) ||
                        (matchScore.getScore(player1) == 2 && matchScore.getScore(player2) == 2 && matchFormat == BEST_OF_FIVE)
                ))
            ) {
                startTiebreak();
            } else {
                startGem();
            }
        }

        @Override
        void registerPoint(String winningPlayer) {
            throw new IllegalStateException("No play is currently in progress");
        }
    }

    private class TiebreakInPlay extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A server has already been chosen");
        }

        @Override
        void startPlay() {
            throw new IllegalStateException("A tiebreak is currently in progress");
        }

        @Override
        void registerPoint(String winningPlayer) {
            String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
            int winningPlayerScore = currentTiebreakScore.getScore(winningPlayer) + 1;
            currentTiebreakScore.setScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = currentTiebreakScore.getScore(loosingPlayer);
            if (winningPlayerScore >= 7 && winningPlayerScore - loosingPlayerScore >= 2) {
                currentSetScore.setScore(winningPlayer, currentSetScore.getScore(winningPlayer) + 1);
                if ((currentSetScore.getScore(winningPlayer) == 6 && currentSetScore.getScore(loosingPlayer) < 5) || currentSetScore.getScore(winningPlayer) == 7) {
                    matchScore.setScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
                    scoresOfSets.add(currentSetScore);
                    currentSetScore = null;
                    if ((matchFormat == BEST_OF_THREE && matchScore.getScore(winningPlayer) == 2) || (matchFormat == BEST_OF_FIVE && matchScore.getScore(winningPlayer) == 3)) {
                        finishMatch(winningPlayer);
                    } else {
                        startBreak();
                    }
                }
                currentTiebreakScore = null;
                currentlyServingPlayer = firstServingPlayerInTiebreak.equals(player1) ? player2 : player1;
            } else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
            }
        }
    }

    private class SupertiebreakInPlay extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A server has already been chosen");
        }

        @Override
        void startPlay() {
            throw new IllegalStateException("A supertiebreak is currently in progress");
        }

        @Override
        void registerPoint(String winningPlayer) {
            String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
            int winningPlayerScore = currentSuperTiebreakScore.getScore(winningPlayer) + 1;
            currentSuperTiebreakScore.setScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = currentSuperTiebreakScore.getScore(loosingPlayer);
            if (winningPlayerScore >= 10 && winningPlayerScore - loosingPlayerScore >= 2) {
                matchScore.setScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
                matchWinner = winningPlayer;
                currentSuperTiebreakScore = null;
                if(finalSetTieResolutionType == SUPER_TIEBREAK) {
                    currentSetScore.setScore(winningPlayer, 7);
                    scoresOfSets.add(currentSetScore);
                }
                currentSetScore = null;
                finishMatch(winningPlayer);
            }  else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
            }
        }
    }

    private class MatchFinished extends MatchState {

        @Override
        void registerFirstServingPlayer(String player) {
            throwException();
        }

        @Override
        void startPlay() {
            throwException();
        }

        @Override
        void registerPoint(String winningPlayer) {
            throwException();
        }

        private void throwException() {
            throw new IllegalStateException("A match is finished");
        }
    }

}
