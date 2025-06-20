package ar.edu.itba.bd.database;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Jedis;

public class RedisConnection {

    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static JedisPool pool;

    static {
        pool = new JedisPool(new JedisPoolConfig(), HOST, PORT);
    }

    private RedisConnection() {}

    public static Jedis getClient() {
        return pool.getResource();
    }
}
