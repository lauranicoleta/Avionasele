import java.io.*;
import java.util.*;

public class ConfigLoader {
    private static final String CONFIG_DIR = "configs";

    public static List<String[]> loadConfigs() {
        List<String[]> configs = new ArrayList<>();
        File dir = new File(CONFIG_DIR);
        String absolutePath = dir.getAbsolutePath();
        System.out.println("Absolute path to CONFIG_DIR: " + absolutePath);

        File[] files = dir.listFiles();
        if (files == null) {
            System.err.println("Unable to list files in directory: " + absolutePath);
            return configs;
        }

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String[] config = new String[10];
                for (int i = 0; i < 10; i++) {
                    config[i] = reader.readLine();
                }
                configs.add(config);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return configs;
    }
}