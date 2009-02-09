/*
 * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
 * This is a java.net project, see https://jets3t.dev.java.net/
 * 
 * Copyright 2008 James Murty
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

import org.jets3t.service.CloudFrontService;
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.model.cloudfront.DistributionConfig;

/**
 * Sample code for performing CloudFront service operations.
 */
public class CloudFrontSamples {

    public static void main(String[] args) throws Exception {
        // Construct a CloudFrontService object to interact with the service.
        CloudFrontService cloudFrontService = new CloudFrontService(
            SamplesUtils.loadAWSCredentials());
        
        // List your distributions
        Distribution[] distributions = cloudFrontService.listDistributions();
        for (int i = 0; i < distributions.length; i++) {
            System.out.println("Distribution " + (i + 1) + ": " + distributions[i]);
        }
        
        /*
        // List the distributions applied to a given S3 bucket
        Distribution[] bucketDistributions = cloudFrontService.listDistributions("jets3t");
        for (int i = 0; i < bucketDistributions.length; i++) {
            System.out.println("Bucket distribution " + (i + 1) + ": " + bucketDistributions[i]);
        }
        
        // Create a new distribution 
        String originBucket = "jets3t.s3.amazonaws.com";
        Distribution newDistribution = cloudFrontService.createDistribution(
            originBucket, 
            "" + System.currentTimeMillis(), // Caller reference - a unique string value
            new String[] {"test1.jamesmurty.com"}, // CNAME aliases for distribution
            "Testing", // Comment
            true  // Enabled?
            );
        System.out.println("New Distribution: " + newDistribution);
        
        // The ID of the new distribution we will use for testing
        String testDistributionId = newDistribution.getId(); 
        
        // List information about a distribution
        Distribution distribution = cloudFrontService.getDistributionInfo(testDistributionId);
        System.out.println("Distribution: " + distribution);

        // List configuration information about a distribution
        DistributionConfig distributionConfig = cloudFrontService.getDistributionConfig(testDistributionId);
        System.out.println("Distribution Config: " + distributionConfig);

        // Update a distribution's configuration, in this case to add an extra CNAME alias.
        DistributionConfig updatedDistributionConfig = cloudFrontService.updateDistributionConfig(
            testDistributionId, 
            new String[] {"test1.jamesmurty.com", "test2.jamesmurty.com"}, // CNAME aliases for distribution
            "Another comment for testing", // Comment 
            true // Enabled?
            );
        System.out.println("Updated Distribution Config: " + updatedDistributionConfig);

        // Disable a distribution, e.g. so that it may be deleted. This operation may take some time to complete...
        DistributionConfig disabledDistributionConfig = cloudFrontService.updateDistributionConfig(
            testDistributionId, new String[] {}, "Deleting distribution", false);
        System.out.println("Disabled Distribution Config: " + disabledDistributionConfig);

        // Check whether a distribution is deployed
        Distribution distribution = cloudFrontService.getDistributionInfo(testDistributionId);
        System.out.println("Distribution is deployed? " + distribution.isDeployed());

        // Delete a distribution (the distribution must be disabled and deployed first)
        cloudFrontService.deleteDistribution(testDistributionId);
        */
        
    }
    
}
