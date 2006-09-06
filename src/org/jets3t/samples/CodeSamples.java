package org.jets3t.samples;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.acl.Permission;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multithread.GetObjectHeadsEvent;
import org.jets3t.service.multithread.S3ServiceEventAdaptor;
import org.jets3t.service.multithread.S3ServiceMulti;
import org.jets3t.service.multithread.ServiceEvent;
import org.jets3t.service.security.AWSCredentials;

public class CodeSamples {
    
    public static void main(String[] args) throws Exception {
        // Load AWS properties from the properties file example.properties in the current working directory. 
        Properties exampleProps = new Properties();
        exampleProps.load(new FileInputStream("example.properties"));        
        String awsAccessKey = exampleProps.getProperty("awsAccessKey");
        String awsSecretKey = exampleProps.getProperty("awsSecretKey");
        if (awsAccessKey == null || awsSecretKey == null) {
            throw new Exception("example.properties file must contain properties awsAccessKey and awsSecretKey");
        }
        
        /* ************
         * Code Samples
         * ************
         */

        /*
         * Connnecting to S3
         */
        
        // Your Amazon Web Services (AWS) login credentials are required to manage S3 accounts. 
        // These credentials are stored in an AWSCredentials object:

        AWSCredentials awsCredentials = 
            new AWSCredentials(awsAccessKey, awsSecretKey);

        // To communicate with S3, create a class that implements an S3Service. 
        // We will use the REST/HTTP implementation based on HttpClient, as this is the most 
        // robust implementation provided with jets3t.

        S3Service s3Service = new RestS3Service(awsCredentials);

        // A good test to see if your S3Service can connect to S3 is to list all the buckets you own. 
        // If a bucket listing produces no exceptions, all is well.

        S3Bucket[] myBuckets = s3Service.listAllBuckets();

        /*
         * Create a bucket 
         */
        
        // To store data in S3 you must first create a bucket, a container for objects.

        S3Bucket testBucket = s3Service.createBucket(awsAccessKey + ".Test");
        System.out.println("Created test bucket: " + testBucket.getName());

        // Notice how the code above used your AWS Access Key as a prefix to the bucket name? 
        // It is a good idea to follow this approach as bucket names must be unique in S3. 
        // If you try using a common name, you will probably not be able to create the 
        // bucket as someone else will already have a bucket of that name.

        // This will probably fail, as someone else has already created a 'Test' bucket.
        try {
            S3Bucket existingBucket = s3Service.createBucket("Test");
        } catch (S3ServiceException e) {
            System.err.println("Error code and message from S3: " 
                + e.getErrorCode() + " - " + e.getErrorMessage());
        }

        /*
         * Uploading data objects 
         */

        // We use S3Object classes to represent data objects in S3. To store some information in our 
        // new test bucket, we must first create an object with a key/name then tell our 
        // S3Service to upload it to S3.

        // In the example below, we print out information about the S3Object before and after 
        // uploading it to S3. These print-outs demonstrate that the S3Object returned by the 
        // putObject method contains extra information provided by S3, such as the date the 
        // object was last modified on an S3 server.

        // Create an empty object with a key/name, and print the object's details.
        S3Object object = new S3Object("object");
        System.out.println("S3Object before upload: " + object);

        // Upload the object to our test bucket in S3.
        object = s3Service.putObject(testBucket, object);

        // Print the details about the uploaded object, which contains more information.
        System.out.println("S3Object after upload: " + object);

        // The example above will create an empty object in S3, which isn't very useful. 
        // To include data in the object you must provide an InputStream containing the information 
        // you want to upload, and you should also tell the object how much data you are uploading. 
        // If you know the Content/Mime type of the data (e.g. text/plain) you should set this too.

        // Note: It isn't strictly necessary to set the Content Length as the jets3t toolkit can 
        // work out the value itself, however it's a good habit to do this as it can prevent 
        // problems when uploading large objects.

        // Create an object containing a greeting string as data.
        String greeting = "Hello World!";
        S3Object helloWorldObject = new S3Object("helloWorld.txt");
        ByteArrayInputStream greetingIS = new ByteArrayInputStream(greeting.getBytes());
        helloWorldObject.setDataInputStream(greetingIS);

        // Set important content properties.
        helloWorldObject.setContentLength(greetingIS.available());
        helloWorldObject.setContentType("text/plain");

        // Upload the data object.
        helloWorldObject = s3Service.putObject(testBucket, helloWorldObject);

        // Print details about the uploaded object.
        System.out.println("S3Object with data: " + helloWorldObject);

        /*
         * Creating common S3Objects 
         */
        
        // As a convenience, S3Objects can be created with constructors that handle 
        // commonly-used kinds of data such as files and text strings.

        // Create an S3Object with data from a File
        S3Object fileObject = new S3Object(testBucket, new File("example.properties"));

        // Create an S3Object with data from a String
        S3Object textObject = new S3Object(testBucket, "textObject.txt", "Hello World!");

        /*
         * Downloading data objects 
         */
        
        // To download data from S3 you retrieve an S3Object through the S3Service. 
        // You may retrieve an object in one of two ways, with the data contents or without.

        // If you just want to know some details about an object and you don't need its contents, 
        // it's faster to use the getObjectDetails method. This returns only the object's details, 
        // also known as its 'HEAD'. Head information includes the object's size, date, and other 
        // metadata associated with it such as the Content Type.

        // Retrieve the HEAD of the data object we created previously.
        S3Object objectDetailsOnly = s3Service.getObjectDetails(testBucket, "helloWorld.txt");
        System.out.println("S3Object, details only: " + objectDetailsOnly);

        // If you need the data contents of the object, the getObject method will return all the 
        // object's details and will also set the object's DataInputStream variable from which 
        // the object's data can be read.

        // Retrieve the whole data object we created previously
        S3Object objectComplete = s3Service.getObject(testBucket, "helloWorld.txt");
        System.out.println("S3Object, complete: " + objectComplete);

        // Read the data from the object's DataInputStream using a loop, and print it out.
        System.out.println("Greeting:");
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(objectComplete.getDataInputStream()));
        String data = null;
        while ((data = reader.readLine()) != null) {
            System.out.println(data);
        }

