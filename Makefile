#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

SHELL = /bin/sh
.SUFFIXES:

MAVEN = ./mvnw

export MAVEN_OPTS MAVEN_ARGS

default:: help

Makefile:: ;

clean::
	${MAVEN} clean

install::
	${MAVEN} clean install

tests: install-fast run-tests

install-fast:: MAVEN_ARGS += -Pfast
install-fast:: install

install-notests:: MAVEN_ARGS += -DskipTests
install-notests:: install

run-tests::
	${MAVEN} surefire:test

deploy::
	${MAVEN} clean deploy

release::
	${MAVEN} clean release:clean release:prepare release:perform

help::
	@echo " * clean           - clean local build tree"
	@echo " * install         - build, run static analysis and unit tests, then install in the local repository"
	@echo " * install-fast    - same as 'install', but skip unit tests and static analysis"
	@echo " * install-notests - same as 'install', but skip unit tests"
	@echo " * tests           - build code and run unit and integration tests"
	@echo " * run-tests       - run all unit and integration tests except really slow tests"
	@echo " * deploy          - builds and deploys the current version to the Sonatype OSS repository"
	@echo " * release         - release a new version to maven central"
