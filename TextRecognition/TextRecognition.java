package com.ks.ec2B;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import java.util.*;
import java.io.*;

public class TextRecognition {

 public static void main(String[] args) {

     String bucket = "cs643-sp25-project1";
     String queue = "MySQS.fifo"; 

     S3Client s3 = S3Client.builder()
             .region(Region.US_EAST_1)
             .build();
     RekognitionClient rekog = RekognitionClient.builder()
             .region(Region.US_EAST_1)
             .build();
     SqsClient SQS = SqsClient.builder()
             .region(Region.US_EAST_1)
             .build();

     processbucketImages(s3, rekog, SQS, bucket, queue);
 }

 public static void processbucketImages(S3Client s3, RekognitionClient rekog, SqsClient SQS, String bucket,
                                     String queue) {

     boolean QExists = false;
     while (!QExists) {
         ListQueuesRequest ReqQList = ListQueuesRequest.builder()
                 .queueNamePrefix(queue)
                 .build();
         ListQueuesResponse ResQList = SQS.listQueues(ReqQList);
         if (ResQList.queueUrls().size() > 0)
             QExists = true;
     }

     String Url = "https://sqs.us-east-1.amazonaws.com/021511551425/MySQS.fifo";
     try {
         GetQueueUrlRequest getReqQ = GetQueueUrlRequest.builder()
                 .queueName(queue)
                 .build();
         Url = SQS.getQueueUrl(getReqQ)
                 .queueUrl();
     } catch (QueueNameExistsException e) {
         throw e;
     }

     try {
         boolean endOfQ = false;
         HashMap<String, String> outputs = new HashMap<String, String>();

         while (!endOfQ) {
             ReceiveMessageRequest MsgReqRx = ReceiveMessageRequest.builder().queueUrl(Url)
                     .maxNumberOfMessages(1).build();
             List<Message> messages = SQS.receiveMessage(MsgReqRx).messages();

             if (messages.size() > 0) {
                 Message message = messages.get(0);
                 String label = message.body();

                 if (label.equals("-1")) {
                     endOfQ = true;
                 } else {
                     System.out.println("Processing image from the S3 bucket <cs643-sp25-project1> : " + label);

                     Image img = Image.builder().s3Object(S3Object.builder().bucket(bucket).name(label).build())
                             .build();
                     DetectTextRequest request = DetectTextRequest.builder()
                             .image(img)
                             .build();
                     DetectTextResponse result = rekog.detectText(request);
                     List<TextDetection> textDetections = result.textDetections();

                     if (textDetections.size() != 0) {
                         String text = "";
                         for (TextDetection textDetection : textDetections) {
                             if (textDetection.type().equals(TextTypes.WORD))
                                 text = text.concat(" " + textDetection.detectedText());
                         }
                         outputs.put(label, text);
                     }
                 }

                 DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(Url)
                         .receiptHandle(message.receiptHandle())
                         .build();
                 SQS.deleteMessage(deleteMessageRequest);
             }
         }
         try {
             FileWriter writer = new FileWriter("output.txt");

             Iterator<Map.Entry<String, String>> it = outputs.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry<String, String> pair = it.next();
                 writer.write(pair.getKey() + ":" + pair.getValue() + "\n");
                 it.remove();
             }

             writer.close();
             System.out.println("Results --> output.txt");
         } catch (IOException e) {
             System.out.println("An error occurred copying to file.");
             e.printStackTrace();
         }
     } catch (Exception e) {
         System.err.println(e.getLocalizedMessage());
         System.exit(1);
     }
 }
}