        /*
         * List your buckets and objects 
         */
        
        // Now that you have a bucket and some objects, it's worth listing them. Note that when 
        // you list objects, the objects returned will not include much information compared to 
        // what you get from the getObject and getObjectDetails methods. However, they will 
        // include the size of each object

        // List all your buckets.
        S3Bucket[] buckets = s3Service.listAllBuckets();

        // List the object contents of each bucket.
        for (int b = 0; b < buckets.length; b++) {
            System.out.println("Bucket '" + buckets[b].getName() + "' contains:");
            
            // List the objects in this bucket.
            S3Object[] objects = s3Service.listObjects(buckets[b]);

            // Print out each object's key and size.
            for (int o = 0; o < objects.length; o++) {
                System.out.println(" " + objects[o].getKey() + " (" + objects[o].getContentLength() + " bytes)");
            }
        }

        /*
         * Deleting objects and buckets 
         */
        
        // Objects can be easily deleted. When they are gone they are gone for good so be careful.

        // Buckets may only be deleted when they are empty.

        // If you try to delete your bucket before it is empty, it will fail.
        try {
            // This will fail if the bucket isn't empty.
            s3Service.deleteBucket(testBucket.getName());
        } catch (S3ServiceException e) {
            e.printStackTrace();
        }

        // Delete all the objects in the bucket
        s3Service.deleteObject(testBucket, object.getKey());
        s3Service.deleteObject(testBucket, helloWorldObject.getKey());

        // Now that the bucket is empty, you can delete it.
        s3Service.deleteBucket(testBucket.getName());
        System.out.println("Deleted bucket " + testBucket.getName());
        
        /* ***********************
         * Multi-threaded Examples
         * *********************** 
         */
        
        // The jets3t Toolkit includes a utility service S3ServiceMulti that can perform an 
        // S3 operation on many objects at a time. This service allows you to use more of your 
        // available bandwidth and perform S3 operations much faster. S3ServiceMulti works with 
        // any thread-safe S3Service implementation, such as the HTTP/REST and SOAP 
        // implementations provided with jets3t.

        // The examples below demonstrate how to use some of the multi-threaded 
        // operations provided by S3ServiceMulti.
        
        /*
         * Construct an S3ServiceMulti service
         */        

        // The S3ServiceMulti service is designed for using in graphical applications and 
        // uses an event-notification approach to communicate its results rather than standard 
        // method calls. This means the service can provide progress reports to an application during 
        // long-running operations. However, this approach makes the service a little harder to use.

        // To use the S3ServiceMulti service you construct it with an object that implements the 
        // S3ServiceEventListener interface. This object will then receive event 
        // notifications from the service.

        // The jets3t toolkit includes a basic implementation of this listener in S3ServiceEventAdaptor 
        // that can be used as a starting point. By default, the adaptor provides nothing 
        // more than a means of checking for errors.

        // Use the default S3ServiceEventListener adaptor so we can check for errors.
        S3ServiceEventAdaptor adaptor = new S3ServiceEventAdaptor();

        // Create a multi service with the default adaptor
        S3ServiceMulti s3ServiceMulti = new S3ServiceMulti(s3Service, adaptor);


        /*
         * Upload multiple objects at once 
         */
        
        // To demonstrate multiple uploads, let's create some small text-data objects and a bucket to put them in.

