IpUtils

### 项目实际

IpUtils.getIpAddress(request)

```
public final class IpUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(IpUtils.class);

    /**
     * 本地回环地址
     */
    public static final String LOOPBACK_ADDRESS = "127.0.0.1";

    /**
     * IPV 6_回环地址
     */
    public static final String IPV6_ADDRESS = "0:0:0:0:0:0:0:1";

    /**
     * 未知ip
     */
    public static final String UNKNOWN_IP = "unknown";

    /**
     * 私有构造函数
     */
    private IpUtils() {
    }

    /**
     * 判断ip是否为空或等于unknown
     *
     * @param ip ip
     * @return boolean false/true
     */
    private static boolean ipComparison(String ip) {
        if (ip == null || ip.length() == 0 || UNKNOWN_IP.equalsIgnoreCase(ip)) {
            return true;
        }
        return false;
    }

    /**
     * 通过HttpServletRequest返回客户端真实IP地址（通过多级代理后也能获取到真实ip） ip对比
     *
     * @param request request
     * @return boolean false/true
     */
    public static String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }
        try {
            // 获取nginx代理的客户端ip
            String ip = getIpFromRequest(request);

            // 通过request.getRemoteAddr();获取
            if (ipComparison(ip)) {
                ip = request.getRemoteAddr();

                // 如果使用localhost访问，对于windows IPv6会返回0:0:0:0:0:0:0:1，将其转为127.0.0.1
                if (IPV6_ADDRESS.equals(ip)) {
                    ip = LOOPBACK_ADDRESS;
                }
                if (LOOPBACK_ADDRESS.equals(ip)) {
                    // 获取真实IP,根据网卡取本机配置的IP
                    ip = getRealIp();
                }
            }
            return ip;
        } catch (Exception e) {
            LOGGER.error("getIpAddress error! {}", getCleanedMessage(e.getMessage()));
        }
        return UNKNOWN_IP;
    }

    /**
     * 通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，X-Forwarded-For中第一个非unknown的，就是有效的IP字符串。
     * <p>
     * X-Forwarded-For: client, proxy1, proxy2
     * <p>
     * 如：X-Forwarded-For：192.168.1.xxx, 192.168.1.xxx 192.168.1.xxx
     * <p>
     * 用户真实IP为： 192.168.1.xxx
     *
     * @param request request
     * @return java.lang.String string
     */
    private static String getIpFromRequest(HttpServletRequest request) {
        // 先获取Nginx是否设置X-Real-IP，如果设置直接返回IP值
        String ipAddress = request.getHeader("X-Real-IP");

        // 判断是否为IP格式 Plugin
        if (StringUtils.hasText(ipAddress)) {
            return ipAddress;
        }
        ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress != null && !ipAddress.contains(UNKNOWN_IP) && ipAddress.contains(",")) {
            String[] ips = ipAddress.split(",");
            /*
             * 对于获取到多ip的情况下，找到公网ip.
             */
            String publicIp = null;
            for (String ip : ips) {
                if (!isInnerIp(ip.trim())) {
                    publicIp = ip.trim();
                    break;
                }
            }
            /*
             * 如果多个ip都是内网ip，则取第一个ip.
             */
            if (publicIp == null) {
                publicIp = ips[0].trim();
            }
            ipAddress = publicIp;
        }
        if (ipAddress != null && ipAddress.contains(UNKNOWN_IP)) {
            ipAddress = ipAddress.replaceAll("unknown,", "");
            ipAddress = ipAddress.trim();
        }
        return ipAddress == null ? "" : ipAddress;
    }

    /**
     * InetAddress.getLocalHost();方法会加锁获取ip。一定程度上会影响性能。
     * 因此使用 NetworkInterface.getNetworkInterfaces();获取ip
     *
     * @return java.lang.String string
     * @throws SocketException SocketException
     */
    @Nullable
    public static String getRealIp() throws SocketException {
        Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
        if (allNetInterfaces == null) {
            return null;
        }
        InetAddress ip;
        while (allNetInterfaces.hasMoreElements()) {
            NetworkInterface netInterface = allNetInterfaces.nextElement();
            if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                continue;
            }
            Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                ip = addresses.nextElement();
                if (ip instanceof Inet4Address) {
                    return ip.getHostAddress();
                }
            }
        }
        return null;
    }

    /**
     * 判断ip值，是否符合IP4规则
     * （二进制类似表示为: 11000000 00000000 00000000 00000001 - 11011111 11111111 11111111 11111110）
     * @param ip ip
     * @return long
     */
    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + ((long) Integer.parseInt(octets[1]) << 16)
                + ((long) Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }

    /**
     * 判断ip是否为内网ip地址
     * 内网地址：
     * A类地址：10.0.0.0--10.255.255.255
     * B类地址：172.16.0.0--172.31.255.255
     * C类地址：192.168.0.0--192.168.255.255
     *
     * @param ip ip
     * @return boolean
     */
    public static boolean isInnerIp(String ip) {
        long longIp = ipV4ToLong(ip);
        if (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255")) {
            return true;
        }
        if (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255")) {
            return true;
        }
        return longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
    }

    /**
     * 替换字符串中空格
     *
     * @param message message
     * @return java.lang.String str
     */
    public static String getCleanedMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.replace('\n', '_').replace('\r', '_');
    }
}
```

### 网站参考-获取本机IP

原文链接：https://blog.csdn.net/sunweiking/article/details/128239232

工具类代码如下：

public class IpUtils {
    public static String getRealIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if(ip!=null&&!"".equals(ip)){
            String[] ips=ip.split(",");
            ip=ips[0];
            System.out.println("X-Forwarded-For获取到的ip:"+ip);
        }
        if (ip == null) {
            ip = request.getHeader("X-Real-IP");
            System.out.println("X-Real-IP获取到的ip:"+ip);
        }
        if (ip == null) {
            ip = request.getRemoteAddr();
            System.out.println("getRemoteAddr获取到的ip:"+ip);
        }
        return ip;
    }
}
说明：

1.当客户端与服务端之间没有任何代理或防火墙时，可直接由request.getRemoteAddr()获取用户的ip。本质上这个方法是获取客户端与服务端之间建立的tcp连接的客户端ip地址

2.当客户端与服务端之间经过代理时就不能用步骤1中的方法了，因为此时与服务端建立tcp连接的就不是客户端而是代理服务器了。获取的是代理服务器的ip地址，解决方案如下，以nginx代理为例，可在nginx配置中加一个配置：

proxy_set_header X-Real-IP $remote_addr;

remote_addr也就是和nginx建立tcp连接的客户端ip赋值到http请求的X-Real-IP请求头，这种方式获取只适用于有一层代理的情况，因为多层代理在最后获取时只能获取倒数第二层代理的ip。

3.当客户端与服务端之间有多层代理时。在多个nginx代理都加以下配置：

proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for；

是指把remote_addr也就是和nginx建立tcp连接的客户端ip追加到http请求的X-Forwarded-For请求头中，如果有多层代理，那这个header的值就是多个ip以逗号分割，这时候获取客户端ip就要取第一个ip。
