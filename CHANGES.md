# Changes

## Version 10 - 2022-08-10

* Update README.md
* rework CollectingManifestResourceTransformer for JDK8
* Align build to BasePOM OSS build

## Version 9
* 2022-05-24 Add new spotbugs suppression file for very noisy checks that were introduced in spotbugs 4.7.0.

## Version 8
* 2020-09-20 Only support JDK 9+ for building, resulting jar still works fine on JDK8
* 2020-09-20 Switch to Github Actions, remove travis
* 2020-09-20 Update CollectingManifestResourceTransformer to work with Shade plugin 3.2.3+

## Version 7

* 2019-07-08 Change travis to support OpenJDK 8 and 11
* 2019-07-08 Support Checkstyle 8.2x
* 2019-07-08 Align dependencies to match basepom 29

## Version 6

This version only supports Checkstyle 8 or better!

* 2017-11-10 move SuppressWithNearbyCommentFilter into treewalker
* 2017-11-10 remove FileContentsHolder

## Version 5

* 2015-06-16 Align dependencies to basepom 12
* 2015-06-16 Ensure correct compilation with Java 7+

## Version 4

* 2015-01-17 Suppress checkstyle for anything that is in a `generated-sources` folder
* 2015-01-17 Allow comment and @SuppressWarnings Checkstyle suppression.


## Version 3

* 2014-07-22 - Add CollectingManifestResourceTransformer for maven shade plugin

## Version 2

* 2014-01-12 - Change License header formatting

## Version 1

* 2013-12-20 - First release
