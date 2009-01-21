package org.jets3t.service.mx;

public interface S3BucketMxMBean {

    public long getTotalRequests();

    public long getTotalListRequests();

    public long getTotalObjectGetRequests();

    public long getTotalObjectPutRequests();

    public long getTotalObjectDeleteRequests();

    public long getTotalObjectCopyRequests();
}
