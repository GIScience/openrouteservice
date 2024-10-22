package utils;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GrcSetupHelper {


    public static String getCurrentDateInFormat(int increaseDaysBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return ZonedDateTime.now().plusDays(increaseDaysBy).format(formatter);
    }

    public static boolean setupGraphRepo(GenericContainer<?> container, String importDate) {
        return setupGraphRepo(container, importDate, "driving-car");
    }

    public static boolean setupGraphRepo(GenericContainer<?> container, String importDate, String profile) {
        String graphPath = "/home/ors/openrouteservice/graphs/" + profile;
        String repoPath = "/tmp/test-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1";
        String scratchGraphPath = "/tmp/scratch";
        String scratchGraphPathProfile = scratchGraphPath + "/" + profile;

        return executeCommands(container, List.of(new String[]{"mkdir", "-p", repoPath, scratchGraphPath}, new String[]{"cp", "-r", graphPath, scratchGraphPath}, new String[]{"sh", "-c", "yq -i e '.import_date = \"" + importDate + "\"' " + scratchGraphPathProfile + "/graph_info.yml"}, new String[]{"zip", "-j", "-r", repoPath + "/fastisochrones_heidelberg_1_driving-car.ghz", scratchGraphPathProfile}, new String[]{"cp", scratchGraphPathProfile + "/graph_info.yml", repoPath + "/fastisochrones_heidelberg_1_driving-car.yml"}));
    }

    private static boolean executeCommands(GenericContainer<?> container, List<String[]> commands) {
        for (String[] command : commands) {
            Container.ExecResult result;
            try {
                result = container.execInContainer(command);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return false;
            }
            if (result.getExitCode() != 0) {
                System.out.println(result.getExitCode());
                System.out.println(result.getStdout());
                System.out.println(result.getStderr());
                return false;
            }
        }
        return true;
    }

}
