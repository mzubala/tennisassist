package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pl.com.bottega.tennisassist.SetFormat.BEST_OF_FIVE;
import static pl.com.bottega.tennisassist.SetFormat.BEST_OF_THREE;
import static pl.com.bottega.tennisassist.SetKind.ADVANTAGE;
import static pl.com.bottega.tennisassist.SetKind.SUPER_TIEBREAK;

public class TennisMatch {

    private final String player1;
    private final String player2;
    private final SetFormat setFormat;
    private final GemScoring gemsKind;
    private final SetKind lastSetKind;
    private final Score score;
    private String currentServer;
    private GemScore gemScore;
    private Score setScore;
    private Score tiebreakScore;
    private Score superTiebreakScore;
    private String winner;
    private String firstServerInTiebreak;
    private List<Score> setScores = new LinkedList<>();

    public TennisMatch(String player1, String player2, SetFormat setFormat, GemScoring gemsKind, SetKind lastSetKind) {
        this.player1 = player1;
        this.player2 = player2;
        this.setFormat = setFormat;
        this.gemsKind = gemsKind;
        this.lastSetKind = lastSetKind;
        this.score = new Score(player1, player2);
    }

    public Score score() {
        return score;
    }

    public Optional<String> currentServer() {
        return Optional.ofNullable(currentServer);
    }

    public void registerFirstServer(String player) {
        if (currentServer != null) {
            throw new IllegalStateException("A server has already been chosen");
        }
        currentServer = player;
    }

    public void startPlay() {
        if (currentServer == null) {
            throw new IllegalStateException("The server has not yet been chosen");
        }
        if (gemScore != null) {
            throw new IllegalStateException("A gem is currently in progress");
        }
        if (setScore == null) {
            setScore = new Score(player1, player2);
        }
        if (winner != null) {
            throw new IllegalStateException("A match is finished");
        }
        if (
            (setFormat == SetFormat.BEST_OF_TWO_WITH_SUPER_TIEBREAK && score.scoreOf(player1) == 1 && score.scoreOf(player2) == 1) ||
                (lastSetKind == SUPER_TIEBREAK && setScore.scoreOf(player1) == 6 && setScore.scoreOf(player2) == 6 &&
                    (
                        (score.scoreOf(player1) == 1 && score.scoreOf(player2) == 1 && setFormat == BEST_OF_THREE) ||
                            (score.scoreOf(player1) == 2 && score.scoreOf(player2) == 2 && setFormat == BEST_OF_FIVE)
                    )
                )
        ) {
            superTiebreakScore = new Score(player1, player2);
        } else if (setScore.scoreOf(player1) == 6 && setScore.scoreOf(player2) == 6 &&
            !(lastSetKind == ADVANTAGE && (
                (score.scoreOf(player1) == 1 && score.scoreOf(player2) == 1 && setFormat == BEST_OF_THREE) ||
                    (score.scoreOf(player1) == 2 && score.scoreOf(player2) == 2 && setFormat == BEST_OF_FIVE)
            ))
        ) {
            tiebreakScore = new Score(player1, player2);
            this.firstServerInTiebreak = currentServer;
        } else {
            gemScore = new GemScore(player1, player2);
        }
    }

    public Optional<GemScore> gemScore() {
        return Optional.ofNullable(gemScore);
    }

    public void registerPoint(String winningPlayer) {
        String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
        if (gemScore != null) {
            GemPoint winningPlayerScore = gemScore.scoreOf(winningPlayer);
            GemPoint loosingPlayerScore = gemScore.scoreOf(loosingPlayer);
            if (gemsKind == GemScoring.ADVANTAGE && loosingPlayerScore == GemPoint.ADVANTAGE) {
                gemScore.setScore(loosingPlayer, GemPoint.FOURTY);
            } else if (
                (gemsKind == GemScoring.ADVANTAGE && (winningPlayerScore == GemPoint.ADVANTAGE || (winningPlayerScore == GemPoint.FOURTY && loosingPlayerScore.compareTo(GemPoint.FOURTY) < 0)))
                    || (gemsKind == GemScoring.NO_ADVANTAGE && winningPlayerScore == GemPoint.FOURTY)
            ) {
                setScore.setScore(winningPlayer, setScore.scoreOf(winningPlayer) + 1);
                if (setScore.scoreOf(winningPlayer) >= 6 && setScore.scoreOf(winningPlayer) - setScore.scoreOf(loosingPlayer) >= 2) {
                    score.setScore(winningPlayer, score.scoreOf(winningPlayer) + 1);
                    setScores.add(setScore);
                    setScore = null;
                    if ((setFormat == BEST_OF_THREE && score.scoreOf(winningPlayer) == 2) || (setFormat == BEST_OF_FIVE && score.scoreOf(winningPlayer) == 3)) {
                        winner = winningPlayer;
                    }
                }
                gemScore = null;
                currentServer = currentServer.equals(player1) ? player2 : player1;
            } else {
                gemScore.setScore(winningPlayer, GemPoint.values()[winningPlayerScore.ordinal() + 1]);
            }
        } else if (tiebreakScore != null) {
            int winningPlayerScore = tiebreakScore.scoreOf(winningPlayer) + 1;
            tiebreakScore.setScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = tiebreakScore.scoreOf(loosingPlayer);
            if (winningPlayerScore >= 7 && winningPlayerScore - loosingPlayerScore >= 2) {
                setScore.setScore(winningPlayer, setScore.scoreOf(winningPlayer) + 1);
                if ((setScore.scoreOf(winningPlayer) == 6 && setScore.scoreOf(loosingPlayer) < 5) || setScore.scoreOf(winningPlayer) == 7) {
                    score.setScore(winningPlayer, score.scoreOf(winningPlayer) + 1);
                    setScores.add(setScore);
                    setScore = null;
                    if ((setFormat == BEST_OF_THREE && score.scoreOf(winningPlayer) == 2) || (setFormat == BEST_OF_FIVE && score.scoreOf(winningPlayer) == 3)) {
                        winner = winningPlayer;
                    }
                }
                tiebreakScore = null;
                currentServer = firstServerInTiebreak.equals(player1) ? player2 : player1;
            } else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentServer = currentServer.equals(player1) ? player2 : player1;
            }
        } else if (superTiebreakScore != null) {
            int winningPlayerScore = superTiebreakScore.scoreOf(winningPlayer) + 1;
            superTiebreakScore.setScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = superTiebreakScore.scoreOf(loosingPlayer);
            if (winningPlayerScore >= 10 && winningPlayerScore - loosingPlayerScore >= 2) {
                score.setScore(winningPlayer, score.scoreOf(winningPlayer) + 1);
                winner = winningPlayer;
                superTiebreakScore = null;
                if(lastSetKind == SUPER_TIEBREAK) {
                    setScore.setScore(winningPlayer, 7);
                    setScores.add(setScore);
                }
                setScore = null;
            }  else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentServer = currentServer.equals(player1) ? player2 : player1;
            }
        } else {
            throw new IllegalStateException("No play is currently in progress");
        }
    }

    public Optional<Score> setScore() {
        return Optional.ofNullable(setScore);
    }

    public Optional<String> winner() {
        return Optional.ofNullable(winner);
    }

    public List<Score> setScores() {
        return Collections.unmodifiableList(setScores);
    }
}
