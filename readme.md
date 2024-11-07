# Twitter Data Analysis Using MongoDB & Groovy

## 👥 Contributors
- Mengqi Liu  
- Bhavya Bhatt  
- Pritesh Patel  

## 📄 Overview
This project implements a prototype system that processes a JSON dataset locally using Groovy and remotely using MongoDB Atlas. It demonstrates the use of cloud automation and NoSQL data processing to extract business intelligence through both standalone and cloud-based approaches.

The main goal is to evaluate both implementations along a chosen dimension: **scalability, consistency, or data evolution**, highlighting key trade-offs and benefits.

## 🚀 How to Use

1. Clone the project and open it in your preferred IDE.
2. Run Groovy scripts located in the `groovy/` directory to perform local data operations (Exercise 2).
3. Run MongoDB-based cloud integration code in `mongodb/` directory to perform remote queries (Exercise 3).
4. Review the analysis and comparison in the markdown documents for Exercises 4 and 5.

## 📁 Project Structure

```
group12_miniproject/
├── groovy/              # Groovy-based standalone solution
├── mongodb/             # Cloud-based solution using MongoDB Atlas
├── dataset/             # JSON dataset used in both implementations
├── exercises.md         # Answers to all exercises
├── slides/              # Presentation slides with annotated usernames
└── README.md            # This file
```

## 📊 Exercises Summary

- **Exercise 1**: Dataset selection and query planning
- **Exercise 2**: Groovy-based local query processing
- **Exercise 3**: Cloud-based MongoDB implementation
- **Exercise 4**: Comparison of scalability/consistency/evolution
- **Exercise 5**: In-depth validation using additional metrics or datasets
- **Exercise 6**: Project management documentation and contribution tracking

## 🛠 Technologies Used
- Groovy
- MongoDB Atlas (Cloud NoSQL Database)
- Java MongoDB Driver
- Gradle

## 🧪 How to Run Tests
- Ensure Java and Groovy are installed.
- Navigate into the relevant folder and run:
  ```bash
  groovy <scriptName>.groovy
  ```

## 🧾 Notes
- Before zipping the final submission, run:
  ```bash
  ./gradlew clean
  ```
  This removes binaries to reduce zip size.

## 📬 License
This project is part of a university coursework submission and intended for educational use only.
