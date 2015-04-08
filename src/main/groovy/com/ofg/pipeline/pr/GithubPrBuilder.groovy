package com.ofg.pipeline.pr

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class GithubPrBuilder implements PrBuilder {

    DslFactory dslFactory
    String organizationUrl
    String cronToPollScm
    String organizationName
    List<String> whitelistedUsers

    void organizationUrl(String organizationUrl) {
        this.organizationUrl = organizationUrl
    }

    void cronToPollScm(String cronToPollScm) {
        this.cronToPollScm = cronToPollScm
    }

    void organizationName(String organizationName) {
        this.organizationName = organizationName
    }

    void whitelistedUsers(List<String> whitelistedUsers) {
        this.whitelistedUsers = whitelistedUsers
    }

    @Override
    Job buildPrJob(String projectName) {
        return dslFactory.freeStyleJob("$projectName-pr-build") {
            configure { Node projectNode ->
                appendProperties(projectNode, "$organizationUrl/$projectName/")
            }
            scm {
                git {
                    remote {
                        name = 'origin'
                        refspec = '+refs/pull/*:refs/remotes/origin/pr/*'
                        url = "$organizationUrl/${projectName}.git"
                    }
                    branch('origin/pr/${ghprbPullId}/head')
                }
            }
            triggers {
                parameters {
                    stringParam('ghprbPullId')
                }
                githubPush()
                pullRequest {
                    userWhitelist = whitelistedUsers
                    orgWhitelist = [organizationName]
                    cron = cronToPollScm
                }
            }
            steps {
                gradle('clean build')
            }
            publishers {
                githubCommitNotifier()
            }
        }
    }

    private void appendProperties(Node projectNode, String organizationName) {
        Node propertiesNode = projectNode / 'properties'
        appendGithubProps(propertiesNode, organizationName)
    }

    private void appendGithubProps(propertiesNode, String organization) {
        def githubProjectNode = propertiesNode / 'com.coravy.hudson.plugins.github.GithubProjectProperty'
        (githubProjectNode / 'projectUrl').setValue(organization)
    }
}
