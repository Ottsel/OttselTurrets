package net.nickhunter.mc.ottselturrets.util;

public enum TiltDirection {
        NORTH(2), EAST(3), SOUTH(0), WEST(1), NORTHEAST(6), SOUTHEAST(7), SOUTHWEST(4), NORTHWEST(5);

        private int opposite;

        private TiltDirection(int opposite) {
            this.opposite = opposite;
        }

        public TiltDirection getOpposite() {
            switch (opposite) {
                case 0:
                    return NORTH;
                case 1:
                    return EAST;
                case 2:
                default:
                    return SOUTH;
                case 3:
                    return WEST;
                case 4:
                    return NORTHEAST;
                case 5:
                    return SOUTHEAST;
                case 6:
                    return SOUTHWEST;
                case 7:
                    return NORTHWEST;
            }
        }
    }