        // First, create a bucket.
        S3Bucket bucket = new S3Bucket(awsAccessKey + ".TestMulti");

        // Create an array of data objects to upload.
        S3Object[] objects = new S3Object[5];
        objects[0] = new S3Object(bucket, "object1.txt", "Hello from object 1");
        objects[1] = new S3Object(bucket, "object2.txt", "Hello from object 2");
        objects[2] = new S3Object(bucket, "object3.txt", "Hello from object 3");
        objects[3] = new S3Object(bucket, "object4.txt", "Hello from object 4");
        objects[4] = new S3Object(bucket, "object5.txt", "Hello from object 5");

        // Now we have some sample objects, we can upload them.

        // Create the bucket in S3.
        bucket = s3Service.createBucket(bucket);

        // Upload multiple objects.
        s3ServiceMulti.putObjects(bucket, objects);
        
        // Check for errors.
        if (adaptor.wasErrorThrown()) {
            throw new Exception("Multi-object upload failed", adaptor.getErrorThrown());
        }
        System.out.println("Uploaded " + objects.length + " objects");

        /*
         * Retrieve the HEAD information of multiple objects 
         */

        // This example demonstrates how to write an S3ServiceEventListener to access useful 
        // information when running the multi-threaded service. To retrieve the HEAD information of 
        // multiple objects, we start with the adaptor used above but extend it to handle 
        // GetObjectHeadsEvent event notifications.

        // Event notifications are based on the ServiceEvent class and every event object has an 
        // Event Code, which will be one of: EVENT_ERROR, EVENT_STARTED, EVENT_COMPLETED, 
        // EVENT_IN_PROGRESS, EVENT_CANCELLED. We are most interested in the EVENT_IN_PROGRESS events, 
        // as these relay all the response objects received from S3.

        // List to store objects with HEAD information.
        final List objectsWithHead = new ArrayList();

        // Extend default adaptor to print information GetObjectHeadsEvent events.
        S3ServiceEventAdaptor headAdaptor = new S3ServiceEventAdaptor() {
            public void s3ServiceEventPerformed(GetObjectHeadsEvent event) {
                // Call the super's method to handle exceptions.
                super.s3ServiceEventPerformed(event);
                
                // In Progress events include S3 responses.
                if (ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                    S3Object[] completedObjects = event.getCompletedObjects();
                    for (int i = 0; i < completedObjects.length; i++) {
                        objectsWithHead.add(completedObjects[i]);
                    }
                }
            }            
        };

        // Create a new multi-threaded service with the new headAdaptor.
        s3ServiceMulti = new S3ServiceMulti(s3Service, headAdaptor);

        // Perform a Details/HEAD query for multiple objects.
        s3ServiceMulti.getObjectsHeads(bucket, objects);

        // Check for errors.
        if (headAdaptor.wasErrorThrown()) {
            throw new Exception("Multi-object Details/HEAD request failed", headAdaptor.getErrorThrown());
        }
        // Print out details about all the objects.
        System.out.println(objectsWithHead);

        /*
         * Delete multiple objects 
         */
        
        // It's time to clean up, so let's get rid of our multiple objects and test bucket.

        // It is possible, though dangerous, to avoid using a S3ServiceEventListener altogether and 
        // just hope everything works out OK. Well, are you feeling lucky, punk!?

        // Create a new multi-threaded service with no listener (it's null, see).
        s3ServiceMulti = new S3ServiceMulti(s3Service, null);

        // Delete multiple objects.
        s3ServiceMulti.deleteObjects(bucket, objects);

        // Without an event listener we have no way of telling whether the delete
        // operation actually worked. However, as we are deleting the bucket and 
        // this will fail if the objects still exist, we will find out soon enough.
        s3Service.deleteBucket(bucket);
        System.out.println("Deleted bucket: " + bucket);

        /* *****************
         * Advanced Examples
         * ***************** 
         */
        
        /*
         * Managing Metadata 
         */
        
        // S3Objects can contain metadata stored as name/value pairs. This metadata is stored in 
        // S3 and can be accessed when an object is retrieved from S3 using getObject 
        // or getObjectDetails methods. To store metadata with an object, add your metadata to 
        // the object prior to uploading it to S3. 
        
        // Note that metadata cannot be updated in S3 without replacing the existing object, 
        // and that metadata names must be strings without spaces.
        
        S3Object objectWithMetadata = new S3Object("metadataObject");
        objectWithMetadata.addMetadata("favourite-colour", "blue");
        objectWithMetadata.addMetadata("document-version", "0.3");
        
        
        /*
         * Save and load encrypted AWS Credentials 
         */
        
        // AWS credentials are your means to login to and manage your S3 account, and should be 
        // kept secure. The jets3t toolkit stores these credentials in AWSCredentials objects. 
        // The AWSCredentials class provides utility methods to allow credentials to be saved to 
        // an encrypted file and loaded from a previously saved file with the right password.
        
