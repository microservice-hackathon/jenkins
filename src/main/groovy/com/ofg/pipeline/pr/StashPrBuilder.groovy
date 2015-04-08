package com.ofg.pipeline.pr
import groovy.transform.PackageScope
import javaposse.jobdsl.dsl.DslFactory
import javaposse.jobdsl.dsl.Job
import javaposse.jobdsl.dsl.helpers.scm.GitContext
import javaposse.jobdsl.dsl.helpers.step.StepContext

class StashPrBuilder implements PrBuilder {

    @PackageScope DslFactory dslFactory
    @PackageScope String stashHost
    @PackageScope String cronToPollScm
    @PackageScope String mergeStrategy = 'default'
    @PackageScope FastForwardMode fastForwardMode = FastForwardMode.FF
    @PackageScope String username
    @PackageScope String password
    @PackageScope String projectCode
    @PackageScope String repoName
    @PackageScope Closure buildSteps = defaultBuildSteps()

    void stashHost(String stashHost) {
        this.stashHost = stashHost
    }

    void cronToPollScm(String cronToPollScm) {
        this.cronToPollScm = cronToPollScm
    }

    void mergeStrategy(String mergeStrategy) {
        this.mergeStrategy = mergeStrategy
    }

    void fastForwardMode(FastForwardMode fastForwardMode) {
        this.fastForwardMode = fastForwardMode
    }

    void username(String username) {
        this.username = username
    }

    void password(String password) {
        this.password = password
    }

    void projectCode(String projectCode) {
        this.projectCode = projectCode
    }

    void repoName(String repoName) {
        this.repoName = repoName
    }

    void buildSteps(@DelegatesTo(StepContext) Closure buildSteps) {
        this.buildSteps = buildStepFromContext(buildSteps)
    }

    private Closure buildStepFromContext(@DelegatesTo(StepContext) Closure buildSteps) {
        return buildSteps
    }

    private Closure defaultBuildSteps() {
        return buildStepFromContext {
            gradle('clean build')
        }
    }

    @Override
    Job buildPrJob(String projectName) {
        return dslFactory.freeStyleJob("$projectName-pr-build") {
            scm {
                git {
                    remote {
                        name = 'origin'
                        url = "http://$username@$stashHost/scm/\${projectCode}/\${repositoryName}.git"
                        credentials('STASH')
                    }
                    branch('*/\${sourceBranch}')
                    mergeOptions('origin', '${targetBranch}')
                    addExtension(delegate as GitContext)
                }
            }
            steps buildSteps
            triggers {
                configure { Node node ->
                    addStashBuildTrigger(node.triggers)
                }
            }
            publishers {
                stashNotifier()
            }
        }
    }

    private void addExtension(GitContext gitContext) {
        def option = gitContext.extensions[0].options[0]
        new Node(option, 'fastForwardMode', fastForwardMode.mode)
    }

    private void addStashBuildTrigger(Node triggers) {
        def node = triggers / 'stashpullrequestbuilder.stashpullrequestbuilder.StashBuildTrigger'
        (node / 'spec').setValue(cronToPollScm)
        (node / 'cron').setValue(cronToPollScm)
        (node / 'stashHost').setValue("http://$stashHost")
        (node / 'username').setValue(username)
        (node / 'password').setValue(password)
        (node / 'projectCode').setValue(projectCode)
        (node / 'repositoryName').setValue(repoName)
        (node / 'checkDestinationCommit').setValue(false)
    }

    enum FastForwardMode {
        FF('FF'), FF_ONLY('FF-ONLY'), NO_FF('NO-FF')

        String mode

        FastForwardMode(String mode) {
            this.mode = mode
        }
    }
}
