package net.doverholm.util;
import java.time.Duration;
import java.time.LocalDateTime;

public class CountdownManager {
    private static boolean passed = false;

    public static String getFormattedTimeLeft() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime target = now
                .withYear(2026)
                .withMonth(4)
                .withDayOfMonth(24)
                .withHour(18)
                .withMinute(0)
                .withSecond(0);
        /*LocalDateTime target = now
                .withYear(2026)
                .withMonth(4)
                .withDayOfMonth(19)
                .withHour(21)
                .withMinute(0)
                .withSecond(0);*/

        Duration duration = Duration.between(now, target);
        if (duration.isNegative()) {
            passed = true;
            return "0d, 0h, 0m, 0s";
        }

        long seconds = duration.getSeconds();

        long days = seconds / (24 * 3600);
        seconds %= (24 * 3600);

        long hours = seconds / 3600;
        seconds %= 3600;

        long minutes = seconds / 60;
        seconds %= 60;

        return days + "d, " + hours + "h, " + minutes + "m, " + seconds + "s";
    }
}
