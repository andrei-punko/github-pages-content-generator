
# Parser to prepare content for GitHub Pages

[![Java CI with Maven](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml/badge.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)
[![Branches](.github/badges/branches.svg)](https://github.com/andrei-punko/github-pages-content-generator/actions/workflows/maven.yml)

Used for several sites already:
- https://github.com/andrei-punko/saint-fathers-citations
- https://andrei-punko.github.io/java-interview-faq-n-answers

## Usage instruction

    java -jar github-pages-content-generator.jar inputFileName templateFileName htmlOutputFileName

Check [GithubPagesContentGeneratorTest](src/test/java/by/andd3dfx/GithubPagesContentGeneratorTest.java) for details
