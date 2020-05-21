# customer-advisors-frontend

[![Build Status](https://travis-ci.org/hmrc/customer-advisors-frontend.svg?branch=master)](https://travis-ci.org/hmrc/customer-advisors-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/customer-advisors-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/customer-advisors-frontend/_latestVersion)

A frontend used by contact advisors to respond to messages.

## /secure-message/hmrc/email - email proxy endpoint

By default proxy is available for all stride users with supported clientId.

To restrict the endpoint to users with particular entitlements, update email.stride.role in environment yaml
For example:
- staging: update app-config-staging/customer-advisor-frontend.yaml
  hmrc_config:
     email.stride.roles.0: RG-RTS_NRL_1_Robot_Staging-GD

- production: update app-config-production/customer-advisor-frontend.yaml
  hmrc_config:
     email.stride.roles.0: RG-RTS_R40_Robot_Live-GD
     email.stride.roles.1: RG-RTS_R40_Robot_Beta-GD

#### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
