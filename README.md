
# 🧠 AWS Image Recognition Pipeline Project

![AWS](https://img.shields.io/badge/AWS-Cloud-orange?logo=amazon-aws&style=flat-square)
![Java](https://img.shields.io/badge/Java-SDK-blue?logo=java&style=flat-square)
![Textract](https://img.shields.io/badge/AWS%20Textract-OCR-lightgrey?style=flat-square)
![Rekognition](https://img.shields.io/badge/AWS%20Rekognition-Face%20Detection-green?style=flat-square)

This project demonstrates a complete pipeline using **AWS services** such as EC2, S3, SQS, Rekognition, and Textract to implement an **image recognition system**. The system processes a batch of images to detect faces and extract text, coordinating tasks between two EC2 instances.

📘 **Full Guide**: [Click here to view the detailed project walkthrough (PDF)](https://github.com/KDShetty11/AWS_Imagerecog/blob/main/AWS%20Image%20Recognition%20guide.pdf)

---

## 🔧 Technologies Used

- Amazon EC2 (Compute Instances)
- Amazon S3 (Image Storage)
- Amazon SQS (Queue Messaging)
- Amazon Rekognition (Face Detection)
- Amazon Textract (Text Extraction)
- Java (SDK with Maven)

---

## 🗂️ Project Structure

```'

├── FaceRecognition
│   └── FaceRecognition.java
│   └── pom.xml (With AWS SDK Dependencies)
├── TextRecognition
│   └── TextRecognition.java
│   └── pom.xml (With AWS SDK Dependencies)
├── FaceRecognition.jar
├── AWS Image Recognition guide.pdf
└── TextRecognition.jar

````

---

## 🚀 How It Works

1. **Face Detection**
   - Reads 10 images from an S3 bucket (`cs643-sp25-project1`)
   - Uses **AWS Rekognition** to detect faces with >75% confidence
   - Sends matching image indexes to **SQS**
   - Sends `-1` to indicate end of processing

2. **Text Recognition**
   - Continuously reads from **SQS**
   - Fetches corresponding images from S3
   - Uses **AWS Textract** to extract text
   - Stops at `-1` and writes output to `output.txt`

---

## 🧪 How to Run

### ✅ Prerequisites

- AWS CLI access with required IAM role
- EC2 Key pair downloaded (.pem or .ppk)
- Java SDK & Maven

### 💻 Setup & Execution

1. Launch two EC2 instances: `ec2A`, `ec2B` (Amazon Linux)
2. Set up **Security Groups** (open ports 22, 80, 443)
3. Configure **AWS CLI** with session credentials
4. Compile Java code into JARs using Maven (`mvn clean package`)
5. Transfer `.jar` files to instances using `scp` or `WinSCP`
6. Run:
   ```bash
   java -jar FaceRecognition.jar    # on ec2A
   java -jar TextRecognition.jar    # on ec2B

7. Check `output.txt` on ec2B for final result.

---

## 🎥 Demo

Watch the complete project demonstration here:
📺 **[AWS Image Recognition Pipeline Project Demo](https://youtu.be/-YelKeHgjAg)**

---

## 📎 Notes

* The `.jar` files included are pre-built with all dependencies.
* Update AWS credentials each session in `~/.aws/credentials`.
* Uses FIFO SQS queue with content-based deduplication.

---

## 👤 Author

**Kurudunje Deekshith Shetty**
*21/02/2025*

---

## 📄 License

This project is for educational purposes under the **AWS Academy** curriculum.
Refer to the [PDF Guide](https://github.com/KDShetty11/AWS_Imagerecog/blob/main/AWS%20Image%20Recognition%20guide.pdf) for full details and implementation steps.




