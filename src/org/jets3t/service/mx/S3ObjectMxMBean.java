package org.jets3t.service.mx;

public interface S3ObjectMxMBean {

    public long getTotalRequests();

    public long getTotalGetRequests();

    public long getTotalPutRequests();
    
    public long getTotalCopyRequests();

    public long getTotalDeleteRequests();
}
