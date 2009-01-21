package org.jets3t.service.mx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.jets3t.service.model.S3Object;

public class S3ObjectMx implements S3ObjectMxMBean {
    private static final boolean isEnabled =
        "true".equals(System.getProperty("s3service.object.mx"));
    private static Map objects = Collections.synchronizedMap(new HashMap());

    private static final int
        TOTAL = 0,
        GET = 1,
        PUT = 2,
        DELETE = 3,
        COPY = 4,
        MAX = 5;
        
    private LongCounter[] counters = new LongCounter[MAX];

    private S3ObjectMx() {
        for (int i=0; i<this.counters.length; i++) {
            this.counters[i] = new LongCounter();
        }
    }

    public long getTotalRequests() {
        return this.counters[TOTAL].getValue();
    }

    public long getTotalGetRequests() {
        return this.counters[GET].getValue();
    }

    public long getTotalPutRequests() {
        return this.counters[PUT].getValue();
    }

    public long getTotalDeleteRequests() {
        return this.counters[DELETE].getValue();
    }

    public long getTotalCopyRequests() {
        return this.counters[COPY].getValue();
    }

    public static void registerMBeans(String bucketName, S3Object[] objects) {
        if (!isEnabled) {
            return;
        }
        for (int i=0; i<objects.length; i++) {
            getInstance(bucketName, objects[i].getKey());
        }
    }

    private static S3ObjectMx getInstance(String bucketName, String key) {
        String props =
            "Type=S3Object" + "," +
            "Bucket=" + bucketName + "," +
            "Name=" + key;

        S3ObjectMx object = (S3ObjectMx)objects.get(props);
        if (object == null) {
            object = new S3ObjectMx();
            objects.put(props, object);
            ObjectName name = S3ServiceMx.getObjectName(props);
            try {
                S3ServiceMx.registerMBean(object, name);
            } catch (Exception e) {
                e.printStackTrace(); //XXX
            }
        }
        return object;
    }

    public static void increment(int type,
                                 String bucketName,
                                 String key) {
        if (isEnabled) {
            S3ObjectMx object = getInstance(bucketName, key);
            object.counters[TOTAL].increment();
            object.counters[type].increment();
        }
    }

    public static void get(String bucketName, String key) {
        increment(GET, bucketName, key);
        S3BucketMx.o_get(bucketName);
    }

    public static void put(String bucketName, String key) {
        increment(PUT, bucketName, key);
        S3BucketMx.o_put(bucketName);
    }
    
    public static void delete(String bucketName, String key) {
        increment(DELETE, bucketName, key);
        S3BucketMx.o_delete(bucketName);
    }

    public static void copy(String bucketName, String key) {
        increment(COPY, bucketName, key);
        S3BucketMx.o_copy(bucketName);
    }
}
