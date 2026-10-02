package com.uncommongoods.tugboat.engine.ports.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.args.BitOP;
import redis.clients.jedis.args.ExpiryOption;

import java.util.*;
import java.util.function.Consumer;


public class JedisRedisAdapter implements ICacheClient, AutoCloseable {
    private final JedisPool jedisPool;
    private final boolean ownsPool;

    public JedisRedisAdapter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        this.ownsPool = false;
    }

    public JedisRedisAdapter(String host, int port) {
        this.jedisPool = new JedisPool(new JedisPoolConfig(), host, port);
        this.ownsPool = true;
    }

    @Override
    public void close() {
        if (ownsPool && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }

    @Override
    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    public void set(String key, String value, long expirySeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expirySeconds, value);
        }
    }

    @Override
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    @Override
    public Boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }

    @Override
    public Long del(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(key);
        }
    }

    @Override
    public Long del(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.del(keys);
        }
    }

    @Override
    public Boolean expire(String key, long seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expire(key, seconds) == 1;
        }
    }

    @Override
    public Boolean expireAt(String key, long unixTime) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.expireAt(key, unixTime) == 1;
        }
    }

    @Override
    public Long ttl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.ttl(key);
        }
    }

    @Override
    public Long incr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incr(key);
        }
    }

    @Override
    public Long incrBy(String key, long increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrBy(key, increment);
        }
    }

    @Override
    public Double incrByFloat(String key, double increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.incrByFloat(key, increment);
        }
    }

    @Override
    public Long decr(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decr(key);
        }
    }

    @Override
    public Long decrBy(String key, long decrement) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.decrBy(key, decrement);
        }
    }

    @Override
    public Long append(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.append(key, value);
        }
    }

    @Override
    public String substr(String key, int start, int end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.substr(key, start, end);
        }
    }

    @Override
    public Long strlen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.strlen(key);
        }
    }

    @Override
    public Long setrange(String key, long offset, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setrange(key, offset, value);
        }
    }

    @Override
    public String getrange(String key, long startOffset, long endOffset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getrange(key, startOffset, endOffset);
        }
    }

    @Override
    public Boolean setNX(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setnx(key, value) == 1;
        }
    }

    @Override
    public Boolean setEx(String key, long seconds, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            String result = jedis.setex(key, seconds, value);
            return "OK".equals(result);
        }
    }

    @Override
    public Boolean pExpire(String key, long milliseconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpire(key, milliseconds) == 1;
        }
    }

    @Override
    public Boolean pExpireAt(String key, long millisecondsTimestamp) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pexpireAt(key, millisecondsTimestamp) == 1;
        }
    }

    @Override
    public Long pTtl(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.pttl(key);
        }
    }

    @Override
    public Boolean setbit(String key, long offset, boolean value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.setbit(key, offset, value);
        }
    }

    @Override
    public Boolean getbit(String key, long offset) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.getbit(key, offset);
        }
    }

    @Override
    public Long bitcount(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key);
        }
    }

    @Override
    public Long bitcount(String key, long start, long end) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.bitcount(key, start, end);
        }
    }


    @Override
    public void mset(Map<String, String> keyValueMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            String[] keyValueArray = mapToKeyValueArray(keyValueMap);
            jedis.mset(keyValueArray);
        }
    }

    @Override
    public Boolean msetnx(Map<String, String> keyValueMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            String[] keyValueArray = mapToKeyValueArray(keyValueMap);
            return jedis.msetnx(keyValueArray) == 1;
        }
    }

    @Override
    public List<String> mget(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.mget(keys);
        }
    }

    @Override
    public Long bitop(BitOperation op, String destKey, String... srcKeys) {
        try (Jedis jedis = jedisPool.getResource()) {
            BitOP jedisOp = switch (op) {
                case AND -> BitOP.AND;
                case OR -> BitOP.OR;
                case XOR -> BitOP.XOR;
                case NOT -> BitOP.NOT;
                default -> throw new IllegalArgumentException("Unsupported bit operation: " + op);
            };
            return jedis.bitop(jedisOp, destKey, srcKeys);
        }
    }

    @Override
    public Boolean hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hset(key, field, value) == 1;
        }
    }

    @Override
    public Boolean hsetnx(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hsetnx(key, field, value) == 1;
        }
    }

    @Override
    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }

    @Override
    public Boolean hexists(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hexists(key, field);
        }
    }

    @Override
    public Long hdel(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hdel(key, fields);
        }
    }

    @Override
    public Long hlen(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hlen(key);
        }
    }

    @Override
    public Long hincrBy(String key, String field, long increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrBy(key, field, increment);
        }
    }

    @Override
    public Double hincrByFloat(String key, String field, double increment) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hincrByFloat(key, field, increment);
        }
    }

    @Override
    public void hmset(String key, Map<String, String> fieldValueMap) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hmset(key, fieldValueMap);
        }
    }

    @Override
    public List<String> hmget(String key, String... fields) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hmget(key, fields);
        }
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }

    @Override
    public Set<String> hkeys(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hkeys(key);
        }
    }

    @Override
    public List<String> hvals(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hvals(key);
        }
    }

    @Override
    public Set<Entry<String, String>> hentries(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> map = jedis.hgetAll(key);
            Set<Entry<String, String>> entrySet = new HashSet<>();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                entrySet.add(new RedisEntry<>(entry.getKey(), entry.getValue()));
            }

            return entrySet;
        }
    }

    @Override
    public Long hstrlen(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hstrlen(key, field);
        }
    }


    @Override
    public Boolean exists(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(keys) == keys.length;
        }
    }

    @Override
    public Boolean pExpireAt(String key, long millisecondsTimestamp, ExpireOption expireOption) {
        try (Jedis jedis = jedisPool.getResource()) {
            ExpiryOption jedisExpireOption = convertExpireOption(expireOption);
            return jedis.pexpireAt(key, millisecondsTimestamp, jedisExpireOption) == 1;
        }
    }

    @Override
    public Boolean expire(String key, long seconds, ExpireOption expireOption) {
        try (Jedis jedis = jedisPool.getResource()) {
            ExpiryOption jedisExpireOption = convertExpireOption(expireOption);
            return jedis.expire(key, seconds, jedisExpireOption) == 1;
        }
    }

    @Override
    public Boolean persist(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.persist(key) == 1;
        }
    }

    @Override
    public Boolean move(String key, int dbIndex) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.move(key, dbIndex) == 1;
        }
    }

    @Override
    public String type(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.type(key);
        }
    }

    @Override
    public Long touch(String... keys) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.touch(keys);
        }
    }

    @Override
    public ICacheClient.ScanResult<String> scan(String cursor) {
        try (Jedis jedis = jedisPool.getResource()) {
            redis.clients.jedis.resps.ScanResult<String> result = jedis.scan(cursor);
            return new ScanResultImpl<>(result.getCursor(), result.getResult());
        }
    }

    @Override
    public ICacheClient.ScanResult<String> scan(String cursor, ICacheClient.ScanParams params) {
        try (Jedis jedis = jedisPool.getResource()) {
            redis.clients.jedis.params.ScanParams jedisParams = convertScanParams(params);
            redis.clients.jedis.resps.ScanResult<String> result = jedis.scan(cursor, jedisParams);
            return new ScanResultImpl<>(result.getCursor(), result.getResult());
        }
    }


    public void executeTransaction(Consumer<Transaction> operations) {
        try (Jedis jedis = jedisPool.getResource()) {
            Transaction transaction = jedis.multi();
            operations.accept(transaction);
            transaction.exec();
        }
    }

    private String[] mapToKeyValueArray(Map<String, String> map) {
        String[] keyValueArray = new String[map.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            keyValueArray[i++] = entry.getKey();
            keyValueArray[i++] = entry.getValue();
        }
        return keyValueArray;
    }

    private ExpiryOption convertExpireOption(ExpireOption option) {
        if (option == null) {
            return null;
        }

        return switch (option) {
            case NX -> ExpiryOption.NX;
            case XX -> ExpiryOption.XX;
            case GT -> ExpiryOption.GT;
            case LT -> ExpiryOption.LT;
            default -> throw new IllegalArgumentException("Unsupported expiry option: " + option);
        };
    }

    private record RedisEntry<K, V>(K key, V value) implements Entry<K, V> {

        @Override
            public String toString() {
                return key + "=" + value;
            }
        }

    private record ScanResultImpl<T>(String cursor, List<T> result) implements ICacheClient.ScanResult<T> {
        @Override
        public String getCursor() {
            return cursor;
        }

        @Override
        public List<T> getResult() {
            return result;
        }
    }

    private static class ScanParamsImpl implements ICacheClient.ScanParams {
        private final redis.clients.jedis.params.ScanParams jedisParams = new redis.clients.jedis.params.ScanParams();
        private String pattern;

        @Override
        public ICacheClient.ScanParams match(String pattern) {
            this.pattern = pattern;
            jedisParams.match(pattern);
            return this;
        }

        @Override
        public ICacheClient.ScanParams count(Integer count) {
            jedisParams.count(count);
            return this;
        }

        @Override
        public String match() {
            return pattern;
        }

        public redis.clients.jedis.params.ScanParams getJedisParams() {
            return jedisParams;
        }
    }

    private redis.clients.jedis.params.ScanParams convertScanParams(ICacheClient.ScanParams params) {
        if (params instanceof ScanParamsImpl) {
            return ((ScanParamsImpl) params).getJedisParams();
        }

        redis.clients.jedis.params.ScanParams jedisParams = new redis.clients.jedis.params.ScanParams();
        if (params.match() != null) {
            jedisParams.match(params.match());
        }
        return jedisParams;
    }
}

