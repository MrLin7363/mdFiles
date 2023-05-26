
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class StreamGobbler extends Thread {
    private InputStream is;

    private List<String> buf = Lists.newArrayList();

    StreamGobbler(InputStream is) {
        super("StreamGobbler");
        this.is = is;
    }

    /**
     * 运行方法
     */
    public void run() {
        try(InputStreamReader in = new InputStreamReader(is, StandardCharsets.UTF_8);
            LineNumberReader reader = new LineNumberReader(in)) {
            String line;
            while ((line = reader.readLine()) != null) {
                buf.add(line);
            }
        } catch (IOException x) {
            log.error("read input stream failed.", x);
        }
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    public List<String> getContent() {
        return this.buf;
    }
}