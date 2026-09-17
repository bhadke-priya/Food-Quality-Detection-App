# Food Quality Detection App

## Overview
The Food Quality Detection App is an Android application designed to empower users to make informed dietary choices by providing tools for food safety and nutritional analysis.

## What I Built
- **Barcode Scanning & Nutrient Analysis**: Enables users to scan food product barcodes to retrieve and display nutritional information, ensuring transparency in food choices.
- **FSSAI License Verification**: Validates the FSSAI license of scanned products, ensuring compliance with food safety standards in India.
- **Scan History Tracking**: Stores users' scan history in Firebase Realtime Database, allowing easy access to previously scanned products.
- **Personalized Health Report & Progress Graph**: Collects user demographic data (age, gender, height, weight, activity level) and consumption frequency, analyzes weekly nutrient intake against WHO guidelines, generates health reports with health scores and recommendations, and visualizes health score trends in a dynamic graph.

## How I Built It
- **Tech Stack**:
  - **Kotlin & XML Layouts**: Used Kotlin for robust Android development and XML for designing intuitive UI layouts, leveraging Android View Binding for efficient view interactions.
  - **Firebase Realtime Database & Authentication**: Integrated Firebase to securely store user data (scan history, health reports) and manage user authentication, ensuring data persistence and security.
  - **Google Gemini API**: Utilized the free Google Gemini API to analyze nutrient intake against WHO guidelines, generating personalized health reports with insights on potential health risks.
  - **MPAndroidChart**: Implemented a line chart in a fragment to visualize weekly/monthly health score trends, enhancing user engagement with clear data visualization.
  - **Gradle Kotlin DSL**: Configured the build system with Kotlin DSL, integrating dependencies like MPAndroidChart (`com.github.PhilJay:MPAndroidChart:v3.1.0`), Firebase (`firebase-auth:23.0.0`, `firebase-database:21.0.0`), and Android libraries.

- **Implementation Details**:
  - Designed a modular architecture with fragments for each feature, ensuring maintainability and scalability.
  - Developed the health report feature to fetch scan history and user inputs from Firebase, process them via Gemini API, and store results (health score, recommendations) back in Firebase.
  - Created a `HealthProgressFragment` using MPAndroidChart to display a smooth, interactive line chart of health scores, with timestamps formatted as dates for the x-axis.
  - Ensured robust error handling and data validation, such as handling failed Firebase queries or missing health report data, to enhance app reliability.

## Impact
- **User Empowerment**: Equips users with tools to verify food safety and understand nutritional content, fostering informed dietary decisions.
- **Health Awareness**: Provides personalized health reports and visualizations, enabling users to track and improve their nutritional intake against global standards.
- **Scalability & Accessibility**: Offers a scalable solution with cloud-based storage and free API integration, making it accessible to a wide audience.
- **Proactive Health Monitoring**: Encourages users to monitor their health progress over time, promoting long-term wellness and adherence to healthy eating habits.

## Usage
- **Barcode Scanning**: Use the camera to scan food product barcodes and view nutritional details.
- **FSSAI Check**: Verify the FSSAI license of scanned products for safety compliance.
- **Scan History**: Access past scans stored in Firebase.
- **Health Report**: Input demographic data and consumption frequency to generate a personalized health report and view progress in a graph.

## Future Enhancements
- Add support for offline mode to cache scan history and reports.
- Integrate machine learning for predictive health risk analysis.
- Expand barcode database to include more regional products.