        // Save credentials to an encrypted file protected with a password.
        File credFile = new File("awscredentials.enc");
        awsCredentials.save("password", credFile);
        
        // Load encrypted credentials from a file.
        AWSCredentials loadedCredentials = AWSCredentials.load("password", credFile);
        System.out.println("AWS Key loaded from file: " + loadedCredentials.getAccessKey());
        
        // You won't get far if you use the wrong password...
        try {
            loadedCredentials = AWSCredentials.load("wrongPassword", credFile);
        } catch (S3ServiceException e) {
            System.err.println("Cannot load credentials from file with the wrong password!");
        }

        /*
         * Manage Access Control Lists 
         */
        
        // S3 uses Access Control Lists to control who has access to buckets and objects in S3. 
        // By default, any bucket or object you create will belong to you and will not be accessible 
        // to anyone else. You can use jets3t's support for access control lists to make buckets or 
        // objects publicly accessible, or to allow other S3 members to access or manage your objects.

        // The ACL capabilities of S3 are quite involved, so to understand this subject fully please 
        // consult Amazon's documentation. The code examples below show how to put your understanding 
        // of the S3 ACL mechanism into practice.
        
        // ACL settings may be provided with a bucket or object when it is created, or the ACL of 
        // existing items may be updated. Let's start by creating a bucket with default (ie private) 
        // access settings, then making it public.
        
        // Create a bucket in S3.
        S3Bucket publicBucket = new S3Bucket(awsAccessKey + ".publicBucket");
        s3Service.createBucket(publicBucket);
        
        // Retrieve the bucket's ACL and modify it to grant public access, 
        // ie READ access to the ALL_USERS group.
        AccessControlList bucketAcl = s3Service.getBucketAcl(publicBucket);
        bucketAcl.grantPermission(GroupGrantee.ALL_USERS, Permission.PERMISSION_READ);
        
        // Update the bucket's ACL. Now anyone can view the list of objects in this bucket.
        publicBucket.setAcl(bucketAcl);
        s3Service.putBucketAcl(publicBucket);
        System.out.println("View bucket's object listing here: http://s3.amazonaws.com/" 
            + publicBucket.getName());
        
        // Now let's create an object that is public from scratch. Note that we will use the bucket's 
        // public ACL object created above, this works fine. Although it is possible to create an 
        // AccessControlList object from scratch, this is more involved as you need to set the 
        // ACL's Owner information which is only readily available from an existing ACL.
        
        // Create a public object in S3. Anyone can download this object. 
        S3Object publicObject = new S3Object(
            publicBucket, "publicObject.txt", "This object is public");
        publicObject.setAcl(bucketAcl);
        s3Service.putObject(publicBucket, publicObject);        
        System.out.println("View public object contents here: http://s3.amazonaws.com/" 
            + publicBucket.getName() + "/" + publicObject.getKey());

        // The ALL_USERS Group is particularly useful, but there are also other grantee types 
        // that can be used with AccessControlList. Please see Amazon's S3 technical documentation
        // for a fuller discussion of these settings.
        
        AccessControlList acl = new AccessControlList();
        
        // Grant access by email address. Note that this only works email address of AWS S3 members.
        acl.grantPermission(new EmailAddressGrantee("someone@somewhere.com"), 
            Permission.PERMISSION_FULL_CONTROL);
        
        // Grant control of ACL settings to a known AWS S3 member.
        acl.grantPermission(new CanonicalGrantee("AWS member's ID"), 
            Permission.PERMISSION_READ_ACP);
        acl.grantPermission(new CanonicalGrantee("AWS member's ID"), 
            Permission.PERMISSION_WRITE_ACP);
        
     
        /*
         * Temporarily make an Object available to anyone 
         */
        
        // A private object stored in S3 can be made publicy available for a limited time using a 
        // signed URL. The signed URL can be used by anyone to download the object, yet it includes 
        // a date and time after which the URL will no longer work.
        
        // Create a private object in S3.
        S3Bucket privateBucket = new S3Bucket(awsAccessKey + ".privateBucket");
        S3Object privateObject = new S3Object(
            privateBucket, "privateObject.txt", "This object is private");
        s3Service.createBucket(privateBucket);
        s3Service.putObject(privateBucket, privateObject);        
        
        // Determine what the time will be in 5 minutes.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        Date expiryDate = cal.getTime();
        
        // Create a signed HTTP URL valid for 5 minutes.
        boolean isHttpsUrl = true; // Choose whether URL is HTTP or HTTPS.
        String url = S3Service.createSignedUrl(
            privateBucket.getName(), privateObject.getKey(), 
            awsCredentials, expiryDate, isHttpsUrl);
        System.out.println("Signed URL: " + url);
    }
    
}
