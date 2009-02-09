/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2006 James Murty
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.jets3t.samples;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.jets3t.service.Constants;
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
import org.jets3t.service.multithread.DownloadPackage;
import org.jets3t.service.multithread.S3ServiceSimpleMulti;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSDevPayCredentials;
import org.jets3t.service.utils.ServiceUtils;

/**
 * This class includes all the code samples as listed in the JetS3t
 * <a href="http://jets3t.s3.amazonaws.com/toolkit/guide.html">Programmer Guide</a>. 
 * <p>
 * This code is provided as a convenience for those who are reading through the guide and don't want
 * to type out the examples themselves.
 * </p>
 * 
 * @author James Murty
 */
public class CodeSamples {
    
    public static void main(String[] args) throws Exception {
        /* ************
         * Code Samples
         * ************
         */

        /*
         * Connecting to S3
         */
        
        // Your Amazon Web Services (AWS) login credentials are required to manage S3 accounts. 
        // These credentials are stored in an AWSCredentials object:

        AWSCredentials awsCredentials = SamplesUtils.loadAWSCredentials();

        // To communicate with S3, create a class that implements an S3Service. 
        // We will use the REST/HTTP implementation based on HttpClient, as this is the most 
        // robust implementation provided with jets3t.

        S3Service s3Service = new RestS3Service(awsCredentials);

        // A good test to see if your S3Service can connect to S3 is to list all the buckets you own. 
        // If a bucket listing produces no exceptions, all is well.

        S3Bucket[] myBuckets = s3Service.listAllBuckets();
        System.out.println("How many buckets to I have in S3? " + myBuckets.length);

        /*
         * Create a bucket 
         */
        
        // To store data in S3 you must first create a bucket, a container for objects.

        S3Bucket testBucket = s3Service.createBucket("test-bucket");
        System.out.println("Created test bucket: " + testBucket.getName());

        // If you try using a common name, you will probably not be able to create the 
        // bucket as someone else will already have a bucket of that name.
        
        // To create a bucket in an S3 data center located somewhere other than 
        // the United States, you can specify a location for your bucket as a 
        // second parameter to the createBucket() method. Currently, the only 
        // alternative S3 location is Europe (EU).

        S3Bucket euBucket = s3Service.createBucket("eu-bucket", S3Bucket.LOCATION_EUROPE);


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
        // To include data in the object you must provide some data for the object.
        // If you know the Content/Mime type of the data (e.g. text/plain) you should set this too.
        
        // S3Object's can contain any data available from an input stream, but JetS3t provides two
        // convenient object types to hold File or String data. These convenient constructors
        // automatically set the Content-Type and Content-Length of the object.
        
        // Create an S3Object based on a string, with Content-Length set automatically and 
        // Content-Type set to "text/plain"  
        String stringData = "Hello World!";
        S3Object stringObject = new S3Object(testBucket, "HelloWorld.txt", stringData);
        
        // Create an S3Object based on a file, with Content-Length set automatically and 
        // Content-Type set based on the file's extension (using the Mimetypes utility class)
        File fileData = new File("src/org/jets3t/samples/CodeSamples.java");
        S3Object fileObject = new S3Object(testBucket, fileData);

        // If your data isn't a File or String you can use any input stream as a data source,
        // but you must manually set the Content-Length.        

        // Create an object containing a greeting string as input stream data.
        String greeting = "Hello World!";
        S3Object helloWorldObject = new S3Object("HelloWorld2.txt");
        ByteArrayInputStream greetingIS = new ByteArrayInputStream(
            greeting.getBytes(Constants.DEFAULT_ENCODING));
        helloWorldObject.setDataInputStream(greetingIS);
        helloWorldObject.setContentLength(greetingIS.available());
        helloWorldObject.setContentType("text/plain");

        // Upload the data objects.
        s3Service.putObject(testBucket, stringObject);
        s3Service.putObject(testBucket, fileObject);
        s3Service.putObject(testBucket, helloWorldObject);

        // Print details about the uploaded object.
        System.out.println("S3Object with data: " + helloWorldObject);
        
        /*
         * Verifying Uploads
         */
        
        // To be 100% sure that data you have uploaded to S3 has not been
        // corrupted in transit, you can verify that the hash value of the data 
        // S3 received matches the hash value of your original data.
        
        // The easiest way to do this is to specify your data's hash value
        // in the Content-MD5 header before you upload the object. JetS3t will
        // do this for you automatically when you use the File- or String-based 
        // S3Object constructors:
        
        S3Object objectWithHash = new S3Object(testBucket, "HelloWorld.txt", stringData);
        System.out.println("Hash value: " + objectWithHash.getMd5HashAsHex());
        
        // If you do not use these constructors, you should *always* set the
        // Content-MD5 header value yourself before you upload an object.
        // JetS3t provides the ServiceUtils#computeMD5Hash method to calculate
        // the hash value of an input stream or byte array.

        ByteArrayInputStream dataIS = new ByteArrayInputStream(
            "Here is my data".getBytes(Constants.DEFAULT_ENCODING));
        byte[] md5Hash = ServiceUtils.computeMD5Hash(dataIS);
        dataIS.reset();        
                
        stringObject = new S3Object("MyData");
        stringObject.setDataInputStream(dataIS);
        stringObject.setMd5Hash(md5Hash);        

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
         * Verifying Downloads
         */
        
        // To be 100% sure that data you have downloaded from S3 has not been
        // corrupted in transit, you can verify the data by calculating its hash
        // value and comparing this against the hash value returned by S3. 
        
        // JetS3t provides convenient methods for verifying data that has been 
        // downloaded to a File, byte array or InputStream.
        
        S3Object downloadedObject = s3Service.getObject(testBucket, "helloWorld.txt");
        String textData = ServiceUtils.readInputStreamToString(
            downloadedObject.getDataInputStream(), "UTF-8");
        boolean valid = downloadedObject.verifyData(textData.getBytes("UTF-8"));
        System.out.println("Object verified? " + valid);

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
        
        // When listing the objects in a bucket you can filter which objects to return based on
        // the names of those objects. This is useful when you are only interested in some 
        // specific objects in a bucket and you don't need to list all the bucket's contents.
        
        // List only objects whose keys match a prefix. 
        String prefix = "Reports";
        String delimiter = null; // Refer to the S3 guide for more information on delimiters
        S3Object[] filteredObjects = s3Service.listObjects(testBucket, prefix, delimiter);        
        
        /*
         * Copying objects
         */
        
        // Objects can be copied within the same bucket and between buckets.
        
        // Create a target S3Object
        S3Object targetObject = new S3Object("targetObjectWithSourcesMetadata");
        
        // Copy an existing source object to the target S3Object
        // This will copy the source's object data and metadata to the target object.
        boolean replaceMetadata = false;
        s3Service.copyObject("test-bucket", "HelloWorld.txt", "destination-bucket", targetObject, replaceMetadata);

        // You can also copy an object and update its metadata at the same time. Perform a 
        // copy-in-place  (with the same bucket and object names for source and destination) 
        // to update an object's metadata while leaving the object's data unchanged.
        targetObject = new S3Object("HelloWorld.txt");
        targetObject.addMetadata(S3Object.METADATA_HEADER_CONTENT_TYPE, "text/html");        
        replaceMetadata = true;
        s3Service.copyObject("test-bucket", "HelloWorld.txt", "test-bucket", targetObject, replaceMetadata);

        /*
         * Moving and Renaming objects
         */
        
        // Objects can be moved within a bucket (to a different name) or to another S3 
        // bucket in the same region (eg US or EU). 
        // A move operation is composed of a copy then a delete operation behind the scenes. 
        // If the initial copy operation fails, the object is not deleted. If the final delete
        // operation fails, the object will exist in both the source and destination locations.

        // Here is a command that moves an object from one bucket to another.
        s3Service.moveObject("test-bucket", "HelloWorld.txt", "destination-bucket", targetObject, false);
        
        // You can move an object to a new name in the same bucket. This is essentially a rename operation.
        s3Service.moveObject("test-bucket", "HelloWorld.txt", "test-bucket", new S3Object("NewName.txt"), false);

        // To make renaming easier, JetS3t has a shortcut method especially for this purpose. 
        s3Service.renameObject("test-bucket", "HelloWorld.txt", targetObject);        
        
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
        
        // The jets3t Toolkit includes utility services, S3ServiceMulti and S3ServiceSimpleMulti, that 
        // can perform an S3 operation on many objects at a time. These services allow you to use more 
        // of your available bandwidth and perform S3 operations much faster. They work with any 
        // thread-safe S3Service implementation, such as the HTTP/REST and SOAP implementations
        // provided with jets3t.

        // The S3ServiceMulti service is intended for advanced developers. It is designed for use in  
        // graphical applications and uses an event-notification approach to communicate its results  
        // rather than standard method calls. This means the service can provide progress reports to  
        // an application during long-running operations. However, this approach makes the service 
        // complicated to use. See the code for the Cockpit application to see how this service is used 
        // to display progress updates.

        // The S3ServiceSimpleMulti is a service that wraps around S3ServiceMulti and provides a 
        // simplified interface, so developers can take advantage of multi-threading without any extra work.

        // The examples below demonstrate how to use some of the multi-threaded operations provided by 
        // S3ServiceSimpleMulti.
        
        /*
         * Construct an S3ServiceSimpleMulti service 
         */       

        // To use the S3ServiceSimpleMulti service you construct it by providing an existing 
        // S3Service object.

        // Create a simple multi-threading service based on our existing S3Service
        S3ServiceSimpleMulti simpleMulti = new S3ServiceSimpleMulti(s3Service);
        
        /*
         * Upload multiple objects at once 
         */
        
        // To demonstrate multiple uploads, let's create some small text-data objects and a bucket to put them in.

        // First, create a bucket.
        S3Bucket bucket = new S3Bucket(awsCredentials.getAccessKey() + ".TestMulti");
        bucket = s3Service.createBucket(bucket);

        // Create an array of data objects to upload.
        S3Object[] objects = new S3Object[5];
        objects[0] = new S3Object(bucket, "object1.txt", "Hello from object 1");
        objects[1] = new S3Object(bucket, "object2.txt", "Hello from object 2");
        objects[2] = new S3Object(bucket, "object3.txt", "Hello from object 3");
        objects[3] = new S3Object(bucket, "object4.txt", "Hello from object 4");
        objects[4] = new S3Object(bucket, "object5.txt", "Hello from object 5");

        // Now we have some sample objects, we can upload them.

        // Upload multiple objects.
        S3Object[] createdObjects = simpleMulti.putObjects(bucket, objects);        
        System.out.println("Uploaded " + createdObjects.length + " objects");

        /*
         * Retrieve the HEAD information of multiple objects 
         */

        // Perform a Details/HEAD query for multiple objects.
        S3Object[] objectsWithHeadDetails = simpleMulti.getObjectsHeads(bucket, objects);

        // Print out details about all the objects.
        System.out.println("Objects with HEAD Details...");
        for (int i = 0; i < objectsWithHeadDetails.length; i++) {
            System.out.println(objectsWithHeadDetails[i]);
        }

        /*
         * Download objects to local files 
         */

        // The multi-threading services provide a method to download multiple objects at a time, but 
        // to use this you must first prepare somewhere to put the data associated with each object. 
        // The most obvious place to put this data is into a file, so let's go through an example of 
        // downloading object data into files.

        // To download our objects into files we first must create a DownloadPackage class for  
        // each object. This class is a simple container which merely associates an object with a  
        // file, to which the object's data will be written.
        
        // Create a DownloadPackage for each object, to associate the object with an output file.
        DownloadPackage[] downloadPackages = new DownloadPackage[5];
        downloadPackages[0] = new DownloadPackage(objects[0], 
            new File(objects[0].getKey()));
        downloadPackages[1] = new DownloadPackage(objects[1], 
            new File(objects[1].getKey()));
        downloadPackages[2] = new DownloadPackage(objects[2], 
            new File(objects[2].getKey()));
        downloadPackages[3] = new DownloadPackage(objects[3], 
            new File(objects[3].getKey()));
        downloadPackages[4] = new DownloadPackage(objects[4], 
            new File(objects[4].getKey()));
        
        // Download the objects.
        simpleMulti.downloadObjects(bucket, downloadPackages);
        System.out.println("Downloaded objects to current working directory");
        
        /*
         * Delete multiple objects 
         */
        
        // It's time to clean up, so let's get rid of our multiple objects and test bucket.

        // Delete multiple objects, then the bucket too.
        simpleMulti.deleteObjects(bucket, objects);
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
        // existing items may be updated. Let's start by creating a bucket with default (i.e. private) 
        // access settings, then making it public.
        
        // Create a bucket in S3.
        S3Bucket publicBucket = new S3Bucket(awsCredentials.getAccessKey() + ".publicBucket");
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
        
        // A private object stored in S3 can be made publicly available for a limited time using a 
        // signed URL. The signed URL can be used by anyone to download the object, yet it includes 
        // a date and time after which the URL will no longer work.
        
        // Create a private object in S3.
        S3Bucket privateBucket = new S3Bucket(awsCredentials.getAccessKey() + ".privateBucket");
        S3Object privateObject = new S3Object(
            privateBucket, "privateObject.txt", "This object is private");
        s3Service.createBucket(privateBucket);
        s3Service.putObject(privateBucket, privateObject);        
        
        // Determine what the time will be in 5 minutes.
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        Date expiryDate = cal.getTime();
        
        // Create a signed HTTP GET URL valid for 5 minutes.
        // If you use the generated URL in a web browser within 5 minutes, you will be able to view 
        // the object's contents. After 5 minutes, the URL will no longer work and you will only 
        // see an Access Denied message.
        String signedUrl = S3Service.createSignedGetUrl(privateBucket.getName(), privateObject.getKey(), 
            awsCredentials, expiryDate, false);
        System.out.println("Signed URL: " + signedUrl);
        
        
        /*
         * Create an S3 POST form   
         */
        
        // When you create and S3 POST form, anyone who accesses that form in
        // a web browser will be able to upload files to S3 directly from the
        // browser, without needing S3-compatible client software.
        // Refer to the S3 documentation for more information:
        // http://docs.amazonwebservices.com/AmazonS3/2006-03-01/UsingHTTPPOST.html
        
        // We will start by creating a POST form with no policy document, 
        // meaning that the form will have no expiration date or usage 
        // conditions. This form will only work if the target bucket has 
        // public write access enabled.
        
        String unrestrictedForm = 
            S3Service.buildPostForm("public-bucket", "${filename}");
        
        // To use this form, save it in a UTF-8 encoded HTML page (ie with
        // the meta tag 
        // <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />)
        // and load the page in a web browser.
        
        
        // We will now create a POST form with a range of policy conditions, 
        // that will allow users to upload image files to a protected bucket.
        String bucketName = "test-bucket";
        String key = "uploads/images/pic.jpg";
        
        // Specify input fields to set the access permissions and content type 
        // of the object created by the form. We will also redirect the user to
        // another web site after they have successfully uploaded a file.
        String[] inputFields = new String[] {
            "<input type=\"hidden\" name=\"acl\" value=\"public-read\">",
            "<input type=\"hidden\" name=\"Content-Type\" value=\"image/jpeg\">",
            "<input type=\"hidden\" name=\"success_action_redirect\" value=\"http://localhost/post_upload\">"
        };

        // We then specify policy conditions for at least the mandatory 
        // 'bucket' and 'key' fields that will be included in the POST request.
        // In addition to the mandatory fields, we will add a condition to 
        // control the size of the file the user can upload. 
        // Note that our list of conditions must include a condition 
        // corresponding to each of the additional input fields we specified above.
        String[] conditions = {
            S3Service.generatePostPolicyCondition_Equality("bucket", bucketName),
            S3Service.generatePostPolicyCondition_Equality("key", key),
            S3Service.generatePostPolicyCondition_Range(10240, 204800),
            // Conditions to allow the additional fields specified above
            S3Service.generatePostPolicyCondition_Equality("acl", "public-read"),
            S3Service.generatePostPolicyCondition_Equality("Content-Type", "image/jpeg"),
            S3Service.generatePostPolicyCondition_Equality("success_action_redirect", "http://localhost/post_upload")
        };

        // Form will expire in 24 hours
        cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 24);
        Date expiration = cal.getTime();
        
