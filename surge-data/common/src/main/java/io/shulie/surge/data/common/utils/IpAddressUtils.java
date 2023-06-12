/*
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.shulie.surge.data.common.utils;


import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * @author pamirs
 */
public class IpAddressUtils {
    private static final int INADDR4SZ = 4;
    private static final int INADDR16SZ = 16;
    private static final int INT16SZ = 2;

    private static final String LOCAL_IP_ADDRESS = getLocalInetAddress();
    private static final String LOCAL_HOST_NAME = getHostName();

    private static String getLocalInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address = null;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();
                    if (!address.isLoopbackAddress() && address.getHostAddress().indexOf(":") == -1) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Throwable t) {
        }
        return "127.0.0.1";
    }

    private static String getHostName() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (Throwable t) {
        }
        return "localhost";
    }

    /**
     * 获取本机 IP
     *
     * @return 找不到 IP 地址则返回 127.0.0.1
     */
    public static final String getLocalAddress() {
        return LOCAL_IP_ADDRESS;
    }

    /**
     * 获取本机 IP
     *
     * @return 找不到 IP 地址则返回 127.0.0.1
     */
    public static final String getLocalHostName() {
        return LOCAL_HOST_NAME;
    }

    private static Pattern IPV4_ADDRESS_PATTERN = Pattern
            .compile("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    public static final boolean isIpv4Address(String str) {
        return IPV4_ADDRESS_PATTERN.matcher(str).matches();
    }

    /**
     * 较快速的判断方法，存在误判的可能
     */
    public static final boolean isIpv4AddressFast(String str) {
        if (CommonUtils.countMatches(str, '.') == 3 && str.length() <= 15) {
            for (int i = 0; i < str.length(); ++i) {
                if (!isDigitOrDot(str.charAt(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private static final boolean isDigitOrDot(char c) {
        return (c >= '0' && c <= '9') || c == '.';
    }

    /**
     * 去掉 IP 地址跟着的端口号，xxx.xxx.xxx.xxx:port -> xxx.xxx.xxx.xxx
     */
    public static final String removePort(String ipWithPort) {
        return StringUtils.substringBeforeLast(ipWithPort, ":");
    }

    /**
     * 去掉 IP 地址的 D 段，即 a.b.c.d -> a.b.c.
     */
    public static final String removeDClass(String ip) {
        if (ip == null) {
            return null;
        }
        int lastDot = ip.lastIndexOf('.');
        return lastDot > 0 ? ip.substring(0, lastDot + 1) : null;
    }

    /**
     * 去掉 IP 地址的 CD 段，即 a.b.c.d -> a.b.
     */
    public static final String removeCAndDClass(String ip) {
        if (ip == null) {
            return null;
        }
        int dot = ip.indexOf('.');
        if (dot > 0) {
            dot = ip.indexOf('.', dot + 1);
        }
        return dot > 0 ? ip.substring(0, dot + 1) : null;
    }

    /**
     * 把IP地址转化为int
     *
     * @return int
     */
    public static byte[] ipToBytesByReg(String ipAddr) {
        byte[] ret = new byte[4];
        try {
            StringTokenizer token = new StringTokenizer(ipAddr, ".");
            ret[0] = (byte) (Integer.parseInt(token.nextToken()) & 0xFF);
            ret[1] = (byte) (Integer.parseInt(token.nextToken()) & 0xFF);
            ret[2] = (byte) (Integer.parseInt(token.nextToken()) & 0xFF);
            ret[3] = (byte) (Integer.parseInt(token.nextToken()) & 0xFF);
            return ret;
        } catch (Exception e) {
            throw new IllegalArgumentException(ipAddr + " is invalid IP");
        }

    }

    /**
     * 字节数组转化为IP
     *
     * @return int
     */
    public static String bytesToIp(byte[] bytes) {
        return new StringBuffer().append(bytes[0] & 0xFF).append('.').append(
                        bytes[1] & 0xFF).append('.').append(bytes[2] & 0xFF)
                .append('.').append(bytes[3] & 0xFF).toString();
    }

    /**
     * 根据位运算把 byte[] -> int
     *
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }

    /**
     * 把IP地址转化为int
     *
     * @return int
     */
    public static int ipToInt(String ipAddr) {
        return bytesToInt(ipToBytesByReg(ipAddr));
    }

    /**
     * 把int->ip地址
     *
     * @return String
     */
    public static String intToIp(int ipInt) {
        return new StringBuilder().append(((ipInt >> 24) & 0xff)).append('.')
                .append((ipInt >> 16) & 0xff).append('.').append(
                        (ipInt >> 8) & 0xff).append('.').append((ipInt & 0xff))
                .toString();
    }

    public static String addressToIp(String address) {
        if (StringUtils.isBlank(address)) {
            return "";
        }
        if (StringUtils.isNotBlank(address)) {
            int colon = address.indexOf(':');
            if (colon > 0) {
                address = address.substring(0, colon);
            }
            if (address.charAt(0) == '/') {
                address = address.substring(1);
            }
        }
        return address;
    }


    /**
     * @param src a String representing an IPv4 address in textual format
     * @return a boolean indicating whether src is an IPv4 literal address
     */
    public static boolean isIPv4LiteralAddress(String src) {
        return textToNumericFormatV4(src) != null;
    }

    /**
     * @param src a String representing an IPv6 address in textual format
     * @return a boolean indicating whether src is an IPv6 literal address
     */
    public static boolean isIPv6LiteralAddress(String src) {
        return textToNumericFormatV6(src) != null;
    }

    public static byte[] textToNumericFormatV6(String src) {
        // Shortest valid string is "::", hence at least 2 chars
        if (src.length() < 2) {
            return null;
        }

        int colonp;
        char ch;
        boolean saw_xdigit;
        int val;
        char[] srcb = src.toCharArray();
        byte[] dst = new byte[INADDR16SZ];

        int srcb_length = srcb.length;
        int pc = src.indexOf('%');
        if (pc == srcb_length - 1) {
            return null;
        }

        if (pc != -1) {
            srcb_length = pc;
        }

        colonp = -1;
        int i = 0, j = 0;
        /* Leading :: requires some special handling. */
        if (srcb[i] == ':') {
            if (srcb[++i] != ':') {
                return null;
            }
        }
        int curtok = i;
        saw_xdigit = false;
        val = 0;
        while (i < srcb_length) {
            ch = srcb[i++];
            int chval = Character.digit(ch, 16);
            if (chval != -1) {
                val <<= 4;
                val |= chval;
                if (val > 0xffff) {
                    return null;
                }
                saw_xdigit = true;
                continue;
            }
            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    if (colonp != -1) {
                        return null;
                    }
                    colonp = j;
                    continue;
                } else if (i == srcb_length) {
                    return null;
                }
                if (j + INT16SZ > INADDR16SZ) {
                    return null;
                }
                dst[j++] = (byte) ((val >> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }
            if (ch == '.' && ((j + INADDR4SZ) <= INADDR16SZ)) {
                String ia4 = src.substring(curtok, srcb_length);
                /* check this IPv4 address has 3 dots, i.e. A.B.C.D */
                int dot_count = 0, index = 0;
                while ((index = ia4.indexOf('.', index)) != -1) {
                    dot_count++;
                    index++;
                }
                if (dot_count != 3) {
                    return null;
                }
                byte[] v4addr = textToNumericFormatV4(ia4);
                if (v4addr == null) {
                    return null;
                }
                for (int k = 0; k < INADDR4SZ; k++) {
                    dst[j++] = v4addr[k];
                }
                saw_xdigit = false;
                break;  /* '\0' was seen by inet_pton4(). */
            }
            return null;
        }
        if (saw_xdigit) {
            if (j + INT16SZ > INADDR16SZ) {
                return null;
            }
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int n = j - colonp;

            if (j == INADDR16SZ) {
                return null;
            }
            for (i = 1; i <= n; i++) {
                dst[INADDR16SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR16SZ;
        }
        if (j != INADDR16SZ) {
            return null;
        }
        byte[] newdst = convertFromIPv4MappedAddress(dst);
        if (newdst != null) {
            return newdst;
        } else {
            return dst;
        }
    }

    public static byte[] convertFromIPv4MappedAddress(byte[] addr) {
        if (isIPv4MappedAddress(addr)) {
            byte[] newAddr = new byte[INADDR4SZ];
            System.arraycopy(addr, 12, newAddr, 0, INADDR4SZ);
            return newAddr;
        }
        return null;
    }

    private static boolean isIPv4MappedAddress(byte[] addr) {
        if (addr.length < INADDR16SZ) {
            return false;
        }
        if ((addr[0] == 0x00) && (addr[1] == 0x00) &&
                (addr[2] == 0x00) && (addr[3] == 0x00) &&
                (addr[4] == 0x00) && (addr[5] == 0x00) &&
                (addr[6] == 0x00) && (addr[7] == 0x00) &&
                (addr[8] == 0x00) && (addr[9] == 0x00) &&
                (addr[10] == (byte) 0xff) &&
                (addr[11] == (byte) 0xff)) {
            return true;
        }
        return false;
    }

    public static byte[] textToNumericFormatV4(String src) {
        byte[] res = new byte[INADDR4SZ];

        long tmpValue = 0;
        int currByte = 0;
        boolean newOctet = true;

        int len = src.length();
        if (len == 0 || len > 15) {
            return null;
        }
        for (int i = 0; i < len; i++) {
            char c = src.charAt(i);
            if (c == '.') {
                if (newOctet || tmpValue < 0 || tmpValue > 0xff || currByte == 3) {
                    return null;
                }
                res[currByte++] = (byte) (tmpValue & 0xff);
                tmpValue = 0;
                newOctet = true;
            } else {
                int digit = Character.digit(c, 10);
                if (digit < 0) {
                    return null;
                }
                tmpValue *= 10;
                tmpValue += digit;
                newOctet = false;
            }
        }
        if (newOctet || tmpValue < 0 || tmpValue >= (1L << ((4 - currByte) * 8))) {
            return null;
        }
        switch (currByte) {
            case 0:
                res[0] = (byte) ((tmpValue >> 24) & 0xff);
            case 1:
                res[1] = (byte) ((tmpValue >> 16) & 0xff);
            case 2:
                res[2] = (byte) ((tmpValue >> 8) & 0xff);
            case 3:
                res[3] = (byte) ((tmpValue >> 0) & 0xff);
        }
        return res;
    }
}

