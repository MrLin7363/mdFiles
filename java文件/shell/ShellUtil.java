
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class ShellUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellUtil.class);
    public static final long MAX_WAIT_SHELL_RUN_TIME = 1440L;
    /**
     * 错误日志的长度限制：20
     */
    private static final int ERROR_LINE_LIMIT = 20;

    /**
     * 调用shell执行命令，返回输出结果
     *
     * @param command 命令
     * @return 结果
     * @throws Exception 服务异常
     */
    public static List<String> runShell(String command) throws Exception {
        StreamGobbler outputGobbler;
        try {
            Process process = Runtime.getRuntime().exec(getShellCommand() + command);
            StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream());
            outputGobbler = new StreamGobbler(process.getInputStream());
            errorGobbler.start();
            outputGobbler.start();

            if (!process.waitFor(MAX_WAIT_SHELL_RUN_TIME, TimeUnit.MINUTES)) {
                LOGGER.error("exec {} timeout(24 hours)", command);
                process.destroy();
                // 暂时保持和之前同样的出错处理
                throw new Exception("script error, timeout");
            }

            errorGobbler.join();
            outputGobbler.join();

            if (!CollectionUtils.isEmpty(errorGobbler.getContent())) {
                List<String> errStrings = errorGobbler.getContent();
                // 只打印最后20条错误日志
                int size = errStrings.size();
                int startIdx = (size <= ERROR_LINE_LIMIT) ? 0 : size - ERROR_LINE_LIMIT;
                LOGGER.warn("[shell] {}", String.join(" ", errStrings.subList(startIdx, size)));
            }

            int exitValue = process.exitValue();
            if (exitValue != 0) {
                throw new Exception("script error, exit code:" + exitValue);
            }

        } catch (InterruptedException | IOException e) {
            throw new Exception("An error occurs when the shell script is executed: " + e, e);
        }

        return outputGobbler.getContent();
    }

    private static String getShellCommand() {
        String osName = System.getProperty("os.name");
        return osName.indexOf("Windows") != -1 ? "cmd /c " : "/bin/bash ";
    }

    public static void main(String[] args) {
        try {
            List<String> strings = runShell("dir");
            System.out.println(strings);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}