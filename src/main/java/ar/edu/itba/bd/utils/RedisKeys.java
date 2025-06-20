package ar.edu.itba.bd.utils;


public final class RedisKeys {
    
    private RedisKeys() {}
    
    // Claves para proveedores (suppliers)
    public static final String SUPPLIERS_ACTIVE = "suppliers:active";
    public static final String SUPPLIERS_INACTIVE = "suppliers:inactive";
    public static final String SUPPLIERS_AUTHORIZED = "suppliers:authorized";
    public static final String SUPPLIERS_UNAUTHORIZED = "suppliers:unauthorized";
    
} 