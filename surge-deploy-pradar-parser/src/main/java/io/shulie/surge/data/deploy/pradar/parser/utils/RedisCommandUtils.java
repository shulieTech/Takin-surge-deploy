package io.shulie.surge.data.deploy.pradar.parser.utils;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.Set;

/**
 * 处理Redis支持的所有的key的集合
 *
 * @author xingchen
 * @description: TODO
 * @date 2022/7/25 2:26 PM
 */
public class RedisCommandUtils {
    private static Logger logger = LoggerFactory.getLogger(RedisCommandUtils.class);

    private static Set<String> REDIS_KEYS = Sets.newHashSet();

    // Redis COMMAND 后续版本可通过此命令获取最新添加,目前是7.0.4版本命令
    static {
        REDIS_KEYS.add("zadd");
        REDIS_KEYS.add("readwrite");
        REDIS_KEYS.add("exec");
        REDIS_KEYS.add("replicaof");
        REDIS_KEYS.add("lmpop");
        REDIS_KEYS.add("xreadgroup");
        REDIS_KEYS.add("command");
        REDIS_KEYS.add("script");
        REDIS_KEYS.add("sync");
        REDIS_KEYS.add("xclaim");
        REDIS_KEYS.add("lpushx");
        REDIS_KEYS.add("setnx");
        REDIS_KEYS.add("xlen");
        REDIS_KEYS.add("lrem");
        REDIS_KEYS.add("rename");
        REDIS_KEYS.add("sadd");
        REDIS_KEYS.add("select");
        REDIS_KEYS.add("getex");
        REDIS_KEYS.add("blmove");
        REDIS_KEYS.add("sinterstore");
        REDIS_KEYS.add("rpushx");
        REDIS_KEYS.add("sscan");
        REDIS_KEYS.add("cluster");
        REDIS_KEYS.add("psetex");
        REDIS_KEYS.add("georadiusbymember");
        REDIS_KEYS.add("xtrim");
        REDIS_KEYS.add("hexists");
        REDIS_KEYS.add("lastsave");
        REDIS_KEYS.add("eval");
        REDIS_KEYS.add("keys");
        REDIS_KEYS.add("srandmember");
        REDIS_KEYS.add("zmscore");
        REDIS_KEYS.add("pubsub");
        REDIS_KEYS.add("lpush");
        REDIS_KEYS.add("time");
        REDIS_KEYS.add("evalsha");
        REDIS_KEYS.add("debug");
        REDIS_KEYS.add("smembers");
        REDIS_KEYS.add("failover");
        REDIS_KEYS.add("zinterstore");
        REDIS_KEYS.add("sunion");
        REDIS_KEYS.add("incrby");
        REDIS_KEYS.add("flushdb");
        REDIS_KEYS.add("punsubscribe");
        REDIS_KEYS.add("srem");
        REDIS_KEYS.add("zremrangebylex");
        REDIS_KEYS.add("lrange");
        REDIS_KEYS.add("pfdebug");
        REDIS_KEYS.add("readonly");
        REDIS_KEYS.add("unwatch");
        REDIS_KEYS.add("bitpos");
        REDIS_KEYS.add("role");
        REDIS_KEYS.add("zdiffstore");
        REDIS_KEYS.add("config");
        REDIS_KEYS.add("latency");
        REDIS_KEYS.add("object");
        REDIS_KEYS.add("bzpopmax");
        REDIS_KEYS.add("geohash");
        REDIS_KEYS.add("sunionstore");
        REDIS_KEYS.add("sdiffstore");
        REDIS_KEYS.add("decr");
        REDIS_KEYS.add("sdiff");
        REDIS_KEYS.add("zrange");
        REDIS_KEYS.add("zincrby");
        REDIS_KEYS.add("geodist");
        REDIS_KEYS.add("unsubscribe");
        REDIS_KEYS.add("brpop");
        REDIS_KEYS.add("bzpopmin");
        REDIS_KEYS.add("zremrangebyrank");
        REDIS_KEYS.add("del");
        REDIS_KEYS.add("zcard");
        REDIS_KEYS.add("expireat");
        REDIS_KEYS.add("spop");
        REDIS_KEYS.add("ttl");
        REDIS_KEYS.add("watch");
        REDIS_KEYS.add("zinter");
        REDIS_KEYS.add("bgrewriteaof");
        REDIS_KEYS.add("spublish");
        REDIS_KEYS.add("asking");
        REDIS_KEYS.add("psubscribe");
        REDIS_KEYS.add("subscribe");
        REDIS_KEYS.add("linsert");
        REDIS_KEYS.add("set");
        REDIS_KEYS.add("hincrbyfloat");
        REDIS_KEYS.add("lindex");
        REDIS_KEYS.add("acl");
        REDIS_KEYS.add("decrby");
        REDIS_KEYS.add("ping");
        REDIS_KEYS.add("bitfield");
        REDIS_KEYS.add("rpop");
        REDIS_KEYS.add("psync");
        REDIS_KEYS.add("ssubscribe");
        REDIS_KEYS.add("append");
        REDIS_KEYS.add("xadd");
        REDIS_KEYS.add("zunion");
        REDIS_KEYS.add("pfselftest");
        REDIS_KEYS.add("lcs");
        REDIS_KEYS.add("shutdown");
        REDIS_KEYS.add("getbit");
        REDIS_KEYS.add("fcall_ro");
        REDIS_KEYS.add("eval_ro");
        REDIS_KEYS.add("lset");
        REDIS_KEYS.add("getdel");
        REDIS_KEYS.add("geoadd");
        REDIS_KEYS.add("bitcount");
        REDIS_KEYS.add("memory");
        REDIS_KEYS.add("unlink");
        REDIS_KEYS.add("hvals");
        REDIS_KEYS.add("dump");
        REDIS_KEYS.add("multi");
        REDIS_KEYS.add("client");
        REDIS_KEYS.add("zrevrange");
        REDIS_KEYS.add("scan");
        REDIS_KEYS.add("zdiff");
        REDIS_KEYS.add("zcount");
        REDIS_KEYS.add("hgetall");
        REDIS_KEYS.add("zrevrangebyscore");
        REDIS_KEYS.add("discard");
        REDIS_KEYS.add("get");
        REDIS_KEYS.add("bitop");
        REDIS_KEYS.add("replconf");
        REDIS_KEYS.add("zrevrank");
        REDIS_KEYS.add("blpop");
        REDIS_KEYS.add("lpop");
        REDIS_KEYS.add("flushall");
        REDIS_KEYS.add("zrank");
        REDIS_KEYS.add("geosearchstore");
        REDIS_KEYS.add("incr");
        REDIS_KEYS.add("zlexcount");
        REDIS_KEYS.add("module");
        REDIS_KEYS.add("setrange");
        REDIS_KEYS.add("zrevrangebylex");
        REDIS_KEYS.add("blmpop");
        REDIS_KEYS.add("zpopmax");
        REDIS_KEYS.add("incrbyfloat");
        REDIS_KEYS.add("xack");
        REDIS_KEYS.add("restore");
        REDIS_KEYS.add("sunsubscribe");
        REDIS_KEYS.add("fcall");
        REDIS_KEYS.add("georadius_ro");
        REDIS_KEYS.add("geosearch");
        REDIS_KEYS.add("copy");
        REDIS_KEYS.add("save");
        REDIS_KEYS.add("bitfield_ro");
        REDIS_KEYS.add("dbsize");
        REDIS_KEYS.add("zremrangebyscore");
        REDIS_KEYS.add("smove");
        REDIS_KEYS.add("xrevrange");
        REDIS_KEYS.add("zrangebylex");
        REDIS_KEYS.add("substr");
        REDIS_KEYS.add("xsetid");
        REDIS_KEYS.add("hlen");
        REDIS_KEYS.add("type");
        REDIS_KEYS.add("geopos");
        REDIS_KEYS.add("ltrim");
        REDIS_KEYS.add("hscan");
        REDIS_KEYS.add("strlen");
        REDIS_KEYS.add("pexpiretime");
        REDIS_KEYS.add("rpush");
        REDIS_KEYS.add("xgroup");
        REDIS_KEYS.add("bgsave");
        REDIS_KEYS.add("xdel");
        REDIS_KEYS.add("xread");
        REDIS_KEYS.add("pfadd");
        REDIS_KEYS.add("georadiusbymember_ro");
        REDIS_KEYS.add("sintercard");
        REDIS_KEYS.add("touch");
        REDIS_KEYS.add("setbit");
        REDIS_KEYS.add("pexpireat");
        REDIS_KEYS.add("sismember");
        REDIS_KEYS.add("hmset");
        REDIS_KEYS.add("xrange");
        REDIS_KEYS.add("msetnx");
        REDIS_KEYS.add("zunionstore");
        REDIS_KEYS.add("rpoplpush");
        REDIS_KEYS.add("slaveof");
        REDIS_KEYS.add("pfmerge");
        REDIS_KEYS.add("bzmpop");
        REDIS_KEYS.add("hget");
        REDIS_KEYS.add("quit");
        REDIS_KEYS.add("hello");
        REDIS_KEYS.add("lolwut");
        REDIS_KEYS.add("zscan");
        REDIS_KEYS.add("auth");
        REDIS_KEYS.add("renamenx");
        REDIS_KEYS.add("migrate");
        REDIS_KEYS.add("publish");
        REDIS_KEYS.add("move");
        REDIS_KEYS.add("sort_ro");
        REDIS_KEYS.add("llen");
        REDIS_KEYS.add("hrandfield");
        REDIS_KEYS.add("hmget");
        REDIS_KEYS.add("lpos");
        REDIS_KEYS.add("function");
        REDIS_KEYS.add("monitor");
        REDIS_KEYS.add("echo");
        REDIS_KEYS.add("info");
        REDIS_KEYS.add("getset");
        REDIS_KEYS.add("scard");
        REDIS_KEYS.add("randomkey");
        REDIS_KEYS.add("pfcount");
        REDIS_KEYS.add("lmove");
        REDIS_KEYS.add("mset");
        REDIS_KEYS.add("hsetnx");
        REDIS_KEYS.add("zscore");
        REDIS_KEYS.add("zpopmin");
        REDIS_KEYS.add("zrangestore");
        REDIS_KEYS.add("evalsha_ro");
        REDIS_KEYS.add("zintercard");
        REDIS_KEYS.add("hincrby");
        REDIS_KEYS.add("pttl");
        REDIS_KEYS.add("mget");
        REDIS_KEYS.add("zrandmember");
        REDIS_KEYS.add("hstrlen");
        REDIS_KEYS.add("expire");
        REDIS_KEYS.add("zrangebyscore");
        REDIS_KEYS.add("xpending");
        REDIS_KEYS.add("brpoplpush");
        REDIS_KEYS.add("setex");
        REDIS_KEYS.add("smismember");
        REDIS_KEYS.add("wait");
        REDIS_KEYS.add("hset");
        REDIS_KEYS.add("hdel");
        REDIS_KEYS.add("sinter");
        REDIS_KEYS.add("sort");
        REDIS_KEYS.add("persist");
        REDIS_KEYS.add("expiretime");
        REDIS_KEYS.add("hkeys");
        REDIS_KEYS.add("restore-asking");
        REDIS_KEYS.add("georadius");
        REDIS_KEYS.add("exists");
        REDIS_KEYS.add("swapdb");
        REDIS_KEYS.add("reset");
        REDIS_KEYS.add("getrange");
        REDIS_KEYS.add("zrem");
        REDIS_KEYS.add("pexpire");
        REDIS_KEYS.add("xautoclaim");
        REDIS_KEYS.add("zmpop");
        REDIS_KEYS.add("clientsetname");
        REDIS_KEYS.add("init");
        REDIS_KEYS.add("authasync");
        REDIS_KEYS.add("authsync");
        REDIS_KEYS.add("getmasteraddrbyname");
        REDIS_KEYS.add("zrangewithscores");
    }

