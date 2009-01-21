package org.jets3t.service.mx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.ObjectName;

import org.jets3t.service.model.S3Bucket;

public class S3BucketMx implements S3BucketMxMBean {
    private static final boolean isEnabled =
        "true".equals(System.getProperty("s3service.bucket.mx"));
    private static Map objects = Collections.synchronizedMap(new HashMap());

    private static final int
        TOTAL = 0,
        O_GET = 1,
        O_PUT = 2,
        O_DELETE = 3,
        O_COPY = 4,
        LIST = 5,
        MAX = 6;
        
    private LongCounter[] counters = new LongCounter[MAX];

    S3BucketMx() {
        for (int i=0; i<this.counters.length; i++) {
            this.counters[i] = new LongCounter();
        }
    }

    public long getTotalRequests() {
        return this.counters[TOTAL].getValue();
    }

    //bucket specific requests
    public long getTotalListRequests() {
        return this.counters[LIST].getValue();
    }

    //getTotalObject* aggregates for object requests on this bucket
    public long getTotalObjectGetRequests() {
        return this.counters[O_GET].getValue();
    }

    public long getTotalObjectPutRequests() {
        return this.counters[O_PUT].getValue();
    }

    public long getTotalObjectDeleteRequests() {
        return this.counters[O_DELETE].getValue();
    }

    public long getTotalObjectCopyRequests() {
        return this.counters[O_COPY].getValue();
    }

    public static void registerMBeans(S3Bucket[] buckets) {
        if (!isEnabled) {
            return;
        }
        for (int i=0; i<buckets.length; i++) {
            getInstance(buckets[i].getName());
        }
    }

    private static S3BucketMx getInstance(String bucketName) {
        String props =
            "Type=S3Bucket" + "," +
            "Name=" + bucketName;

        S3BucketMx object = (S3BucketMx)objects.get(props);
        if (object == null) {
            object = new S3BucketMx();
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

    private void increment(int type) {
        counters[TOTAL].increment();
        counters[type].increment();        
    }

    public static void increment(int type,
                                 String bucketName) {
        if (isEnabled) {
            S3BucketMx object = getInstance(bucketName);
            object.increment(type);
        }
        S3ServiceMx.getInstance().bucketCounter.increment(type);
    }

    static void o_get(String bucketName) {
        increment(O_GET, bucketName);
    }

    static void o_put(String bucketName) {
        increment(O_PUT, bucketName);
    }
    
    static void o_delete(String bucketName) {
        increment(O_DELETE, bucketName);
    }

    static void o_copy(String bucketName) {
        increment(O_COPY, bucketName);
    }

    public static void list(String bucketName) {
        increment(LIST, bucketName);
    }
}
