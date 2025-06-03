# Generator to prepare content for GitHub Pages
[![Java CI with Maven](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)
[![Branches](.github/badges/branches.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)

Output html generated from text file using template

## Prerequisites

- JDK 21
- Maven isn't required because of embedded Maven presence in the project

## Usage instructions

Build generator jar:

    ./mvnw clean install

Prepare input text file and html template

Use prepared jar to generate output html:

    java -jar github-pages-content-generator.jar inputFileName templateFileName htmlOutputFileName

Check [GithubPagesContentGeneratorTest](src/test/java/by/andd3dfx/GithubPagesContentGeneratorTest.java) for details

## Usage examples

- https://github.com/andrei-punko/java-interview-faq-n-answers
- https://github.com/andrei-punko/st-fathers-citations