    /**
     * 针对redis解析对应的methodName
     *
     * @param serviceName
     * @param methodName
     * @return
     */
    public static String parseMethod(String serviceName, String methodName) {
        // 判断下serviceName是否在命令行里面
        serviceName = StringUtils.replace(serviceName, "<", "");
        serviceName = StringUtils.replace(serviceName, ">", "");
        methodName = StringUtils.replace(methodName, "<", "");
        methodName = StringUtils.replace(methodName, ">", "");
        if (StringUtils.isNotBlank(serviceName) && REDIS_KEYS.contains(serviceName.toLowerCase())) {
            return serviceName;
        }
        // 判断下methodName是否在命令行里面
        if (StringUtils.isNotBlank(methodName) && REDIS_KEYS.contains(methodName.toLowerCase())) {
            return methodName;
        }
        // 其他情况暂时处理默认值
        logger.warn("current Redis trace log setting is error {},{}", serviceName, methodName);
        return "command";
    }

    public static void main(String[] args) {
        Assert.isTrue(parseMethod("SET", "").equals("SET"));
        Assert.isTrue(parseMethod("", "GET").equals("GET"));
        Assert.isTrue(parseMethod("", "").equals("redis_command"));
        Assert.isTrue(parseMethod("xx", "yy").equals("redis_command"));
    }
}
