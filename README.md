
# customer-advisors-frontend

[![Build Status](https://travis-ci.org/hmrc/customer-advisors-frontend.svg?branch=master)](https://travis-ci.org/hmrc/customer-advisors-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/customer-advisors-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/customer-advisors-frontend/_latestVersion)

A frontend used by contact advisors to respond to messages.

## Run the tests and sbt fmt before raising a PR

Format:

`sbt fmt`

Then run the tests and coverage report:

`sbt clean coverage test coverageReport`

If your build fails due to poor test coverage, *DO NOT* lower the test coverage threshold, instead inspect the generated report located here on your local repo: `/target/scala-2.12/scoverage-report/index.html`

Then run the integration tests:

`sbt it:test`


#### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
