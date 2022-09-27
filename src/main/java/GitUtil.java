import org.apache.commons.lang3.StringUtils;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GitUtil {

    public static void main(String[] args) {
        Map<String, Map<String, String>> masterBranchMap =
                gitLogFromBranch("git log master");
        Map<String, Map<String, String>> master_v2BranchMap =
                gitLogFromBranch("git log master_v2");


        List list = master_v2BranchMap
                .values()
                .stream()
                .filter(e -> !masterBranchMap.values().contains(e))
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            System.out.println("分支无差异");
        }else {
            System.out.println("文件已down,请在桌面查看！！！");
            writeFile(list, "aa");
        }
    }

    private static Map<String, Map<String, String>> gitLogFromBranch(String command) {
        try {
            if (StringUtils.isBlank(command)) {
                return null;
            }
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String line;
            List<String> bigList = Lists.newArrayList();
            while ((line = input.readLine()) != null) {
                if (StringUtils.isNotBlank(line)) {
                    bigList.add(line);
                }
            }

            if (CollectionUtils.isEmpty(bigList)) {
                return null;
            }

            Map<String, Map<String, String>> bigGitMap = Maps.newHashMap();
            if (!CollectionUtils.isEmpty(bigList)) {
                Lists.partition(bigList, 4).forEach(smallList -> {
                    if (!CollectionUtils.isEmpty(smallList)) {
                        Map<String, String> map = handleMap(smallList);
                        if (map != null) {
                            bigGitMap.put(map.get("commit_id"), map);
                        }
                    }
                });
            }
            return bigGitMap;
        }catch (IOException e) {
            return null;
        }
    }

    private static Map<String, String> handleMap(List<String> list) {
        if(list == null) {
            return null;
        }

        String commit_id = "";
        String author_email = "";
        String date = "";
        for (String str : list) {
            if (str.contains("commit")) {
                commit_id = str.substring(7).trim();
            } else if (str.contains("Author:")) {
                author_email = str.substring(8).trim();
            } else if (str.contains("Date:")) {
                date = str.substring(5).trim();
            }
        }

        if (StringUtils.isNotBlank(commit_id)
                && StringUtils.isNotBlank(author_email)
                && StringUtils.isNotBlank(date)) {
            return ImmutableMap.of("commit_id", commit_id,
                    "author_email", author_email,
                    "date", date);
        }
        return null;
    }

    /**
     * 写文件流
     * @param list
     * @param dirName
     */
    private static void writeFile(List list, String dirName) {
        try {
            if (list == null || StringUtils.isBlank(dirName)) {
                return;
            }


            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            File com = fileSystemView.getHomeDirectory();
            File file = new File(com.getPath() + "\\" + dirName +".txt");

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (int i = 0;i < list.size();i++) {
                bufferedWriter.write(list.get(i).toString());
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        }catch (Exception e) {

        }
        return;
    }
}