        // Generate the form.
        String restrictedForm = S3Service.buildPostForm(
            bucketName, key, awsCredentials, expiration, conditions, 
            inputFields, null, true);
        
        /*
         * Activate Requester Pays for a bucket.
         */
        
        // A bucket in S3 is normally configured such that the bucket's owner
        // pays all the service fees for accessing, sharing and storing objects.
        // The Requester Pays feature of S3 allows a bucket to be configured 
        // such that the individual who sends requests to a bucket is charged
        // the S3 request and data transfer fees, instead of the bucket's owner.

        // Note: Only the S3 REST API supports the Requester Pays feature, so 
        // you must use a RestS3Service for all the operations described below.
        // The SoapS3Service will not work.

        // Set a bucket to be Requester Pays 
        s3Service.setRequesterPaysBucket(bucketName, true);

        // Set a bucket to be Owner pays (the default value for S3 buckets)
        s3Service.setRequesterPaysBucket(bucketName, false);

        // Find out whether a bucket is configured as Requester pays
        s3Service.isRequesterPaysBucket(bucketName);
                
        /*
         * Access a Requester Pays bucket when you are not the bucket's owner
         */
        
        // When a bucket is configured as Requester Pays, other AWS users can
        // upload objects to the bucket or retrieve them provided the user:
        // - has the necessary Access Control List permissions, and
        // - indicates that he/she is willing to pay the Requester Pays fees,
        //   by including a special flag in the request.
        
