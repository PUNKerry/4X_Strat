package game.model.world;

public class Hex {
    public final int col;
    public final int row;

    public Hex(int col, int row) {
        this.col = col;
        this.row = row;
    }



    public int distanceTo(Hex other) {
        int q1 = col - (row - (row & 1)) / 2;
        int r1 = row;
        int q2 = other.col - (other.row - (other.row & 1)) / 2;
        int r2 = other.row;
        int s1 = -q1 - r1;
        int s2 = -q2 - r2;
        return (Math.abs(q1 - q2) + Math.abs(r1 - r2) + Math.abs(s1 - s2)) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return col == hex.col && row == hex.row;
    }

    @Override
    public int hashCode() {
        return 31 * col + row;
    }

    public Hex[] neighbors() {
        Hex[] result = new Hex[6];
        if (row % 2 == 0) {
            // Чётная строка
            result[0] = new Hex(col + 1, row);     // восток
            result[1] = new Hex(col, row - 1);     // северо-восток
            result[2] = new Hex(col - 1, row - 1); // северо-запад
            result[3] = new Hex(col - 1, row);     // запад
            result[4] = new Hex(col - 1, row + 1); // юго-запад
            result[5] = new Hex(col, row + 1);     // юго-восток
        } else {
            // Нечётная строка
            result[0] = new Hex(col + 1, row);     // восток
            result[1] = new Hex(col + 1, row - 1); // северо-восток
            result[2] = new Hex(col, row - 1);     // северо-запад
            result[3] = new Hex(col - 1, row);     // запад
            result[4] = new Hex(col, row + 1);     // юго-запад
            result[5] = new Hex(col + 1, row + 1); // юго-восток
        }
        return result;
    }
}