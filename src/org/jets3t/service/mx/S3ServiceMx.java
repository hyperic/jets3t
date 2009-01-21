package org.jets3t.service.mx;

import java.util.List;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

public class S3ServiceMx implements S3ServiceMxMBean {
    static final String DOMAIN = "jets3t";
    S3BucketMx bucketCounter;
    private static S3ServiceMx instance;

    private S3ServiceMx() {
        this.bucketCounter = new S3BucketMx();
    }

    static ObjectName getObjectName(String props) {
        try {
            return new ObjectName(DOMAIN + ":" + props);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException(props);
        }
    }

    private static MBeanServer getMBeanServer() {
        List servers = 
            MBeanServerFactory.findMBeanServer(null);
        if (servers.size() == 0) {
            return null;
        }
        return (MBeanServer)servers.get(0);
    }

    public static ObjectInstance registerMBean(Object object, ObjectName name)
        throws InstanceAlreadyExistsException,
               MBeanRegistrationException,
               NotCompliantMBeanException {
        MBeanServer server = getMBeanServer();
        if (server == null) {
            return null;
        }
        return server.registerMBean(object, name);
    }

    public static void registerMBean() {
        getInstance();
    }

    static S3ServiceMx getInstance() {
        if (instance == null) {
            String props = "Type=S3Service";

            instance = new S3ServiceMx();
            ObjectName name = getObjectName(props);
            try {
                registerMBean(instance, name);
            } catch (Exception e) {
                e.printStackTrace(); //XXX
            }
        }
        return instance;
    }

    public long getTotalListRequests() {
        return this.bucketCounter.getTotalListRequests();
    }

    public long getTotalObjectCopyRequests() {
        return this.bucketCounter.getTotalObjectCopyRequests();
    }

    public long getTotalObjectDeleteRequests() {
        return this.bucketCounter.getTotalObjectDeleteRequests();
    }

    public long getTotalObjectGetRequests() {
        return this.bucketCounter.getTotalObjectGetRequests();
    }

    public long getTotalObjectPutRequests() {
        return this.bucketCounter.getTotalObjectPutRequests();
    }

    public long getTotalRequests() {
        return this.bucketCounter.getTotalRequests();
    }
}
