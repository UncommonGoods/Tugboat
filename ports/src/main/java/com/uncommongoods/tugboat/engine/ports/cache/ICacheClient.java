package com.uncommongoods.tugboat.engine.ports.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ICacheClient {

    // Basic String Operations
    void set(String key, String value);

    void set(String key, String value, long expirySeconds);

    String get(String key);

    Boolean exists(String key);

    Long del(String key);

    Long del(String... keys);

    Boolean expire(String key, long seconds);

    Boolean expireAt(String key, long unixTime);

    Long ttl(String key);

    Long incr(String key);

    Long incrBy(String key, long increment);

    Double incrByFloat(String key, double increment);

    Long decr(String key);

    Long decrBy(String key, long decrement);

    Long append(String key, String value);

    String substr(String key, int start, int end);

    Long strlen(String key);

    Long setrange(String key, long offset, String value);

    String getrange(String key, long startOffset, long endOffset);

    Boolean setNX(String key, String value);

    Boolean setEx(String key, long seconds, String value);

    Boolean pExpire(String key, long milliseconds);

    Boolean pExpireAt(String key, long millisecondsTimestamp);

    Long pTtl(String key);

    Boolean setbit(String key, long offset, boolean value);

    Boolean getbit(String key, long offset);

    Long bitcount(String key);

    Long bitcount(String key, long start, long end);

    // Multiple String Operations
    void mset(Map<String, String> keyValueMap);

    Boolean msetnx(Map<String, String> keyValueMap);

    List<String> mget(String... keys);

    Long bitop(BitOperation op, String destKey, String... srcKeys);


    // Basic Hash Operations
    Boolean hset(String key, String field, String value);

    Boolean hsetnx(String key, String field, String value);

    String hget(String key, String field);

    Boolean hexists(String key, String field);

    Long hdel(String key, String... fields);

    Long hlen(String key);

    Long hincrBy(String key, String field, long increment);

    Double hincrByFloat(String key, String field, double increment);

    // Multiple Hash Operations
    void hmset(String key, Map<String, String> fieldValueMap);

    List<String> hmget(String key, String... fields);

    Map<String, String> hgetAll(String key);

    Set<String> hkeys(String key);

    List<String> hvals(String key);

    Set<Entry<String, String>> hentries(String key);

    Long hstrlen(String key, String field);


    Boolean exists(String... keys);

    Boolean pExpireAt(String key, long millisecondsTimestamp, ExpireOption expireOption);

    Boolean expire(String key, long seconds, ExpireOption expireOption);

    Boolean persist(String key);

    Boolean move(String key, int dbIndex);

    String type(String key);

    Long touch(String... keys);

    ScanResult<String> scan(String cursor);
    ScanResult<String> scan(String cursor, ScanParams params);

    enum BitOperation {
        AND, OR, XOR, NOT
    }

    enum ExpireOption {
        NX, // Only set expire if the key has no expiry
        XX, // Only set expire if the key already has an expiry
        GT, // Only set expire if the new expiry is greater than current expiry
        LT  // Only set expire if the new expiry is less than current expiry
    }

    interface Entry<K, V> {
        K key();

        V value();
    }

    interface ScanResult<T> {
        String getCursor();
        List<T> getResult();
    }

    interface ScanParams {
        ScanParams match(String pattern);
        ScanParams count(Integer count);

        String match();
    }
}
