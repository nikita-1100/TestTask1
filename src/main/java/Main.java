import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String[] firstStrings = new String[0];
        String[] secondStrings = new String[0];
        //Чтение из файла
        try {
            File file = new File("src/main/java/input.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            firstStrings = new String[Integer.parseInt(line)];
            for (int i = 0; i < firstStrings.length; i++) {
                firstStrings[i] = reader.readLine();
            }
            line = reader.readLine();
            secondStrings = new String[Integer.parseInt(line)];
            for (int i = 0; i < secondStrings.length; i++) {
                secondStrings[i] = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Построю матрицу с коэфициентами соответствия для каждых двух строк
        double[][] koefs = new double[firstStrings.length][secondStrings.length];
        for (int i = 0; i < firstStrings.length; i++) {
            for (int j = 0; j < secondStrings.length; j++) {
                koefs[i][j] = similarity(firstStrings[i], secondStrings[j]);
                System.out.print(firstStrings[i] + "^" + secondStrings[j] + ": " + similarity(firstStrings[i], secondStrings[j]) + " | ");
            }
            System.out.println();
        }

        Integer[] resultIndexes = new Integer[firstStrings.length];
        int[] stopIndexesSecond = new int[secondStrings.length];

        //Найду самые лучшие сочетания
        for (int k = 0; k < firstStrings.length; k++) {
            double nowMaximum = 0;
            Integer indexMaximumJ = null;
            Integer indexMaximumI = null;
            for (int i = 0; i < firstStrings.length; i++) {
                for (int j = 0; j < secondStrings.length; j++) {
                    if (koefs[i][j] > nowMaximum && stopIndexesSecond[j] != 1) {
                        nowMaximum = koefs[i][j];
                        indexMaximumJ = j;
                        indexMaximumI = i;
                    }
                }
            }
            if (indexMaximumJ != null)
                stopIndexesSecond[indexMaximumJ] = 1;
            if (indexMaximumI != null)
                resultIndexes[indexMaximumI] = indexMaximumJ;
        }
        //Сохранение в файл
        File fileOutput = new File("src/main/java/output.txt");
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter(fileOutput);
            for (int i = 0; i < firstStrings.length; i++) {
                fileWriter.write(firstStrings[i] + ":" + (resultIndexes[i] == null ? "?" : secondStrings[resultIndexes[i]])
                        + System.getProperty("line.separator"));
            }
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    //Подор коэфициентов с помощью алгоритма косинусного расстояния
    //Выбран этот алгоритм т.к. он менее других зависим от порядка слов
    //Реализация алгоритма взята из библиотеки java-string-similarity
    //https://github.com/tdebatty/java-string-similarity
    //Сюда код перенесен чтобы избежать импортов.

    public static double similarity(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 1.0;
        } else if (s1.length() >= 3 && s2.length() >= 3) {
            Map<String, Integer> profile1 = getProfile(s1);
            Map<String, Integer> profile2 = getProfile(s2);
            return dotProduct(profile1, profile2) / (norm(profile1) * norm(profile2));
        } else {
            return 0.0;
        }
    }

    public static Map<String, Integer> getProfile(String string) {
        HashMap<String, Integer> shingles = new HashMap();
        String string_no_space = Pattern.compile("\\s+").matcher(string).replaceAll(" ");

        for (int i = 0; i < string_no_space.length() - 3 + 1; ++i) {
            String shingle = string_no_space.substring(i, i + 3);
            Integer old = (Integer) shingles.get(shingle);
            if (old != null) {
                shingles.put(shingle, old + 1);
            } else {
                shingles.put(shingle, 1);
            }
        }

        return Collections.unmodifiableMap(shingles);
    }

    private static double norm(Map<String, Integer> profile) {
        double agg = 0.0;

        Map.Entry entry;
        for (Iterator var3 = profile.entrySet().iterator(); var3.hasNext(); agg += 1.0 * (double) (Integer) entry.getValue() * (double) (Integer) entry.getValue()) {
            entry = (Map.Entry) var3.next();
        }
        return Math.sqrt(agg);
    }

    private static double dotProduct(Map<String, Integer> profile1, Map<String, Integer> profile2) {
        Map<String, Integer> small_profile = profile2;
        Map<String, Integer> large_profile = profile1;
        if (profile1.size() < profile2.size()) {
            small_profile = profile1;
            large_profile = profile2;
        }

        double agg = 0.0;
        Iterator var6 = small_profile.entrySet().iterator();

        while (var6.hasNext()) {
            Map.Entry<String, Integer> entry = (Map.Entry) var6.next();
            Integer i = (Integer) large_profile.get(entry.getKey());
            if (i != null) {
                agg += 1.0 * (double) (Integer) entry.getValue() * (double) i;
            }
        }

        return agg;
    }
}
