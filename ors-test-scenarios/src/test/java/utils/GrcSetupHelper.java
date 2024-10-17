package utils;

import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GrcSetupHelper {


    public static String getCurrentDateInFormat(Integer increaseDaysBy) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
        return ZonedDateTime.now().plusDays(increaseDaysBy).format(formatter);
    }

    public static boolean setupGraphRepo(GenericContainer<?> container, String importDate) {
        String carGraphPath = "/home/ors/openrouteservice/graphs/driving-car";
        String repoPath = "/tmp/test-filesystem-repo/vendor-xyz/fastisochrones/heidelberg/1";
        String scratchGraphPath = "/tmp/scratch";
        String scratchGraphPathDrivingCar = scratchGraphPath + "/driving-car";

        return executeCommands(container, List.of(new String[]{"mkdir", "-p", repoPath, scratchGraphPath}, new String[]{"cp", "-r", carGraphPath, scratchGraphPath}, new String[]{"sh", "-c", "yq -i e '.import_date = \"" + importDate + "\"' " + scratchGraphPathDrivingCar + "/graph_info.yml"}, new String[]{"zip", "-j", "-r", repoPath + "/fastisochrones_heidelberg_1_driving-car.ghz", scratchGraphPathDrivingCar}, new String[]{"cp", scratchGraphPathDrivingCar + "/graph_info.yml", repoPath + "/fastisochrones_heidelberg_1_driving-car.yml"}));
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
