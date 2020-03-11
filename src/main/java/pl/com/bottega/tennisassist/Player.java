package pl.com.bottega.tennisassist;

import java.util.Objects;

public interface Player {

    static SinglesPlayer singlesPlayer(String name) {
        return new SinglesPlayer(name);
    }

    static DoublesPlayer doublesPlayer(String player1, String player2) {
        return new DoublesPlayer(singlesPlayer(player1), singlesPlayer(player2));
    }

    class SinglesPlayer implements Player {
        private final String name;

        public SinglesPlayer(String name) {
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SinglesPlayer that = (SinglesPlayer) o;
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    class DoublesPlayer implements Player {
        private final SinglesPlayer player1;
        private final SinglesPlayer player2;

        public DoublesPlayer(SinglesPlayer player1, SinglesPlayer player2) {
            this.player1 = player1;
            this.player2 = player2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DoublesPlayer that = (DoublesPlayer) o;
            return player1.equals(that.player1) &&
                player2.equals(that.player2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(player1, player2);
        }

        @Override
        public String toString() {
            return String.format("%s, %s", player1, player2);
        }
    }
}