        // Indicate that you will accept any Requester Pays fees by setting
        // the RequesterPaysEnabled flag to true in your RestS3Service class.
        // You can then use the service to list, upload, or download objects as 
        // normal.
        // Support for Requester Pays buckets is disabled by default in JetS3t 
        // with the jets3t.properties setting 
        // 'httpclient.requester-pays-buckets-enabled=false'
        s3Service.setRequesterPaysEnabled(true);
        
        /*
         * Generate a Signed URL for a Requester Pays bucket
         */
        
        // Third party users of a Requester Pays bucket can generate Signed 
        // URLs that permit public access to objects. To generate such a URL, 
        // these users call the S3Service#createSignedUrl method with a flag to
        // indicate that the he/she is willing to pay the Requester Pays fees
        // incurred by the use of the signed URL.
        
        // Generate a signed GET URL for
        Map httpHeaders = null;
        long expirySecsAfterEpoch = System.currentTimeMillis() / 1000 + 300;
        boolean isVirtualHost = false;
        boolean isHttpsUrl = false;
        
        String requesterPaysSignedGetUrl = 
            S3Service.createSignedUrl("GET", bucketName, "object-name", 
                Constants.REQUESTER_PAYS_BUCKET_FLAG, // Include Requester Pays flag  
                httpHeaders,
                awsCredentials, expirySecsAfterEpoch, 
                isVirtualHost, isHttpsUrl);
        
        /*
         * Accessing Amazon DevPay S3 accounts
         */
        
        // Amazon's DevPay service allows vendors to sell user-pays S3 accounts.
        // To access the S3 portions of a DevPay product, JetS3t needs
        // additional credentials that include the DevPay User Token, and the
        // DevPay Product Token.
        
        AWSDevPayCredentials devPayCredentials = new AWSDevPayCredentials(
            "YOUR_AWS_ACCESSS_KEY", "YOUR_AWS_SECRET_KEY",
            "DEVPAY_USER_TOKEN", "DEVPAY_PRODUCT_TOKEN");
        
        // Once you have defined your DevPay S3 credentials, you can create an
        // S3Service class based on these and access the DevPay account as usual.
        S3Service devPayService = new RestS3Service(devPayCredentials);
        devPayService.listAllBuckets();
        
        // You can also generate signed URLs for DevPay S3 accounts. Here is the
        // code to generate a linke that makes an object in a DevPay account 
        // temporary available for public download.
        
        cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        
        String signedDevPayUrl = S3Service.createSignedGetUrl(
            "devpay-bucket-name", "devpay-object-name", 
            devPayCredentials, cal.getTime());
        
    }
    
}
