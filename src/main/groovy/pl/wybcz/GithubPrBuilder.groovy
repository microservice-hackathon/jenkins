package pl.wybcz

import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job

class GithubPrBuilder {

    private final DslFactory dslFactory

    GithubPrBuilder(DslFactory dslFactory) {
        this.dslFactory = dslFactory
    }

    Job buildPrJob(String organizationUrl,
                   String projectName,
                   String cronToPollScm,
                   String organizationName,
                   List<String> whitelistedUsers) {
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
                configure {  Node project ->
                    def githubCommitNotifier = project / 'publishers' / 'com.cloudbees.jenkins.GitHubCommitNotifier'
                    (githubCommitNotifier / 'resultOnFailure').setValue('FAILURE')
                }
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


