
# Generator to prepare content for GitHub Pages

[![Java CI with Maven](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml/badge.svg)](actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](actions/workflows/maven.yml)
[![Branches](.github/badges/branches.svg)](actions/workflows/maven.yml)

Output html generated from text file using template

Used for several sites already:
- https://github.com/andrei-punko/saint-fathers-citations
- https://github.com/andrei-punko/java-interview-faq-n-answers

## Prerequisites

- Maven 3
- JDK 17

## Usage instructions

Build generator jar:

    mvn clean install

Prepare input text file and html template

Use prepared jar to generate output html:

    java -jar github-pages-content-generator.jar inputFileName templateFileName htmlOutputFileName

Check [GithubPagesContentGeneratorTest](src/test/java/by/andd3dfx/GithubPagesContentGeneratorTest.java) for details
