package com.ks.ec2A;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.*;

public class FaceRecognition{

  public static void main(String[] args) {

      String bucket = "cs643-sp25-project1";
      String queue = "MySQS.fifo"; 
      String group = "A";

      S3Client s3 = S3Client.builder()
              .region(Region.US_EAST_1)
              .build();
      RekognitionClient rekog = RekognitionClient.builder()
              .region(Region.US_EAST_1)
              .build();
      SqsClient SQS = SqsClient.builder()
              .region(Region.US_EAST_1)
              .build();

      processBucketImages(s3, rekog, SQS, bucket, queue, group);
  }

  public static void processBucketImages(S3Client s3, RekognitionClient rekog, SqsClient SQS, String bucket,
                                         String queue, String group) {

      String Url = "https://sqs.us-east-1.amazonaws.com/021511551425/MySQS.fifo";
      try {
          ListQueuesRequest QueReq = ListQueuesRequest.builder()
                  .queueNamePrefix(queue)
                  .build();
          ListQueuesResponse QueRes = SQS.listQueues(QueReq);

          if (QueRes.queueUrls().size() == 0) {
              CreateQueueRequest request = CreateQueueRequest.builder()
                      .attributesWithStrings(Map.of("FifoQueue", "true", "ContentBasedDeduplication", "true"))
                      .queueName(queue)
                      .build();
              SQS.createQueue(request);

              GetQueueUrlRequest getURLQue = GetQueueUrlRequest.builder()
                      .queueName(queue)
                      .build();
              Url = SQS.getQueueUrl(getURLQue).queueUrl();
          } else {
              Url = QueRes.queueUrls().get(0);
          }
      } catch (QueueNameExistsException e) {
          throw e;
      }

      try {
          ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder().bucket(bucket).maxKeys(10)
                  .build();
          ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);

          for (S3Object obj : listObjResponse.contents()) {
              System.out.println("Gathered image: " + obj.key());

              Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object
                              .builder().bucket(bucket).name(obj.key()).build())
                      .build();
              DetectLabelsRequest request = DetectLabelsRequest.builder().image(img).minConfidence((float) 75)
                      .build();
              DetectLabelsResponse result = rekog.detectLabels(request);
              List<Label> labels = result.labels();

              for (Label label : labels) {
                  if (label.name().equals("Face")) {
                      SQS.sendMessage(SendMessageRequest.builder().messageGroupId(group).queueUrl(Url)
                              .messageBody(obj.key()).build());
                      break;
                  }
              }
          }

          SQS.sendMessage(SendMessageRequest.builder().queueUrl(Url).messageGroupId(group).messageBody("-1")
                  .build());
      } catch (Exception e) {
          System.err.println(e.getLocalizedMessage());
          System.exit(1);
      }
  }
}

