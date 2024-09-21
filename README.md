# ethersyncj

<!---
[![start with why](https://img.shields.io/badge/start%20with-why%3F-brightgreen.svg?style=flat)](http://www.ted.com/talks/simon_sinek_how_great_leaders_inspire_action)
--->
[![GitHub release](https://img.shields.io/github/release/elbosso/ethersyncj/all.svg?maxAge=1)](https://GitHub.com/elbosso/ethersyncj/releases/)
[![GitHub tag](https://img.shields.io/github/tag/elbosso/ethersyncj.svg)](https://GitHub.com/elbosso/ethersyncj/tags/)
[![GitHub license](https://img.shields.io/github/license/elbosso/ethersyncj.svg)](https://github.com/elbosso/ethersyncj/blob/master/LICENSE)
[![GitHub issues](https://img.shields.io/github/issues/elbosso/ethersyncj.svg)](https://GitHub.com/elbosso/ethersyncj/issues/)
[![GitHub issues-closed](https://img.shields.io/github/issues-closed/elbosso/ethersyncj.svg)](https://GitHub.com/elbosso/ethersyncj/issues?q=is%3Aissue+is%3Aclosed)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/elbosso/ethersyncj/issues)
[![GitHub contributors](https://img.shields.io/github/contributors/elbosso/ethersyncj.svg)](https://GitHub.com/elbosso/ethersyncj/graphs/contributors/)
[![Github All Releases](https://img.shields.io/github/downloads/elbosso/ethersyncj/total.svg)](https://github.com/elbosso/ethersyncj)
[![Website elbosso.github.io](https://img.shields.io/website-up-down-green-red/https/elbosso.github.io.svg)](https://elbosso.github.io/)

## Overview

This project offers Java bindings for the [ethersync protocol](https://ethersync.github.io/ethersync/)

To use it, you have to manually adjust the filename in the source code according to your setup.

```
mvn compile package
```

and then starting the resulting monolithic jar file by issuing

```
$JAVA_HOME/bin/java -jar target/ethersyncj-<version>-jar-with-dependencies.jar
```
Alternatively one could just start the example application using maven by  issuing

```
mvn compile exec:java
```

The example app opens an editor window based on `javax.swing.JTextArea`, editing the file given. If you start it multiple times, any edits done in one of the editors is reflected in the others.

## Diagrams

### Communication
![image](https://github.com/user-attachments/assets/21528263-9cde-4da0-a8ec-59680ba4298b)
### Creation / Use
![image](https://github.com/user-attachments/assets/0f218abe-ed58-4ced-a5e3-153304ed45e0)
### Class Hierarchy
![image](https://github.com/user-attachments/assets/311176c3-eb0b-4ed3-8ccf-a93368bce256)
