# 🚑 MediBridge - AI-Powered Emergency Medical Triage System

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gemini AI](https://img.shields.io/badge/Gemini%20AI-Google%20LLM-orange.svg)](https://ai.google.dev/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## 📌 Problem Statement

**PromptWars Hackathon Challenge:**
Build a Gemini-powered App that acts as a **universal bridge between human intent and complex systems**, taking unstructured, messy real-world inputs (voice, text, images) and instantly converting them into **structured, verified, life-saving actions**.

## 🎯 Solution

**MediBridge** is an AI-powered emergency triage system that:
- 🎤 Accepts **messy voice/text inputs** describing medical emergencies
- 🧠 Uses **Google Gemini AI** to analyze symptoms in real-time
- 📊 Generates **structured medical assessment** with severity scoring
- 🚨 Provides **life-saving recommendations** and first aid instructions
- 📞 Enables **one-click emergency contact** for critical cases

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🎙️ **Voice Input** | Speech recognition for hands-free emergency reporting |
| 📝 **Text Input** | Manual symptom description with natural language |
| 🤖 **AI Analysis** | Google Gemini 1.5 Flash for rapid medical assessment |
| 📊 **Severity Scoring** | 1-10 scale with color-coded indicators |
| 🏥 **Action Recommendations** | Call ambulance, ER, doctor visit, or home care |
| 🩺 **First Aid Instructions** | Immediate care steps for the situation |
| 🚨 **Emergency Alert** | Critical cases trigger visual alerts and call button |
| ⚡ **Fast Response** | Average response time < 2 seconds |
| 📱 **Mobile Responsive** | Works seamlessly on all devices |
| ♿ **Accessible** | WCAG compliant, screen reader support |

## 🏗️ Architecture

- **Backend:** Java 17, Spring Boot 3.2
- **AI Integration:** Google Gemini 2.0 Flash API (via direct REST/HttpClient)
- **Deployment:** Docker & Google Cloud Run

## 🚀 Getting Started

### Prerequisites
- Java 17+ installed
- Maven
- A [Google Gemini API Key](https://aistudio.google.com/apikey)

### Running Locally

1. Export your Gemini API key as an environment variable:
   ```bash
   export GEMINI_API_KEY="your_api_key_here"
   ```
2. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
3. The server will start on `http://localhost:8080`. Send a POST request to `/api/triage`:
   ```bash
   curl -X POST http://localhost:8080/api/triage \
     -H "Content-Type: application/json" \
     -d '{"voiceText": "I have severe chest pain and cannot breathe."}'
   ```

## ☁️ Deployment (Google Cloud Run)

The repo includes a **multi-stage `Dockerfile`** (Java 17) and listens on **`PORT`** as required by Cloud Run.

### One-time setup

1. Install and log in: [Google Cloud SDK](https://cloud.google.com/sdk/docs/install), then:
   ```bash
   gcloud auth login
   gcloud config set project YOUR_GCP_PROJECT_ID
   ```
2. Enable billing on the project (Cloud Run needs it).

### Deploy from project root

**Option A — script (passes `GEMINI_API_KEY` from your shell):**

```bash
chmod +x scripts/deploy-cloud-run.sh
export GCP_PROJECT="your-gcp-project-id"
export GEMINI_API_KEY="your_gemini_api_key"
./scripts/deploy-cloud-run.sh
```

**Option B — single `gcloud` command:**

```bash
gcloud run deploy medibridge \
  --source . \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="GEMINI_API_KEY=YOUR_GEMINI_KEY"
```

Optional: override the model with `--set-env-vars="GEMINI_API_KEY=...,GEMINI_MODEL=gemini-2.0-flash"`.

For production, store the key in **Secret Manager** and use `--set-secrets=GEMINI_API_KEY=gemini-api-key:latest` instead of plain env vars.

After deploy, open the printed **Service URL**; the UI is served from `/` and the API from `/api/triage`.

## ⚠️ Notes on API Quota
If you encounter a `429 Too Many Requests` status, your API key has exceeded its free tier usage or is restricted in your region. Ensure billing is enabled or verify your available AI Studio quotas.
